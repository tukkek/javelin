package javelin.controller.content.kit.wizard;

import java.util.stream.Collectors;

import javelin.controller.content.quality.perception.Vision;
import javelin.controller.content.quality.resistance.PoisonImmunity;
import javelin.controller.content.upgrade.ability.RaiseIntelligence;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.abilities.spell.necromancy.RayOfExhaustion;
import javelin.model.unit.abilities.spell.necromancy.SlayLiving;
import javelin.model.unit.abilities.spell.necromancy.VampiricTouch;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictCriticalWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictLightWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictModerateWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictSeriousWounds;

/**
 * Necromancy spells.
 *
 * @author alex
 */
public class Necromancer extends Wizard{
	/** Singleton. */
	public static final Necromancer INSTANCE=new Necromancer();

	/** Constructor. */
	public Necromancer(){
		super("Necromancer",RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void extend(){
		super.extend();
		extension.add(new SlayLiving());
		extension.add(new VampiricTouch());
		extension.add(new Doom());
		extension.add(new Poison());
		extension.add(new RayOfExhaustion());
		extension.add(new InflictLightWounds());
		extension.add(new InflictModerateWounds());
		extension.add(new InflictSeriousWounds());
		extension.add(new InflictCriticalWounds());
		extension.add(PoisonImmunity.UPGRADE);
		extension.add(Vision.DARKVISION);
	}

	@Override
	public void finish(){
		var undead=Summon.SUMMONS.stream()
				.filter(s->Monster.get(s.monstername).type.equals(MonsterType.UNDEAD))
				.collect(Collectors.toList());
		extension.addAll(Summon.select(undead,1));
		super.finish();
	}
}
