package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaGateway;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.BattleScreen;

/**
 * TODO would be cool if could generate heroes to fight against at some point
 *
 * TODO would be amazing to have 4 different map types generated (1 per
 * quadrant), also visually, when the engine allows it
 *
 * TODO clicking on a building, even if far away, should tell you your current
 * amount of XP
 *
 * TODO instead of current victory condition, add a Gate building that on raise
 * tension has a (current monster EL/EL goal) chance of activating (minimum
 * always 5%). If it is destroyed, lose the arena. If it is activated, save
 * progress and allow to pick up from there.
 *
 * TODO things to add to make it more strategic: towers, powerup (shrines).
 * Towers could be static spawners, summoning units with a percent chance arch
 * turn or static buildings with a single unlimited-use spell. Shrines would be
 * ArenaBuildings that work like Campaign-mode shrines.
 *
 * TODO having buildings to be attacked would add a lot to gemeplay, instead of
 * being only a defensive game. The goal could then be to killa all buildings
 * instead.
 *
 * TODO it would be cool if when an enemy structure is razed, the player could
 * choose which one of build in its stead (by paying the level 1 building price
 * and waiting for it to be reconstructed). The same could apply for monsters
 * razing player's structures.
 *
 * TODO to make space for new structures, altering fountains to heal everyone in
 * an area instead of just the user would be great.
 *
 * @see Arena
 *
 * @author alex
 */
public class ArenaFight extends Minigame {

	public static final float BOOST = 4; // 3,5 talvez?

	static final boolean SPAWN = true;
	/**
	 * Each level up is supposed to take 13.3_ moderate fights. Each Arena bout
	 * is supposed to take a tier up (5 levels). Take {@link #BOOST} into
	 * consideration to lower the resulting value.
	 */
	static final float NFIGHTS = .8f * 13 * 5 / BOOST;
	static final int TENSIONMIN = -5;
	static final int TENSIONMAX = 0;
	static final int ELMIN = -12;
	static final int ELMAX = 0;

	/** {@link Item} bag for {@link #gladiators}. */
	public HashMap<Integer, ArrayList<Item>> items = new HashMap<Integer, ArrayList<Item>>();
	public int gold = 0;
	public Combatants victors = new Combatants();

	/**
	 * Non-mercenary units, live and dead.
	 *
	 * @see #getgladiators()
	 */
	Combatants gladiators;
	int tension = RPG.r(TENSIONMIN, TENSIONMAX);
	float check = -Float.MAX_VALUE;
	/**
	 * Ensures that new waves are never becoming less dangerous (pressuring the
	 * player to upgrade and not just sit around).
	 */
	int baseline = Integer.MIN_VALUE;
	ArrayList<ArrayList<Combatant>> foes = new ArrayList<ArrayList<Combatant>>();
	int goal = 6;
	ArenaSetup arenasetup = new ArenaSetup(this);

	/** Constructor. */
	public ArenaFight(Combatants gladiatorsp) {
		gladiators = gladiatorsp;
		meld = true;
		weather = Weather.DRY;
		period = Javelin.PERIODNOON;
		setup = arenasetup;
		meld = false;
		canflee = false;
		endless = true;
	}

	@Override
	public ArrayList<Combatant> generate() {
		return new ArrayList<Combatant>();
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return new ArrayList<Combatant>(gladiators);
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
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
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
		if (state.redTeam.isEmpty()) {
			while (state.next.automatic) {
				state.next.ap += .5f;
				state.next();
			}
			acting = state.next;
		}
		super.startturn(acting);
		for (ArrayList<Combatant> group : new ArrayList<ArrayList<Combatant>>(
				foes)) {
			rewardxp(group);
		}
		awaken();
		if (!state.blueTeam.contains(state.next)) {
			return;
		}
		if (acting.ap < check) {
			if (state.redTeam.isEmpty()) {
				reward(state.dead);
			}
			return;
		}
		int elblue = ChallengeCalculator.calculateel(getgladiators());
		int elred = ChallengeCalculator.calculateel(state.redTeam);
		if (elred - elblue < tension) {
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
				if (!c.summoned) {
					Float cr = c.source.cr;
					gold += RewardCalculator.getgold(cr) * BOOST;
				}
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

	/**
	 * @return Live gladiators.
	 *
	 * @see BattleState#dead
	 */
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
			if (c.ap >= state.next.ap) {
				continue;
			}
			c.ap += 1;
			if (state.getcombatant(c.location[0], c.location[1]) != null) {
				continue;
			}
			if (RPG.r(1, 10) > Math.abs(c.hp)) {
				state.dead.remove(c);
				c.hp = 1;
				state.blueTeam.add(c);
				notify(c + " awakens!", c.getlocation());
			}
		}
	}

