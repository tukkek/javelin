package javelin.model.world.place.town;

import java.util.ArrayList;

import javelin.model.world.place.town.research.Research;

/**
 * Queue of {@link Research} to be finished as soon as there is enough labor in
 * town.
 * 
 * @see Research#finish(Town,
 *      javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen)
 * @see Town#labor
 * @author alex
 */
public class ResearchQueue extends ArrayList<Research> {
	@Override
	public String toString() {
		if (isEmpty()) {
			return "empty";
		}
		String s = "";
		for (Research b : this) {
			if (s.isEmpty()) {
				s += b;
			} else {
				s += ", " + b;
			}
		}
		return s;
	}
}
