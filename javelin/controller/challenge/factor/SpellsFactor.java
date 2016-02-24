package javelin.controller.challenge.factor;

import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.spell.Blink;
import javelin.model.spell.DayLight;
import javelin.model.spell.DeeperDarkness;
import javelin.model.spell.DominateMonster;
import javelin.model.spell.Doom;
import javelin.model.spell.Heroism;
import javelin.model.spell.HoldMonster;
import javelin.model.spell.SlayLiving;
import javelin.model.spell.Summon;
import javelin.model.spell.VampiricRay;
import javelin.model.spell.totem.BearsEndurance;
import javelin.model.spell.totem.BullsStrength;
import javelin.model.spell.totem.CatsGrace;
import javelin.model.spell.totem.EaglesSplendor;
import javelin.model.spell.totem.FoxsCunning;
import javelin.model.spell.totem.OwlsWisdom;
import javelin.model.spell.wounds.CureCriticalWounds;
import javelin.model.spell.wounds.CureLightWounds;
import javelin.model.spell.wounds.CureModerateWounds;
import javelin.model.spell.wounds.CureSeriousWounds;
import javelin.model.spell.wounds.InflictCriticalWounds;
import javelin.model.spell.wounds.InflictLightWounds;
import javelin.model.spell.wounds.InflictModerateWounds;
import javelin.model.spell.wounds.InflictSeriousWounds;
import javelin.model.unit.Monster;

/**
 * TODO {@link Monster#spellcr}
 * 
 * @see CrFactor
 */
public class SpellsFactor extends CrFactor {

	public static int spells;

	@Override
	public float calculate(Monster monster) {
		/**
		 * TODO the correct way for this would be to have spell lists on the XML
		 * and keep them on a container like Javelin#descriptions and
		 * instantiate the spell at Combatant#generatespells
		 */
		return monster.spellcr;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		/* TODO could make it a constructor parameter for Spell */
		int before = handler.count();
		handler.water.add(new CureLightWounds());
		handler.good.add(new CureModerateWounds());
		handler.water.add(new CureSeriousWounds());
		handler.good.add(new CureCriticalWounds());

		handler.water.add(new CatsGrace());
		handler.fire.add(new BullsStrength());
		handler.earth.add(new BearsEndurance());
		handler.wind.add(new OwlsWisdom());
		handler.wind.add(new EaglesSplendor());
		handler.magic.add(new FoxsCunning());

		handler.fire.add(new Heroism());
		handler.magic.add(new HoldMonster());
		handler.magic.add(new DominateMonster());
		handler.magic.add(new Blink());

		handler.good.add(new DayLight());
		handler.evil.add(new DeeperDarkness());

		handler.evil.add(new InflictLightWounds());
		handler.evil.add(new InflictModerateWounds());
		handler.evil.add(new InflictSeriousWounds());
		handler.evil.add(new InflictCriticalWounds());
		handler.evil.add(new SlayLiving());
		handler.evil.add(new VampiricRay());
		handler.evil.add(new Doom());
		handler.evil.add(new Summon("Dretch", 1));
		handler.evil.add(new Summon("Gray slaad", 1));
		spells = handler.count() - before;
	}

	/**
	 * I'm not sure if it's a typo or proposital but .001 seems to be too low a
	 * factor, I'm using .01
	 */
	public static float ratespelllikeability(int casterlevel, int spelllevel) {
		return casterlevel * spelllevel * .01f;
	}

	public static float ratespelllikeability(int spelllevel) {
		return ratespelllikeability(Spell.calculatecasterlevel(spelllevel),
				spelllevel);
	}

	public static float ratetouchconvertedtoray(int spelllevel) {
		return .4f * spelllevel;
	}
}
