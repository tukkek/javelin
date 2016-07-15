package javelin.controller.exception.battle;

import java.util.List;

import javelin.Javelin;
import javelin.controller.BattleSetup;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.generator.encounter.GeneratedFight;
import javelin.controller.terrain.Terrain;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
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
		BattleMap.blueTeam = Squad.active.members;
		Terrain t = Terrain.current();
		List<Combatant> foes = fight.generate(t.getel(
				ChallengeRatingCalculator.calculateel(BattleMap.blueTeam)), t);
		if (fight.avoid(foes)) {
			return;
		}
		Javelin.app.preparebattle(new GeneratedFight(foes).opponents);
		Javelin.app.battlemap = BattleSetup.place();
		final BattleScreen battleScreen =
				fight.getscreen(Javelin.app, Javelin.app.battlemap);
		try {
			battleScreen.mainLoop();
		} catch (final EndBattle end) {
			EndBattle.end(battleScreen, Javelin.app.originalteam);
			Javelin.app.fight = null;
		}
	}
}
