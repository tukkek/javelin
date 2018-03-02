package javelin.model.unit.feat;

import java.io.Serializable;

import javelin.controller.challenge.factor.FeatsFactor;
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

	public boolean stack = false;
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
	public boolean arena = true;

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

	/** Called as part of the {@link #update} flow. */
	public void remove(Combatant c) {
		// do nothing

	}

	/**
	 * This will be called when a {@link Monster} is being made into a
	 * {@link Combatant}. It is also called after upgrades when the
	 * {@link #update} flag is set.
	 * 
	 * Can be called multiple times if a monster has more than one feat of the
	 * same type as allowed by {@link #stack}.
	 * 
	 * Note that many feats do not have to implement this as the stat block
	 * itself will have the changes pre-computated in it. For example:
	 * {@link Toughness} and {@link ImprovedInitiative} only need to implement
	 * {@link #upgrade(Combatant)}.
	 * 
	 * @return <code>true</code> in case of success.
	 */
	public boolean add(Combatant c) {
		// do nothing
		return true;
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

	/** This is called when an existing unit is being upgraded. */
	public boolean upgrade(Combatant c) {
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

	public void update(Combatant c) {
		if (update) {
			c.source = c.source.clone();
			remove(c);
			add(c);
		}
	}
}
