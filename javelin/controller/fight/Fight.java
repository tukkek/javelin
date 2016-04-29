package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;

/**
 * A battle scenario.
 * 
 * TODO turn into class with fields?
 */
public interface Fight {
	/**
	 * @return an encounter level for which an appropriate challenge should be
	 *         generated.
	 * 
	 * @see ChallengeRatingCalculator
	 */
	int getel(JavelinApp javelinApp, int teamel);

	/**
	 * TODO as much as possible should move controller behavior from the screen
	 * to this interface.
	 */
	BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap);

	/**
	 * @return The list of monsters that are going to be featured in this fight.
	 *         If <code>null</code>, will then use
	 *         {@link #getel(JavelinApp, int)}.
	 */
	List<Combatant> getmonsters(int teamel);

	/**
	 * @return <code>true</code> if {@link Meld} should be generated.
	 */
	boolean meld();

	/**
	 * @return map this battle is to happen on or <code>null</code> for one to
	 *         be generated.
	 */
	Map getmap();

	/**
	 * @return If <code>true</code> will remove opponents at first sign of blood
	 *         instead of at negative hit points.
	 */
	boolean friendly();

	/**
	 * @return If <code>false</code> will not reward gold after victory.
	 */
	boolean rewardgold();

	/**
	 * @return <code>true</code> if there is a chance for the {@link Squad} to
	 *         hide and avoid this combat. This doesn't make sense for
	 *         {@link Siege}s for example since they are actually engaging the
	 *         enemy.
	 */
	boolean hide();

	/**
	 * @return <code>true</code> if this fight is susceptible to
	 *         {@link Skills#diplomacy}.
	 */
	boolean canbribe();

	/**
	 * Called in case of a succesful bribe.
	 * 
	 * @see #canbribe()
	 */
	void bribe();
}
