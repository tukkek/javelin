package javelin.view.mappanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;

/**
 * TODO needs an explicit method for incremenetal updates TODO make sure the
 * screen isn't fully repainting at every turn when done
 * 
 * @author alex
 */
public abstract class MapPanel extends MapPanelCommon {
	public static int tilesize = Preferences.TILESIZEWORLD;

	public Tile[][] tiles = null;
	ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
	Panel parent = new Panel();
	int mapwidth;
	int mapheight;

	boolean initial = true;

	/**
	 * Make sure we have a field for this to ensure we're going to instantiate
	 * {@link #tilesize}**2 listeners for this.
	 */
	public Mouse mouse = getmouselistener();

	final private String configurationkey;

	public MapPanel(int widthp, int heightp, String configurationkeyp) {
		mapwidth = widthp;
		mapheight = heightp;
		setFocusable(false);
		scroll.setFocusable(false);
		scroll.setWheelScrollingEnabled(false);
		scroll.addMouseWheelListener(mouse);
		add(scroll);
		parent.setFocusable(false);
		configurationkey = configurationkeyp;
	}

	protected Mouse getmouselistener() {
		return new Mouse(this);
	}

	void updatesize() {
		parent.setSize(tilesize * mapwidth, tilesize * mapheight);
	}

	void updatetilesize() {
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				t.setSize(tilesize, tilesize);
			}
		}
		updatesize();
		scroll.validate();
	}

	@Override
	public void init() {
		tilesize = gettilesize();
		scroll.setVisible(false);
		updatesize();
		parent.setLayout(new GridLayout(mapwidth, mapheight));
		tiles = new Tile[mapwidth][mapheight];
		for (int y = 0; y < mapheight; y++) {
			for (int x = 0; x < mapwidth; x++) {
				Tile t = newtile(x, y);
				parent.add(t);
				tiles[x][y] = t;
			}
		}
		scroll.add(parent);
		scroll.setVisible(true);
	}

	abstract protected int gettilesize();

	protected abstract Tile newtile(int x, int y);

	@Override
	public void viewPosition(BattleMap m, int x, int y) {
		center(x, y, false);
	}

	@Override
	public void setPosition(BattleMap m, int x, int y) {
		center(x, y, true);
	}

	@Override
	public void update(Graphics g) {
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return getParent().getBounds().getSize();
	}

	@Override
	public void render() {

	}

	void ensureminimumsize() {
		Dimension preferredSize = getPreferredSize();
		while (tilesize * mapwidth < preferredSize.getWidth()
				|| tilesize * mapheight < preferredSize.getHeight()) {
			tilesize += 1;
		}
	}

	@Override
	public void setoverlay(Set<Point> area) {
		// TODO Auto-generated method stub
	}

	@Override
	public void zoom(int factor, boolean save, int x, int y) {
		tilesize += factor * 4;
		ensureminimumsize();
		updatetilesize();
		center(x, y, false);
		Preferences.setoption(configurationkey, tilesize);
	}

	@Override
	public void autozoom(ArrayList<Combatant> combatants, int x, int y) {
		center(x, y, true);
	}

	@Override
	public boolean center(int x, int y, boolean force) {
		int width = scroll.getWidth();
		int height = scroll.getHeight();
		java.awt.Point current = scroll.getScrollPosition();
		x *= tilesize;
		y *= tilesize;
		if (!force && isinside(current.getX(), x, width - tilesize * 2)
				&& isinside(current.getY(), y, height - tilesize * 2)) {
			scroll.setScrollPosition(current);
			return false;
		}
		x -= width / 2;
		y -= height / 2;
		x = Math.min(x,
				parent.getWidth() - width + scroll.getHScrollbarHeight());
		y = Math.min(y,
				parent.getHeight() - height + scroll.getVScrollbarWidth());
		x = Math.max(0, x);
		y = Math.max(0, y);
		scroll.setScrollPosition(x, y);
		return true;
	}

	static private boolean isinside(double from, int value, int offset) {
		return from <= value && value <= from + offset;
	}

	@Override
	public void settilesize(int i) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setdiscovered(HashSet<Point> hashSet) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		if (initial) {
			initial = false;
			scroll.setBounds(getBounds());
			int before = tilesize;
			ensureminimumsize();
			if (tilesize != before) {
				updatetilesize();
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				t.repaint();
			}
		}
	}
}
