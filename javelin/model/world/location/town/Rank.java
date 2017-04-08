package javelin.model.world.location.town;

import java.io.Serializable;

public class Rank implements Serializable {
	public static final Rank CITY = new Rank("city", 20, 4);
	public static final Rank TOWN = new Rank("town", 15, 3);
	public static final Rank VILLAGE = new Rank("village", 10, 2);
	public static final Rank HAMLET = new Rank("hamlet", 5, 1);
	public static final Rank[] RANKS = new Rank[] { HAMLET, VILLAGE, TOWN,
			CITY };

	public String title;
	public int maxpopulation;
	public int rank;

	public Rank(String name, int size, int rank) {
		title = name;
		maxpopulation = size;
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
}