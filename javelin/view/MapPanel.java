package javelin.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.BattleMap;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Animation;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Tile;

// Panel descendant used to display a Map
// contains most of graphics redraw logic
// also some animation/explosion handling
public class MapPanel extends Panel implements Runnable {
	private static final Color[][] STATUS_COLORS = {
			{ new Color(0, 0, 127), new Color(0, 0, 153), new Color(0, 0, 178),
					new Color(0, 0, 204), new Color(0, 0, 229),
					new Color(0, 0, 255), },
			{ new Color(127, 0, 0), new Color(153, 0, 0), new Color(178, 0, 0),
					new Color(204, 0, 0), new Color(229, 0, 0),
					new Color(255, 0, 0), } };
	private static final long serialVersionUID = 3616728257933161270L;
	// tile size in pixels
	public final static int TILEWIDTH = 32;
	public final static int TILEHEIGHT = 32;

	// 3/4 perspective slant
	// TODO: implement changes if there is interest
	private final boolean slant = false;

	// size of viewable area
	protected int width = TILEWIDTH == 32 ? 15 : 25;
	protected int height = TILEWIDTH == 32 ? 15 : 25;

	// zoom factor
	public int zoomfactor = 100;
	private int lastzoomfactor = 100;

	// back buffer
	private Graphics buffergraphics;
	private Image buffer;

	// animation buffer
	private Graphics animationgraphics;
	private Image animationbuffer;

	// which map to draw
	public BattleMap map = new BattleMap(5, 5);

	// viewing state
	protected int scrollx = 0;
	protected int scrolly = 0;
	public int curx = 0;
	public int cury = 0;
	public boolean cursor = false;

	// drawing fields
	public int currentTile = Tile.CAVEWALL;

	private boolean animating = false;
	private Set<Point> overlay = null;

	// private boolean animationdone = false;

