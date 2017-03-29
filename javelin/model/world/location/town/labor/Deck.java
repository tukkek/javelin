package javelin.model.world.location.town.labor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javelin.Javelin;
import javelin.model.world.location.fortification.MagesGuild.BuildMagesGuild;
import javelin.model.world.location.fortification.MartialAcademy.BuildMartialAcademy;
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
import javelin.model.world.location.unique.AssassinsGuild;

/**
 * This class provides the deck-building mini-game logic for {@link Labor}
 * cards. It is not a model entity, making it easier to develop as it can be
 * more freely altered during the course of a game. It is then fully loaded and
 * processed when the game starts.
 *
 * @author alex
 */
public class Deck extends ArrayList<Labor> {
	private static final Labor[] BASE = new Labor[] { new Growth(),
			new BuildInn(), new Redraw(), new BuildDwelling(), new BuildShop(),
			new BuildTransportHub(), new BuildTransportHub(),
			new BuildAcademy(new Academy(null)) };
	private static final Labor[] CRIMINAL = new Labor[] {
			new BuildAcademy(new AssassinsGuild()), };
	private static final Labor[] CULTURAL = new Labor[] {
			new BuildMagesGuild(), };
	private static final Labor[] ECOLOGICAL = new Labor[] {};
	private static final Labor[] EXPANSIVE = new Labor[] { new Settler(),
			new BuildOutpost(), new BuildRoad(), new BuildHighway() };
	private static final Labor[] MILITARY = new Labor[] {
			new BuildMartialAcademy(), };
	private static final Labor[] PRODUCTIVE = new Labor[] { new BuildMine(),
			new Deforestate() };
	private static final Labor[] RELIGIOUS = new Labor[] {};

	public static final String NAMERELIGIOUS = "religious";
	public static final String NAMECRIMINAL = "criminal";

	static final HashMap<String, Deck> DECKS = new HashMap<String, Deck>();
	static final Deck DEFAULT = new Deck();

	static {
		populate(DEFAULT, null, BASE);
		populate(new Deck(), "expansive", EXPANSIVE);
		populate(new Deck(), "productive", PRODUCTIVE);
		populate(new Deck(), "military", MILITARY);
		populate(new Deck(), "cultural", CULTURAL);
		populate(new Deck(), NAMECRIMINAL, CRIMINAL);
		populate(new Deck(), NAMERELIGIOUS, RELIGIOUS);
		populate(new Deck(), "ecological", ECOLOGICAL);
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

	public static int getntraits() {
		return DECKS.size();
	}

	public static void printstats() {
		int nprojects = DEFAULT.size();
		int min = Integer.MAX_VALUE;
		for (Deck d : DECKS.values()) {
			int n = d.size();
			nprojects += n;
			if (n < min) {
				min = n;
			}
		}
		System.out.println(Deck.getntraits() + " town traits, " + nprojects
				+ " district projects (minimum deck size: " + min);
		// if (Javelin.DEBUG) {
		// System.out.println(DECKS);
		// }
	}
}
