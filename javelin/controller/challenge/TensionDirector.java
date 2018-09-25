package javelin.controller.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;

/**
 * A director to help {@link Minigame} and {@link Haunt} {@link Fight}s maintain
 * a certain {@link Difficulty} balance throughout their durations.
 *
 * @author alex
 */
public class TensionDirector{
	/**
	 * A group of units that fulfills the next tension requirement. May be empty
	 * if {@link EncounterGenerator} had trouble coming up with
	 * appropriately-challenging enemies.
	 *
	 * Starts as <code>null</code> and is never reset, only replaced. This can be
	 * used by consumers to identify whether the first group has been generated.
	 *
	 * @see #raise(List, ArrayList, float)
	 */
	public List<Combatant> monsters=null;

	int tensionmin;
	int tensionmax;
	float raiseat=-Float.MAX_VALUE;
	int tension=RPG.r(tensionmin,tensionmax);
	/**
	 * Ensures that new waves are never becoming less dangerous (pressuring the
	 * player to upgrade and not just sit around).
	 */
	int baseline=Integer.MIN_VALUE;

	/**
	 * Note that this range is not guaranteed but fullfilled on a best-attempt
	 * basis.
	 *
	 * @param min Minimum {@link Difficulty}.
	 * @param max Maximum {@link Difficulty}.
	 */
	public TensionDirector(int min,int max){
		tensionmin=min;
		tensionmax=max;
	}

	/**
	 * @param ap Current {@link Combatant#ap}.
	 * @return <code>false</code> if doesn't need to raise {@link Fight} tension,
	 *         otherwise <code>true</code> and updates {@link #monsters}.
	 */
	public boolean raise(List<Combatant> blue,List<Combatant> red,float ap){
		if(ap<raiseat) return false;
		raiseat=ap+RPG.r(10,40)/10f;
		int elblue=ChallengeCalculator.calculateel(blue);
		int elred=ChallengeCalculator.calculateel(red);
		if(elblue-elred>=tension) return false;
		monsters=generate(elblue,red);
		tension=RPG.r(tensionmin,tensionmax);
		return true;
	}

	/**
	 * @param elblue Blue team Encounter Level.
	 * @param red Red team.
	 * @return List of monsters that tries to fulfill the {@link #tension}
	 *         requirement or {@link Collections#emptyList()} if couldn't find
	 *         one.
	 */
	protected List<Combatant> generate(int elblue,List<Combatant> red){
		baseline=Math.max(elblue+tensionmin,baseline);
		ArrayList<Combatant> last=null;
		for(int el=baseline;el<=elblue+tensionmax;el++)
			try{
				ArrayList<Combatant> group=EncounterGenerator.generate(el,
						Arrays.asList(Terrain.NONWATER));
				ArrayList<Combatant> monsters=new ArrayList<>(red);
				monsters.addAll(group);
				int tension=ChallengeCalculator.calculateel(monsters)-elblue;
				if(tension==this.tension) return group;
				if(tension>this.tension) return last==null?group:last;
				last=group;
			}catch(GaveUp e){
				continue;
			}
		return Collections.emptyList();
	}
}
