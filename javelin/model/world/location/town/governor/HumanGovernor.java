package javelin.model.world.location.town.governor;

import java.util.Comparator;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;

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
		if (!isfull() && town.labor >= 1) {
			if (draw()) {
				town.labor -= 1;
				return;
			}
		}
		if (hand.isEmpty()) {
			return;
		}
		hand.sort(new Comparator<Research>() {
			@Override
			public int compare(Research o1, Research o2) {
				return o1.cost - o2.cost;
			}
		});
		queue.add(hand.get(0));
	}
}