	@Override
	public void run() {
		while (true) {
			try {
				if (animating) {
					// animationdone = false;
					repaint();
				}
				Thread.sleep(50);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addAnimation(final Animation a) {
		synchronized (animationElements) {
			if (!animating) {
				animating = true;
				// Game.warn("Animation start");
			}
			animationElements.add(a);
		}
	}

	public MapPanel(final BattleScreen owner) {
		super();

		final Thread animloop = new Thread(this);
		animloop.start();

		addKeyListener(owner.questapp.keyadapter);

		setBackground(Color.black);
		// if (!Game.instance().isDesigner()) {
		// addMouseListener(new MyMouseListener());
		// }
	}

	// sets current scroll position and repaints map
	public void viewPosition(final BattleMap m, final int x, final int y) {
		setPosition(m, x, y);
		// repaint();
	}

	public void scroll(final int xDelta, final int yDelta) {
		scrollx = Math
				.min(Math.max(0, xDelta + scrollx), map.width - width / 2);
		scrolly = Math.min(Math.max(0, yDelta + scrolly), map.height - height
				/ 2);
		render();
		repaint();
	}

	public void setPosition(final BattleMap m, final int x, final int y) {
		map = m;
		if (map != null) {
			scrollx = RPG.middle(0, x - width / 2, map.getWidth() - width);
			scrolly = RPG.middle(0, y - height / 2, map.getHeight() - height);
		}
	}

	// override update to stop flicker TODO
	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width * TILEWIDTH, height * TILEHEIGHT);
	}

	// draws tiles in box (x1,y1)-->(x2,y2) to back buffer
	private void drawTiles(final int startx, final int starty, final int endx,
			final int endy) {
		for (int y = starty; y <= endy; y++) {
			for (int x = startx; x <= endx; x++) {
				int m = !map.isHeroLOS(x, y) ? 0 : map.getTile(x, y);
				final int tile = m & 65535;
				if (tile == 0) { // blank
					buffergraphics.setColor(Color.black);
					buffergraphics.fillRect((x - scrollx) * TILEWIDTH,
							(y - scrolly) * TILEHEIGHT, TILEWIDTH, TILEHEIGHT);
				} else if (overlay != null
						&& overlay.contains(new javelin.controller.Point(x, y))) {
					buffergraphics.setColor(Color.CYAN);
					buffergraphics.fillRect((x - scrollx) * TILEWIDTH,
							(y - scrolly) * TILEHEIGHT, TILEWIDTH, TILEHEIGHT);
				} else {
					final int image = map.isDiscovered(x, y + 1)
							&& Tile.filling[map.getTile(x, y + 1) & 65535] ? Tile.imagefill[tile]
							: Tile.images[tile];
					final int px = (x - scrollx) * TILEWIDTH;
					final int py = slant ? py * 3 / 4 : (y - scrolly)
							* TILEHEIGHT;
					final int sx = image % 20 * TILEWIDTH;
					final int sy = image / 20 * TILEHEIGHT;
					final Image source = map.isVisible(x, y) ? QuestApp.tiles
							: QuestApp.greytiles;
					buffergraphics.drawImage(source, px, py, px + TILEWIDTH, py
							+ TILEHEIGHT, sx, sy, sx + TILEWIDTH, sy
							+ TILEHEIGHT, null);
					// draw in coastlines
					if (Tile.borders[tile] > 0 && source == QuestApp.tiles) {
						drawcoastline(y, x, m, px, py);
					}
				}
				final Thing h = Game.hero();
				if (map.isVisible(x, y)) {
					drawThings(x, y);
				} else if (x == h.x && y == h.y) {
					drawThing(x, y, h);
				}

			}
		}
	}

	public void drawcoastline(int y, int x, int m, final int px, final int py) {
		if (x > 0 && map.getTile(x - 1, y) != m) {
			buffergraphics.drawImage(QuestApp.scenery, px, py, px + TILEWIDTH,
					py + TILEHEIGHT, 0, 16 * TILEHEIGHT, TILEWIDTH,
					17 * TILEHEIGHT, null);
		}
		if (x < map.width - 1 && map.getTile(x + 1, y) != m) {
			buffergraphics.drawImage(QuestApp.scenery, px, py, px + TILEWIDTH,
					py + TILEHEIGHT, TILEWIDTH, 16 * TILEHEIGHT, 2 * TILEWIDTH,
					17 * TILEHEIGHT, null);
		}
		if (y > 0 && map.getTile(x, y - 1) != m) {
			buffergraphics.drawImage(QuestApp.scenery, px, py, px + TILEWIDTH,
					py + TILEHEIGHT, 2 * TILEWIDTH, 16 * TILEHEIGHT,
					3 * TILEWIDTH, 17 * TILEHEIGHT, null);
		}
		if (y < map.height - 1 && map.getTile(x, y + 1) != m) {
			buffergraphics.drawImage(QuestApp.scenery, px, py, px + TILEWIDTH,
					py + TILEHEIGHT, 3 * TILEWIDTH, 16 * TILEHEIGHT,
					4 * TILEWIDTH, 17 * TILEHEIGHT, null);
		}
	}

	private void drawThing(final int x, final int y, final Thing t) {
		final Color color;
		if (t == Game.hero()) {
			color = t.combatant != null
					&& BattleMap.redTeam.contains(t.combatant) ? Color.ORANGE
					: Color.GREEN;
		} else if (overlay == null && t.combatant != null
				&& BattleScreen.active.drawbackground()) {
			color = STATUS_COLORS[BattleMap.blueTeam.indexOf(t.combatant) >= 0 ? 0
					: 1][t.combatant.getNumericStatus()];
		} else {
			color = null;
		}
		final int px = (x - scrollx) * TILEWIDTH;
		int py = slant ? py * 3 / 4 : (y - scrolly) * TILEHEIGHT;
		if (color != null) {
			buffergraphics.setColor(color);
			buffergraphics.fillRect(px, py, TILEWIDTH * 2, TILEHEIGHT * 2);
		}
		if (t.combatant == null || t.combatant.source.avatarfile == null) {
			final int image = t.getImage();
			final int sx = image % 20 * TILEWIDTH;
			final int sy = image / 20 * TILEHEIGHT;
			final Object source = t.get("ImageSource");
			buffergraphics.drawImage(source == null ? QuestApp.items
					: (Image) QuestApp.images.get(source), px, py, px
					+ TILEWIDTH, py + TILEHEIGHT, sx, sy, sx + TILEWIDTH, sy
					+ TILEHEIGHT, null);
		} else {
			try {
				buffergraphics.drawImage(ImageHandler.getImage(t.combatant),
						px, py, null);
			} catch (Exception e) {
				System.out.println("Error drawing " + t.combatant.toString());
				throw new RuntimeException(e);
			}
		}
	}

	private int getBiggerTileCoordinate(final int px2) {
		final int i = px2;
		return i < 0 ? 0 : i;
	}

	// Draw all visible objects on map to back buffer
	// side effect: sorts map objects in increasing z-order
	private void drawThings(final int x, final int y) {
		Thing head = map.sortZ(x, y);
		if (head == null) {
			return;
		}
		boolean numberOfThings = false;
		do {
			drawThing(x, y, head);
			head = head.next;
			numberOfThings = true;
		} while (head != null);

		// // draw plus icon for designer
		// if (numberOfThings) {
		// final Image plus = Designer.getPlusImage();
		// if (plus != null) {
		// final int px = (x - scrollx) * TILEWIDTH;
		// final int py = (y - scrolly) * TILEHEIGHT;
		// final int plusWidth = plus.getWidth(null);
		// final int plusHeight = plus.getHeight(null);
		// buffergraphics.drawImage(plus, px + TILEWIDTH - plusWidth, py,
		// px + TILEWIDTH, py + plusHeight, 0, 0, plusWidth,
		// plusHeight, null);
		// }
		// }
	}

	// place cursor at specified position
	public void setCursor(final int x, final int y) {
		cursor = true;
		curx = x;
		cury = y;
		repaint();
	}

	// remove cursor from map
	public void clearCursor() {
		cursor = false;
		repaint();
	}

	public void drawImage(final Graphics g, final double x, final double y,
			final int image) {
		if (!map.isVisible((int) Math.round(x), (int) Math.round(y))) {
			return;
		}
		final int px = (int) ((x - scrollx) * TILEWIDTH);
		final int py = (int) ((y - scrolly) * TILEHEIGHT);
		final int sx = image % 20 * TILEWIDTH;
		final int sy = TILEHEIGHT * (image / 20);
		g.drawImage(QuestApp.effects, px, py, px + TILEWIDTH, py + TILEHEIGHT,
				sx, sy, sx + TILEWIDTH, sy + TILEHEIGHT, null);
	}

	// simple explosion
	public void doExplosion(final int x, final int y, final int c,
			final int dam, final String damtype) {
		Game.instance().doExplosion(x, y, c, 1);
		map.areaDamage(x, y, 2, dam, damtype);
	}

	// draws cursor at given location to buffer
	public void drawCursor(final int x, final int y) {
		final int px = (x - scrollx) * TILEWIDTH;
		final int py = (y - scrolly) * TILEHEIGHT;
		final int sx = 6 * TILEWIDTH;
		final int sy = 0 * TILEHEIGHT;
		buffergraphics.drawImage(QuestApp.effects, px, py, px + TILEWIDTH, py
				+ TILEHEIGHT, sx, sy, sx + TILEWIDTH, sy + TILEHEIGHT, null);
	}

	// draw buffer to screen in correct location
	final public void drawMap(final Graphics g) {
		final Rectangle rect = getBounds();
		final int w = rect.width;
		final int h = rect.height;
		g.drawImage(!animating || animationbuffer == null ? buffer
				: animationbuffer,
				(w - width * TILEWIDTH * zoomfactor / 100) / 2, (h - height
						* TILEHEIGHT * zoomfactor / 100) / 2, width * TILEWIDTH
						* zoomfactor / 100, height * TILEHEIGHT * zoomfactor
						/ 100, null);
	}

	public void render() {
		// create back buffer if needed
		if (buffer == null) {
			buffer = createImage(width * TILEWIDTH, height * TILEHEIGHT);
			if (buffer == null) {
				return;
			}
			buffergraphics = buffer.getGraphics();
			animationbuffer = createImage(width * TILEWIDTH, height
					* TILEHEIGHT);
			animationgraphics = animationbuffer.getGraphics();
		}
		if (map == null) {
			return;
		}
		// draw area to back buffer
		drawTiles(scrollx, scrolly, scrollx + width - 1, scrolly + height - 1);
		if (cursor) {
			drawCursor(curx, cury);
		}
	}

	public void renderAnimation() {
		if (buffer != null) {
			animationgraphics.drawImage(buffer, 0, 0, null);
			drawAnimationFrame(animationgraphics);
		}
	}

	/**
	 * standard paint method. // - builds map image in back buffer then copies
	 * to screen
	 */
	@Override
	public void paint(final Graphics g) {
		final Rectangle rect = getBounds();
		if (zoomfactor != lastzoomfactor) {
			final int w = rect.width;
			final int h = rect.height;
			g.setColor(Color.black);
			g.fillRect(0, 0, w, h);
			lastzoomfactor = zoomfactor;
		}
		if (buffer != null) {
			drawMap(g);
		}
	}

	private final ArrayList animationElements = new ArrayList();

	public void drawAnimationFrame(final Graphics g) {
		synchronized (animationElements) {
			final Iterator it = animationElements.iterator();
			while (it.hasNext()) {
				final Animation ae = (Animation) it.next();
				ae.draw(this, g);

				// remove finished animation parts
				if (ae.isExpired()) {
					it.remove();
				}
			}
			if (animationElements.size() == 0) {
				// Game.warn("Animation stop");
				animating = false;
			}
		}
	}

	public void setoverlay(Set<Point> area) {
		overlay = area;
		render();
		repaint();
	}
}