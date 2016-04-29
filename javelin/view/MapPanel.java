package javelin.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Tile;

/**
 * Panel descendant used to display a Map contains most of graphics redraw logic
 * also some animation/explosion handling
 * 
 * TODO this redraws the entire screen all the time (besides my background
 * hack), this needs to be totally redesign into a modern UI like that from
 * MegaMek.
 * 
 * @author Tyrant
 * @author alex
 */
public class MapPanel extends Panel {
	private static final Border BUFF =
			BorderFactory.createLineBorder(Color.WHITE);
	private static final Border INVASIONPORTAL =
			BorderFactory.createLineBorder(Color.RED);
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
	protected int width = MapPanel.TILEWIDTH == 32 ? 15 : 25;
	protected int height = MapPanel.TILEWIDTH == 32 ? 15 : 25;

	// zoom factor
	public int tilesize = 32;

	// back buffer
	private Graphics buffergraphics;
	private Image buffer;

	// which map to draw
	public BattleMap map;

	// viewing state
	protected int scrollx = 0;
	protected int scrolly = 0;
	public int curx = 0;
	public int cury = 0;
	public boolean cursor = false;

	// drawing fields
	public int currentTile = Tile.CAVEWALL;

	private Set<Point> overlay = null;
	public HashSet<Point> discovered = new HashSet<Point>();
	public int startx = -1;
	public int starty = -1;
	public int endx = -1;
	public int endy = -1;
	/**
	 * Internal buffer that can be reused if the background didn't change (if we
	 * didn't have to change the {@link #viewPosition(BattleMap, int, int)}).
	 */
	private Image background;
	private BattleState state;
	public static final Preferences PREFERENCES =
			Preferences.userNodeForPackage(BattleScreen.class);
	private boolean isworldscreen;
	/**
	 * Buffer of "x:y" string to prevent from iterating over all
	 * {@link WorldActor}s for every square drawing.
	 */
	static HashMap<String, WorldActor> actors =
			new HashMap<String, WorldActor>();

	public MapPanel(final BattleScreen owner) {
		super();
		addKeyListener(owner.questapp.keyadapter);
		setBackground(Color.black);
	}

	// sets current scroll position and repaints map
	public void viewPosition(final BattleMap m, final int x, final int y) {
		setPosition(m, x, y);
	}

	public void scroll(final int xDelta, final int yDelta) {
		scrollx =
				Math.min(Math.max(0, xDelta + scrollx), map.width - width / 2);
		scrolly = Math.min(Math.max(0, yDelta + scrolly),
				map.height - height / 2);
		render();
		repaint();
	}

	public void setPosition(final BattleMap m, final int x, final int y) {
		if (buffer == null) {
			render();
		}
		background = null;
		map = m;
		int width = endx - startx;
		int height = endy - starty;
		int halfwidth = (int) Math.ceil(width / 2f);
		int halfheight = (int) Math.ceil(height / 2f);
		scrollx = x - halfwidth;
		scrolly = y - halfheight;
		if (scrollx < 0) {
			scrollx = 0;
		} else if (x + halfwidth > map.width) {
			scrollx = map.width - width;
		}
		if (scrolly < 0) {
			scrolly = 0;
		} else if (y + halfheight > map.height) {
			scrolly = map.height - height;
		}
	}

	// override update to stop flicker TODO
	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	@Override
	public Dimension getPreferredSize() {
		return getParent().getBounds().getSize();
	}

