package tyrant.mikera.tyrant;

import java.awt.*;

import javelin.view.mappanel.MapPanelOld;


public class GameTile extends Tile {
	private int image;
	private boolean transparent;
	private boolean blocking;

	public GameTile(int i, boolean b, boolean t) {
		image = i;
		blocking = b;
		transparent = t;
	}

	public int getImage() {
		return image;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public boolean isTranparent() {
		return transparent;
	}

	public void draw(Graphics g, int x, int y) {
		int w = MapPanelOld.TILEWIDTH;
		int h = MapPanelOld.TILEHEIGHT;
		int sx = (image % 20) * w;
		int sy = (image / 20) * h;
		g.drawImage(QuestApp.tiles, x, y, x + w, y + h, sx, sy, sx + w, sy + h,
				null);
	}
}