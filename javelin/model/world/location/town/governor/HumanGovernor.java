package javelin.model.world.location.town.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import tyrant.mikera.engine.RPG;

/**
 * Governor for human-captured towns. Auto-manage can be toggled on and off.
 * 
 * @author alex
 */
public class HumanGovernor extends Governor {

	/** Constructor. */
	public HumanGovernor(Town t) {
		super(t);
	}

	@Override
	public void manage() {
		ArrayList<Labor> hand = new ArrayList<Labor>(this.hand);
		Collections.shuffle(hand);
		hand.sort(new Comparator<Labor>() {
			@Override
			public int compare(Labor o1, Labor o2) {
				return o1.cost - o2.cost;
			}
		});
		Labor selected = null;
		for (Labor l : hand) {
			if (l.automatic) {
				selected = l;
				if (RPG.r(1, 2) == 1) {
					break;
				}
			}
		}
		if (selected != null) {
			start(selected);
		}
	}
}
