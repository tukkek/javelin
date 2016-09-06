package javelin.model.feat;

import java.io.Serializable;
import java.util.TreeMap;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.attack.BullRush;
import javelin.model.feat.attack.Cleave;
import javelin.model.feat.attack.CombatExpertise;
import javelin.model.feat.attack.ExoticWeaponProficiency;
import javelin.model.feat.attack.GreatCleave;
import javelin.model.feat.attack.ImprovedFeint;
import javelin.model.feat.attack.ImprovedGrapple;
import javelin.model.feat.attack.ImprovedPreciseShot;
import javelin.model.feat.attack.ImprovedTrip;
import javelin.model.feat.attack.Multiattack;
import javelin.model.feat.attack.MultiweaponFighting;
import javelin.model.feat.attack.PointBlankShot;
import javelin.model.feat.attack.PowerAttack;
import javelin.model.feat.attack.PreciseShot;
import javelin.model.feat.attack.RapidShot;
import javelin.model.feat.attack.WeaponFinesse;
import javelin.model.feat.attack.WeaponFocus;
import javelin.model.feat.save.GreatFortitude;
import javelin.model.feat.save.IronWill;
import javelin.model.feat.save.LightningReflexes;
import javelin.model.feat.skill.Alertness;
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
	transient static public TreeMap<String, Feat> ALL =
			new TreeMap<String, Feat>();
	public final String name;
	/**
	 * If a feat needs updating, every time a {@link Combatant} is upgraded
	 * {@link #remove(Combatant)} and {@link #add(Combatant)} will be called so
	 * that there is a chance to update any statistics.
	 */
	public boolean update = false;

	static {
		new Alertness();
		new BullRush();
		new Cleave();
		new ExoticWeaponProficiency();
		new GreatCleave();
		new GreatFortitude();
		new ImprovedInitiative();
		new ImprovedPreciseShot();
		new IronWill();
		new LightningReflexes();
		new Multiattack();
		new MultiweaponFighting();
		new PointBlankShot();
		new PowerAttack();
		new PreciseShot();
		new RapidShot();
		new Toughness();
		new WeaponFinesse();
		new WeaponFocus();
		new CombatExpertise();
		new ImprovedFeint();
		new ImprovedTrip();
		new ImprovedGrapple();
	}

	public Feat(String namep) {
		name = namep.toLowerCase();
		ALL.put(name.toLowerCase(), this);
	}

	@Override
	public boolean equals(final Object obj) {
		return name.equals(((Feat) obj).name);
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
}
