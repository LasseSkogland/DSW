package skogland.lasse.DSW.lwjglwrapper.renderer;

import org.lwjgl.BufferUtils;
import skogland.lasse.DSW.lwjglwrapper.Renderer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class GL11Renderer extends Renderer {

	public GL11Renderer() {
		super();
	}

	@Override
	public void render(FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID) {
		render(GL_TRIANGLES, vertices, color, textureCoords, textureID);
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
	public int loadDefaultShaders() {return 0;}

	@Override
	public int getVersion() {
		return OPENGL_11;
	}

	@Override
	public void render(int renderMode, FloatBuffer vertices, FloatBuffer color, FloatBuffer textureCoords, int textureID) {
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, vertices);
		if (textureCoords != null) {
			glBindTexture(GL_TEXTURE_2D, textureID);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 0, textureCoords);
		}
		if (color != null) {
			glEnableClientState(GL_COLOR_ARRAY);
			glColorPointer(4, GL_FLOAT, 0, color);
		} else glColor3f(1, 1, 1);
		glDrawArrays(renderMode, 0, vertices.capacity());
		glDisableClientState(GL_VERTEX_ARRAY);
		if (textureCoords != null) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		if (color != null) glDisableClientState(GL_COLOR_ARRAY);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
}