	/**
	 * draws tiles in box (x1,y1)-->(x2,y2) to back buffer
	 * 
	 * make sure we draw only once to {@link #buffergraphics} to avoid
	 * flickering
	 */
	private void drawTiles(final int startx, final int starty, final int endx,
			final int endy) {
		if (background == null) {
			drawbackground(startx, starty, endx, endy);
		}
		final Image tempimage = createImage(background.getWidth(null),
				background.getHeight(null));
		final Graphics temp = tempimage.getGraphics();
		temp.drawImage(background, 0, 0, null);
		for (int y = starty; y <= endy; y++) {
			for (int x = startx; x <= endx; x++) {
				final int px = (x - scrollx) * MapPanel.TILEWIDTH;
				final int py = (y - scrolly) * MapPanel.TILEHEIGHT;
				for (Thing head = map.sortZ(x, y); head != null; head =
						head.next) {
					if (head.combatant != null) {
						drawThing(head, px, py, temp, x, y);
					}
				}
				for (final Meld m : state.meld) {
					if (m.x == x && m.y == y) {
						temp.drawImage(state.next.ap >= m.meldsat
								? Images.crystal : Images.dead, px, py, null);
						break;
					}
				}
				final Point p = new Point(x, y);
				if (overlay != null && overlay
						.contains(new javelin.controller.Point(x, y))) {
					temp.setColor(Color.CYAN);
					temp.fillRect((x - scrollx) * MapPanel.TILEWIDTH,
							(y - scrolly) * MapPanel.TILEHEIGHT,
							MapPanel.TILEWIDTH, MapPanel.TILEHEIGHT);
				}
				if (map.isVisible(x, y)) {
					discovered.add(p);
				} else {
					temp.setColor(new Color(0, 0, 0,
							discovered.contains(p) ? 0.8f : 1.0f));
					temp.fillRect(px, py, MapPanel.TILEWIDTH,
							MapPanel.TILEHEIGHT);
				}
			}
		}
		buffergraphics.drawImage(tempimage, 0, 0, null);
		temp.dispose();
	}

	void drawbackground(final int startx, final int starty, final int endx,
			final int endy) {
		background = createImage((endx - startx) * MapPanel.TILEWIDTH,
				(endy - starty) * MapPanel.TILEHEIGHT);
		final Graphics backgroungfx = background.getGraphics();
		for (int y = starty; y <= endy; y++) {
			for (int x = startx; x <= endx; x++) {
				final int px = (x - scrollx) * MapPanel.TILEWIDTH;
				final int py = (y - scrolly) * MapPanel.TILEHEIGHT;
				final int m = map.getTile(x, y);
				final int tile = m & 65535;
				final int image = Tile.filling[map.getTile(x, y + 1) & 65535]
						? Tile.imagefill[tile] : Tile.images[tile];
				final int sx = image % 20 * MapPanel.TILEWIDTH;
				final int sy = image / 20 * MapPanel.TILEHEIGHT;
				backgroungfx.drawImage(QuestApp.tiles, px, py,
						px + MapPanel.TILEWIDTH, py + MapPanel.TILEHEIGHT, sx,
						sy, sx + MapPanel.TILEWIDTH, sy + MapPanel.TILEHEIGHT,
						null);
				if (Tile.borders[tile] > 0) {
					MapPanel.drawcoastline(y, x, m, px, py, backgroungfx, map);
				}
				for (Thing head = map.sortZ(x, y); head != null; head =
						head.next) {
					if (head.combatant == null
							|| head.combatant.source == null) {
						drawThing(head, px, py, backgroungfx, x, y);
					}
				}
			}
		}
		backgroungfx.dispose();
	}

	static void drawcoastline(int y, int x, int m, final int px, final int py,
			Graphics gfx, BattleMap map) {
		if (x > 0 && map.getTile(x - 1, y) != m) {
			gfx.drawImage(QuestApp.scenery, px, py, px + MapPanel.TILEWIDTH,
					py + MapPanel.TILEHEIGHT, 0, 16 * MapPanel.TILEHEIGHT,
					MapPanel.TILEWIDTH, 17 * MapPanel.TILEHEIGHT, null);
		}
		if (x < map.width - 1 && map.getTile(x + 1, y) != m) {
			gfx.drawImage(QuestApp.scenery, px, py, px + MapPanel.TILEWIDTH,
					py + MapPanel.TILEHEIGHT, MapPanel.TILEWIDTH,
					16 * MapPanel.TILEHEIGHT, 2 * MapPanel.TILEWIDTH,
					17 * MapPanel.TILEHEIGHT, null);
		}
		if (y > 0 && map.getTile(x, y - 1) != m) {
			gfx.drawImage(QuestApp.scenery, px, py, px + MapPanel.TILEWIDTH,
					py + MapPanel.TILEHEIGHT, 2 * MapPanel.TILEWIDTH,
					16 * MapPanel.TILEHEIGHT, 3 * MapPanel.TILEWIDTH,
					17 * MapPanel.TILEHEIGHT, null);
		}
		if (y < map.height - 1 && map.getTile(x, y + 1) != m) {
			gfx.drawImage(QuestApp.scenery, px, py, px + MapPanel.TILEWIDTH,
					py + MapPanel.TILEHEIGHT, 3 * MapPanel.TILEWIDTH,
					16 * MapPanel.TILEHEIGHT, 4 * MapPanel.TILEWIDTH,
					17 * MapPanel.TILEHEIGHT, null);
		}
	}

