package skogland.lasse.DSW.world;

import java.awt.image.BufferedImage;

public class Chunk {

	/* DEBUG, not final class */
	public BufferedImage img;
	public int textureID = -1;
	public int x, y;

	public Chunk() {

	}

	public Chunk(int x, int y) {
		this.x = x;
		this.y = y;
	}
	/* DEBUG END*/
}
