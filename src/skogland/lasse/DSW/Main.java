package skogland.lasse.DSW;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import skogland.lasse.DSW.lwjglwrapper.OpenCL;
import skogland.lasse.DSW.lwjglwrapper.OpenCL.CLPointer;
import skogland.lasse.DSW.lwjglwrapper.Renderer;
import skogland.lasse.DSW.lwjglwrapper.Util;
import skogland.lasse.DSW.lwjglwrapper.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glDeleteTextures;

public class Main {

	private static Window window;
	private static Renderer renderer;
	public static boolean gameAlive = true, needsRefresh = true, escPressed = false, saveToDisk = true;
	;
	static int texture = 0;

	static long context, commands, program, kernel;
	static CLPointer output;

	static int size = 1024;
	static float divident = 0.0125f;//0.00125f;
	static float step = (divident);
	static float X = 0, Y = 0;

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath", "native");
		OpenCL cl = new OpenCL();
		cl.createCL();
		window = new Window();
		window.initialize("DSW", 1000, 1000);
		glfwSetKeyCallback(window.getWindowIdentifier(), keyCallback);
		renderer = window.getRenderer(Renderer.OPENGL_11);
		//renderer.loadDefaultShaders();
		context = cl.createContext(0);
		commands = cl.createCommandQueue(context, 0);
		String kernelSource = Util.loadTextFromResource("/skogland/lasse/DSW/noise_kernel.cl");
		program = cl.createProgram(context, kernelSource);
		kernel = cl.createKernel(program, "worldgen");
		output = cl.createOutputBuffer(context, (size * size) * Float.BYTES);
		window.show();

		while (gameAlive && !escPressed) {
			renderer.render(0, 0, 1, 1, texture);
			if (needsRefresh) {
				needsRefresh = false;
				runKernel(cl);
			}
			gameAlive = !window.update();
		}

		window.destroy();
		glfwTerminate();
		cl.releaseProgram(program);
		cl.releaseContext(context, commands);
	}

	static GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if (action == GLFW_PRESS)
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
		}
	};

	public static void runKernel(OpenCL cl) {
		if (texture != 0) glDeleteTextures(texture);
		FloatBuffer result = BufferUtils.createFloatBuffer(size * size);
		cl.setKernelArg(kernel, output);
		cl.setKernelArg(kernel, X);
		cl.setKernelArg(kernel, Y);
		cl.setKernelArg(kernel, divident);
		cl.executeKernel(kernel, commands, size, size);
		cl.readBuffer(commands, output, result);
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++) {
				float n = result.get(y * size + x);
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

		try {
			if(saveToDisk) {
				ImageIO.write(img, "png", new File("out.png"));
				saveToDisk = false;
			}
			texture = renderer.loadTexture(img, Renderer.NO_ALPHA);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (img == null || texture == 0) System.out.println("Som ting wong");
	}
}
