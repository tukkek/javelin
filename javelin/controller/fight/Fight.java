package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.BattleSetup;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.controller.terrain.map.Map;
import javelin.model.condition.Dominated;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.tyrant.QuestApp;

/**
 * A battle scenario.
 */
public abstract class Fight {
	/** Global fight state. */
	public static BattleState state = null;
	/** See {@link #win(BattleScreen)}. */
	public static Boolean victory;

	Image texture = QuestApp.DEFAULTTEXTURE;

	/**
	 * @return <code>true</code> if {@link Meld} should be generated.
	 */
	public boolean meld = false;

	/**
	 * Map this battle is to happen on or <code>null</code> for one to be
	 * generated according to current tile's terrain.
	 */
	public Map map = null;

	/**
	 * If <code>true</code> will remove opponents at first sign of blood instead
	 * of at negative hit points.
	 */
	public boolean friendly = false;

	/**
	 * If <code>false</code> will not reward experience points after victory.
	 */
	public boolean rewardxp = true;

	/**
	 * If <code>false</code> will not reward gold after victory.
	 */
	public boolean rewardgold = true;

	/**
	 * <code>true</code> if there is a chance for the {@link Squad} to hide and
	 * avoid this combat. This doesn't make sense for {@link Siege}s for example
	 * since they are actually engaging the enemy.
	 */
	public boolean hide = true;

	/**
	 * <code>true</code> if this fight is susceptible to
	 * {@link Skills#diplomacy}.
	 */
	public boolean bribe = true;

	/** If not <code>null</code> will use this terrain when generating a map. */
	public Terrain terrain = null;

	/**
	 * If not <code>null</code> will override any other flooding level.
	 * 
	 * @see Weather#current
	 * @see Map#maxflooding
	 */
	public Integer weather = null;

	/**
	 * Since {@link Squad#hourselapsed} is always ticking and needs to be
	 * updated even when fights do happen this by default holds the period at
	 * the moment of instantiation, so we can be more faithful to what appears
	 * on screen instead of the period after the {@link WorldMove} or similar
	 * has been completed.
	 * 
	 * @see Javelin#getDayPeriod()
	 */
	public String period = Javelin.getDayPeriod();

	/** Status to remove {@link Combatant} from a {@link #friendly} battle. */
	public int friendlylevel = Combatant.STATUSWOUNDED;

	/** Delegates some setup details.TODO */
	public BattleSetup setup = new BattleSetup();
	public boolean denydarkvision = false;

	/**
	 * @return an encounter level for which an appropriate challenge should be
	 *         generated.
	 * 
	 * @see ChallengeRatingCalculator
	 */
	public int getel(int teamel) {
		return Terrain.current().getel(teamel);
	}

	/**
	 * @return The list of monsters that are going to be featured in this fight.
	 *         If <code>null</code>, will then use
	 *         {@link #getel(JavelinApp, int)}.
	 */
	public abstract ArrayList<Combatant> getmonsters(int teamel);

	/**
	 * Called in case of a successful bribe.
	 */
	public void bribe() {
		if (Javelin.DEBUG && !bribe) {
			throw new RuntimeException("Cannot bribe this fight! " + getClass());
		}
		// just avoid the fight
	}

	/**
	 * Only called on victory.
	 * 
	 * @return Reward description.
	 */
	public String dealreward() {
		/* should at least serve as food for 1 day */
		final int bonus = Math
				.round(Math.max(Squad.active.eat() / 2, RewardCalculator.receivegold(BattleScreen.originalredteam)));
		// Squad.active.members = Fight.state.blueTeam;
		String rewards = "";
		if (Javelin.app.fight.rewardxp) {
			rewards += RewardCalculator.rewardxp(Fight.state.blueTeam, BattleScreen.originalblueteam,
					BattleScreen.originalredteam, 1);
		}
		if (Javelin.app.fight.rewardgold) {
			Squad.active.gold += bonus;
			rewards += " Party receives $" + SelectScreen.formatcost(bonus) + "!\n";
		}
		return rewards;
	}

	/**
	 * Called when a battle ends.
	 * 
	 * @param screen
	 *            Currently open screen.
	 * @param originalTeam
	 *            Team state before the battle started.
	 * @param s
	 *            Final battle state.
	 * @param combatresult
	 *            Description of rewards and other remarks such as
	 *            vehicle/allies left behind...
	 */
	public boolean onend() {
		for (Combatant c : BattleScreen.active.fleeing) {
			Fight.state.blueTeam.add(c);
		}
		EndBattle.showcombatresult("Congratulations! ");
		return true;
	}

