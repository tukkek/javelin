package javelin.model.world.location.town.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.Javelin;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import tyrant.mikera.engine.RPG;

/**
 * Governor for human-captured towns. Auto-manage can be toggled on and off.
 * 
 * @author alex
 */
public class HumanGovernor extends Governor {
	Comparator<Labor> SORTBYCOST = new Comparator<Labor>() {
		@Override
		public int compare(Labor o1, Labor o2) {
			return o1.cost - o2.cost;
		}
	};

	/** Constructor. */
	public HumanGovernor(Town t) {
		super(t);
	}

	@Override
	public void manage() {
		ArrayList<Labor> hand = new ArrayList<Labor>();
		for (Labor l : gethand()) {
			if (l.automatic) {
				hand.add(l);
			}
		}
		if (hand.isEmpty()) {
			if (Javelin.DEBUG) {
				throw new RuntimeException("Empty hand! #humangovernor");
			}
			return;
		}
		Labor selected = null;
		if (nexttrait(hand)) {
			selected = picktrait(hand);
		}
		if (selected == null) {
			selected = normalpick(hand);
		}
		if (selected == null) {
			/* no smart choice? then any choice! */
			selected = RPG.pick(hand);
		}
		if (selected == null && Javelin.DEBUG) {
			throw new RuntimeException("No trait to pick! #humangovernor");
		}
		selected.start();
	}

	/**
	 * @return <code>true</code> if should pick town's first trait or if every
	 *         option in our hand is basic (and we should thus expand the
	 *         possibilities).
	 */
	private boolean nexttrait(ArrayList<Labor> hand) {
		if (town.traits.isEmpty()) {
			return true;
		}
		for (Labor l : hand) {
			if (!Deck.isbasic(l)) {
				return false;
			}
		}
		return true;
	}

	public Labor picktrait(ArrayList<Labor> hand) {
		Collections.shuffle(hand);
		for (Labor l : hand) {
			if (l instanceof Trait) {
				return l;
			}
		}
		return null;
	}

	/**
	 * Pretty weird, somewhat lazy but very random normal pick algorithm.
	 */
	private Labor normalpick(ArrayList<Labor> hand) {
		hand = new ArrayList<Labor>(hand);
		for (Labor l : new ArrayList<Labor>(hand)) {
			if (l instanceof Trait) {
				hand.remove(l);
			}
		}
		if (hand.isEmpty()) {
			return null;
		}
		if (hand.size() == 1) {
			return hand.get(0);
		}
		float total = 0;
		for (Labor l : hand) {
			total += l.cost;
		}
		float[] chances = new float[hand.size()];
		for (int i = 0; i < hand.size(); i++) {
			/*
			 * inverted cost-chance array: 0 cost means 100% chance, total cost
			 * means 0% chance. Minimum of 10% to prevent potential infinite
			 * loop edge cases.
			 */
			chances[i] = Math.max(.1f, (total - hand.get(i).cost) / total);
		}
		Labor selected = null;
		while (selected == null) {
			selected = RPG.pick(hand);// pick random card
			if (RPG.random() > chances[hand.indexOf(selected)]) {
				selected = null; // chance roll failed
			}
		}
		return selected;
	}
}
