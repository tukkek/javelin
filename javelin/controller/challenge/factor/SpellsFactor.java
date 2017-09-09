package javelin.controller.challenge.factor;

import javelin.Javelin;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.abjuration.Barkskin;
import javelin.model.unit.abilities.spell.abjuration.Blink;
import javelin.model.unit.abilities.spell.abjuration.DispelMagic;
import javelin.model.unit.abilities.spell.abjuration.ResistEnergy;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.abilities.spell.conjuration.healing.NeutralizePoison;
import javelin.model.unit.abilities.spell.conjuration.healing.RaiseDead;
import javelin.model.unit.abilities.spell.conjuration.healing.Ressurect;
import javelin.model.unit.abilities.spell.conjuration.healing.Restoration;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureCriticalWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureSeriousWounds;
import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;
import javelin.model.unit.abilities.spell.conjuration.teleportation.SecureShelter;
import javelin.model.unit.abilities.spell.conjuration.teleportation.WordOfRecall;
import javelin.model.unit.abilities.spell.divination.DiscernLocation;
import javelin.model.unit.abilities.spell.divination.FindTraps;
import javelin.model.unit.abilities.spell.divination.LocateObject;
import javelin.model.unit.abilities.spell.divination.PryingEyes;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bane;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Rage;
import javelin.model.unit.abilities.spell.evocation.DayLight;
import javelin.model.unit.abilities.spell.evocation.DeeperDarkness;
import javelin.model.unit.abilities.spell.evocation.FlameStrike;
import javelin.model.unit.abilities.spell.evocation.MagicMissile;
import javelin.model.unit.abilities.spell.evocation.PolarRay;
import javelin.model.unit.abilities.spell.evocation.ScorchingRay;
import javelin.model.unit.abilities.spell.evocation.SoundBurst;
import javelin.model.unit.abilities.spell.illusion.Displacement;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.abilities.spell.necromancy.RayOfExhaustion;
import javelin.model.unit.abilities.spell.necromancy.SlayLiving;
import javelin.model.unit.abilities.spell.necromancy.VampiricTouch;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictCriticalWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictLightWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictModerateWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictSeriousWounds;
import javelin.model.unit.abilities.spell.totem.BearsEndurance;
import javelin.model.unit.abilities.spell.totem.BullsStrength;
import javelin.model.unit.abilities.spell.totem.CatsGrace;
import javelin.model.unit.abilities.spell.totem.EaglesSplendor;
import javelin.model.unit.abilities.spell.totem.FoxsCunning;
import javelin.model.unit.abilities.spell.totem.OwlsWisdom;
import javelin.model.unit.abilities.spell.transmutation.ControlWeather;
import javelin.model.unit.abilities.spell.transmutation.Darkvision;
import javelin.model.unit.abilities.spell.transmutation.Fly;
import javelin.model.unit.abilities.spell.transmutation.Longstrider;

/**
 * TODO {@link Monster#spellcr}
 * 
 * @see CrFactor
 */
public class SpellsFactor extends CrFactor {
	@Override
	public float calculate(Monster monster) {
		return monster.spellcr;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler) {
		handler.schoolhealwounds.add(new CureLightWounds());// conjuration
		handler.schoolhealwounds.add(new CureModerateWounds());
		handler.schoolhealwounds.add(new CureSeriousWounds());
		handler.schoolhealwounds.add(new CureCriticalWounds());

		handler.schoolrestoration.add(new NeutralizePoison()); // conjuration
		handler.schoolrestoration.add(new RaiseDead());
		handler.schoolrestoration.add(new Ressurect());
		handler.schoolrestoration.add(new Restoration());

		// TODO separate body+mind when mass* comes
		handler.schooltotem.add(new BearsEndurance()); // transmutation
		handler.schooltotem.add(new BullsStrength());
		handler.schooltotem.add(new CatsGrace());
		handler.schooltotem.add(new EaglesSplendor());
		handler.schooltotem.add(new FoxsCunning());
		handler.schooltotem.add(new OwlsWisdom());

		handler.schoolabjuration.add(new Blink());
		handler.schoolabjuration.add(new Barkskin());
		handler.schoolabjuration.add(new ResistEnergy());
		handler.schoolabjuration.add(new DispelMagic());
		handler.schoolabjuration.add(new Displacement()); // TODO illusion

		handler.schooltransmutation.add(new Darkvision()); // transmutation
		handler.schooltransmutation.add(new ControlWeather());
		handler.schooltransmutation.add(new Fly()); // movement
		handler.schooltransmutation.add(new Longstrider());// movement

		handler.schooldivination.add(new LocateObject());
		handler.schooldivination.add(new PryingEyes());
		handler.schooldivination.add(new DiscernLocation());
		handler.schooldivination.add(new FindTraps());

		handler.schoolcompulsion.add(new Heroism());// enchantment
		handler.schoolcompulsion.add(new HoldMonster());
		handler.schoolcompulsion.add(new DominateMonster());
		handler.schoolcompulsion.add(new Bless());
		handler.schoolcompulsion.add(new Bane());
		handler.schoolcompulsion.add(new Rage());
		handler.schoolcompulsion.add(new BarbarianRage());

		handler.schoolwounding.add(new InflictLightWounds()); // necromancy
		handler.schoolwounding.add(new InflictModerateWounds());
		handler.schoolwounding.add(new InflictSeriousWounds());
		handler.schoolwounding.add(new InflictCriticalWounds());

		handler.schoolnecromancy.add(new SlayLiving());
		handler.schoolnecromancy.add(new VampiricTouch());
		handler.schoolnecromancy.add(new Doom());
		handler.schoolnecromancy.add(new Poison());
		handler.schoolnecromancy.add(new RayOfExhaustion());

		handler.schoolconjuration.add(new WordOfRecall()); // teleportation
		handler.schoolconjuration.add(new GreaterTeleport()); // teleportation
		handler.schoolconjuration.add(new SecureShelter()); // teleportation
		handler.schoolconjuration.add(new DayLight());// evocation, light
		handler.schoolconjuration.add(new DeeperDarkness());// evocation, dark

		handler.schoolevocation.add(new ScorchingRay());
		handler.schoolevocation.add(new MagicMissile());
		handler.schoolevocation.add(new PolarRay());
		handler.schoolevocation.add(new SoundBurst());
		handler.schoolevocation.add(new FlameStrike());
	}

	/**
	 * To be called post monster initialization.
	 * {@link #registerupgrades(UpgradeHandler)} is called before monster are
	 * loaded.
	 */
	static public void init() {
		for (Monster m : Javelin.ALLMONSTERS) {
			UpgradeHandler.singleton.schoolsummoning.add(new Summon(m.name, 1));
		}
	}
}
