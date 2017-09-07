package javelin.controller.action.maneuver;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Disciplines;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Maneuvers;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.town.SelectScreen;

public class ExecuteManeuver extends Action implements AiAction {
	public static final ExecuteManeuver INSTANCE = new ExecuteManeuver();

	private ExecuteManeuver() {
		super("Execute maneuvers", "m");
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState s, Combatant c) {
		final ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>();
		final Disciplines disciplines = c.disciplines;
		for (String discipline : disciplines.keySet()) {
			for (Maneuver m : disciplines.get(discipline)) {
				if (m.spent) {
					BattleState s2 = s.clone();
					Combatant c2 = s2.clone(c);
					c2.ready(c2.disciplines.find(m));
					ArrayList<ChanceNode> chance = new ArrayList<ChanceNode>();
					final ChanceNode node = new ChanceNode(s2, 1,
							c2 + " readies " + m + "!", Delay.BLOCK);
					chance.add(node);
					outcomes.add(chance);
				} else {
					outcomes.addAll(m.getoutcomes(s, c));
				}
			}
		}
		return outcomes;
	}

	@Override
	public boolean perform(Combatant c) {
		final Disciplines disciplines = c.disciplines;
		if (disciplines.isEmpty()) {
			Game.message("No known manuevers...", Delay.WAIT);
			BattleScreen.active.block();
			throw new RepeatTurn();
		}
		Maneuvers maneuvers = new Maneuvers();
		String prompt = "Choose a manuever to execute (or ready), or press any other key to exit...";
		for (String discipline : disciplines.keySet()) {
			prompt += "\n\n" + discipline + ":\n";
			for (Maneuver m : disciplines.get(discipline)) {
				final String spent = m.spent ? " (not ready)" : "";
				prompt += "  " + SelectScreen.KEYS[maneuvers.size()] + " - "
						+ m.name + spent + "\n";
				maneuvers.add(m);
			}
		}
		int choice = SelectScreen
				.convertkeytoindex(Javelin.promptscreen(prompt));
		Javelin.app.switchScreen(BattleScreen.active);
		if (choice == -1 && choice < maneuvers.size()) {
			throw new RepeatTurn();
		}
		Maneuver m = maneuvers.get(choice);
		if (m.spent) {
			c.ready(m);
			return true;
		}
		return m.perform(c);
	}
}
