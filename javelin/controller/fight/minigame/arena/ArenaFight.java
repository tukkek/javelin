package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.InfiniteList;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.SizeComparator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.minigame.arena.building.ArenaAcademy;
import javelin.controller.fight.minigame.arena.building.ArenaBuilding;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaLair;
import javelin.controller.fight.minigame.arena.building.ArenaShop;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.old.Game;
import javelin.controller.scenario.Campaign;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * TODO would be cool if could generate heroes to fight against at some point
 * 
 * TODO would be amazing to have 4 different map types generated (1 per
 * quadrant), also visually, when the engine allows it
 * 
 * TODO clicking on a building, even if far away, should tell you your current
 * amount of XP
 * 
 * TODO would be cool to have gates that let you teleport anywhere in the map.
 * It could just transport you to a building who has foes closest to it.
 * 
 * TODO instead of current victory condition, add a Gate building that on raise
 * tension has a (current monster EL/EL goal) chance of activating (minimum
 * always 5%). If it is destroyed, lose the arena. If it is activated, save
 * progress and allow to pick up from there.
 * 
 * TODO would be a good time to implement Map#canfly for disallowing flying over
 * obstacles
 * 
 * @see Arena
 * 
 * @author alex
 */
public class ArenaFight extends Minigame {
	public static final float BOOST = 4; // 3,5 talvez?
	static final String RETIRE = "You have beaten this level of the arena! Do you want to retire and have your gladiators available as recruits?\n\nPress r to retire or c to continue...";
	static final int MAPSIZE = 28;
	static final int[] ARENASIZE = new int[] { 21, 10 };
	static final int TENSIONMIN = -5;
	static final int TENSIONMAX = 0;
	static final int ELMIN = -12;
	static final int ELMAX = 0;
	static final Point[] AREA = new Point[] {
			new Point((MAPSIZE - ARENASIZE[0]) / 2,
					(MAPSIZE - ARENASIZE[1]) / 2),
			new Point((MAPSIZE + ARENASIZE[0]) / 2,
					(MAPSIZE + ARENASIZE[1]) / 2) };

	class ArenaSetup extends BattleSetup {
		@Override
		public void generatemap(Fight f) {
			Terrain t = Terrain.NONWATER[RPG.r(0, Terrain.NONWATER.length - 1)];
			f.map = RPG.pick(t.getmaps());
			super.generatemap(f);
			Square[][] map = f.map.map;
			f.map.map = new Square[MAPSIZE][];
			Fight.state.map = f.map.map;
			for (int i = 0; i < MAPSIZE; i++) {
				f.map.map[i] = Arrays.copyOfRange(map[i], 0, MAPSIZE);
			}
			for (int x = 0; x < MAPSIZE; x++) {
				for (int y = 0; y < MAPSIZE; y++) {
					Point p = new Point(x, y);
					if (!p.validate(AREA[0].x, AREA[0].y, AREA[1].x,
							AREA[1].y)) {
						f.map.map[x][y].blocked = true;
						f.map.map[x][y].flooded = false;
					} else if (x == AREA[0].x || x == AREA[1].x - 1
							|| y == AREA[0].y || y == AREA[1].y - 1) {
						f.map.map[x][y].blocked = false;
					}
				}
			}
		}

		@Override
		public void place() {
			ArrayList<Combatant> gladiators = new ArrayList<Combatant>(
					state.blueTeam);
			placebuildings();
			Point p = null;
			while (p == null || !validate(p)) {
				p = getcenterpoint();
			}
			enter(gladiators, state.blueTeam, p);
		}

		Point getcenterpoint() {
			return new Point(RPG.r(AREA[0].x, AREA[1].x),
					RPG.r(AREA[0].y, AREA[1].y));
		}

		void placebuildings() {
			ArrayList<ArrayList<ArenaBuilding>> quadrants = new ArrayList<ArrayList<ArenaBuilding>>(
					4);
			for (int i = 0; i < 4; i++) {
				quadrants.add(new ArrayList<ArenaBuilding>());
			}
			generate(1, ArenaAcademy.class, quadrants);
			generate(1, ArenaLair.class, quadrants);
			generate(1, ArenaShop.class, quadrants);
			generate(state.blueTeam.size(), ArenaFountain.class, quadrants);
			Collections.shuffle(quadrants);
			for (int i = 0; i < quadrants.size(); i++) {
				for (ArenaBuilding b : quadrants.get(i)) {
					place(b, i);
				}
			}
		}

