//
// Dungeon in traditional rogue style
//

package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;

class RogueDungeon {
	// size of map in terms of blocks
	protected int bh;
	protected int bw;

	// size of blocks in number of tiles
	protected int th;
	protected int tw;

	// room links
	protected static final int LN = 1;
	protected static final int LE = 2;
	protected static final int LS = 4;
	protected static final int LW = 8;
	protected int[][] links;
	protected int[][] linkedzones;

	// room dimensions
	protected int[][] rx;
	protected int[][] ry;
	protected int[][] rw;
	protected int[][] rh;

	private RogueDungeon(int width, int height, int tilewidth, int tileheight) {
		try {
			th = tileheight;
			tw = tilewidth;
			bw = width;
			bh = height;

			rx = new int[width][];
			ry = new int[width][];
			rw = new int[width][];
			rh = new int[width][];
			links = new int[width][];
			linkedzones = new int[width][];

			for (int w = 0; w < width; w++) {
				rx[w] = new int[height];
				ry[w] = new int[height];
				rw[w] = new int[height];
				rh[w] = new int[height];
				links[w] = new int[height];
				linkedzones[w] = new int[height];
			}
		} catch (Exception e) {
			Game.messageTyrant("Failed to construct RogueDungeon");
			Game.messageTyrant(e.toString());
		}
	}

	public static BattleMap create(int l, Theme theme) {
			RogueDungeon rd = new RogueDungeon(3 + RPG.r(l / 3), 3 + RPG
					.r(l / 3), 8 + RPG.d(l), 8 + RPG.d(l));
			BattleMap map=new BattleMap(rd.bw*rd.tw,rd.bh*rd.th);
			map.setLevel(l);
			map.setTheme(theme);
			rd.build(map);
			return map;

	}

	public void replaceLinkedZones(int fromzone, int tozone) {
		for (int lx = 0; lx < bw; lx++)
			for (int ly = 0; ly < bh; ly++) {
				if (linkedzones[lx][ly] == fromzone)
					linkedzones[lx][ly] = tozone;
			}
		// System.out.println(fromzone+" -> "+tozone);
	}

	// link two adjacent linkedzones
	public void makeLink(int x, int y, int dx, int dy) {
		if (dx == 1) {
			links[x][y] |= LE;
			links[x + dx][y + dy] |= LW;
		}
		if (dx == -1) {
			links[x][y] |= LW;
			links[x + dx][y + dy] |= LE;
		}
		if (dy == 1) {
			links[x][y] |= LS;
			links[x + dx][y + dy] |= LN;
		}
		if (dy == -1) {
			links[x][y] |= LN;
			links[x + dx][y + dy] |= LS;
		}

		try {
			int fromzone = linkedzones[x][y];
			int tozone = linkedzones[x + dx][y + dy];
			replaceLinkedZones(fromzone, tozone);
		} catch (Exception e) {
			Game.messageTyrant(e.toString() + " (" + x + "," + y + ") (" + (x + dx)
					+ "," + (y + dy) + ")");
		}
	}

	// ensure the whole map is a single zone
	public void makeConnected() {

		boolean connected = false;
		for (int i = 0; (i < 100000) && (!connected); i++) {
			int tx = RPG.r(bw);
			int ty = RPG.r(bh);
			int dx = RPG.r(3) - 1;
			int dy = RPG.r(3) - 1;

			while ((((dy == 0) && (dx == 0)) || ((dx * dy) != 0))
					|| ((tx + dx) < 0) || ((tx + dx) >= bw) || ((ty + dy) < 0)
					|| ((ty + dy) >= bh)
					|| (linkedzones[tx][ty] == linkedzones[tx + dx][ty + dy])) {
				tx = RPG.r(bw);
				ty = RPG.r(bh);
				dx = RPG.r(3) - 1;
				dy = RPG.r(3) - 1;
			}

			makeLink(tx, ty, dx, dy);

			// test to see if everything is connected
			int minzone = 10000;
			int maxzone = 0;
			for (int x = 0; x < bw; x++)
				for (int y = 0; y < bh; y++) {
					int z = linkedzones[x][y];
					if (z < minzone)
						minzone = z;
					if (z > maxzone)
						maxzone = z;
				}
			connected = (minzone == maxzone);
		}

	}

	public void createDoor(BattleMap m, int tx, int ty) {
		switch (RPG.d(3)) {
			case 1 :
				m.setTile(tx, ty, m.floor());
				break;

			default :
				m.addThing(Door.createDoor(m.getLevel()),tx,ty);
		}
	}

