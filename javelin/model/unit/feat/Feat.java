package javelin.model.unit.feat;

import java.io.Serializable;

import javelin.controller.challenge.factor.FeatsFactor;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

/**
 * See the d20 SRD for more info.
 *
 * Would be nice to have Feat be a subclass of {@link Upgrade}, like
 * {@link Spell}?
 *
 * @author alex
 */
public abstract class Feat implements Serializable, javelin.model.Cloneable {
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
			return "Doesn't have " + name;
		} else {
			return "Has bought " + name + " " + count + " times";
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

	public Feat generate(String name2) {
		return this;
	}

	public void postupgradeautomatic(Combatant c) {
		// does nothing by default
	}

	public void postupgrade(Combatant c) {
		// nothing by defautl
	}

	@Override
	public Feat clone() {
		try {
			return (Feat) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return how many feats this should count as for purposes of normal
	 *         progression.
	 * @see FeatsFactor#getnormalprogression(Monster)
	 */
	public int count() {
		return 1;
	}
}