		void place(ArenaBuilding b, int quadrant) {
			int minx = AREA[0].x + 1;
			int maxx = AREA[1].x - 2;
			int midx = (minx + maxx) / 2;
			int miny = AREA[0].y + 1;
			int maxy = AREA[1].y - 2;
			int midy = (miny + maxy) / 2;
			Point p = null;
			searching: while (p == null) {
				int xa = RPG.r(minx, midx);
				int xb = RPG.r(midx, maxx);
				int ya = RPG.r(miny, midy);
				int yb = RPG.r(midy, maxy);
				if (quadrant == 0) {
					p = new Point(xa, ya);
				} else if (quadrant == 1) {
					p = new Point(xa, yb);
				} else if (quadrant == 2) {
					p = new Point(xb, ya);
				} else if (quadrant == 3) {
					p = new Point(xb, yb);
				}
				for (int x = p.x - 1; x <= p.x + 1; x++) {
					for (int y = p.y - 1; y <= p.y + 1; y++) {
						if (state.getcombatant(p.x, p.y) != null) {
							p = null;
							continue searching;
						}
					}
				}
			}
			for (int x = p.x - 1; x <= p.x + 1; x++) {
				for (int y = p.y - 1; y <= p.y + 1; y++) {
					state.map[x][y].clear();
				}
			}
			b.setlocation(p);
			state.blueTeam.add(b);
		}

