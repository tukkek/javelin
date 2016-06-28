package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.map.Map;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
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
}
