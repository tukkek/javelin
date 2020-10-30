package javelin.controller.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
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
	public enum TensionAction{
		LOWER,KEEP,RAISE
	}

	/**
	 * A group of units that fulfills the next tension requirement. May be empty
	 * if {@link EncounterGenerator} had trouble coming up with
	 * appropriately-challenging enemies.
	 *
	 * Starts as <code>null</code> and is never reset, only replaced. This can be
	 * used by consumers to identify whether the first group has been generated.
	 *
	 * @see #check(List, ArrayList, float)
	 */
	public List<Combatant> monsters=null;

	protected boolean escalating;

	int tensionmin;
	int tensionmax;
	float next=-Float.MAX_VALUE;
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
	 * @param raising If set to <code>true</code>, will ensure new waves are never
	 *          less challenging than previous ones.
	 */
	public TensionDirector(int min,int max,boolean raising){
		tensionmin=min;
		tensionmax=max;
		escalating=raising;
	}

	/** Default director, suitable to {@link Haunt}s, for example. */
	public TensionDirector(){
		this(Difficulty.MODERATE,Difficulty.DIFFICULT,false);
	}

	/**
	 * @param ap Current {@link Combatant#ap}.
	 * @return <code>false</code> if doesn't need to raise {@link Fight} tension,
	 *         otherwise <code>true</code> and updates {@link #monsters}.
	 */
	public TensionAction check(List<Combatant> blue,List<Combatant> red,float ap){
		if(ap<next) return TensionAction.KEEP;
		delay(ap);
		int elblue=ChallengeCalculator.calculateel(blue);
		int elred=ChallengeCalculator.calculateel(red);
		int current=elred-elblue;
		if(current==tension) return TensionAction.KEEP;
		TensionAction r;
		if(current<tension){
			r=TensionAction.RAISE;
			monsters=generate(elblue,red);
		}else if(current>tensionmax){
			r=TensionAction.LOWER;
			monsters=generate(elred,blue);
		}else
			return TensionAction.KEEP;
		tension=RPG.r(tensionmin,tensionmax);
		return r;
	}

	/**
	 * Default implementation adds 1d4 turns (with .1ap granurality).
	 *
	 * @param currentap Will add a certain delay to this given value and not allow
	 *          a {@link #check(List, List, float)} before that.
	 */
	public void delay(float currentap){
		next=currentap+RPG.r(10,40)/10f;
	}

	/**
	 * Calls {@link #generate(int)} systematically to produce an appropriate gruop
	 * of {@link #monsters}.
	 *
	 * @param elblue Blue team Encounter Level.
	 * @param red Red team.
	 * @return List of monsters that tries to fulfill the {@link #tension}
	 *         requirement or {@link Collections#emptyList()} if couldn't find
	 *         one.
	 */
	protected List<Combatant> generate(int elblue,List<Combatant> red){
		int el=elblue+tensionmin;
		if(escalating){
			baseline=Math.max(el,baseline);
			el=baseline;
		}
		List<Combatant> last=null;
		for(;el<=elblue+tensionmax;el++)
			try{
				List<Combatant> group=generate(el);
				List<Combatant> monsters=new ArrayList<>(red);
				monsters.addAll(group);
				int tension=ChallengeCalculator.calculateel(monsters)-elblue;
				if(tension==this.tension) return group;
				if(tension>this.tension) return last==null?group:last;
				last=group;
			}catch(GaveUp e){
				continue;
			}
		return last==null?Collections.emptyList():last;
	}

	/**
	 * @return Given an Encounter Level, an appropriate gruop of
	 *         {@link #monsters}.
	 */
	protected List<Combatant> generate(int el) throws GaveUp{
		return EncounterGenerator.generate(el,Terrain.NONWATER);
	}

	/**
	 * Makes sure next call to {@link #check(List, List, float)} will go through
	 * regardless of the Action Point parameter.
	 */
	public void force(){
		next=-Float.MAX_VALUE;
	}
}
