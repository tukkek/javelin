package javelin.controller;

import javelin.Javelin;
import javelin.controller.action.Help;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.scenario.Scenario;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * A collection of methods that can be altered to facilitate testing the game.
 * Entry-points start with "on", such as {@link #oncampaignstart()} and should
 * never be called from other parts of the game unless {@link Javelin#DEBUG} is
 * <code>true</code>. Other methods are helpers to be used within the class,
 * such as {@link #additems(Item[])}.
 * 
 * Ideally changes to this class should never be commited unless when expanding
 * debug functionalities (such adding new entry or helper methods).
 * 
 * @author alex
 */
public class Debug {
	/**
	 * Called every time a game starts (roughly the first time the
	 * {@link WorldScreen} is shown.
	 */
	public static void ongamestart() {

	}

	/** Called only once when a {@link Scenario} is initialized. */
	public static void oncampaignstart() {
		Squad.active.members
				.add(new Combatant(Javelin.getmonster("Medusa"), true));
	}

	/**
	 * Called from {@link Help}. Useful for making changes during the course of
	 * a game or testing sequence, since Javelin doesn't have a developer
	 * console for debugging purposes.
	 * 
	 * @return Any text will be printed below the usual help output.
	 */
	public static String onhelp() {
		return "";
	}

	static void healall() {
		for (Combatant c : Squad.active.members) {
			c.hp = c.maxhp;
			c.detox(c.source.poison);
		}
	}

	static void additems(Item[] items) {
		for (Item i : items) {
			Squad.active.receiveitem(i);
		}
	}

	static String printtowninfo() {
		String s = "\n\n";
		for (Town t : Town.gettowns()) {
			s += t.population + " ";
		}
		s += "\n\n";
		for (Town t : Town.gettowns()) {
			s += ChallengeRatingCalculator.calculateel(t.garrison) + " ";
		}
		return s;
	}

}