	private void drawThing(final Thing t, int x, int y, Graphics gfx,
			final int mapx, final int mapy) {
		drawbackground(t, x, y, gfx);
		if (t.combatant == null || t.combatant.source.avatarfile == null) {
			WorldActor a = actors.get(mapx + ":" + mapy);
			WorldPlace p = isworldscreen && Dungeon.active == null
					&& a instanceof WorldPlace ? (WorldPlace) a : null;
			if (p == null) {
				final int image = t.getImage();
				final int sx = image % 20 * MapPanel.TILEWIDTH;
				final int sy = image / 20 * MapPanel.TILEHEIGHT;
				final Object source = t.get("ImageSource");
				gfx.drawImage(
						source == null ? QuestApp.items
								: (Image) QuestApp.images.get(source),
						x, y, x + MapPanel.TILEWIDTH, y + MapPanel.TILEHEIGHT,
						sx, sy, sx + MapPanel.TILEWIDTH,
						sy + MapPanel.TILEHEIGHT, null);
			} else {
				gfx.drawImage(p.getimage(), x, y, null);
			}
			if (isworldscreen && Dungeon.active == null) {
				if (a != null) {
					Town town = a instanceof Town ? (Town) a : null;
					if (town != null && town.garrison.isEmpty()) {
						// don't draw border for human towns
					} else if (a.realm != null) {
						drawborder(x, y, gfx, a.realm.getawtcolor());
					}
					if (p != null) {
						if (p.ishostile()) {
							gfx.drawImage(Images.hostile, x, y - 2, null);
						} else {
							enhanceplace(mapx, mapy, x, y, gfx, p);
						}
					}
				}
			}
		} else {
			gfx.drawImage(ImageHandler.getImage(t.combatant), x, y, null);
			if (!isworldscreen) {
				if (t.combatant.ispenalized(state)) {
					gfx.drawImage(Images.penalized, x, y, null);
				}
				if (t.combatant.isbuffed()) {
					BUFF.paintBorder(this, gfx, x, y, TILEWIDTH, TILEHEIGHT);
				}
			} else {
				WorldActor a = actors.get(mapx + ":" + mapy);
				if (a != null && a.realm != null) {
					drawborder(x, y, gfx, a.realm.getawtcolor());
				}
			}
		}
	}

	void drawbackground(final Thing t, int x, int y, Graphics gfx) {
		if (t == Game.hero()) {
			gfx.setColor(t.combatant != null
					&& BattleMap.redTeam.contains(t.combatant) ? Color.ORANGE
							: Color.GREEN);
			gfx.fillRect(x, y, MapPanel.TILEWIDTH, MapPanel.TILEHEIGHT);
		} else if (overlay == null && t.combatant != null
				&& BattleScreen.active.drawbackground()) {
			gfx.setColor(MapPanel.STATUS_COLORS[BattleMap.blueTeam
					.indexOf(t.combatant) >= 0 ? 0 : 1][t.combatant
							.getNumericStatus()]);
			gfx.fillRect(x, y, MapPanel.TILEWIDTH, MapPanel.TILEHEIGHT);
		}
	}

	static void drawborder(int x, int y, Graphics gfx, Color getawtcolor) {
		gfx.setColor(getawtcolor);
		gfx.drawLine(x, y + TILEHEIGHT - 1, x + TILEWIDTH, y + TILEHEIGHT - 1);
		gfx.drawLine(x, y + TILEHEIGHT - 2, x + TILEWIDTH, y + TILEHEIGHT - 2);
	}

	void updateplaces() {
		actors.clear();
		for (WorldActor a : WorldScreen.getactors()) {
			actors.put(a.x + ":" + a.y, a);
		}
	}

