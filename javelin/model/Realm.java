package javelin.model;

import java.util.TreeMap;

import javelin.model.unit.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * A realm is an in-game faction, associated with d20 concepts in an arbitrary
 * manner, following a generic lore of colors (as in Magic: The Gathering) or
 * factions/races (as featured in many real-time strategy titles).
 * 
 * @author alex
 */
public enum Realm {
	WIND, FIRE, EARTH, WATER, GOOD, EVIL, MAGICAL;

	public static TreeMap<Realm, String> REALMS = new TreeMap<Realm, String>();

	/**
	 * @return Equivalent color for the view package or <code>null</code> for
	 *         {@link #WIND}.
	 */
	public java.awt.Color getawtcolor() {
		switch (this) {
		case WATER:
			return java.awt.Color.BLUE;
		case MAGICAL:
			return java.awt.Color.MAGENTA;
		case GOOD:
			return java.awt.Color.WHITE;
		case FIRE:
			return java.awt.Color.RED;
		case EARTH:
			return java.awt.Color.GREEN;
		case EVIL:
			return java.awt.Color.BLACK;
		case WIND:
			return java.awt.Color.GRAY;
		default:
			throw new RuntimeException("#unknownColor");
		}
	}

	@Override
	public String toString() {
		switch (this) {
		case WATER:
			return "Blue";
		case MAGICAL:
			return "Octarine";
		case GOOD:
			return "White";
		case FIRE:
			return "Red";
		case EARTH:
			return "Green";
		case EVIL:
			return "Black";
		case WIND:
			return "Translucid";
		default:
			throw new RuntimeException("#unknownColor");
		}
	}

	/**
	 * @return A randomly-chosen realm.
	 */
	public static Realm random() {
		Realm[] realms = Realm.values();
		return realms[RPG.r(0, realms.length - 1)];
	}

	/**
	 * @param opponent
	 *            Add's a realm-related keyword as a prefix to this unit's name.
	 */
	public void baptize(Combatant opponent) {
		String prefix = prefixate();
		opponent.source.customName =
				prefix + " " + opponent.source.name.toLowerCase();
		opponent.source.customName =
				Character.toUpperCase(opponent.source.customName.charAt(0))
						+ opponent.source.customName.substring(1).toLowerCase();
	}

	String prefixate() {
		if (equals(GOOD)) {
			return "Blessed";
		}
		if (equals(EVIL)) {
			return "Cursed";
		}
		String prefix = name();
		return prefix.charAt(0) + prefix.substring(1).toLowerCase();
	}
}