	/**
	 * @param screen
	 *            Active screen.
	 * @throws EndBattle
	 *             If this battle is over.
	 */
	public void checkendbattle() {
		if (win() || Fight.state.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

	/**
	 * @return <code>true</code> if there are any active enemies here.
	 */
	public boolean checkforenemies() {
		for (Combatant c : Fight.state.redTeam) {
			if (c.hascondition(Dominated.class) == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO probablby better to just have flee=true/false in Fight.
	 * 
	 * @param combatant
	 *            Fleeing unit.
	 * @param screen
	 *            Active screen.
	 */
	public void withdraw(Combatant combatant, BattleScreen screen) {
		if (Javelin.DEBUG) {
			Game.message("Press w to cancel battle (debug feature)", Delay.NONE);
			if (Game.getInput().getKeyChar() == 'w') {
				for (Combatant c : new ArrayList<Combatant>(Fight.state.blueTeam)) {
					c.escape(screen);
				}
				throw new EndBattle();
			}
			Game.messagepanel.clear();
		}
		if (Fight.state.isengaged(combatant)) {
			Game.message("Disengage first!", Delay.BLOCK);
			InfoScreen.feedback();
			throw new RepeatTurn();
		}
		Game.message("Are you sure you want to escape? Press ENTER to confirm...\n", Delay.NONE);
		if (Game.getInput().getKeyChar() != '\n') {
			throw new RepeatTurn();
		}
		combatant.escape(screen);
		if (Fight.state.blueTeam.isEmpty()) {
			throw new EndBattle();
		} else {
			throw new RepeatTurn();
		}
	}

	/**
	 * @param foes
	 *            List of enemies.
	 * @return <code>true</code> if this battle has been avoided.
	 */
	public boolean avoid(List<Combatant> foes) {
		if (hide && Squad.active.hide(foes)) {
			return true;
		}
		if (bribe && Squad.active.bribe(foes)) {
			bribe();
			return true;
		}
		return false;
	}

	/** Helper method for {@link #withdraw(Combatant, BattleScreen)}. */
	static public void dontflee(BattleScreen s) {
		Game.message("Cannot flee!", Delay.BLOCK);
		s.checkblock();
		throw new RepeatTurn();
	}

	/**
	 * @param foes
	 *            Gives an opportunity to alter the generated enemies.
	 */
	public void enhance(List<Combatant> foes) {
		// nothing by default
	}

	/**
	 * @param el
	 *            Target encounter level for the fight. Taken as a guideline
	 *            because given {@link Terrain} and such a fight cannot be
	 *            generated for this exact level.
	 * @param terrain
	 *            Terrain this fight takes place on.
	 * @return The resulting opponents.
	 */
	public ArrayList<Combatant> generate(final int el) {
		ArrayList<Terrain> terrains = getterrains();
		ArrayList<Combatant> foes = getmonsters(el);
		if (foes != null) {
			enhance(foes);
			return foes;
		}
		foes = generate(el, terrains);
		enhance(foes);
		return foes;
	}

	/**
	 * @param el
	 *            Encounter level.
	 * @param terrains
	 *            Possible {@link Monster} terrains.
	 * @return A group of enemies that closely match the given EL, as far as
	 *         possible.
	 */
	static public ArrayList<Combatant> generate(final int el, ArrayList<Terrain> terrains) {
		int delta = 0;
		ArrayList<Combatant> generated = null;
		while (generated == null) {
			generated = chooseopponents(el - delta, terrains);
			if (generated != null) {
				break;
			}
			if (delta != 0) {
				generated = chooseopponents(el + delta, terrains);
			}
			delta += 1;
		}
		return generated;
	}

	static ArrayList<Combatant> chooseopponents(final int el, ArrayList<Terrain> terrains) {
		try {
			return EncounterGenerator.generate(el, terrains);
		} catch (final GaveUpException e) {
			return null;
		}
	}

	/**
	 * @return <code>false</code> if any of these {@link Combatant}s are not
	 *         supposed to be in this fight.
	 * @see TempleEncounter
	 */
	public boolean validate(ArrayList<Combatant> encounter) {
		return true;
	}

	/**
	 * @param town
	 *            Terrain hint. Usually {@link Terrain#current()}.
	 * @return A list of {@link Terrain}s which players in this fight can
	 *         inhabit.
	 */
	public ArrayList<Terrain> getterrains() {
		return getdefaultterrains(Terrain.current(), flood());
	}

	/**
	 * Default implementation of {@link #getterrains(Terrain)}.
	 * 
	 * @param t
	 *            Will return this (alongside {@link Water} if enough flood
	 *            level)...
	 * @return or {@link Underground} if there is inside a {@link Dungeon}.
	 * 
	 * @see Weather#flood(BattleMap, int)
	 * @see Map#maxflooding
	 * @see Dungeon#active
	 */
	public static ArrayList<Terrain> getdefaultterrains(Terrain t, int floodlevel) {
		ArrayList<Terrain> terrains = new ArrayList<Terrain>();
		if (Dungeon.active != null) {
			terrains.add(Terrain.UNDERGROUND);
			return terrains;
		}
		terrains.add(t);
		if (floodlevel == Weather.STORM) {
			terrains.add(Terrain.WATER);
		}
		return terrains;
	}

	/**
	 * @return <code>true</code> if battle has been won.
	 */
	public Boolean win() {
		return Fight.state.redTeam.isEmpty() || !checkforenemies();
	}

	/**
	 * @return The {@link #weather} level for this fight, taking into account
	 *         all factors.
	 */
	public int flood() {
		if (weather != null) {
			return weather;
		}
		if (map == null) {
			return Weather.current;
		}
		return Math.min(Weather.current, map.maxflooding);
	}

	/**
	 * @return Team that the player will fight with.
	 */
	public ArrayList<Combatant> getblueteam() {
		return Squad.active.members;
	}

	/**
	 * @return Inventory for the given unit..
	 */
	public ArrayList<Item> getbag(Combatant combatant) {
		return Squad.active.equipment.get(combatant.id);
	}

	/**
	 * Last opportunity for changing this fight before battle begins. At this
	 * point the entire stack should be setup.
	 * 
	 * @see BattleSetup
	 */
	public void ready() {
		// see javadoc
	}

	/**
	 * Setups {@link #state} and {@link BattleState#blueTeam}.
	 * 
	 * @return Opponent units.
	 */
	public ArrayList<Combatant> init() {
		Fight.state = new BattleState(this);
		Fight.state.blueTeam = getblueteam();
		return generate(getel(ChallengeRatingCalculator.calculateel(Fight.state.blueTeam)));
	}

	/**
	 * Called after a unit completes an {@link Action}.
	 */
	public void endturn() {
		if (friendly) {
			BattleState s = Fight.state;
			int ncombatants = s.blueTeam.size() + s.redTeam.size();
			cleanwounded(s.blueTeam, s);
			cleanwounded(s.redTeam, s);
			if (s.blueTeam.size() + s.redTeam.size() < ncombatants) {
				Fight.state = s;
				Game.redraw();
			}
		}
	}

	void cleanwounded(ArrayList<Combatant> team, BattleState s) {
		for (Combatant c : (List<Combatant>) team.clone()) {
			if (c.getnumericstatus() <= friendlylevel) {
				if (team == s.blueTeam) {
					BattleScreen.active.fleeing.add(c);
				}
				team.remove(c);
				if (s.next == c) {
					s.next();
				}
				Javelin.app.fight.addmeld(c.location[0], c.location[1], c.source, s);
				Game.message(c + " is removed from the battlefield!\nPress ENTER to continue...", Delay.NONE);
				while (Game.getInput().getKeyChar() != '\n') {
					// wait for enter
				}
				Game.messagepanel.clear();
			}
		}
	}

	/**
	 * Called when an unit reaches {@link Meld}. Note that only human units use
	 * this, computer units use {@link Combatant#meld()} directly.
	 * 
	 * @param hero
	 *            Meld collector.
	 * @param meld2
	 */
	public void meld(Combatant hero, Meld m) {
		Game.message(hero + " powers up!", Delay.BLOCK);
		Javelin.getCombatant(hero.id).meld();
		Fight.state.meld.remove(m);
	}

	/**
	 * @param x
	 *            Meld location.
	 * @param y
	 *            Meld location.
	 * @param dead
	 *            Unit that died, triggering {@link Meld} creation.
	 * @param s
	 *            Current battle state.
	 * @return Created meld.
	 */
	public Meld addmeld(int x, int y, Monster dead, BattleState s) {
		Meld m = new Meld(x, y, s.next.ap + 1, dead);
		s.meld.add(m);
		return m;
	}

	/**
	 * Called after painting the {@link BattleScreen} for the first time.
	 */
	public void draw() {
		// nothing by default
	}
}
