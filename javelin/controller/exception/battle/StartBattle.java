package javelin.controller.exception.battle;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.BattleSetup;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

/**
 * A {@link Fight} has started.
 * 
 * @see BattleSetup
 * @author alex
 */
public class StartBattle extends BattleEvent {
	/** Controller for the battle. */
	public final Fight fight;

	/** Constructor. */
	public StartBattle(final Fight d) {
		fight = d;
	}

	/** Prepares and switches to a {@link BattleScreen}. */
	public void battle() {
		Fight.state = new BattleState(fight);
		Fight.state.blueTeam = fight.getblueteam();
		ArrayList<Combatant> foes = fight.generate(Terrain.current().getel(
				ChallengeRatingCalculator.calculateel(Fight.state.blueTeam)));
		if (fight.avoid(foes)) {
			return;
		}
		Javelin.app.preparebattle(foes);
		BattleSetup.place();
		Fight.state.checkwhoisnext();
		fight.ready();
		BattlePanel.current = Fight.state.next;
		BattleScreen battleScreen = new BattleScreen(true);
		try {
			battleScreen.mainLoop();
		} catch (final EndBattle end) {
			EndBattle.end(battleScreen, Javelin.app.originalteam);
			Javelin.app.fight = null;
		}
	}
}
