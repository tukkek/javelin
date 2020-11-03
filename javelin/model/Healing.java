package javelin.model;

import javelin.model.item.Item;
import javelin.model.item.Recharger;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Sources of healing of any kind for Squad#quickheal(). Should not spend
 * {@link Item#consumable} instances as that should always be left to player
 * choice but it's OK to spend charges of {@link Recharger}-based items.
 *
 * @author alex
 */
public interface Healing{
	/**
	 * Doesn't need to check for things like {@link Spell#exhausted()} or
	 * {@link Item#canuse(Combatant)}, this should be done by
	 * {@link Squad#canheal()}.
	 *
	 * Note that {@link Spell}s implementing this should be able to accept a
	 * <code>null</code> parameter on
	 * {@link Spell#castpeacefully(Combatant, Combatant, java.util.List)}.
	 *
	 * @return <code>true</code> if the given {@link Combatant} can be healed.
	 */
	boolean canheal(Combatant c);

	/** @param c Heals target, spending charges as needed. */
	void heal(Combatant c);

	/** @return Number of times this healing source can be used right now. */
	int getheals();
}