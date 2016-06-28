package javelin.controller.exception.battle;

import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.BattleSetup;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.encounter.GeneratedFight;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.LairFight;
import javelin.controller.terrain.Terrain;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.view.screen.BattleScreen;

/**
 * A {@link Fight} has started.
 * 
 * @see BattleSetup
 * @author alex
 */
public class StartBattle extends BattleEvent {

	public final Fight fight;

	public StartBattle(final Fight d) {
		fight = d;
	}

	public void battle() {
		Javelin.app.fight = fight;
		BattleMap.blueTeam = Squad.active.members;
		int teamel = ChallengeRatingCalculator.calculateel(BattleMap.blueTeam);
		List<Combatant> foes = fight.getmonsters(teamel);
		if (foes == null) {
			Terrain currentterrain = Terrain.current();
			foes = JavelinApp.generatefight(fight.getel(teamel),
					currentterrain).opponents;
			/* TODO enhance Fight hierarchy with these: */
			while (foes.size() == 1 && fight instanceof LairFight) {
				foes = JavelinApp.generatefight(fight.getel(teamel),
						currentterrain).opponents;
				if (Preferences.DEBUGFOE != null) {
					break;
				}
			}
			if (fight instanceof IncursionFight) {
				((IncursionFight) fight).incursion.squad =
						Incursion.getsafeincursion(foes);
			}
		}
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
		}
	}
}
