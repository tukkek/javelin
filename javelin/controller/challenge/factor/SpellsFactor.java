package javelin.controller.challenge.factor;

import javelin.Javelin;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.spell.Summon;
import javelin.model.spell.abjuration.Barkskin;
import javelin.model.spell.abjuration.Blink;
import javelin.model.spell.abjuration.DispelMagic;
import javelin.model.spell.abjuration.ResistEnergy;
import javelin.model.spell.conjuration.healing.NeutralizePoison;
import javelin.model.spell.conjuration.healing.RaiseDead;
import javelin.model.spell.conjuration.healing.Ressurect;
import javelin.model.spell.conjuration.healing.Restoration;
import javelin.model.spell.conjuration.healing.wounds.CureCriticalWounds;
import javelin.model.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.spell.conjuration.healing.wounds.CureSeriousWounds;
import javelin.model.spell.conjuration.teleportation.GreaterTeleport;
import javelin.model.spell.conjuration.teleportation.SecureShelter;
import javelin.model.spell.conjuration.teleportation.WordOfRecall;
import javelin.model.spell.divination.DiscernLocation;
import javelin.model.spell.divination.FindTraps;
import javelin.model.spell.divination.LocateObject;
import javelin.model.spell.divination.PryingEyes;
import javelin.model.spell.enchantment.compulsion.DominateMonster;
import javelin.model.spell.enchantment.compulsion.Heroism;
import javelin.model.spell.enchantment.compulsion.HoldMonster;
import javelin.model.spell.evocation.DayLight;
import javelin.model.spell.evocation.DeeperDarkness;
import javelin.model.spell.evocation.FlameStrike;
import javelin.model.spell.evocation.MagicMissile;
import javelin.model.spell.evocation.PolarRay;
import javelin.model.spell.evocation.ScorchingRay;
import javelin.model.spell.evocation.SoundBurst;
import javelin.model.spell.necromancy.Doom;
import javelin.model.spell.necromancy.Poison;
import javelin.model.spell.necromancy.RayOfExhaustion;
import javelin.model.spell.necromancy.SlayLiving;
import javelin.model.spell.necromancy.VampiricTouch;
import javelin.model.spell.necromancy.wounds.InflictCriticalWounds;
import javelin.model.spell.necromancy.wounds.InflictLightWounds;
import javelin.model.spell.necromancy.wounds.InflictModerateWounds;
import javelin.model.spell.necromancy.wounds.InflictSeriousWounds;
import javelin.model.spell.totem.BearsEndurance;
import javelin.model.spell.totem.BullsStrength;
import javelin.model.spell.totem.CatsGrace;
import javelin.model.spell.totem.EaglesSplendor;
import javelin.model.spell.totem.FoxsCunning;
import javelin.model.spell.totem.OwlsWisdom;
import javelin.model.spell.transmutation.ControlWeather;
import javelin.model.spell.transmutation.Darkvision;
import javelin.model.spell.transmutation.Fly;
import javelin.model.spell.transmutation.Longstrider;
import javelin.model.unit.Monster;

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
	public void listupgrades(UpgradeHandler handler) {
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
	 * {@link #listupgrades(UpgradeHandler)} is called before monster are
	 * loaded.
	 */
	static public void init() {
		for (Monster m : Javelin.ALLMONSTERS) {
			UpgradeHandler.singleton.schoolsummoning.add(new Summon(m.name, 1));
		}
	}
}
