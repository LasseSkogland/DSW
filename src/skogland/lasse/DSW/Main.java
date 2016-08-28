package skogland.lasse.DSW;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import skogland.lasse.DSW.lwjglwrapper.OpenCL;
import skogland.lasse.DSW.lwjglwrapper.OpenCL.CLPointer;
import skogland.lasse.DSW.lwjglwrapper.Renderer;
import skogland.lasse.DSW.lwjglwrapper.Util;
import skogland.lasse.DSW.lwjglwrapper.Window;
import skogland.lasse.DSW.world.Chunk;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

	public static int PERMUTATION[] = new int[512];
	public static final int CHUNK_SIZE = 32;
	public static final int MAX_WORLD_SIZE = 32;

	private static Window window;
	private static Renderer renderer;
	private static OpenCL cl;
	public static boolean gameAlive = true, needsRefresh = true, escPressed = false, saveToDisk = false;
	static int texture = 0;

	static long context, commands, program, kernel;
	static CLPointer output, permutation;

	static float divident = .125f / 1.0f;
	final static float step = 1;
	static int X = 0, Y = 0;

	public static void setSeed(long seed) {
		short[] source = new short[256];
		for (short i = 0; i < 256; i++)
			source[i] = i;
		seed = seed * 6364136223846793005l + 1442695040888963407l;
		seed = seed * 6364136223846793005l + 1442695040888963407l;
		seed = seed * 6364136223846793005l + 1442695040888963407l;
		for (int i = 255; i >= 0; i--) {
			seed = seed * 6364136223846793005l + 1442695040888963407l;
			int r = (int) ((seed + 31) % (i + 1));
			if (r < 0)
				r += (i + 1);
			PERMUTATION[i] = source[r];
			source[r] = source[i];
		}
		for (int i = 256; i < 512; i++) {
			PERMUTATION[i] = PERMUTATION[i - 256];
		}
		permutation = cl.createInputBuffer(context, (512) * Integer.SIZE);
		IntBuffer perm = Util.makeBuffer(PERMUTATION);
		cl.writebuffer(commands, permutation, perm);
	}

	public static ConcurrentHashMap<Integer, Chunk> chunkMap = new ConcurrentHashMap<>();
	public static ConcurrentLinkedQueue<Chunk> chunkQueue = new ConcurrentLinkedQueue<>();

	public static Thread chunkThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				setupCLGenerator();
				while (true) {
					Chunk c;
					if ((c = chunkQueue.poll()) == null) {
						Thread.sleep(10);
						continue;
					}
					c.img = runKernel(cl, c.x, c.y);
					chunkMap.put((c.y * MAX_WORLD_SIZE) + c.x, c);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	});

	public static void setupCLGenerator() {
		context = cl.createContext(0);
		commands = cl.createCommandQueue(context, 0);
		String kernelSource = Util.loadTextFromResource("/skogland/lasse/DSW/worldgen.cl");
		program = cl.createProgram(context, kernelSource, "-cl-fast-relaxed-math");
		kernel = cl.createKernel(program, "worldgen");
		output = cl.createOutputBuffer(context, (CHUNK_SIZE * CHUNK_SIZE) * Float.SIZE);
		setSeed(0XBEEFDEAD);
	}

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath", "native");
		cl = new OpenCL();
		window = new Window();
		window.initialize("DSW", 1024, 1024);
		glfwSetKeyCallback(window.getWindowIdentifier(), keyCallback);
		renderer = window.getRenderer(Renderer.OPENGL_11);
		chunkThread.start();
		window.show();

		while (gameAlive && !escPressed) {
			for (Chunk c : chunkMap.values()) {
				if (c.textureID == -1) c.textureID = renderer.loadTexture(c.img, Renderer.NO_ALPHA);
				renderer.render(((-X + c.x) * CHUNK_SIZE), ((-Y + c.y) * CHUNK_SIZE), ((-X + c.x) * CHUNK_SIZE) + CHUNK_SIZE, ((-Y + c.y) * CHUNK_SIZE) + CHUNK_SIZE, c.textureID);
			}

			gameAlive = !window.update();
		}

		window.destroy();
		glfwTerminate();
		cl.releaseProgram(program);
		cl.releaseContext(context, commands);
	}

	static GLFWKeyCallbackI keyCallback = (window1, key, scancode, action, mods) -> {
		if (action == GLFW_PRESS) {
			if (key == GLFW_KEY_ESCAPE) {
				escPressed = true;
			} else if (key == GLFW_KEY_UP) {
				Y += step;
				needsRefresh = true;
			} else if (key == GLFW_KEY_DOWN) {
				Y -= step;
				needsRefresh = true;
			} else if (key == GLFW_KEY_LEFT) {
				X -= step;
				needsRefresh = true;
			} else if (key == GLFW_KEY_RIGHT) {
				X += step;
				needsRefresh = true;
			} else if (key == GLFW_KEY_W) {
				divident *= 2;
				needsRefresh = true;
			} else if (key == GLFW_KEY_S) {
				divident /= 2;
				needsRefresh = true;
			} else if (key == GLFW_KEY_P) {
				needsRefresh = true;
				saveToDisk = true;
			}
			if (!chunkMap.containsKey((Y + 1) * MAX_WORLD_SIZE + X + 1)) chunkQueue.offer(new Chunk(X + 1, Y + 1));
			if (!chunkMap.containsKey((Y + 1) * MAX_WORLD_SIZE + X)) chunkQueue.offer(new Chunk(X, Y + 1));
			if (!chunkMap.containsKey((Y + 1) * MAX_WORLD_SIZE + X - 1)) chunkQueue.offer(new Chunk(X - 1, Y + 1));
			if (!chunkMap.containsKey(Y * MAX_WORLD_SIZE + X + 1)) chunkQueue.offer(new Chunk(X + 1, Y));
			if (!chunkMap.containsKey(Y * MAX_WORLD_SIZE + X)) chunkQueue.offer(new Chunk(X, Y));
			if (!chunkMap.containsKey(Y * MAX_WORLD_SIZE + X - 1)) chunkQueue.offer(new Chunk(X - 1, Y));
			if (!chunkMap.containsKey((Y - 1) * MAX_WORLD_SIZE + X + 1)) chunkQueue.offer(new Chunk(X + 1, Y - 1));
			if (!chunkMap.containsKey((Y - 1) * MAX_WORLD_SIZE + X)) chunkQueue.offer(new Chunk(X, Y - 1));
			if (!chunkMap.containsKey((Y - 1) * MAX_WORLD_SIZE + X - 1)) chunkQueue.offer(new Chunk(X - 1, Y - 1));
		}

	};

	public static BufferedImage runKernel(OpenCL cl, float X, float Y) {
		//if (texture != 0) glDeleteTextures(texture);
		int texture = 0;
		FloatBuffer result = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE);
		cl.setKernelArg(kernel, output);
		cl.setKernelArg(kernel, X);
		cl.setKernelArg(kernel, Y);
		cl.setKernelArg(kernel, divident);
		cl.setKernelArg(kernel, permutation);
		cl.executeKernel(kernel, commands, CHUNK_SIZE, CHUNK_SIZE);
		cl.readBuffer(commands, output, result);
		BufferedImage img = new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < CHUNK_SIZE; y++)
			for (int x = 0; x < CHUNK_SIZE; x++) {
				float n = result.get(y * CHUNK_SIZE + x);
				int color;
				if (n == 0) color = 0;
				else if (n < 0.01) color = 0xff046D8B;
				else if (n < 0.025) color = 0xffEDC9AF;
				else if (n < 0.75) color = 0xff005C09;
				else if (n < 1.0) color = 0xff00680A;
				else if (n < 1.25) color = 0xff007B0C;
				else if (n < 1.5) color = 0xff018E0E;
				else if (n < 1.75) color = 0xff01A611;
				else if (n < 1.95) color = 0xff777777;
				else color = 0xffffffff;//*/
				img.setRGB(x, y, color);
			}
		if (img == null) System.out.println("Som ting wong");
		return img;
	}
}
