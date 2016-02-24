package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * A battle scenario.
 * 
 * TODO turn into class with fields.
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

	boolean meld();

	/**
	 * @return map this battle is to happen on or <code>null</code> for one to
	 *         be generated.
	 */
	Map getmap();

	boolean friendly();
}
