package javelin.controller.challenge.factor;

import java.util.ArrayList;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.spell.BearsEndurance;
import javelin.model.spell.BullsStrength;
import javelin.model.spell.CatsGrace;
import javelin.model.spell.DayLight;
import javelin.model.spell.DeeperDarkness;
import javelin.model.spell.DominateMonster;
import javelin.model.spell.Heroism;
import javelin.model.spell.HoldMonster;
import javelin.model.spell.SlayLiving;
import javelin.model.spell.wounds.CureCriticalWounds;
import javelin.model.spell.wounds.CureModerateWounds;
import javelin.model.spell.wounds.CureSeriousWounds;
import javelin.model.spell.wounds.InflictCriticalWounds;
import javelin.model.spell.wounds.InflictModerateWounds;
import javelin.model.spell.wounds.InflictSeriousWounds;
import javelin.model.unit.Monster;

public class SpellsFactor extends CrFactor {

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
		/* Dont forget to update SpellbookGenerator */
		ArrayList<Upgrade> list = handler.addset();
		list.add(new CureModerateWounds("Spell: "));
		list.add(new CureSeriousWounds("Spell: "));
		list.add(new CureCriticalWounds("Spell: "));
		list.add(new CatsGrace("Spell: "));
		list.add(new DayLight("Spell: "));
		list.add(new Heroism("Spell: "));
		list.add(new BullsStrength("Spell: "));
		list.add(new BearsEndurance("Spell: "));
		list = handler.addset();
		list.add(new InflictModerateWounds("Spell: "));
		list.add(new InflictSeriousWounds("Spell: "));
		list.add(new InflictCriticalWounds("Spell: "));
		list.add(new DominateMonster("Spell: "));
		list.add(new DeeperDarkness("Spell: "));
		list.add(new SlayLiving("Spell: "));
		list.add(new HoldMonster("Spell: "));
	}

	public static float calculatechallengeforspelllikeability(int casterlevel,
			int spelllevel) {
		return casterlevel * spelllevel * .001f;
	}

	public static float calculatechallengefortouchspellconvertedtoray(
			int spelllevel) {
		return .4f * spelllevel;
	}
}
