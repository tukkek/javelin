package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.BattleSetup;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.unique.minigame.Ziggurat;
import javelin.view.mappanel.Tile;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * {@link Ziggurat} match.
 * 
 * @author alex
 */
public class Run extends Fight {
	class Segment {
		boolean captured = false;
		Monster monster = null;
		int cr;
		int x, y;

		public Segment(int xp, int yp, int crp) {
			x = xp;
			y = yp;
			cr = crp;
			pick: while (monster == null) {
				monster = RPG.pick(POOL.get(cr));
				for (Segment[] ss : segments) {
					for (Segment s : ss) {
						if (s != null && s.monster.equals(monster)) {
							monster = null;
							continue pick;
						}
					}
				}
			}
		}

		@Override
		public String toString() {
			return Integer.toString(cr);
		}

		public Point getpointa() {
			return new Point(x * SEGMENTSIZE,
					javelin.controller.map.Ziggurat.SIZE
							- (y + 1) * SEGMENTSIZE);
		}

		public Point getpointb() {
			return new Point((x + 1) * SEGMENTSIZE - 1,
					javelin.controller.map.Ziggurat.SIZE - 1 - y * SEGMENTSIZE);
		}

		public void discover() {
			Point a = getpointa();
			Point b = getpointb();
			for (int x = a.x; x <= b.x; x++) {
				for (int y = a.y; y <= b.y; y++) {
					Tile tile = BattleScreen.active.mappanel.tiles[x][y];
					tile.discovered = true;
					tile.repaint();
				}
			}
		}

		void open() {
			int delta = SEGMENTSIZE - 1;
			int x = this.x * SEGMENTSIZE;
			int y = this.y * SEGMENTSIZE;
			state.map[x][y].blocked = true;
			state.map[x + delta][y].blocked = true;
			state.map[x][y + delta].blocked = true;
			state.map[x + delta][y + delta].blocked = true;
			for (int borderx = 0; borderx < SEGMENTSIZE; borderx++) {
				for (int bordery = 0; bordery < SEGMENTSIZE; bordery++) {
					if (borderx == 0 || bordery == 0
							|| borderx == SEGMENTSIZE - 1
							|| bordery == SEGMENTSIZE - 1) {
						state.map[x + borderx][y + bordery].obstructed = true;
					}
				}
			}
		}
	}

	/** Number of segments in each {@link javelin.controller.map.Ziggurat}. */
	public static final int NSEGMENTS = 6;
	public static final int SEGMENTSIZE = 5;

	static final HashMap<Integer, ArrayList<Monster>> POOL =
			new HashMap<Integer, ArrayList<Monster>>();
	static final float INITIALEL = 7;

	static {
		for (float cr : Javelin.MONSTERSBYCR.keySet()) {
			List<Monster> tier =
					new ArrayList<Monster>(Javelin.MONSTERSBYCR.get(cr));
			// for (Monster m : new ArrayList<Monster>(tier)) {
			// if (m.fly > 0) {
			// tier.remove(m);
			// }
			// }
			int round = Math.max(1, Math.round(cr));
			ArrayList<Monster> pooltier = POOL.get(cr);
			if (pooltier == null) {
				pooltier = new ArrayList<Monster>(tier);
				POOL.put(round, pooltier);
			} else {
				pooltier.addAll(tier);
			}
		}
	}

	/**
	 * Organized with [0][0] being southwesternmost, which causes some funky
	 * math in comparison to normal map coordinates.
	 */
	Segment[][] segments = new Segment[NSEGMENTS][NSEGMENTS];
	Segment disputed = null;

	/** Constructor. */
	public Run() {
		map = new javelin.controller.map.Ziggurat();
		period = Javelin.PERIODNIGHT;
		weather = Weather.DRY;
		denydarkvision = true;
		createsegment(0, 0, 1);
		segments[0][0].captured = true;
		segments[1][0].captured = true;
		segments[0][1].captured = true;
		segments[0][0].open();
		segments[1][0].open();
		segments[0][1].open();
		setup = new BattleSetup() {
			@Override
			public void place() {
				for (Combatant c : state.blueTeam) {
					Segment s = segments[0][0];
					c.setlocation(getrandompoint(state, s.getpointa(),
							s.getpointb()));
				}
			}
		};
	}

