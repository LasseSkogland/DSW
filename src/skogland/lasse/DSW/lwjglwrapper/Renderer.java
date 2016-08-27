package skogland.lasse.DSW.lwjglwrapper;

import com.sun.istack.internal.Nullable;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public abstract class Renderer {

	public static int GL_TEXTURE_MIN_FILTER_SETTING = GL_LINEAR;
	public static int GL_TEXTURE_MAG_FILTER_SETTING = GL_NEAREST;
	public static int GL_TEXTURE_WRAP_S_SETTING = GL_CLAMP;
	public static int GL_TEXTURE_WRAP_T_SETTING = GL_CLAMP;

	public static final int NO_ALPHA = 0x00000000;
	public static final int OPENGL_11 = 1;
	public static final int OPENGL_21 = 2;

	public Renderer() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 1, 0, 1, -1.0, 1.0);
		glDisable(GL_DEPTH_TEST);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glShadeModel(GL_SMOOTH);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_COLOR_MATERIAL);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0, 0, 0, 1);
	}

	public abstract int getVersion();

	public abstract void render(FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID);

	public abstract void render(int renderMode, FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID);

	public abstract void render(float x, float y, float x2, float y2, int textureID);

	public int loadTexture(BufferedImage image, int alpha) {
		if (image == null) {
			return -1;
		}
		int textureID = 0;
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getHeight());
		ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == alpha) pixels[i] = 0x00000000; /*Make alpha seethrough*/
			buffer.put((byte) (pixels[i] >> 16 & 0xFF));
			buffer.put((byte) (pixels[i] >> 8 & 0xFF));
			buffer.put((byte) (pixels[i] & 0xFF));
			buffer.put((byte) (pixels[i] >> 24 & 0xFF));
		}
		buffer.flip();
		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_TEXTURE_MIN_FILTER_SETTING);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_TEXTURE_MAG_FILTER_SETTING);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_S_SETTING);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_TEXTURE_WRAP_T_SETTING);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glBindTexture(GL_TEXTURE_2D, 0);
		return textureID;
	}

	public int loadShader(int shaderType, String shaderSource) {
		if (getVersion() < OPENGL_21) return 0;
		int shader = glCreateShader(shaderType);
		glShaderSource(shader, shaderSource);
		glCompileShader(shader);
		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE) {

			String error = glGetShaderInfoLog(shader);

			String ShaderTypeString = null;
			switch (shaderType) {
				case GL_VERTEX_SHADER:
					ShaderTypeString = "vertex";
					break;
				case GL_GEOMETRY_SHADER:
					ShaderTypeString = "geometry";
					break;
				case GL_FRAGMENT_SHADER:
					ShaderTypeString = "fragment";
					break;
			}

			System.err.println("Compile failure in " + ShaderTypeString + " shader:\n" + error);
		}
		return shader;
	}

	public int loadShaders(String vertexShaderSource, String fragmentShaderSource, @Nullable String geometryShaderSource) {
		if (getVersion() < OPENGL_21) return 0;
		int program = 0;
		int vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderSource);
		int fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
		program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		return program;
	}

	public abstract int loadDefaultShaders();
}
