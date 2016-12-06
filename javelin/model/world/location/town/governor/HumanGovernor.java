package javelin.model.world.location.town.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Labor;
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
		int selected = 0;
		while (RPG.r(1, 2) == 1 && selected < hand.size() - 1) {
			selected += 1;
		}
		start(hand.get(selected));
		// if (!isfull() && town.labor >= 1) {
		// if (draw()) {
		// town.labor -= 1;
		// return;
		// }
		// }
		// if (hand.isEmpty()) {
		// return;
		// }
		// hand.sort(new Comparator<Labor>() {
		// @Override
		// public int compare(Labor o1, Labor o2) {
		// return o1.cost - o2.cost;
		// }
		// });
		// queue.add(hand.get(0));
	}
}
