package javelin.model.world.location.town.labor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javelin.Javelin;
import javelin.model.world.location.town.Academy;
import javelin.model.world.location.town.Academy.BuildAcademy;
import javelin.model.world.location.town.Shop.BuildShop;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.TransportHub.BuildTransportHub;
import javelin.model.world.location.town.labor.expansive.BuildHighway;
import javelin.model.world.location.town.labor.expansive.BuildOutpost;
import javelin.model.world.location.town.labor.expansive.BuildRoad;
import javelin.model.world.location.town.labor.expansive.Settler;
import javelin.model.world.location.town.labor.industrious.BuildMine;
import javelin.model.world.location.town.labor.industrious.Deforestate;
import javelin.model.world.location.town.labor.military.BuildDwelling;

/**
 * This class provides the deck-building mini-game logic for {@link Labor}
 * cards. It is not a model entity, making it easier to develop as it can be
 * more freely altered during the course of a game. It is then fully loaded and
 * processed when the game starts.
 *
 * @author alex
 */
public class Deck extends ArrayList<Labor> {
	public static final String RELIGIOUS = "religious";
	public static final String CRIMINAL = "criminal";
	static final HashMap<String, Deck> DECKS = new HashMap<String, Deck>();
	static final Deck DEFAULT = new Deck();

	static {
		populate(DEFAULT, null,
				new Labor[] { new Growth(), new BuildInn(), new Redraw(),
						new BuildDwelling(), new BuildShop(),
						new BuildTransportHub(), new BuildTransportHub(),
						new BuildAcademy(new Academy(null)) });
		populate(new Deck(), "expansive", new Labor[] { new Settler(),
				new BuildOutpost(), new BuildRoad(), new BuildHighway() });
		populate(new Deck(), "productive",
				new Labor[] { new BuildMine(), new Deforestate() });
		populate(new Deck(), "military", new Labor[] { new BuildDwelling(), });
		populate(new Deck(), "cultural", new Labor[] {});
		populate(new Deck(), CRIMINAL, new Labor[] {});
		populate(new Deck(), RELIGIOUS, new Labor[] {});
		populate(new Deck(), "ecological", new Labor[] {});
		for (String title : new ArrayList<String>(DECKS.keySet())) {
			/*
			 * TODO just a placeholder to get rid on unused sets during
			 * development:
			 */
			if (DECKS.get(title).isEmpty()) {
				DECKS.remove(title);
				continue;
			}
			DEFAULT.add(new Trait(title, DECKS.get(title)));
		}
	}

	public static ArrayList<Labor> generate(Town t) {
		Deck d = new Deck();
		d.addAll(DEFAULT);
		for (String trait : t.traits) {
			d.addAll(DECKS.get(trait));
		}
		Collections.shuffle(d);
		if (Javelin.DEBUG) {
			d.add(0, new Redraw());
		}
		return d;
	}

	static void populate(Deck d, String title, Labor[] cards) {
		for (Labor l : cards) {
			d.add(l);
		}
		if (title != null) {
			DECKS.put(title, d);
		}
	}

	public static boolean isbasic(Labor card) {
		for (Labor l : DEFAULT) {
			if (card.getClass().equals(l.getClass())) {
				return true;
			}
		}
		return false;
	}

	public static int getnprojects() {
		int projects = DEFAULT.size();
		for (Deck d : DECKS.values()) {
			projects += d.size();
		}
		return projects;
	}

	public static int getntraits() {
		return DECKS.size();
	}

	public static void printstats() {
		System.out.println(Deck.getnprojects() + " district projects, "
				+ Deck.getntraits() + " town traits");
		if (Javelin.DEBUG) {
			System.out.println(DECKS);
		}
	}
}
