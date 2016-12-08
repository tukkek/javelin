package javelin.model.world.location.town.governor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;

/**
 * Holds the {@link Labor} options for each {@link Town} and possibly
 * auto-manages it.
 * 
 * @author alex
 */
public abstract class Governor implements Serializable {
	static final int STARTINGHAND = 2 + 1;
	static final Comparator<? super Labor> SORTYBYNAME = new Comparator<Labor>() {
		@Override
		public int compare(Labor o1, Labor o2) {
			return o1.name.compareTo(o2.name);
		}
	};

	// /** <code>true</code> to draw and use cards automatically. */
	// public boolean automanage = true;
	/** Current labor. */
	protected ArrayList<Labor> queue = new ArrayList<Labor>(0);
	protected ArrayList<Labor> hand = new ArrayList<Labor>(STARTINGHAND);

	final Town town;

	/** Constructor. */
	public Governor(Town t) {
		town = t;
		// validate();
	}

	public int redraw() {
		int drawn = 0;
		while (!isfull() && draw()) {
			drawn += 1;
		}
		return drawn;
	}

	public boolean draw() {
		District d = town.getdistrict();
		for (Labor r : Deck.generate(town)) {
			r = r.generate(town);
			if (!hand.contains(r) && !queue.contains(r) && r.validate(d)) {
				hand.add(r);
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes the current {@link #queue}.
	 * 
	 * @param labor
	 *            Labor to be distributed among the {@link #queue}.
	 * 
	 * @return <code>false</code> if there is no item in the queue.
	 */
	public void work(float labor) {
		float step = labor / queue.size();
		for (Labor l : new ArrayList<Labor>(queue)) {
			l.work(step);
		}
		validate();
		if (queue.isEmpty() && !hand.isEmpty()) {
			manage();
			if (queue.isEmpty()) {
				throw new RuntimeException("empty queue!");
			}
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

	// public String printqueue() {
	// if (queue.isEmpty()) {
	// return "(empty)";
	// }
	// String q = "";
	// for (Labor r : queue) {
	// q += r + ", ";
	// }
	// return q.substring(0, q.length() - 2);
	// }
	//
	// public String printhand() {
	// if (hand.isEmpty()) {
	// return "(empty)";
	// }
	// District d = town.getdistrict();
	// validate();
	// String q = "";
	// for (int i = 0; i < hand.size(); i++) {
	// Labor r = hand.get(i);
	// q += (i + 1) + " - " + r + " (" + r.cost + " labor)\n";
	// }
	// return q.substring(0, q.length() - 1);
	// }

	public void validate() {
		District d = town.getdistrict();
		for (Labor l : new ArrayList<Labor>(hand)) {
			if (!l.validate(d)) {
				hand.remove(l);
			}
		}
		for (Labor l : new ArrayList<Labor>(queue)) {
			if (l.progress >= l.cost || !l.validate(d)) {
				queue.remove(l);
			}
		}
		redraw();
	}

	public ArrayList<Labor> gethand() {
		validate();
		hand.sort(SORTYBYNAME);
		return hand;
	}

	public ArrayList<Labor> getqueue() {
		queue.sort(SORTYBYNAME);
		return queue;
	}

	public void removefromqueue(Labor labor) {
		queue.remove(labor);
	}

	public void removefromhand(Labor l) {
		hand.remove(l);
	}

	public void addtoqueue(Labor l) {
		queue.add(l);
	}
}
