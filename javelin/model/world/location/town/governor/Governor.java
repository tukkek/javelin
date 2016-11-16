package javelin.model.world.location.town.governor;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;

/**
 * Holds the {@link Research} options for each {@link Town} and possibly
 * auto-manages it.
 * 
 * @author alex
 */
public abstract class Governor implements Serializable {
	static final int STARTINGHAND = 2 + 1;

	/** <code>true</code> to draw and use cards automatically. */
	public boolean automanage = true;
	/** Next cards to be used. */
	public ArrayList<Research> queue = new ArrayList<Research>(0);
	public ArrayList<Research> hand = new ArrayList<Research>(STARTINGHAND);
	final Town town;

	/** Constructor. */
	public Governor(Town t) {
		town = t;
	}

	public int redraw() {
		int drawn = 0;
		while (!isfull() && draw()) {
			drawn += 1;
		}
		return drawn;
	}

	public boolean draw() {
		for (Research r : Research.get(town)) {
			if (!hand.contains(r) && r.validate(town)) {
				hand.add(r);
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes the current {@link #queue}.
	 * 
	 * @return <code>false</code> if there is no item in the queue.
	 */
	public void update() {
		if (queue.isEmpty()) {
			return;
		}
		Research next = queue.get(0);
		if (town.labor >= next.cost) {
			town.labor -= next.cost;
			queue.remove(0);
			next.play(town);
		}
	}

	/**
	 * Selects next task for {@link #queue}.
	 */
	public abstract void manage();

	/**
	 * The maximum number of cards is 2 + {@link Town#getrank()} (3 minimum, 7
	 * maximum).
	 * 
	 * @return <code>true</code> if currently has the maximum number of cards in
	 *         hand.
	 */
	public boolean isfull() {
		return hand.size() >= gethandsize();
	}

	public int gethandsize() {
		return town.getrank() + 2;
	}

	public String printqueue() {
		if (queue.isEmpty()) {
			return "(empty)";
		}
		String q = "";
		for (Research r : queue) {
			q += r + ", ";
		}
		return q.substring(0, q.length() - 2);
	}

	public String printhand() {
		if (hand.isEmpty()) {
			return "(empty)";
		}
		District d = town.getdistrict();
		for (Research r : new ArrayList<Research>(hand)) {
			if (!r.validate(town, d)) {
				hand.remove(r);
			}
		}
		String q = "";
		for (int i = 0; i < hand.size(); i++) {
			Research r = hand.get(i);
			q += (i + 1) + " - " + r + " (" + r.cost + " labor)\n";
		}
		return q.substring(0, q.length() - 1);
	}
}
