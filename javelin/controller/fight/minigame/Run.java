package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.BattleSetup;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.item.Key;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.model.world.location.unique.minigame.Ziggurat;
import javelin.view.mappanel.Tile;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * {@link Ziggurat} match.
 * 
 * @author alex
 */
public class Run extends Minigame {
	static final boolean DEBUG = false;

	class Segment {
		boolean captured = false;
		Monster monster = null;
		int cr;
		int x, y;

		public Segment(int xp, int yp, int crp) {
			x = xp;
			y = yp;
			cr = crp;
			List<Monster> tier = null;
			while (tier == null || tier.isEmpty()) {
				tier = pool.get(crp);
				crp -= 1;
			}
			monster = RPG.pick(tier);
			tier.remove(monster);
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
			Point a = getpointa();
			Point b = getpointb();
			for (int x = a.x; x <= b.x; x++) {
				for (int y = a.y; y <= b.y; y++) {
					Square s = state.map[x][y];
					s.obstructed = x == a.x || y == a.y || x == b.x || y == b.y;
					s.blocked = false;
				}
			}
			state.map[a.x][a.y].blocked = true;
			state.map[b.x][a.y].blocked = true;
			state.map[a.x][b.y].blocked = true;
			state.map[b.x][b.y].blocked = true;
			for (int x = a.x; x <= b.x; x++) {
				for (int y = a.y; y <= b.y; y++) {
					BattleScreen.active.mappanel.tiles[x][y].repaint();
				}
			}
		}

		public void capture() {
			captured = true;
			for (Segment s : getneighbors(this)) {
				s.open();
			}
		}
	}

	/** Number of segments in each {@link javelin.controller.map.Ziggurat}. */
	public static final int NSEGMENTS = 6;
	/** Dimension of each segment. */
	public static final int SEGMENTSIZE = 5;

	static final float INITIALEL = 7;

	final HashMap<Integer, ArrayList<Monster>> pool =
			new HashMap<Integer, ArrayList<Monster>>();

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
		meld = false;
		pool();
		createsegment(0, 0, 1);
		setup = new BattleSetup() {
			@Override
			public void place() {
				segments[0][0].capture();
				segments[1][0].capture();
				segments[0][1].capture();
				segments[0][0].open();
				segments[1][0].open();
				segments[0][1].open();
				for (Combatant c : state.blueTeam) {
					Segment s = segments[0][0];
					c.setlocation(getrandompoint(state, s.getpointa(),
							s.getpointb()));
				}
			}

			@Override
			public void rollinitiative() {
				// dont
			}
		};
	}

	void pool() {
		for (float cr : Javelin.MONSTERSBYCR.keySet()) {
			List<Monster> tier =
					new ArrayList<Monster>(Javelin.MONSTERSBYCR.get(cr));
			for (Monster m : new ArrayList<Monster>(tier)) {
				if (m.fly > 0) {
					tier.remove(m);
				}
			}
			int round = Math.max(1, Math.round(cr));
			ArrayList<Monster> pooltier = pool.get(cr);
			if (pooltier == null) {
				pooltier = new ArrayList<Monster>(tier);
				pool.put(round, pooltier);
			} else {
				pooltier.addAll(tier);
			}
		}
	}

	void createsegment(int x, int y, float cr) {
		if (0 <= x && x < NSEGMENTS && 0 <= y && y < NSEGMENTS
				&& segments[x][y] == null) {
			segments[x][y] = new Segment(x, y, Math.round(cr));
			createsegment(x + 1, y, cr + 1.25f);
			createsegment(x, y + 1, cr + 1.25f);
		}
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return new ArrayList<Combatant>();
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
			disputed.capture();
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

	enum Neighbor {
		NORTH, EAST, SOUTH, WEST
	}

	void startbattle(Segment s) {
		disputed = s;
		if (!DEBUG) {
			ArrayList<Segment> neighbors = getneighbors(s);
			for (Segment neighbor : new ArrayList<Segment>(neighbors)) {
				if (neighbor.captured) {
					neighbors.remove(neighbor);
				}
			}
			neighbors.add(s);
			while (state.redTeam.isEmpty() || ChallengeRatingCalculator
					.calculateel(state.redTeam) < ChallengeRatingCalculator
							.calculateel(state.blueTeam) - 1) {
				Segment neighbor =
						neighbors.isEmpty() ? s : RPG.pick(neighbors);
				neighbors.remove(neighbor);
				Combatant c = new Combatant(neighbor.monster.clone(), true);
				state.redTeam.add(c);
				c.setlocation(positionenemy(s, neighbor));
				c.rollinitiative();
				c.ap = state.next.ap;
				c.initialap = state.next.ap;
			}
		}
		s.discover();
	}

	Point positionenemy(Segment current, Segment neighbor) {
		Point a = current.getpointa();
		Point b = current.getpointb();
		if (current == neighbor) {
			a.x += 1;
			a.y += 1;
			b.x -= 1;
			b.y -= 1;
		} else if (neighbor.y > current.y) {// north
			b.y = a.y;
		} else if (neighbor.y < current.y) {// south
			a.y = b.y;
		} else if (neighbor.x > current.x) {// east
			a.x = b.x;
		} else {// west
			b.x = a.x;
		}
		return BattleSetup.getrandompoint(state, a, b);
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
		if (win()) {
			return;
		}
		ArrayList<Monster> pool = getrecruits();
		while (ChallengeRatingCalculator
				.calculateel(state.blueTeam) < targetel) {
			String currentteam = "Current team: ";
			int perline = 0;
			for (Combatant c : state.blueTeam) {
				currentteam += c.toString() + ", ";
				perline += 1;
				if (perline >= 3) {
					perline = 0;
					currentteam += "\n";
				}
			}
			currentteam = currentteam.substring(0, currentteam.length() - 2);
			Monster m = pool.get(Javelin.choose(
					"What unit do you want to recruit?\n\n" + currentteam, pool,
					true, true));
			Combatant c = new Combatant(m.clone(), true);
			c.setlocation(BattleSetup.getrandompoint(state,
					disputed.getpointa(), disputed.getpointb()));
			c.ap = state.next.ap;
			c.initialap = state.next.ap;
			state.blueTeam.add(c);
			Javelin.app.switchScreen(BattleScreen.active);
		}
	}

	ArrayList<Monster> getrecruits() {
		ArrayList<Monster> pool = new ArrayList<Monster>();
		for (Segment[] ss : segments) {
			for (Segment s : ss) {
				if (s.captured) {
					pool.add(s.monster);
				}
			}
		}
		pool.sort(new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				return o2.challengerating.compareTo(o1.challengerating);
			}
		});
		while (pool.size() > 5) {
			pool.remove(5);
		}
		return pool;
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
	public boolean onend() {
		if (victory) {
			Javelin.message(
					"Congratulations, you have won!\n"
							+ "You can now visit the Ziggurat on the world map to obtain a temple key.",
					true);
			Ziggurat z = (Ziggurat) World.getall(Ziggurat.class).get(0);
			z.key = Key.generate();
		} else {
			Javelin.message("You lost...", true);
		}
		return false;
	}
}
