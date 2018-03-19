package javelin.controller.fight.minigame;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.Point;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Building;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

public class BattlefieldFight extends Minigame {
	static final boolean DEBUG = true;
	/** This is used as a come-back mechanic (negative feedback loop). */
	static final float MAXARMY = 30;
	static final int CHOICES = 3;
	static final List<Monster> COMMANDERS = getcommanders();
	static final List<Terrain> TERRAIN = Arrays.asList(Terrain.NONWATER);
	static final float POINTSPERTURN = .25f;

	class Reinforcement {
		ArrayList<Combatant> commander = new ArrayList<Combatant>();
		ArrayList<Combatant> elites;
		ArrayList<Combatant> footsoldiers = new ArrayList<Combatant>();

		Reinforcement(int el) {
			el = Math.min(20, el);
			generatecommander(el);
			generateelites(el);
			generatefootsoldiers(el);
		}

		Reinforcement(float el) {
			this(Math.round(el));
		}

		void generatefootsoldiers(int el) {
			int base = Math.max(1, el - RPG.r(5, 10));
			ArrayList<Combatant> footsoldiers = null;
			while (footsoldiers == null) {
				try {
					footsoldiers = EncounterGenerator.generate(base, TERRAIN);
					if (footsoldiers.size() == 1) {
						footsoldiers = null;
					}
				} catch (GaveUpException e) {
					base += RPG.chancein(2) ? +1 : -1;
				}
			}
			this.footsoldiers.addAll(footsoldiers);
			while (CrCalculator.calculateel(this.footsoldiers) < el) {
				for (Combatant c : footsoldiers) {
					this.footsoldiers.add(new Combatant(c.source, true));
				}
			}
		}

		void generateelites(int el) {
			for (int target = el; elites == null;) {
				try {
					elites = EncounterGenerator.generate(target, TERRAIN);
					if (elites.size() == 1) {
						elites = null;
					}
				} catch (GaveUpException e) {
					target += RPG.chancein(2) ? +1 : -1;
				}
			}
		}

		void generatecommander(int el) {
			for (float cr = el; commander.isEmpty(); cr -= 1) {
				List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
				if (tier == null) {
					continue;
				}
				commander.add(new Combatant(RPG.pick(tier), true));
			}
		}
	}

	class Flagpole extends Building {
		boolean blueteam;
		float rank;

		public Flagpole(float rank, boolean blueteam) {
			super(Javelin.getmonster("building"), false);
			this.rank = rank;
			maxhp = Math.round(rank * 25);
			hp = maxhp;
			source.challengerating = 5f * rank;
			source.customName = "Flagpole";
			setteam(blueteam);
		}

		void setteam(boolean blueteam) {
			this.blueteam = blueteam;
			source.avatarfile = blueteam ? "flagpoleblue" : "flagpolered";
		}

		@Override
		public void damage(int damagep, BattleState s, int reduce) {
			super.damage(damagep, s, reduce);
			if (hp <= 0) {
				Flagpole clone = (Flagpole) s.clone(this).clonesource();
				if (blueteam) {
					s.blueTeam.remove(clone);
					s.redTeam.add(clone);
				} else {
					s.redTeam.remove(clone);
					s.blueTeam.add(clone);
				}
				clone.setteam(!blueteam);
				clone.hp = 1;
			}
		}

		@Override
		public void act(BattleState s) {
			super.act(s);
			hp += rank;
			if (hp > maxhp) {
				hp = maxhp;
			}
			ap += 1;
		}

		@Override
		public BattleMouseAction getmouseaction() {
			return new BattleMouseAction() {
				@Override
				public void onenter(Combatant current, Combatant target, Tile t,
						BattleState s) {
					BigDecimal points = new BigDecimal(rank * POINTSPERTURN);
					points.setScale(2);
					updateflagpoles();
					int upkeep = Math.round(
							100 * getupkeep(state.blueTeam, blueflagpoles));
					String message = "This is your flagpole. It generates "
							+ points
							+ " army point(s) per turn, reduced by your current army upkeep ("
							+ upkeep
							+ "%).\nIf it is captured by the enemy, attack it to recapture it for your team!\nYou currently have "
							+ Math.round(bluepoints)
							+ " army points, click to recruit new units.";
					Game.message(message, Delay.NONE);
				}

				@Override
				public boolean validate(Combatant current, Combatant target,
						BattleState s) {
					return blueteam;
				}

				@Override
				public void act(Combatant current, Combatant target,
						BattleState s) {
					BattleScreen.perform(new Runnable() {
						@Override
						public void run() {
							recruitbluearmy();
						}
					});
				}
			};
		}
	}

