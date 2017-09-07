package javelin.view.screen.haxor;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.attack.Combatant;

/**
 * Remvoes a single undesired ability from a {@link Combatant}.
 * 
 * @author alex
 */
public class RemoveAbility extends Hax {
	/** Constructor. */
	public RemoveAbility(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		s.text = "";
		s.refresh();
		ArrayList<String> types = new ArrayList<String>();
		s.listremovals(target, types);
		if (types.isEmpty()) {
			s.text = "Unit has no abilities that can be removed.";
			s.refresh();
			s.getInput();
			return false;// abort
		}
		int i = Javelin.choose("Which type of ability? Press q to quit.", types,
				true, false);
		if (i < 0 || i >= types.size()) {
			return false;// abort
		}
		String type = types.get(i);
		if (type == "Breath") {
			target.source.breaths.remove(Javelin.choose("Select a breath:",
					target.source.breaths, true, true));
		} else if (type == "Spell") {
			target.spells.remove(Javelin.choose("Select a spell:",
					target.spells, true, true));
		} else if (type == "Mêléé attack") {
			s.removeaattack(target.source.melee);
		} else if (type == "Ranged attack") {
			s.removeaattack(target.source.ranged);
		}
		return true;
	}
}