	void createsegment(int x, int y, int cr) {
		if (0 <= x && x < NSEGMENTS && 0 <= y && y < NSEGMENTS
				&& segments[x][y] == null) {
			segments[x][y] = new Segment(x, y, cr);
			createsegment(x + 1, y, cr + 1);
			createsegment(x, y + 1, cr + 1);
		}
	}

	@Override
	public int getel(int teamel) {
		throw new RuntimeException("don't generate #zigguratrun");
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return new ArrayList<Combatant>();
	}

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		dontflee(screen);
	}

	@Override
	public Boolean win() {
		return super.win() && segments[NSEGMENTS - 1][NSEGMENTS - 1].captured;
	}

	@Override
	public void endturn() {
		super.endturn();
		if (!state.redTeam.isEmpty()) {
			return;
		}
		if (disputed != null) {
			disputed.captured = true;
			recruit(getel());
			disputed = null;
		}
		Segment s = disputesegment();
		if (s != null) {
			startbattle(s);
			return;
		}
	}

	int getel() {
		return Math.round(INITIALEL + disputed.cr);
	}

	void startbattle(Segment s) {
		disputed = s;
		while (state.redTeam.isEmpty() || ChallengeRatingCalculator
				.calculateel(state.getRedTeam()) < ChallengeRatingCalculator
						.calculateel(state.blueTeam) - 1) {
			ArrayList<Monster> pool = new ArrayList<Monster>();
			pool.add(s.monster);
			for (Segment neighbor : getneighbors(s)) {
				if (!neighbor.captured) {
					pool.add(neighbor.monster);
				}
			}
			Combatant c = new Combatant(RPG.pick(pool).clone(), true);
			state.redTeam.add(c);
			Point a = s.getpointa();
			Point b = s.getpointb();
			c.setlocation(
					BattleSetup.getrandompoint(state, a.x, b.x, a.y, b.y));
			c.rollinitiative();
			c.ap = state.next.ap;
			c.initialap = state.next.ap;
			// TODO place neighbors on border
		}
		s.discover();
	}

	Segment disputesegment() {
		for (Segment[] ss : segments) {
			for (Segment s : ss) {
				if (s.captured) {
					continue;
				}
				Point a = s.getpointa();
				Point b = s.getpointb();
				for (Combatant c : state.blueTeam) {
					if (a.x <= c.location[0] && c.location[0] <= b.x
							&& a.y <= c.location[1] && c.location[1] <= b.y) {
						return s;
					}
				}
			}
		}
		return null;
	}

	void recruit(int targetel) {
		while (ChallengeRatingCalculator
				.calculateel(state.blueTeam) < targetel) {
			// TODO
		}
	}

	ArrayList<Segment> getneighbors(Segment s) {
		ArrayList<Segment> neighbors = new ArrayList<Run.Segment>();
		Point[] points = new Point[] { //
				new Point(s.x + 1, s.y), //
				new Point(s.x - 1, s.y), //
				new Point(s.x, s.y + 1), //
				new Point(s.x, s.y - 1), };
		for (Point p : points) {
			if (0 <= p.x && p.x < NSEGMENTS && 0 <= p.y && p.y < NSEGMENTS) {
				neighbors.add(segments[p.x][p.y]);
			}
		}
		return neighbors;
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		ArrayList<Monster> pool = new ArrayList<Monster>(3);
		pool.add(segments[0][0].monster);
		pool.add(segments[1][0].monster);
		pool.add(segments[0][1].monster);
		ArrayList<Combatant> blueteam = new ArrayList<Combatant>();
		while (blueteam.isEmpty() || ChallengeRatingCalculator
				.calculateel(blueteam) < INITIALEL) {
			blueteam.add(new Combatant(RPG.pick(pool).clone(), true));
		}
		return blueteam;
	}

	@Override
	public String toString() {
		String table = "";
		String monsters = "";
		for (Segment[] ss : segments) {
			for (Segment s : ss) {
				String cell = Integer.toString(s.cr);
				while (cell.length() < 3) {
					cell += " ";
				}
				table += cell;
				monsters += "CR" + s.cr + " " + s.monster + "\n";
			}
			table += "\n";
		}
		table += "\n" + monsters;
		return table;
	}

	@Override
	public void draw() {
		super.draw();
		for (Segment[] ss : segments) {
			for (Segment s : ss) {
				if (s.captured) {
					s.discover();
				}
			}
		}
	}

	@Override
	public boolean onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		return false;
	}
}
