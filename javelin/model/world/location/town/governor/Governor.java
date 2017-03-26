package javelin.model.world.location.town.governor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import javelin.Javelin;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import tyrant.mikera.engine.RPG;

/**
 * Holds the {@link Labor} options for each {@link Town} and possibly
 * auto-manages it.
 * 
 * TODO promote specializing in one trait at a time in subclasses
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
	private ArrayList<Labor> projects = new ArrayList<Labor>(0);
	private ArrayList<Labor> hand = new ArrayList<Labor>(STARTINGHAND);

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
		for (Labor l : Deck.generate(town)) {
			l = l.generate(town);
			if (!hand.contains(l) && !projects.contains(l) && l.validate(d)) {
				hand.add(l);
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes the current {@link #projects}.
	 * 
	 * @param labor
	 *            Labor to be distributed among the {@link #projects}.
	 * 
	 * @return <code>false</code> if there is no current project.
	 */
	public void work(float labor) {
		float step = labor / projects.size();
		for (Labor l : new ArrayList<Labor>(projects)) {
			l.work(step);
		}
		validate(town.getdistrict());
		if (projects.isEmpty() && !hand.isEmpty()) {
			manage();
			if (Javelin.DEBUG && projects.isEmpty()) {
				throw new RuntimeException("empty project list!");
			}
		}
	}

	/**
	 * Selects next task for {@link #projects}.
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
		return town.getrank() + (Javelin.DEBUG ? 3 : 2);
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

	public void validate(District d) {
		for (Labor l : new ArrayList<Labor>(hand)) {
			if (!l.validate(d)) {
				hand.remove(l);
			}
		}
		for (Labor l : new ArrayList<Labor>(projects)) {
			if (l.progress >= l.cost || !l.validate(d)) {
				projects.remove(l);
			}
		}
		redraw();
	}

	/**
	 * @return A hand of cards sorted by name, including any building upgrades
	 *         in the {@link District}.
	 * 
	 * @see Location#getupgrades()
	 */
	public ArrayList<Labor> gethand() {
		District d = town.getdistrict();
		validate(d);
		ArrayList<Labor> hand = new ArrayList<Labor>(this.hand);
		for (Location l : d.getlocations()) {
			for (Labor upgrade : l.getupgrades(d)) {
				if (hand.contains(upgrade)) {
					continue;
				}
				upgrade = upgrade.generate(town);
				if (upgrade.validate(d)) {
					hand.add(upgrade);
				}
			}
		}
		hand.sort(SORTYBYNAME);
		return hand;
	}

	public ArrayList<Labor> getprojects() {
		projects.sort(SORTYBYNAME);
		return projects;
	}

	public void removeproject(Labor labor) {
		projects.remove(labor);
	}

	public void removefromhand(Labor l) {
		hand.remove(l);
	}

	public void addproject(Labor l) {
		projects.add(l);
	}

	public int getprojectssize() {
		return projects.size();
	}

	/**
	 * Pretty weird, somewhat lazy but very random normal algorithm that allows
	 * a computer player to select {@link Labor} with a higher chance if they
	 * are cheaper.
	 * 
	 * @return <code>null</code> if there are no option, otherwise a labor card.
	 */
	static protected Labor pick(ArrayList<Labor> cards) {
		if (cards.isEmpty()) {
			return null;
		}
		if (cards.size() == 1) {
			return cards.get(0);
		}
		float total = 0;
		for (Labor l : cards) {
			total += l.cost;
		}
		float[] chances = new float[cards.size()];
		for (int i = 0; i < cards.size(); i++) {
			/*
			 * inverted cost-chance array: 0 cost means 100% chance, total cost
			 * means 0% chance. Minimum of 10% to prevent potential infinite
			 * loop edge cases.
			 */
			chances[i] = Math.max(.1f, (total - cards.get(i).cost) / total);
		}
		Labor selected = null;
		while (selected == null) {
			selected = RPG.pick(cards);// pick random card
			if (RPG.random() > chances[cards.indexOf(selected)]) {
				selected = null; // chance roll failed
			}
		}
		return selected;
	}
}