	void raisetension(int elblue) {
		Integer el = placefoes(elblue);
		if (el == null) {
			return;
		}
		float open = ChallengeCalculator.useresources(el - elblue) / NFIGHTS;
		if (RPG.random() <= open) {
			ArenaGateway g = new ArenaGateway();
			g.place();
			notify("A gateway to victory apears!", g.getlocation());
		}
		refillfountains();
	}

	void refillfountains() {
		ArrayList<ArenaFountain> fountains = getfountains();
		if (check == -Float.MAX_VALUE || fountains.isEmpty()) {
			return;
		}
		int refilled = 0;
		Point p = null;
		for (ArenaFountain f : fountains) {
			if (f.spent && !f.isdamaged() && RPG.random() < f.refillchance) {
				f.setspent(false);
				refilled += 1;
				p = f.getlocation();
			}
		}
		if (refilled > 0) {
			notify(refilled + " fountain(s) refilled!", p);
		}
	}

	Integer placefoes(int elblue) {
		ArrayList<Combatant> last = null;
		int min = Math.max(elblue + ELMIN, baseline);
		baseline = min;
		ArrayList<Combatant> redteam = new ArrayList<Combatant>();
		for (Combatant c : state.redTeam) {
			if (!c.summoned) {
				redteam.add(c);
			}
		}
		for (int el = min; el <= elblue + ELMAX; el += 1) {
			ArrayList<Combatant> group = generatefoes(el);
			if (group == null) {
				continue;
			}
			ArrayList<Combatant> newteam = new ArrayList<Combatant>(redteam);
			newteam.addAll(group);
			int tension = ChallengeCalculator.calculateel(newteam) - elblue;
			if (tension == this.tension) {
				enter(group, state.redTeam);
				return el;
			}
			if (tension > this.tension) {
				enter(last == null ? group : last, state.redTeam);
				return el;
			}
			last = group;
		}
		return null;
	}

	ArrayList<Combatant> generatefoes(int el) {
		try {
			return EncounterGenerator.generate(el, Arrays.asList(Terrain.ALL));
		} catch (GaveUp e) {
			return null;
		}
	}

	public void enter(ArrayList<Combatant> group, ArrayList<Combatant> team) {
		Point entrance = null;
		while (entrance == null || !arenasetup.validate(entrance)) {
			entrance = ArenaSetup.getmonsterentry();
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

	void enter(ArrayList<Combatant> entering, List<Combatant> team,
			Point entry) {
		if (team == state.redTeam) {
			if (!SPAWN) {
				return;
			}
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
			Point p = displace(last.getlocation());
			last = place.pop();
			last.setlocation(p);
		}
		if (team == state.redTeam) {
			String msg = "New enemies enter the arena:\n"
					+ Combatant.group(entering) + "!";
			notify(msg, last.getlocation());
		}
	}

	Point displace(Point reference) {
		Point p = null;
		while (p == null || !arenasetup.validate(p)) {
			p = new Point(reference);
			p.x += RPG.r(-1, +1) + RPG.randomize(2);
			p.y += RPG.r(-1, +1) + RPG.randomize(2);
		}
		return p;
	}

	void notify(String text, Point p) {
		BattleScreen.active.center(p.x, p.y);
		Javelin.redraw();
		Javelin.message(text, true);
		MessagePanel.active.clear();
	}

	@Override
	public void checkend() {
		List<Combatant> gladiators = getgladiators();
		if (!gladiators.isEmpty()) {
			return;
		}
		if (victors.isEmpty()) {
			state.blueTeam.clear();
			String loss = "You've lost this match... better luck next time!";
			Javelin.message(loss, true);
		} else {
			Javelin.message("You have beaten this level of the arena!", true);
			for (Combatant c : gladiators) {
				ArenaFountain.heal(c);
			}
			Arena.get().gladiators = victors;
		}
		throw new EndBattle();
	}

	public static ArenaFight get() {
		Fight f = Javelin.app.fight;
		return f != null && f instanceof ArenaFight ? (ArenaFight) f : null;
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
