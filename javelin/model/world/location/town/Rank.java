package javelin.model.world.location.town;

import java.io.Serializable;

public class Rank implements Serializable {
	public static final Rank HAMLET = new Rank("hamlet", 1, 5, 1);
	public static final Rank VILLAGE = new Rank("village", 6, 10, 2);
	public static final Rank TOWN = new Rank("town", 11, 15, 3);
	public static final Rank CITY = new Rank("city", 16, 20, 4);
	public static final Rank[] RANKS = new Rank[] { HAMLET, VILLAGE, TOWN,
			CITY };

	public String title;
	public int maxpopulation;
	private int minpopulation;
	/** Numerical rank, from 1 ({@link #HAMLET}) to 4 ({@link #CITY}). */
	public int rank;

	public Rank(String name, int minsize, int maxsize, int rank) {
		title = name;
		minpopulation = minsize;
		maxpopulation = maxsize;
		this.rank = rank;
	}

	@Override
	public String toString() {
		return title;
	}

	public static Rank get(int population) {
		for (Rank r : Rank.RANKS) {
			if (population <= r.maxpopulation) {
				return r;
			}
		}
		return Rank.CITY;
	}

	public int getradius() {
		return rank <= 2 ? 2 : 3;
	}
}