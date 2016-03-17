package javelin.model;

import java.util.TreeMap;

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
			return null;
		default:
			throw new RuntimeException("#unknownColor");
		}
	}
}