	class BattlefieldSetup extends BattleSetup {
		@Override
		public void place() {
			int width = map.map.length;
			int height = map.map[0].length;
			int midx = width / 2;
			int midy = height / 2;
			boolean[] regions = new boolean[] { false, false, false, false };
			HashSet<Integer> blueterrains = new HashSet<Integer>();
			while (blueterrains.size() < 2) {
				int i = RPG.r(0, 3);
				blueterrains.add(i);
				regions[i] = true;
			}
			placeflagpoles(new Point(1, 1), new Point(midx - 1, midy - 1),
					regions[0]);
			placeflagpoles(new Point(midx + 1, 1),
					new Point(width - 1, midy - 1), regions[1]);
			placeflagpoles(new Point(1, midy + 1),
					new Point(midx - 1, height - 1), regions[2]);
			placeflagpoles(new Point(midx + 1, midy + 1),
					new Point(width - 1, height - 1), regions[3]);
			updateflagpoles();
			for (Combatant c : state.getcombatants()) {
				if (c instanceof Flagpole) {
					continue;
				}
				placeunit(c);
			}
		}

		void placeflagpoles(Point from, Point to, boolean blueteam) {
			int flagpoles = RPG.pick(new int[] { 1, 2, 4 });
			int placed = 0;
			placing: while (placed < flagpoles) {
				Point spot = new Point(RPG.r(from.x, to.x),
						RPG.r(from.y, to.y));
				if (checkblocked(spot)) {
					continue placing;
				}
				for (Point p : Point.getadjacent()) {
					p.x += spot.x;
					p.y += spot.y;
					if (checkblocked(p)) {
						continue placing;
					}
				}
				Flagpole flag = new Flagpole(4 / flagpoles, blueteam);
				flag.setlocation(spot);
				if (blueteam) {
					state.blueTeam.add(flag);
				} else {
					state.redTeam.add(flag);
				}
				placed += 1;
			}
		}
	}

	ArrayList<Combatant> bluearmy = new ArrayList<Combatant>();
	ArrayList<Combatant> redarmy = new ArrayList<Combatant>();

	ArrayList<Flagpole> blueflagpoles = new ArrayList<Flagpole>();
	ArrayList<Flagpole> redflagpoles = new ArrayList<Flagpole>();

	ArrayList<Combatant> redcommanders = new ArrayList<Combatant>();
	ArrayList<Combatant> redelites = new ArrayList<Combatant>();
	ArrayList<Combatant> redfootsoliders = new ArrayList<Combatant>();

	float lastupdate = Float.MIN_VALUE;
	float redpoints = 0;
	float bluepoints = 0;

	public BattlefieldFight() {
		setup = new BattlefieldSetup();
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		redcommanders.add(new Combatant(RPG.pick(COMMANDERS), true));
		redarmy.addAll(redcommanders);
		redelites.addAll(generatesquad(11, 15));
		redarmy.addAll(redelites);
		int blueel = CrCalculator.calculateel(bluearmy);
		ArrayList<Combatant> fodder = generatesquad(5, 10);
		while (blueel > CrCalculator.calculateel(redarmy)) {
			for (Combatant c : fodder) {
				c = new Combatant(c.source, true);
				redfootsoliders.add(c);
				redarmy.add(c);
			}
		}
		return redarmy;
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return bluearmy;
	}

	public boolean choosearmy() {
		List<Monster> commanders = reduce(COMMANDERS);
		int commanderi = Javelin.choose("Choose your commander:", commanders,
				false, false);
		if (commanderi < 0) {
			return false;
		}
		bluearmy.add(new Combatant(commanders.get(commanderi), true));
		List<Combatant> elites = choosearmy("Choose your elite units:", 11, 15,
				1);
		if (elites == null) {
			return false;
		}
		bluearmy.addAll(elites);
		List<Combatant> footsoldiers = choosearmy("Choose your footsoldiers:",
				5, 10, 5);
		if (footsoldiers == null) {
			return false;
		}
		for (Combatant c : footsoldiers) {
			c.setmercenary(true);
		}
		bluearmy.addAll(footsoldiers);
		return true;
	}

