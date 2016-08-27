package skogland.lasse.DSW.lwjglwrapper;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.net.URL;
import java.nio.*;

public class Util {

	public static String loadTextFromResource(String name) {
		System.out.printf("Loading Internal Resource: %s\n", name);
		String text = "";
		try {
			InputStream in = getResource(name).openStream();
			int i = 0;
			while((i = in.read()) != -1) text += (char)i;
		} catch (IOException e) {}
		return text;
	}

	public static String loadText(String name) {
		BufferedReader br = null;
		String resultString = null;
		try {
			File file = new File(name);
			br = new BufferedReader(new FileReader(file));
			String line;
			StringBuilder result = new StringBuilder();
			while ((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch (NullPointerException npe) {
			System.err.println("Error retrieving file: ");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error reading file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("Error closing file");
				ex.printStackTrace();
			}
		}
		return resultString;
	}
	
	public static FloatBuffer makeBuffer(float[] buf) {
		return (FloatBuffer) BufferUtils.createFloatBuffer(buf.length).put(buf).flip();
	}

	public static DoubleBuffer makeBuffer(double[] buf) {
		return (DoubleBuffer) BufferUtils.createDoubleBuffer(buf.length).put(buf).flip();
	}

	public static IntBuffer makeBuffer(int[] buf) {
		return (IntBuffer) BufferUtils.createIntBuffer(buf.length).put(buf).flip();
	}

	public static ShortBuffer makeBuffer(short[] buf) {
		return (ShortBuffer) BufferUtils.createShortBuffer(buf.length).put(buf).flip();
	}

	public static ByteBuffer makeBuffer(byte[] buf) {
		return (ByteBuffer) BufferUtils.createByteBuffer(buf.length).put(buf).flip();
	}

	public static LongBuffer makeBuffer(long[] buf) {
		return (LongBuffer) BufferUtils.createLongBuffer(buf.length).put(buf).flip();
	}

	public static CharBuffer makeBuffer(char[] buf) {
		return (CharBuffer) BufferUtils.createCharBuffer(buf.length).put(buf).flip();
	}

	public static PointerBuffer makePointerBuffer(long[] buf) {
		return BufferUtils.createPointerBuffer(buf.length).put(buf).flip();
	}

	public static URL getResource(String name) {
		return Util.class.getResource(name);
	}

	public static void printBuffer(Buffer b) {
		while (b.remaining() > 0) {
			if (b instanceof FloatBuffer) {
				System.out.print(((FloatBuffer) b).get());
			} else if (b instanceof DoubleBuffer) {
				System.out.print(((DoubleBuffer) b).get());
			} else if (b instanceof IntBuffer) {
				System.out.print(((IntBuffer) b).get());
			} else if (b instanceof ShortBuffer) {
				System.out.print(((ShortBuffer) b).get());
			} else if (b instanceof ByteBuffer) {
				System.out.print(((ByteBuffer) b).get());
			} else if (b instanceof LongBuffer) {
				System.out.print(((LongBuffer) b).get());
			}
			System.out.print(" ");
		}
		System.out.println();
	}

	public static double getDistance(double x1, double y1, double x2, double y2) {
		double x = x1 - x2;
		double y = y1 - y2;
		return Math.sqrt((x * x) + (y * y));
	}

	public static double stripDecimals(double d, int decimals) {
		double dec = Math.pow(10.0, decimals);
		return Math.round(d * dec) / dec;
	}

	public static boolean isPointWithin(int mx, int my, int x, int y, int w, int h) {
		return (mx >= x && mx <= x + w) && (my >= y && my <= y + h);
	}

	public static double getTime() {
		return GLFW.glfwGetTime();
	}
}
