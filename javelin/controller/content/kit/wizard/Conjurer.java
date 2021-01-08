package javelin.controller.content.kit.wizard;

import java.util.List;

import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.SecureShelter;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.abilities.spell.conjuration.healing.NeutralizePoison;
import javelin.model.unit.abilities.spell.conjuration.healing.RaiseDead;
import javelin.model.unit.abilities.spell.conjuration.healing.Ressurect;
import javelin.model.unit.abilities.spell.conjuration.healing.LesserRestoration;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureCriticalWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureSeriousWounds;
import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;
import javelin.model.unit.abilities.spell.conjuration.teleportation.WordOfRecall;
import javelin.model.unit.abilities.spell.evocation.DayLight;
import javelin.model.unit.abilities.spell.evocation.DeeperDarkness;

/**
 * Conjuration wizard.
 *
 * @author alex
 */
public class Conjurer extends Wizard{
	/** Healing spells like {@link CureModerateWounds}. */
	public static final List<Spell> HEALING=List.of(new CureLightWounds(),
			new CureModerateWounds(),new CureSeriousWounds(),
			new CureCriticalWounds());
	/** Restoration spells like {@link Ressurect} and {@link LesserRestoration}. */
	public static final List<Spell> RESTORATION=List.of(new NeutralizePoison(),
			new RaiseDead(),new Ressurect(),new LesserRestoration());
	/** Singleton. */
	public static final Conjurer INSTANCE=new Conjurer();

	/** Constructor. */
	public Conjurer(){
		super("Conjurer",RaiseWisdom.SINGLETON);
	}

	@Override
	protected void extend(){
		extension.add(new WordOfRecall()); // teleportation
		extension.add(new GreaterTeleport()); // teleportation
		extension.add(new SecureShelter()); // teleportation
		extension.add(new DayLight());// evocation, light
		extension.add(new DeeperDarkness());// evocation, dark
		extension.addAll(HEALING);
		extension.addAll(RESTORATION);
	}

	@Override
	public void finish(){
		extension.addAll(Summon.select(Summon.SUMMONS, 1));
		super.finish();
	}
}