	public List<Combatant> choosearmy(String prompt, int crmin, int crmax,
			int multiply) {
		ArrayList<List<Combatant>> choices = new ArrayList<List<Combatant>>(
				CHOICES);
		ArrayList<String> groups = new ArrayList<String>(CHOICES);
		while (choices.size() < CHOICES) {
			List<Combatant> squad = generatesquad(crmin, crmax);
			ArrayList<Combatant> multiplied = new ArrayList<Combatant>(
					groups.size() * multiply);
			for (int i = 0; i < multiply; i++) {
				for (Combatant c : squad) {
					multiplied.add(new Combatant(c.source, true));
				}
			}
			choices.add(multiplied);
			groups.add(Combatant.group(multiplied));
		}
		int choice = Javelin.choose(prompt, groups, false, false);
		return choice < 0 ? null : choices.get(choice);
	}

	public ArrayList<Combatant> generatesquad(int crmin, int crmax) {
		try {
			ArrayList<Combatant> group = EncounterGenerator
					.generate(RPG.r(crmin, crmax), TERRAIN);
			if (group.size() == 1) {
				return generatesquad(crmin, crmax);
			}
			return group;
		} catch (GaveUpException e) {
			return generatesquad(crmin, crmax);
		}
	}

	<K> List<K> reduce(List<K> l) {
		return l.size() <= CHOICES ? l : l.subList(0, CHOICES);
	}