	public void build(BattleMap m) {
	
		// set up lots of individual linkedzones
		for (int x = 0; x < bw; x++)
			for (int y = 0; y < bh; y++) {
				linkedzones[x][y] = x + bw * y;
			}

		// connect all the linkedzones
		makeConnected();

		// set the room dimensions (ex. wall)
		for (int x = 0; x < bw; x++)
			for (int y = 0; y < bh; y++) {
				rw[x][y] = RPG.rspread(2, tw - 3);
				rh[x][y] = RPG.rspread(2, th - 3);
				rx[x][y] = x * tw + RPG.d(tw - rw[x][y] - 2);
				ry[x][y] = y * th + RPG.d(th - rh[x][y] - 2);
			}

		// make all the rooms
		for (int x = 0; x < bw; x++) {
			for (int y = 0; y < bh; y++) {
				makeRoom(m,rx[x][y], ry[x][y], rx[x][y] + rw[x][y] - 1, ry[x][y]
						+ rh[x][y] - 1);
			}
		}

		// make all the corridors, looking E
		for (int x = 0; x < (bw - 1); x++)
			for (int y = 0; y < bh; y++) {
				if ((links[x][y] & LE) > 0) {
					int py1 = ry[x][y] + RPG.r(rh[x][y]);
					int py2 = ry[x + 1][y] + RPG.r(rh[x + 1][y]);

					int minx = rx[x][y] + rw[x][y] + 1;
					int maxx = rx[x + 1][y] - 2;

					createDoor(m,minx - 1, py1);
					createDoor(m,maxx + 1, py2);

					int px = RPG.rspread(minx, maxx);

					m.fillArea(minx, py1, px, py1, m.floor());
					m.fillArea(px, py1, px, py2, m.floor());
					m.fillArea(px, py2, maxx, py2, m.floor());
				}
			}

		// make all the corridors, looking S
		for (int x = 0; x < bw; x++)
			for (int y = 0; y < (bh - 1); y++) {
				if ((links[x][y] & LS) > 0) {
					int px1 = rx[x][y] + RPG.r(rw[x][y]);
					int px2 = rx[x][y + 1] + RPG.r(rw[x][y + 1]);

					int miny = ry[x][y] + rh[x][y] + 1;
					int maxy = ry[x][y + 1] - 2;

					createDoor(m,px1, miny - 1);
					createDoor(m,px2, maxy + 1);

					int py = RPG.rspread(miny, maxy);

					m.fillArea(px1, miny, px1, py, m.floor());
					m.fillArea(px1, py, px2, py, m.floor());
					m.fillArea(px2, py, px2, maxy, m.floor());
				}
			}

		m.replaceTiles(0, m.wall());

	}
	
	private void makeRoom(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1,y1,x2,y2,m.floor());
		decorateRoom(m,x1,y1,x2,y2);
	}
	
	public void decorateRoom(BattleMap m, int x1, int y1, int x2, int y2) {
		int w=x2-x1+1;
		int h=y2-y1+1;
		if ((w<3)||(h<3)) return;
		switch(RPG.d(20)) {
			case 1:
				// vertical columns
				for (int i=1; i<h-1; i+=2) {
					m.setTile(x1+1,y1+i,m.wall());
					m.setTile(x2-1,y1+i,m.wall());
				}
				break;
			case 2:
				// horizontal columns
				for (int i=1; i<h-1; i+=2) {
					m.setTile(x1+i,y1+1,m.wall());
					m.setTile(x1+i,y1-1,m.wall());
				}
				break;	
			default:
				decorateArea(m,x1,y1,x2,y2);
				break;
		}
	}
	
	public void decorateArea(BattleMap m, int x1, int y1, int x2, int y2) {
		int w=x2-x1+1;
		int h=y2-y1+1;
		if ((w<2)||(h<2)) return;
		switch (RPG.d(10)) {
			case 1: {
				// inner area only
				decorateArea(m,x1+1,y1+1,x2-1,y2-1);
				break;
			}
			case 2: {
				// floor surrounded pool
				m.fillArea(x1,y1,x2,y2,Tile.FLOOR);
				if ((w<3)||(h<3)) break;
				m.fillArea(x1+1,y1+1,x2-1,y2-1,Tile.POOL);
				break;
			}
			case 3: {
				// veritically split decorations
				int ym=RPG.rspread(y1,y2);
				decorateArea(m,x1,x2,y1,ym);
				decorateArea(m,x1,x2,ym+1,y2);
			}
			case 4: {
				// horizontally split decorations
				int xm=RPG.rspread(x1,x2);
				decorateArea(m,x1,xm,y1,y2);
				decorateArea(m,xm+1,x2,y1,y2);
			}
			case 5: {
				m.addThing(Lib.createType("IsScenery",m.getLevel()),x1,y1,x2,y2);
			}
				
			default:
				// nothing special
		}
	}
}