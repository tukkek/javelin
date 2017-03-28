package javelin.model.feat;

import java.io.Serializable;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 *
 * Would be nice to have Feat be a subclass of {@link Upgrade}, like
 * {@link Spell}?
 *
 * @author alex
 */
public abstract class Feat implements Serializable {
	/** Feat name as per d20 rules. */
	public final String name;

	boolean stack = false;
	/**
	 * If a feat needs updating, every time a {@link Combatant} is upgraded
	 * {@link #remove(Combatant)} and {@link #add(Combatant)} will be called so
	 * that there is a chance to update any statistics.
	 */
	public boolean update = false;
	/**
	 * If not <code>null</code>, symbolizes a previous requirement before
	 * acquiring this feat.
	 */
	public Feat prerequisite = null;

	/** Constructor. */
	public Feat(String namep) {
		name = namep.toLowerCase();
	}

	@Override
	public boolean equals(final Object obj) {
		return name.equals(((Feat) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * This is used by {@link MonsterReader} for when a monster source stat
	 * block needs to updated when it has a feat.
	 *
	 * Will be called multiple times if a monster has more than one feat of the
	 * same type.
	 *
	 * @param monster
	 *            Original unique stat block to derive.
	 */
	public void update(Monster m) {
		// do nothing
	}

	/**
	 * @see #update
	 */
	public void remove(Combatant c) {
		// do nothing

	}

	/**
	 * @see #update
	 */
	public void add(Combatant c) {
		// do nothing
	}

	@Override
	public String toString() {
		return name;
	}

	/** See {@link Upgrade#inform(Combatant) } */
	public String inform(Combatant c) {
		int count = c.source.countfeat(this);
		String name = toString().toLowerCase();
		if (count == 0) {
			return c + " doesn't have " + name;
		} else {
			return c + " has bought " + name + " " + count + " times";
		}
	}

	/** See {@link Upgrade} */
	public boolean apply(Combatant c) {
		if (!stack && c.source.hasfeat(this)) {
			return false;
		}
		if (prerequisite != null && !c.source.hasfeat(prerequisite)) {
			return false;
		}
		c.source.addfeat(this);
		return true;
	}
}
