package javelin.model.item;

import java.util.HashSet;

import javelin.controller.action.CastSpell;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Can only be used out-of-combat. The lore concept is that there is actually a
 * non-combatant able spellcaster (or at least spellreader) accompanying each
 * {@link Squad}.
 * 
 * @author alex
 */
public class Scroll extends Item {
	/** Contains one instance of each type of spell. */
	public static final HashSet<Scroll> SCROLLS = new HashSet<Scroll>();
	/** Spell this scroll can cast once. */
	public final Spell spell;

	/**
	 * @param s
	 *            The Spell this scroll casts.
	 * @see Item#Item(String, int, ItemSelection)
	 */
	public Scroll(final Spell s) {
		super("Scroll of " + s.name.toLowerCase(),
				s.level * s.casterlevel * 50 + s.components,
				s.realm.getitems());
		spell = s.clone();
		usedinbattle = s.castinbattle;
		usedoutofbattle = s.castoutofbattle;
		apcost = 0;
		SCROLLS.add(this);
	}

	@Override
	public boolean use(Combatant user) {
		CastSpell.SINGLETON.cast(spell, user);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant c) {
		failure = null;
		if (!c.source.read(this)) {
			failure = c + " needs more experience before reading this scroll.";
			return false;
		}
		if (!spell.validate(c, null)) {
			return false;
		}
		spell.castpeacefully(c);
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && name.equals(((Scroll) obj).name);
	}

	@Override
	public String describefailure() {
		return failure == null ? super.describefailure() : failure;
	}

	@Override
	public String canuse(Combatant c) {
		return c.source.read(this) ? null : "can't read";
	}
}
