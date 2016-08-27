package skogland.lasse.DSW.lwjglwrapper.renderer;

import org.lwjgl.BufferUtils;
import skogland.lasse.DSW.lwjglwrapper.Renderer;
import skogland.lasse.DSW.lwjglwrapper.Util;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class GL21Renderer extends Renderer {

	private int vertexArray;
	private int colorArray;
	private int textureCoordsArray;

	public GL21Renderer() {
		super();
		vertexArray = glGenBuffers();
		colorArray = glGenBuffers();
		textureCoordsArray = glGenBuffers();
	}

	@Override
	public void render(FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID) {
		render(GL_TRIANGLES, vertices, color, textureCoords, textureID);
	}

	@Override
	public void render(int renderMode, FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID) {
		glBindBuffer(GL_ARRAY_BUFFER, vertexArray);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, vertexArray);
		if (color != null) {
			glEnableClientState(GL_COLOR_ARRAY_POINTER);
			glBindBuffer(GL_ARRAY_BUFFER, colorArray);
			glBufferData(GL_ARRAY_BUFFER, color, GL_STATIC_DRAW);
			glColorPointer(color.capacity(), GL_FLOAT, 0, colorArray);
		}
		if (textureCoords != null) {
			glEnableClientState(GL_TEXTURE_COORD_ARRAY_POINTER);
			glBindTexture(GL_TEXTURE_2D, textureID);
			glBindBuffer(GL_ARRAY_BUFFER, textureCoordsArray);
			glBufferData(GL_ARRAY_BUFFER, textureCoords, GL_STATIC_DRAW);
			glTexCoordPointer(textureCoords.capacity(), GL_FLOAT, 0, textureCoordsArray);
		}
		glDrawArrays(GL_TRIANGLES, 0, 3);
		if(color != null) glDisableClientState(GL_COLOR_ARRAY_POINTER);
		if(textureCoords != null) {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY_POINTER);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		glDisableVertexAttribArray(0);
	}

	@Override
	public void render(float x, float y, float x2, float y2, int textureID) {
		FloatBuffer vertices = BufferUtils.createFloatBuffer(18);
		vertices.put(new float[]{
				x, y, 0,
				x2, y, 0,
				x, y2, 0,
				x, y2, 0,
				x2, y, 0,
				x2, y2, 0
		}).flip();
		FloatBuffer textureCoords = BufferUtils.createFloatBuffer(12);
		textureCoords.put(new float[]{
				0, 0,
				1, 0,
				0, 1,
				0, 1,
				1, 0,
				1, 1
		}).flip();
		render(GL_TRIANGLES, vertices, null, textureCoords, textureID);
		textureCoords.clear();
		vertices.clear();
	}

	@Override
	public int getVersion() {
		return OPENGL_21;
	}

	public int loadDefaultShaders() {
		String classLocation = "/" + this.getClass().getPackage().getName().replace(".", "/");
		String vertexShaderSource = Util.loadTextFromResource(classLocation + "/default_shaders/vertex21.glsl");
		int vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderSource);
		String fragmentShaderSource = Util.loadTextFromResource(classLocation + "/default_shaders/fragment21.glsl");
		int fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		return program;
	}
}
