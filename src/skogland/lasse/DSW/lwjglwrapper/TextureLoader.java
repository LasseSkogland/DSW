package skogland.lasse.DSW.lwjglwrapper;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextureLoader {

	public static int GL_TEXTURE_MIN_FILTER_SETTING = GL_LINEAR;
	public static int GL_TEXTURE_MAG_FILTER_SETTING = GL_NEAREST;
	public static int GL_TEXTURE_WRAP_S_SETTING = GL_CLAMP;
	public static int GL_TEXTURE_WRAP_T_SETTING = GL_CLAMP;

	public static final int NO_ALPHA = 0x00000000;

	public static int loadTexture(String name) throws Exception {
		return loadTexture(name, NO_ALPHA);
	}

	public static int loadTexture(String name, int alpha) throws Exception {
		BufferedImage image = ImageIO.read(TextureLoader.class.getResource(name));
		return loadGLTexture(image, alpha);
	}

	public static int loadGLTexture(BufferedImage image, int alpha) throws Exception {
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

	public static double getTileX(int x, int w) {
		if (x == w) return 1.0;
		return (1.0 / w) * x;
	}

	public static double getTileY(int y, int h) {
		if (y == h) return 1.0;
		return (1.0 / h) * y;
	}
}