		void generate(int amount, Class<? extends ArenaBuilding> building,
				ArrayList<ArrayList<ArenaBuilding>> quadrants) {
			Collections.shuffle(quadrants);
			quadrants.sort(SizeComparator.INSTANCE);
			for (int i = 0; i < amount; i++) {
				try {
					quadrants.get(i % 4).add(building.newInstance());
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public int gold = 0;
	/** {@link Item} bag for {@link #gladiators}. */
	public HashMap<Integer, ArrayList<Item>> items = new HashMap<Integer, ArrayList<Item>>();

	ArrayList<Combatant> gladiators = new ArrayList<Combatant>();
	int tension = RPG.r(TENSIONMIN, TENSIONMAX);
	float check = -Float.MAX_VALUE;
	/**
	 * Ensures that new waves are never becoming less dangerous (pressuring the
	 * player to upgrade and not just sit around).
	 */
	int baseline = Integer.MIN_VALUE;
	ArrayList<ArrayList<Combatant>> foes = new ArrayList<ArrayList<Combatant>>();
	int goal = 6;

	/** Constructor. */
	public ArenaFight() {
		meld = true;
		weather = Weather.DRY;
		period = Javelin.PERIODNOON;
		setup = new ArenaSetup();
		meld = false;
		canflee = false;
		endless = true;
	}

	@Override
	public ArrayList<Combatant> generate(Integer el) {
		return new ArrayList<Combatant>();
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return choosegladiators(Integer.MIN_VALUE, 1.25);
	}

	public ArrayList<Combatant> choosegladiators(double crmin, double crmax) {
		InfiniteList<Monster> candidates = new InfiniteList<Monster>();
		for (float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (crmin <= cr && cr <= crmax) {
				for (Monster m : Javelin.MONSTERSBYCR.get(cr)) {
					if (!m.internal) {
						candidates.add(m);
					}
				}
			}
		}
		ArrayList<Combatant> gladiators = new ArrayList<Combatant>();
		while (CrCalculator.calculateel(gladiators) < Campaign.INITIALEL) {
			ArrayList<Monster> page = candidates.pop(3);
			ArrayList<String> names = new ArrayList<String>(3);
			for (int i = 0; i < 3; i++) {
				Monster m = page.get(i);
				names.add(m + " (level " + Math.round(m.challengerating) + ")");
			}
			Monster choice = page.get(Javelin.choose("Select your gladiators:",
					names, false, true));
			gladiators.add(new Combatant(choice, true));
			candidates.remove(choice);
		}
		return gladiators;
	}

	@Override
	public ArrayList<Item> getbag(Combatant c) {
		ArrayList<Item> bag = items.get(c.id);
		if (bag == null) {
			bag = new ArrayList<Item>();
			items.put(c.id, bag);
		}
		return bag;
	}

	@Override
	public void ready() {
		/* TODO this will be selected later */
		for (Combatant c : state.blueTeam) {
			if (c instanceof ArenaBuilding) {
				continue;
			}
			gladiators.add(c);
		}
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		// TODO parent is enough
		super.withdraw(combatant, screen);
	}

	@Override
	public boolean onend() {
		for (Combatant c : new ArrayList<Combatant>(state.blueTeam)) {
			if (c.source.passive || c.mercenary) {
				state.blueTeam.remove(c);
			}
		}
		return false;
	}

	@Override
	public void startturn(Combatant acting) {
		super.startturn(acting);
		for (ArrayList<Combatant> group : new ArrayList<ArrayList<Combatant>>(
				foes)) {
			rewardxp(group);
		}
		if (!state.blueTeam.contains(state.next)) {
			return;
		}
		awaken();
		if (acting.ap < check) {
			if (state.redTeam.isEmpty()) {
				reward(state.dead);
			}
			return;
		}
		int elblue = CrCalculator.calculateel(getgladiators());
		int elred = CrCalculator.calculateel(state.redTeam);
		if (elred - elblue < tension && !goalreached(getgladiators())) {
			raisetension(elblue);
			tension = RPG.r(TENSIONMIN, TENSIONMAX);
			reward(state.dead);
		}
		check = acting.ap + RPG.r(10, 40) / 10f;
	}

	void reward(ArrayList<Combatant> dead) {
		ArrayList<Combatant> defeated = new ArrayList<Combatant>(dead.size());
		for (Combatant c : new ArrayList<Combatant>(dead)) {
			if (c.mercenary || c.summoned) {
				dead.remove(c);
			} else if (!gladiators.contains(c)) {
				defeated.add(c);
				dead.remove(c);
				this.gold += RewardCalculator.getgold(c.source.challengerating)
						* BOOST;
			}
		}
	}

	void rewardxp(ArrayList<Combatant> group) {
		List<Combatant> redteam = state.getredTeam();
		for (Combatant foe : group) {
			if (redteam.contains(foe)) {
				return;
			}
		}
		RewardCalculator.rewardxp(getallies(), group, BOOST);
		foes.remove(group);
	}

	public List<Combatant> getgladiators() {
		ArrayList<Combatant> gladiators = new ArrayList<Combatant>(
				this.gladiators.size());
		for (Combatant c : state.blueTeam) {
			if (this.gladiators.contains(c)) {
				gladiators.add(c);
			}
		}
		return gladiators;
	}

	void awaken() {
		for (Combatant c : new ArrayList<Combatant>(state.dead)) {
			if (c.mercenary || !gladiators.contains(c)) {
				continue;
			}
			if (c.getnumericstatus() == Combatant.STATUSDEAD) {
				state.dead.remove(c);
				gladiators.remove(c);
				continue;
			}
			if (state.getcombatant(c.location[0], c.location[1]) != null) {
				continue;
			}
			if (c.ap >= state.next.ap) {
				continue;
			}
			c.ap += 1;
			if (RPG.r(1, 10) > Math.abs(c.hp)) {
				state.dead.remove(c);
				c.hp = 1;
				state.blueTeam.add(c);
				notify(c + " awakens!", c.getlocation());
			}
		}
	}

	void raisetension(int elblue) {
		placefoes(elblue);
		refillfountains(getfountains());
	}

	public void refillfountains(ArrayList<ArenaFountain> fountains) {
		if (check == -Float.MAX_VALUE || fountains.isEmpty()) {
			return;
		}
		float refillchance = 1f / fountains.size();
		int i = 0;
		Point p = null;
		for (ArenaFountain f : fountains) {
			if (f.spent && !f.isdamaged() && RPG.random() < refillchance) {
				f.setspent(false);
				i += 1;
				p = f.getlocation();
			}
		}
		if (i > 0) {
			notify(i + " fountain(s) refilled!", p);
		}
	}

	public void placefoes(int elblue) {
		ArrayList<Combatant> last = null;
		int min = Math.max(elblue + ELMIN, baseline);
		baseline = min;
		for (int el = min; el <= elblue + ELMAX; el += 1) {
			ArrayList<Combatant> group;
			try {
				group = EncounterGenerator.generate(el,
						Arrays.asList(Terrain.ALL));
			} catch (GaveUpException e) {
				continue;
			}
			ArrayList<Combatant> redteam = (ArrayList<Combatant>) state.redTeam
					.clone();
			redteam.addAll(group);
			int tension = CrCalculator.calculateel(redteam) - elblue;
			if (tension == this.tension) {
				enter(group, state.redTeam);
				break;
			}
			if (tension > this.tension) {
				enter(last == null ? group : last, state.redTeam);
				break;
			}
			last = group;
		}
	}

	public void enter(ArrayList<Combatant> group, ArrayList<Combatant> team) {
		Point entrance = null;
		while (entrance == null || !validate(entrance)) {
			entrance = getmonsterentry();
		}
		enter(group, team, entrance);
	}

	float getbaseap() {
		float ap = 0;
		List<Combatant> gladiators = getgladiators();
		for (Combatant c : gladiators) {
			ap += c.ap;
		}
		return ap / gladiators.size();
	}

	ArrayList<ArenaFountain> getfountains() {
		ArrayList<ArenaFountain> fountains = new ArrayList<ArenaFountain>();
		for (Combatant c : state.blueTeam) {
			ArenaFountain f = c instanceof ArenaFountain ? (ArenaFountain) c
					: null;
			if (f != null/* && !f.isdamaged() */) {
				fountains.add(f);
			}
		}
		return fountains;
	}

	Point getmonsterentry() {
		Point p = new Point(RPG.r(MAPSIZE), RPG.r(MAPSIZE));
		if (RPG.chancein(2)) {
			p.x = RPG.chancein(2) ? AREA[0].x : AREA[1].x - 1;
		} else {
			p.y = RPG.chancein(2) ? AREA[0].y : AREA[1].y - 1;
		}
		return p;
	}

	void enter(ArrayList<Combatant> entering, List<Combatant> team,
			Point entry) {
		if (team == state.redTeam) {
			foes.add(entering);
		}
		LinkedList<Combatant> place = new LinkedList<Combatant>(entering);
		Collections.shuffle(place);
		Combatant last = place.pop();
		last.setlocation(entry);
		float ap = getbaseap();
		if (!team.contains(last)) {
			team.addAll(entering);
			for (Combatant c : entering) {
				c.rollinitiative();
				if (check != -Float.MAX_VALUE) {
					c.ap += ap;
					c.initialap = c.ap;
				}
			}
		}
		while (!place.isEmpty()) {
			Point p = last.getlocation();
			p.x += RPG.r(-1, +1);
			p.y += RPG.r(-1, +1);
			if (!validate(p)) {
				continue;
			}
			last = place.pop();
			last.setlocation(p);
		}
		if (team == state.redTeam) {
			notify("New enemies enter the arena!", last.getlocation());
		}
	}

	public boolean validate(Point p) {
		return p.validate(AREA[0].x, AREA[0].y, AREA[1].x, AREA[1].y)
				&& !state.map[p.x][p.y].blocked
				&& state.getcombatant(p.x, p.y) == null;
	}

	public void notify(String text, Point p) {
		Game.redraw();
		BattleScreen.active.center(p.x, p.y);
		Javelin.message(text, false);
	}

	@Override
	public void checkend() {
		List<Combatant> gladiators = getgladiators();
		if (gladiators.isEmpty()) {
			state.blueTeam.clear();
			String msg = "You've lost this match... better luck next time!";
			Javelin.message(msg, true);
			throw new EndBattle();
		}
		if (goalreached(gladiators)) {
			char retire = ' ';
			while (retire != 'c' && retire != 'r') {
				retire = Javelin.prompt(RETIRE);
			}
			if (retire == 'c') {
				goal += 5;
			} else {
				for (Combatant c : gladiators) {
					ArenaFountain.heal(c);
				}
				Arena.get().gladiators.addAll(gladiators);
				throw new EndBattle();
			}
		}
	}

	public boolean goalreached(List<Combatant> gladiators) {
		boolean goalreached = true;
		for (Combatant c : gladiators) {
			if (c.source.challengerating < goal) {
				goalreached = false;
				break;
			}
		}
		return goalreached;
	}

	public static ArenaFight get() {
		Fight f = Javelin.app.fight;
		return f != null && f instanceof ArenaFight ? (ArenaFight) f : null;
	}

	@Override
	public void die(Combatant c, BattleState s) {
		// if (c instanceof ArenaAcademy) {
		// c.hp = 1;
		// return;
		// }
		super.die(c, s);
	}

	List<Combatant> getallies() {
		ArrayList<Combatant> allies = new ArrayList<Combatant>();
		for (Combatant c : state.blueTeam) {
			if (!c.source.passive) {
				allies.add(c);
			}
		}
		return allies;
	}
}
