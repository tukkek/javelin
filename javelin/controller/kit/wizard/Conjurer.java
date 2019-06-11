package javelin.controller.kit.wizard;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.SecureShelter;
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
import javelin.model.unit.abilities.spell.conjuration.teleportation.WordOfRecall;
import javelin.model.unit.abilities.spell.evocation.DayLight;
import javelin.model.unit.abilities.spell.evocation.DeeperDarkness;
import javelin.model.world.location.unique.SummoningCircle;
import javelin.old.RPG;

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
	/** Restoration spells like {@link Ressurect} and {@link Restoration}. */
	public static final List<Spell> RESTORATION=List.of(new NeutralizePoison(),
			new RaiseDead(),new Ressurect(),new Restoration());
	/**
	 * Every summoning {@link Spell}, for each {@link Monster} available.
	 *
	 * Since we don't want these to completely overwhelm the kit, only one per
	 * {@link Spell#casterlevel} is registered with the kit iself. More can be
	 * accessed through {@link SummoningCircle}s.
	 */
	public static final List<Summon> SUMMON=new ArrayList<>();
	/** Singleton. */
	public static final Conjurer INSTANCE=new Conjurer();

	/** Constructor. */
	public Conjurer(){
		super("Conjurer",RaiseWisdom.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(new WordOfRecall()); // teleportation
		extension.add(new GreaterTeleport()); // teleportation
		extension.add(new SecureShelter()); // teleportation
		extension.add(new DayLight());// evocation, light
		extension.add(new DeeperDarkness());// evocation, dark
		extension.addAll(HEALING);
		extension.addAll(RESTORATION);
	}

	static Summon findsummon(int casterlevel){
		for(var s:SUMMON)
			if(s.casterlevel==casterlevel) return s;
		return null;
	}

	/**
	 * Unlike most Kits, {@link Summon} spells need to be created after all
	 * {@link Monster}s are loaded.
	 *
	 * @see MonsterReader
	 */
	public static void initsummons(){
		Javelin.ALLMONSTERS.stream().filter(m->!m.passive)
				.map(m->new Summon(m.name,1)).forEach(s->SUMMON.add(s));
		RPG.shuffle(SUMMON);
		for(var casterlevel=1;casterlevel<=9;casterlevel++){
			var s=findsummon(casterlevel);
			if(s!=null) Conjurer.INSTANCE.extension.add(s);
		}
	}
}
