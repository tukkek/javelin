package javelin.model.world.location.town.labor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javelin.Javelin;
import javelin.model.world.location.Outpost.BuildOutpost;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.base.Dwelling.BuildDwelling;
import javelin.model.world.location.town.labor.base.Growth;
import javelin.model.world.location.town.labor.base.Lodge.BuildLodge;
import javelin.model.world.location.town.labor.base.Redraw;
import javelin.model.world.location.town.labor.criminal.Sewers.BuildSewers;
import javelin.model.world.location.town.labor.criminal.Slums.BuildSlums;
import javelin.model.world.location.town.labor.cultural.BardsGuild.BuildBardsGuild;
import javelin.model.world.location.town.labor.cultural.MagesGuild.BuildMagesGuild;
import javelin.model.world.location.town.labor.ecological.ArcheryRange.BuildArcheryRange;
import javelin.model.world.location.town.labor.ecological.Henge.BuildHenge;
import javelin.model.world.location.town.labor.ecological.MeadHall.BuildMeadHall;
import javelin.model.world.location.town.labor.expansive.BuildHighway;
import javelin.model.world.location.town.labor.expansive.BuildRoad;
import javelin.model.world.location.town.labor.expansive.Settler;
import javelin.model.world.location.town.labor.expansive.TransportHub.BuildTransportHub;
import javelin.model.world.location.town.labor.military.Academy.BuildCommonAcademy;
import javelin.model.world.location.town.labor.military.MartialAcademy.BuildMartialAcademy;
import javelin.model.world.location.town.labor.productive.Deforestate;
import javelin.model.world.location.town.labor.productive.Mine.BuildMine;
import javelin.model.world.location.town.labor.productive.Shop.BuildShop;
import javelin.model.world.location.town.labor.religious.Sanctuary.BuildSanctuary;
import javelin.model.world.location.town.labor.religious.Shrine.BuildShrine;
import javelin.model.world.location.unique.Artificer.BuildArtificer;
import javelin.model.world.location.unique.AssassinsGuild.BuildAssassinsGuild;
import javelin.model.world.location.unique.MercenariesGuild.BuildMercenariesGuild;
import javelin.model.world.location.unique.SummoningCircle.BuildSummoningCircle;

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
			new BuildLodge(), new Redraw(), new BuildDwelling() };
	private static final Labor[] CRIMINAL = new Labor[] {
			new BuildAssassinsGuild(), new BuildSewers(), new BuildSlums() };
	private static final Labor[] CULTURAL = new Labor[] { new BuildMagesGuild(),
			new BuildArtificer(), new BuildSummoningCircle(),
			new BuildBardsGuild() };
	private static final Labor[] ECOLOGICAL = new Labor[] { new BuildHenge(),
			new BuildArcheryRange(), new BuildMeadHall() };
	private static final Labor[] EXPANSIVE = new Labor[] { new Settler(),
			new BuildOutpost(), new BuildRoad(), new BuildHighway(),
			new BuildTransportHub() };
	private static final Labor[] MILITARY = new Labor[] {
			new BuildMartialAcademy(), new BuildCommonAcademy(),
			new BuildMercenariesGuild() };
	private static final Labor[] PRODUCTIVE = new Labor[] { new BuildMine(),
			new Deforestate(), new BuildShop() };
	private static final Labor[] RELIGIOUS = new Labor[] { new BuildShrine(),
			new BuildSanctuary() };

	static final HashMap<String, Deck> DECKS = new HashMap<String, Deck>();
	static final Deck DEFAULT = new Deck();

	static {
		populate(DEFAULT, null, BASE);
		populate(new Deck(), "expansive", EXPANSIVE);
		populate(new Deck(), "productive", PRODUCTIVE);
		populate(new Deck(), "military", MILITARY);
		populate(new Deck(), "cultural", CULTURAL);
		populate(new Deck(), "criminal", CRIMINAL);
		populate(new Deck(), "religious", RELIGIOUS);
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
		if (Javelin.DEBUG && !t.ishostile()) {
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
				+ " district projects (minimum deck size: " + min + ")");
	}
}
