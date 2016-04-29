package javelin.model.item.scroll;

import java.util.HashSet;

import javelin.controller.upgrade.Spell;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;

/**
 * Can only be used out-of-combat. The lore concept is that there is actually a
 * non-combatant able spellcaster (or at least spellreader) accompanying each
 * {@link Squad}.
 * 
 * @author alex
 */
public abstract class Scroll extends Item {
	public static final HashSet<Scroll> SCROLLS = new HashSet<Scroll>();
	/** @see Spell#cr */
	final public float incrementcost;
	/**
	 * Spell level.
	 * 
	 * @see Spell#calculatecasterlevel(int)
	 */
	final public int level;

	/**
	 * @param levelp
	 * @param incrementcostp
	 * @see Item#Item(String, int, ItemSelection)
	 */
	public Scroll(final String name, final int price, final ItemSelection town,
			int levelp, float incrementcostp) {
		super(name, price, town);
		level = levelp;
		incrementcost = incrementcostp;
		usedinbattle = false;
		SCROLLS.add(this);
	}

	@Override
	final public boolean use(Combatant user) {
		throw new RuntimeException("Scrolls are not use in combat.");
	}
}
