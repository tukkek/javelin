package javelin.controller.content.quality;

import java.util.ArrayList;

import javelin.controller.content.quality.perception.Darkvision;
import javelin.controller.content.quality.perception.LowLightVision;
import javelin.controller.content.quality.perception.Perception;
import javelin.controller.content.quality.resistance.CriticalImmunity;
import javelin.controller.content.quality.resistance.DamageReduction;
import javelin.controller.content.quality.resistance.EnergyImmunity;
import javelin.controller.content.quality.resistance.EnergyResistance;
import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.quality.resistance.ParalysisImmunity;
import javelin.controller.content.quality.resistance.PoisonImmunity;
import javelin.controller.content.quality.resistance.SpellImmunity;
import javelin.controller.content.quality.resistance.SpellResistance;
import javelin.controller.content.quality.subtype.Construct;
import javelin.controller.content.quality.subtype.Elemental;
import javelin.controller.content.quality.subtype.Ooze;
import javelin.controller.content.quality.subtype.Subtype;
import javelin.controller.content.quality.subtype.Undead;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;

/**
 * Represents a {@link Monster}'s special attack or special ability.
 *
 * Note that not at specialties are implemented as a {@link Quality}. For
 * example {@link Monster#breaths}.
 *
 * TODO make Quality a subclass of Upgrade as well? For example: MindImmunity is
 * not being offered as upgrade.
 *
 * @author alex
 */
public abstract class Quality{

	/**
	 * Should always be lowercase.
	 */
	public final String name;

	/**
	 * @param name Will be converted to lower-case.
	 */
	public Quality(final String name){
		this.name=name.toLowerCase();
	}

	public static final ArrayList<Quality> qualities=new ArrayList<>();

	static{
		for(final Quality q:new Quality[]{new FastHealing("fast healing"),
				new LowLightVision("low-light vision",Monster.VISION_LOWLIGHT),
				new Darkvision("darkvision",Monster.VISION_DARK),new DamageReduction(),
				new EnergyResistance(),new SpellResistance(),new SpellImmunity(),
				new EnergyImmunity(),new MindImmunity(),new Perception(),
				new CriticalImmunity(),new ImprovedGrab(),new Elemental(),new Subtype(),
				new SunlightVulnerability(),new Undead(),new Construct(),new Ooze(),
				new FrightfulPresence(),new ParalysisImmunity(),new PoisonImmunity(),
				new Poison(),new NoHealing(),new Constrict()})
			qualities.add(q);
	}

	/**
	 * @param declaration Textual parameters from monsters.xml.
	 * @param m Apply quality.
	 */
	public abstract void add(String declaration,Monster m);

	/**
	 * This is used only in order to decide if uses {@link #rate(Monster)} or not.
	 *
	 * @return <code>true</code> if given {@link Monster} has this quality.
	 * @see #apply(String)
	 */
	abstract public boolean has(Monster m);

	/**
	 * @return Challenge rating factor fot the given {@link Monster}.
	 */
	abstract public float rate(Monster m);

	/**
	 * @return A description of this {@link Monster}s ability if it's one he
	 *         {@link #has(Monster)}. May return <code>null</code> in order to be
	 *         ignored.
	 */
	public String describe(Monster m){
		return name;
	}

	/**
	 * This is used only in order to decide if uses {@link #add(Monster)} or not.
	 *
	 * @param monster Monster being examined. Note that this entry is still being
	 *          read by {@link MonsterReader} and could be incomplete.
	 *
	 * @return <code>true</code> if given lowercase {@link String} is describing
	 *         this quality.
	 * @see #has(Monster)
	 */
	public boolean apply(String text,Monster m){
		return text.contains(name);
	}
}
