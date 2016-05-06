package javelin.controller.quality;

import java.util.ArrayList;

import javelin.model.unit.Monster;

/**
 * Represents a {@link Monster}'s special attack or special ability.
 * 
 * Note that not at specialties are implemented as a {@link Quality}. For
 * example {@link Monster#breaths}.
 * 
 * @author alex
 */
public abstract class Quality {

	/**
	 * Should always be lowercase.
	 */
	public final String name;

	public Quality(final String name) {
		this.name = name.toLowerCase();
	}

	public static final ArrayList<Quality> qualities = new ArrayList<Quality>();

	static {
		for (final Quality q : new Quality[] { new FastHealing("fast healing"),
				new Vision("low-light vision", Monster.VISION_LOWLIGHT),
				new Vision("blindsight", Monster.VISION_LOWLIGHT),
				new Vision("darkvision", Monster.VISION_DARK),
				new Vision("keen vision", Monster.VISION_DARK),
				new Vision("keen senses", Monster.VISION_DARK),
				new DamageReduction(), new EnergyResistance(),
				new SpellResistance(), new SpellImmunity(),
				new EnergyImmunity(), new MindImmunity(), new Tremorsense() }) {
			qualities.add(q);
		}
	}

	public abstract void add(String declaration, Monster m);

	abstract public boolean has(Monster m);

	/**
	 * @return Challenge rating factor fot the given {@link Monster}.
	 */
	abstract public float rate(Monster m);
}
