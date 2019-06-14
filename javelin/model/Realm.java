package javelin.model;

import javelin.model.unit.Combatant;
import javelin.old.RPG;

/**
 * A realm is an in-game faction, associated with d20 concepts in an arbitrary
 * manner, following a generic lore of colors (as in Magic: The Gathering) or
 * factions/races (as featured in many real-time strategy titles).
 *
 * @author alex
 */
public enum Realm{
	AIR,FIRE,EARTH,WATER,GOOD,EVIL,MAGIC;

	/**
	 * @return Equivalent color for the view package or <code>null</code> for
	 *         {@link #AIR}.
	 */
	public java.awt.Color getawtcolor(){
		switch(this){
			case WATER:
				return java.awt.Color.BLUE;
			case MAGIC:
				return java.awt.Color.MAGENTA;
			case GOOD:
				return java.awt.Color.WHITE;
			case FIRE:
				return java.awt.Color.RED;
			case EARTH:
				return java.awt.Color.GREEN.darker();
			case EVIL:
				return java.awt.Color.BLACK;
			case AIR:
				return java.awt.Color.GRAY;
			default:
				throw new RuntimeException("#unknownColor");
		}
	}

	@Override
	public String toString(){
		switch(this){
			case WATER:
				return "Water";
			case FIRE:
				return "Fire";
			case EARTH:
				return "Earth";
			case AIR:
				return "Air";
			case GOOD:
				return "Blessed";
			case EVIL:
				return "Cursed";
			case MAGIC:
				return "Magic";
			default:
				throw new RuntimeException("#unknownColor");
		}
	}

	/**
	 * @return A randomly-chosen realm.
	 */
	public static Realm random(){
		Realm[] realms=Realm.values();
		return realms[RPG.r(0,realms.length-1)];
	}

	/**
	 * @param opponent Add's a realm-related keyword as a prefix to this unit's
	 *          name.
	 */
	public void baptize(Combatant opponent){
		String prefix=prefixate();
		opponent.source.customName=prefix+" "+opponent.source.name.toLowerCase();
		opponent.source.customName=Character
				.toUpperCase(opponent.source.customName.charAt(0))
				+opponent.source.customName.substring(1).toLowerCase();
	}

	public String prefixate(){
		if(equals(GOOD)) return "Blessed";
		if(equals(EVIL)) return "Cursed";
		String prefix=name();
		return prefix.charAt(0)+prefix.substring(1).toLowerCase();
	}

	/**
	 * @return Names in the following format: Water, Earth, Good...
	 */
	public String getname(){
		String name=name();
		return name.charAt(0)+name().substring(1).toLowerCase();
	}
}