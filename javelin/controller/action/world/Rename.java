package javelin.controller.action.world;

import javelin.controller.exception.RepeatTurnException;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.RecruitScreen;

/**
 * Rename combatant.
 * 
 * @author alex
 */
public class Rename extends WorldAction {
	/** Constructor. */
	public Rename() {
		super("Rename squad members", new int[] {}, new String[] { "r" });
	}

	@Override
	public void perform(WorldScreen screen) {
		int i = CastSpells.choose("Rename which unit?", Squad.active.members,
				false, false);
		if (i == -1) {
			throw new RepeatTurnException();
		}
		Combatant m = Squad.active.members.get(i);
		m.source.customName = RecruitScreen.namingscreen(m.source.toString());
	}
}
