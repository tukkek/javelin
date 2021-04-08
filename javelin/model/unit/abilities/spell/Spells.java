package javelin.model.unit.abilities.spell;

import javelin.model.unit.CloneableList;
import javelin.model.unit.Combatant;

/**
 * Known spells for a {@link Combatant}.
 *
 * @author alex
 */
public class Spells extends CloneableList<Spell>{
	/**
	 * Convenience method, prefer using {@link #get(Spell)}.
	 *
	 * @param type Given a spell class...
	 * @return the instance of such spell or <code>null</code> if none is found.
	 */
	public <K extends Spell> K get(Class<K> type){
		for(var s:this)
			if(type.isInstance(s)) return (K)s;
		return null;
	}

	/**
	 * @return The total number of spell casts per day for all current
	 *         {@link Spell}s.
	 * @see Spell#perday
	 */
	public int count(){
		int sum=0;
		for(Spell s:this)
			sum+=s.perday;
		return sum;
	}

	/**
	 * @return A Spell that {@link #equals(Object)} the given one or
	 *         <code>null</code> if none was found.
	 */
	@SuppressWarnings("unchecked")
	public <K extends Spell> K get(K spell){
		for(var s:this)
			if(s.equals(spell)) return (K)s;
		return null;
	}
}
