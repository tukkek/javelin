package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.generator.encounter.GeneratedFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.controller.terrain.map.Map;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
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
	/**
	 * <code>true</code> if the game should reward experience points after this
	 * fight.
	 */
	public boolean ewardxp = true;

	/** If not <code>null</code> will use this terrain when generating a map. */
	public Terrain terrain = null;

	/**
	 * If not <code>null</code> will override any other flooding level.
	 * 
	 * @see Weather#current
	 * @see Map#maxflooding
	 */
	public Integer floodlevel = null;

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

	/**
	 * @return an encounter level for which an appropriate challenge should be
	 *         generated.
	 * 
	 * @see ChallengeRatingCalculator
	 */
	public abstract int getel(int teamel);

	final public BattleScreen getscreen(JavelinApp javelinApp,
			BattleMap battlemap) {
		return new BattleScreen(javelinApp, battlemap, true);
	}

	/**
	 * @return The list of monsters that are going to be featured in this fight.
	 *         If <code>null</code>, will then use
	 *         {@link #getel(JavelinApp, int)}.
	 */
	public abstract List<Combatant> getmonsters(int teamel);

	/**
	 * Called in case of a successful bribe.
	 */
	public void bribe() {
		if (Javelin.DEBUG && !bribe) {
			throw new RuntimeException(
					"Cannot bribe this fight! " + getClass());
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
		final int bonus = Math.round(Math.max(Squad.active.eat() / 2,
				RewardCalculator.receivegold(BattleScreen.originalredteam)));
		Squad.active.members = BattleMap.blueTeam;
		String rewards = "";
		if (Javelin.app.fight.rewardxp) {
			rewards += RewardCalculator.rewardxp(BattleMap.blueTeam,
					BattleScreen.originalblueteam, BattleScreen.originalredteam,
					1);
		}
		if (Javelin.app.fight.rewardgold) {
			Squad.active.gold += bonus;
			rewards += " Party receives $" + SelectScreen.formatcost(bonus)
					+ "!\n";
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
	public void onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		screen.map.setState(s);
		for (Combatant c : screen.fleeing) {
			BattleMap.blueTeam.add(c);
			BattleMap.combatants.add(c);
		}
		EndBattle.showcombatresult(screen, originalTeam, "Congratulations! ");
	}

	public void checkEndBattle(BattleScreen screen) {
		if (BattleMap.redTeam.isEmpty() || BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
		if (!screen.checkforenemies()) {
			throw new EndBattle();
		}
	}

	public void withdraw(Combatant combatant, BattleScreen screen) {
		if (screen.map.getState().isengaged(combatant)) {
			if (Javelin.DEBUG) {
				Game.message("Press w to cancel battle (debug feature)", null,
						Delay.NONE);
				if (Game.getInput().getKeyChar() == 'w') {
					for (Combatant c : new ArrayList<Combatant>(
							BattleMap.blueTeam)) {
						c.escape(screen);
					}
					throw new EndBattle();
				}
			}
			Game.message("Disengage first!", null, Delay.BLOCK);
			InfoScreen.feedback();
			throw new RepeatTurn();
		}
		Game.message(
				"Are you sure you want to escape? Press ENTER to confirm...\n",
				null, Delay.NONE);
		if (Game.getInput().getKeyChar() != '\n') {
			throw new RepeatTurn();
		}
		combatant.escape(screen);
		Game.message("Escapes!", null, Delay.WAIT);
		if (BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

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

	static public void dontflee(BattleScreen s) {
		Game.message("Cannot flee!", null, Delay.BLOCK);
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
	public List<Combatant> generate(final int teamel, Terrain terrain) {
		List<Combatant> foes = getmonsters(teamel);
		if (foes != null) {
			enhance(foes);
			return foes;
		}

		int delta = 0;
		GeneratedFight generated = null;
		while (generated == null) {
			generated = chooseopponents(teamel - delta, terrain);
			if (generated != null) {
				break;
			}
			if (delta != 0) {
				generated = chooseopponents(teamel + delta, terrain);
			}
			delta += 1;
		}
		foes = generated.opponents;
		enhance(foes);
		return foes;
	}

	GeneratedFight chooseopponents(final int el, Terrain terrain) {
		try {
			return new GeneratedFight(EncounterGenerator.generate(el, terrain));
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
	 * @param t
	 *            Terrain hint. Usually {@link Terrain#current()}.
	 * @return A list of {@link Terrain}s which players in this fight can
	 *         inhabit.
	 */
	public ArrayList<Terrain> getterrains(Terrain t) {
		return getdefaultterrains(t);
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
	static public ArrayList<Terrain> getdefaultterrains(Terrain t) {
		ArrayList<Terrain> terrains = new ArrayList<Terrain>();
		if (Dungeon.active != null) {
			terrains.add(Terrain.UNDERGROUND);
			return terrains;
		}
		terrains.add(t);
		if (Weather.current == Weather.STORM) {
			terrains.add(Terrain.WATER);
		}
		return terrains;
	}

	/**
	 * @return <code>true</code> if battle has been won.
	 */
	public Boolean win(BattleScreen screen) {
		return BattleMap.redTeam.isEmpty() || !screen.checkforenemies();
	}
}
