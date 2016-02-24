package javelin.controller.tournament;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.PlanarFight;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

/**
 * TODO this stuff needs to go to {@link JavelinApp#context} as a controller
 * hierarchy.
 * 
 * @author alex
 */
public class ExhibitionScreen extends BattleScreen {
	public ExhibitionScreen(QuestApp q, BattleMap mapp, boolean addsidebar) {
		super(q, mapp, addsidebar);
	}

	@Override
	public void checkEndBattle() {
		super.checkEndBattle();
		if (BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

	@Override
	protected void endturn() {
		BattleState s = map.getState();
		int blue = s.blueTeam.size();
		int red = s.redTeam.size();
		cleanwounded(s.blueTeam, s);
		cleanwounded(s.redTeam, s);
		if (s.blueTeam.size() != blue || s.redTeam.size() != red) {
			map.setState(s);
			Game.redraw();
		}
	}

	public void cleanwounded(ArrayList<Combatant> team, BattleState s) {
		for (Combatant c : (List<Combatant>) team.clone()) {
			if (c.getNumericStatus() <= 2) {
				if (team == s.blueTeam) {
					BattleScreen.active.fleeing.add(c);
				}
				team.remove(c);
				if (s.next == c) {
					s.checkwhoisnext();
				}
				s.addmeld(c.location[0], c.location[1]);
				Game.message(
						c + " is removed from the arena!\nPress ENTER to continue...",
						null, Delay.NONE);
				while (Game.getInput().getKeyChar() != '\n') {
					// wait for enter
				}
				Game.messagepanel.clear();
			}
		}
	}

	@Override
	protected void withdraw(Combatant combatant) {
		PlanarFight.dontflee(this);
	}
}