	static private boolean enhanceplace(final int mapx, final int mapy,
			final int x, final int y, final Graphics gfx, WorldPlace t) {
		if (t.ishosting()) {
			gfx.drawImage(Images.banner, x, y, null);
		}
		if (t.iscrafting()) {
			gfx.drawImage(Images.crafting, x, y, null);
		}
		if (t.isupgrading()) {
			gfx.drawImage(Images.upgrading, x, y, null);
		}
		return true;
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

	// draws cursor at given location to buffer
	public void drawCursor(final int x, final int y) {
		final int px = (x - scrollx) * MapPanel.TILEWIDTH;
		final int py = (y - scrolly) * MapPanel.TILEHEIGHT;
		final int sx = 6 * MapPanel.TILEWIDTH;
		final int sy = 0 * MapPanel.TILEHEIGHT;
		buffergraphics.drawImage(QuestApp.effects, px, py,
				px + MapPanel.TILEWIDTH, py + MapPanel.TILEHEIGHT, sx, sy,
				sx + MapPanel.TILEWIDTH, sy + MapPanel.TILEHEIGHT, null);
	}

	// draw buffer to screen in correct location
	final public void drawMap(final Graphics g) {
		final Rectangle rect = getBounds();
		if (BattleScreen.active.scale()) {
			g.drawImage(buffer.getScaledInstance(rect.width, rect.height,
					Image.SCALE_FAST), 0, 0, null);
		} else {
			int size = rect.height;
			int offset = (rect.width - size) / 2;
			g.drawImage(buffer.getScaledInstance(size, size, Image.SCALE_FAST),
					offset, 0, null);
		}
	}

	// draw area to back buffer
	public void render() {
		if (map == null) {
			return;
		}
		final Rectangle b = getBounds();
		if (BattleScreen.active.scale()) {
			updateposition(scrollx, scrolly,
					scrollx + (int) Math.floor(b.width / tilesize),
					scrolly + (int) Math.floor(b.height / tilesize));
		} else {
			startx = 0;
			starty = 0;
			endx = map.width - 1;
			endy = map.height - 1;
		}
		if (buffer == null) {
			buffer = new BufferedImage((endx - startx) * MapPanel.TILEWIDTH,
					(endy - starty) * MapPanel.TILEHEIGHT,
					BufferedImage.TYPE_INT_RGB);
			if (buffer == null) {
				return;
			}
			buffergraphics = buffer.getGraphics();
		}
		state = map.getState();
		isworldscreen = BattleScreen.active instanceof WorldScreen;
		updateplaces();
		drawTiles(startx, starty, endx, endy);
		if (cursor) {
			drawCursor(curx, cury);
		}
	}

	private void updateposition(int startx, int starty, int endx, int endy) {
		if (startx < 0) {
			endx += -startx;
			startx = 0;
		}
		if (starty < 0) {
			endy += -starty;
			starty = 0;
		}
		if (endx >= map.width) {
			startx = map.width - (endx - startx) - 1;
			if (startx < 0) {
				startx = 0;
			}
			endx = map.width - 1;
		}
		if (endy >= map.height) {
			starty = map.height - (endy - starty) - 1;
			if (starty < 0) {
				starty = 0;
			}
			endy = map.height - 1;
		}
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
	}

	/**
	 * standard paint method. // - builds map image in back buffer then copies
	 * to screen
	 */
	@Override
	public void paint(final Graphics g) {
		if (buffer != null) {
			drawMap(g);
		}
	}

	public void setoverlay(Set<Point> area) {
		overlay = area;
		render();
		repaint();
	}

	public void zoom(int factor, boolean save, int x, int y) {
		tilesize += factor;
		buffer = null;
		render();
		viewPosition(map, x, y);
		if (save) {
			MapPanel.PREFERENCES.putInt("zoom", tilesize);
		}
	}

	public void autozoom(ArrayList<Combatant> combatants, int x, int y) {
		if (checkallonscreen(combatants)) {// zoom in
			while (tilesize <= 32 * 1.5) {
				zoom(+1, false, x, y);
				if (!checkallonscreen(combatants)) {
					zoom(-1, false, x, y);
					return;
				}
			}
		} else {// zoom out
			while (tilesize >= 32 / 1.5) {
				zoom(-1, false, x, y);
				if (checkallonscreen(combatants)) {
					return;
				}
			}
		}
	}

	private boolean checkallonscreen(ArrayList<Combatant> combatants) {
		for (Combatant c : combatants) {
			if (!(startx <= c.location[0] && c.location[0] <= endx
					&& starty <= c.location[1] && c.location[1] <= endy)) {
				return false;
			}
		}
		return true;
	}
}