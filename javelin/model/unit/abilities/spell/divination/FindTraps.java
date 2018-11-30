package javelin.model.unit.abilities.spell.divination;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * http://www.d20srd.org/srd/spells/findTraps.htm
 *
 * Theoretically level 3 but we want this to work for 1 hour so it can be used
 * during a {@link Dungeon} exploration.
 *
 * @author alex
 */
public class FindTraps extends Spell{
	public class FindingTraps extends Condition{
		/**
		 * Constructor.
		 *
		 * @param casterlevelp
		 */
		public FindingTraps(Combatant c,Integer casterlevelp){
			super(c,"finding traps",Effect.NEUTRAL,casterlevelp,Float.MAX_VALUE,1);
		}

		@Override
		public void start(Combatant c){
			// c.source.skills.search += 3;
		}

		@Override
		public void end(Combatant c){
			// c.source.skills.search -= 3;
		}
	}

	/** Constructor. */
	public FindTraps(){
		super("Find traps",3,ChallengeCalculator.ratespelllikeability(3),Realm.AIR);
		ispotion=true;
		castinbattle=false;
		castonallies=false;
		castoutofbattle=true;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		caster.addcondition(new FindingTraps(caster,casterlevel));
		return caster+" is finding traps more easily!";
	}
}
