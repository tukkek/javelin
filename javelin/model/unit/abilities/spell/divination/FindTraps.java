package javelin.model.unit.abilities.spell.divination;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.trap.Trap;

/**
 * http://www.d20srd.org/srd/spells/findTraps.htm
 *
 * Theoretically level 3 but we want this to work for 1 hour so it can be used
 * during a {@link DungeonFloor} exploration.
 *
 * @author alex
 */
public class FindTraps extends Spell{
	/** @see Trap */
	public class FindingTraps extends Condition{
		/**
		 * Constructor.
		 *
		 * @param casterlevelp
		 */
		FindingTraps(Spell s){
			super("trapfinder",s.level,s.casterlevel,Float.MAX_VALUE,1,
					Effect.NEUTRAL);
		}

		@Override
		public void start(Combatant c){
			//see Trap
		}

		@Override
		public void end(Combatant c){
			//see Trap
		}
	}

	/** Constructor. */
	public FindTraps(){
		super("Find traps",3,ChallengeCalculator.ratespell(3));
		ispotion=true;
		castinbattle=false;
		castonallies=false;
		castoutofbattle=true;
		isrune=new FindingTraps(this);
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		caster.addcondition(new FindingTraps(this));
		return caster+" is finding traps more easily!";
	}
}
