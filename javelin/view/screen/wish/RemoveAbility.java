package javelin.view.screen.wish;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;

/**
 * Remvoes a single undesired ability from a {@link Combatant}.
 * 
 * @author alex
 */
public class RemoveAbility extends Wish {
	/**
	 * Constructor.
	 * 
	 * @param haxorScreen
	 */
	public RemoveAbility(String name, Character keyp, double price,
			boolean requirestargetp, WishScreen haxorScreen) {
		super(name, keyp, price, requirestargetp, haxorScreen);
	}

	@Override
	protected boolean wish(Combatant target) {
		screen.text = "";
		screen.refresh();
		ArrayList<String> types = new ArrayList<String>();
		screen.listremovals(target, types);
		if (types.isEmpty()) {
			screen.text = "Unit has no abilities that can be removed.";
			screen.refresh();
			screen.getInput();
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
			screen.removeaattack(target.source.melee);
		} else if (type == "Ranged attack") {
			screen.removeaattack(target.source.ranged);
		}
		return true;
	}
}