	static List<Monster> getcommanders() {
		ArrayList<Monster> commanders = new ArrayList<Monster>();
		for (Float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (15 <= cr && cr <= 20) {
				commanders.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		}
		Collections.shuffle(commanders);
		return commanders;
	}

	@Override
	public void startturn(Combatant acting) {
		super.startturn(acting);
		if (lastupdate == Float.MIN_VALUE) {
			lastupdate = acting.ap;
		} else if (acting.ap < lastupdate + .5) {
			return;
		}
		updateflagpoles();
		bluepoints += updatepoints(acting.ap - lastupdate, blueflagpoles,
				state.blueTeam);
		redpoints += updatepoints(acting.ap - lastupdate, redflagpoles,
				state.redTeam);
		lastupdate = acting.ap;
		int elred = calculateteammel(state.redTeam, redflagpoles);
		int elblue = calculateteammel(state.blueTeam, blueflagpoles);
		if (elred < elblue && elred + redpoints >= elblue) {
			ArrayList<Combatant> units = reinforceenemy();
			Game.redraw();
			Javelin.message("The enemy calls for reinforcements:\n"
					+ Combatant.group(units) + "!\n", true);
		}
	}

	int calculateteammel(ArrayList<Combatant> team,
			ArrayList<Flagpole> flagpoles) {
		ArrayList<Combatant> clean = new ArrayList<Combatant>(
				team.size() - flagpoles.size());
		for (Combatant c : team) {
			if (!(c instanceof Flagpole)) {
				clean.add(c);
			}
		}
		return CrCalculator.calculateel(clean);
	}

	ArrayList<Combatant> reinforceenemy() {
		updateredarmy(redcommanders);
		updateredarmy(redelites);
		updateredarmy(redfootsoliders);
		Reinforcement r = new Reinforcement(redpoints);
		int elcommander = CrCalculator.calculateel(redcommanders);
		int elelites = CrCalculator.calculateel(redelites);
		int elfootsolider = CrCalculator.calculateel(redfootsoliders);
		int lowest = Math.min(elcommander, Math.min(elelites, elfootsolider));
		ArrayList<Combatant> selection;
		if (lowest == elcommander) {
			selection = r.commander;
			redcommanders.addAll(selection);
		} else if (lowest == elelites) {
			selection = r.elites;
			redelites.addAll(selection);
		} else {
			selection = r.footsoldiers;
			redfootsoliders.addAll(selection);
		}
		state.redTeam.addAll(selection);
		redpoints -= CrCalculator.calculateel(selection);
		for (Combatant c : selection) {
			placeunit(c);
		}
		return selection;
	}

	float updatepoints(float ap, ArrayList<Flagpole> flagpoles,
			ArrayList<Combatant> team) {
		float points = 0;
		for (Flagpole f : flagpoles) {
			points += f.rank * POINTSPERTURN * ap;
		}
		float upkeep = getupkeep(team, flagpoles);
		return points * upkeep;
	}

	public float getupkeep(ArrayList<Combatant> team,
			ArrayList<Flagpole> flagpoles) {
		team = (ArrayList<Combatant>) team.clone();
		team.removeAll(flagpoles);
		return 1 - (CrCalculator.calculateel(team) / MAXARMY);
	}

	void updateredarmy(ArrayList<Combatant> tier) {
		for (Combatant c : new ArrayList<Combatant>(tier)) {
			if (!state.redTeam.contains(c)) {
				tier.remove(c);
			}
		}
	}

	@Override
	public void die(Combatant c, BattleState s) {
		if (!(c instanceof Flagpole)) {
			super.die(c, s);
		}
	}

	void recruitbluearmy() {
		Game.messagepanel.clear();
		if (bluepoints < 1) {
			Game.message("You don't have army points yet...", Delay.WAIT);
			return;
		}
		Reinforcement r = new Reinforcement(bluepoints);
		ArrayList<String> choices = new ArrayList<String>(3);
		choices.add(group(r.commander));
		choices.add(group(r.elites));
		choices.add(group(r.footsoldiers));
		int choice = Javelin.choose(
				"You have " + Math.round(bluepoints)
						+ " army points. Which unit(s) do you want to reinforce with?\n"
						+ "(Keep in mind that recruiting once with more army points is better than recruiting many times with fewer points.)",
				choices, true, true);
		ArrayList<Combatant> units;
		if (choice == 0) {
			units = r.commander;
		} else if (choice == 1) {
			units = r.elites;
		} else {
			units = r.footsoldiers;
		}
		bluepoints -= CrCalculator.calculateel(units);
		state.blueTeam.addAll(units);
		for (Combatant c : units) {
			placeunit(c);
			c.setmercenary(choice == 2);
		}
		Javelin.app.switchScreen(BattleScreen.active);
	}

	String group(ArrayList<Combatant> group) {
		int el = CrCalculator.calculateel(group);
		return Combatant.group(group) + " for " + el + " army points.";
	}

	void updateflagpoles() {
		blueflagpoles.clear();
		redflagpoles.clear();
		for (Combatant c : state.getcombatants()) {
			Flagpole f = c instanceof Flagpole ? (Flagpole) c : null;
			if (f != null) {
				if (f.blueteam) {
					blueflagpoles.add(f);
				} else {
					redflagpoles.add(f);
				}
			}
		}
	}

	void placeunit(Combatant c) {
		ArrayList<Combatant> team = state.getteam(c);
		Flagpole f = RPG
				.pick(team == state.blueTeam ? blueflagpoles : redflagpoles);
		Point p = new Point(f);
		while (checkblocked(p)) {
			p.x += RPG.randomize(3);
			p.y += RPG.randomize(3);
			if (!p.validate(0, 0, map.map.length, map.map[0].length)) {
				p = new Point(f);
			}
		}
		c.setlocation(p);
		c.initialap = lastupdate == Float.MIN_VALUE ? 0 : lastupdate;
		c.ap = c.initialap;
		c.rollinitiative();
	}

	public boolean checkblocked(Point p) {
		return !p.validate(0, 0, map.map.length, map.map[0].length)
				|| map.map[p.x][p.y].blocked
				|| state.getcombatant(p.x, p.y) != null;
	}

	@Override
	public void checkend() {
		updateflagpoles();
		ArrayList<Combatant> blueteam = (ArrayList<Combatant>) state.blueTeam
				.clone();
		ArrayList<Combatant> redteam = (ArrayList<Combatant>) state.redTeam
				.clone();
		blueteam.removeAll(blueflagpoles);
		redteam.removeAll(redflagpoles);
		if (blueteam.isEmpty() || redteam.isEmpty()) {
			throw new EndBattle();
		}
	}

	@Override
	public boolean onend() {
		state.blueTeam.removeAll(blueflagpoles);
		state.redTeam.removeAll(redflagpoles);
		if (state.blueTeam.isEmpty()) {
			Javelin.prompt("You've lost this match... Better luck next time!");
			return false;
		}
		Javelin.prompt("Congratulations, you've won!\n"
				+ "Your surviving units will be available for hire at the Battlefiled location.");
		Battlefield b = Battlefield.get();
		b.survivors.clear();
		CountingSet counter = new CountingSet();
		for (Combatant c : state.blueTeam) {
			if (!c.summoned) {
				counter.add(c.source.name);
			}
		}
		for (String name : counter.getelements()) {
			b.survivors.put(name, counter.getcount(name));
		}
		return DEBUG ? false : false;
	}
}
