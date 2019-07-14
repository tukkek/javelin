package javelin.controller.action.ai.attack;

import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.BattleAi;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.skill.Bluff;

/**
 * In Javelin 1.7 a new approach of rolling one d20 to resolve attack sequences
 * will be attempted. This is a major win for the game because latter iterative
 * attacks are unlikely to hit, especially on higher levels and represent a
 * major pain for players - even more than inthe tabletop game where it's a
 * constant source of house rules and criticism. It will also help eliminate the
 * number of {@link BattleState} during {@link BattleAi} machinations, which
 * could represent something between a small to huge performance improvement. It
 * also overall helps with attack feeling more powerful and less
 * "choppy"/confusing gameplay overall and removes some implementation
 * complexity of keeping track of {@link AttackSequence} state.
 *
 * The first attack of the sequence will take a {@link ActionCost#STANDARD}
 * action, which is equivalent to both a single-attack sequence and a standard
 * (non-full) attack action. The rest of the attacks take a total of
 * {@link ActionCost#SWIFT} action, which each attempted attack taking an equal
 * share of that total. This makes a full-attack equivalent to a full turn of a
 * Standard, a Swift and a {@link ActionCost#FIVEFOOTSTEP} (1AP total).
 *
 * If an attack fails, the remainder of the sequence is halted, adding an amount
 * of dynamism and unpredactibility to attacking and improving performance since
 * later attacks would fail anyway, assuming they are ordered by attack bonus.
 *
 * Implementation details: for each 5% chance of a d20, a cache is created
 * holding the result of each attack. This cache will determine as equal any
 * rolls with the same results, so that outcome chances can be calculated
 * appropriately instead of generating {@link BattleState}s for all 20
 * possibilities. A second pass will then resolve criticals and a third pass
 * will calculate damage.
 *
 * @author alex
 */
public abstract class AbstractAttack extends Action implements AiAction{
	/** Manuever to be applied. */
	public Strike maneuver=null;
	/** @see Bluff#feign(Combatant) */
	public boolean feign=false;
	/** @see Cleave */
	public boolean cleave=false;

	String soundhit;
	String soundmiss;

	/** Constructor. */
	public AbstractAttack(String name,Strike s,String hitsound,String misssound){
		super(name);
		maneuver=s;
		soundhit=hitsound;
		soundmiss=misssound;
	}

	/** @return A bonus or penalty to damage. */
	@SuppressWarnings("unused")
	protected int getdamagebonus(Combatant attacker,Combatant target){
		return 0;
	}

	//	/**
	//	 * Always a full attack (1AP) but divided among the {@link AttackSequence}.
	//	 * This would penalize creatures with only one attack so max AP cost is .5 per
	//	 * attack.
	//	 *
	//	 * If a {@link #CURRENTMANEUVER} is being used, returns {@link Maneuver}
	//	 * instead.
	//	 */
	//	float calculateattackap(AttackSequence attacks){
	//		if(maneuver!=null) return maneuver.ap;
	//		int nattacks=attacks.size();
	//		if(nattacks==1) return .5f;
	//		/* if we let ap=.5 in this case it means that a combatant with a 2-attack
	//		 * sequence is identical to one with 1 attack */
	//		if(nattacks==2) return .4f;
	//		return 1f/nattacks;
	//	}

	abstract List<AttackSequence> getattacks(Combatant active);

	/**
	 * @param c Checks if swimmer.
	 * @return The penalty for attacking while standing on water (same as the
	 *         bonus for being attacked while staning on water).
	 */
	static int waterpenalty(BattleState s,Combatant c){
		return c.source.swim()>0&&s.map[c.location[0]][c.location[1]].flooded?2:0;
	}

	/**
	 * @param target Target of the attack
	 * @return Positive integer describing a penalty.
	 */
	protected int getpenalty(Combatant c,Combatant target,BattleState s){
		var penalty=waterpenalty(s,c)-waterpenalty(s,target)+target.surprise();
		if(target.burrowed) penalty+=4;
		return penalty;
	}

	//	/**
	//	 * @param attackbonus Bonus of the given any extraordinary bonuses (such as +2
	//	 *          from charge). Most common chances are calculated here or by the
	//	 *          concrete class.
	//	 * @return A bound % chance of an attack completely missing it's target.
	//	 * @see #bind(float)
	//	 */
	//	public static float misschance(BattleState s,Combatant c,Combatant target,
	//			int attackbonus){
	//		var misschance=(target.getac()+getpenalty(c,target,s)-attackbonus)/20f;
	//		return Action.bind(Action.or(misschance,target.source.misschance));
	//	}

	//	/** @return An estimate of the chance of hitting an attack ("easy to hit"). */
	//	public String getchance(Combatant c,Combatant target,Attack a,BattleState s){
	//		var misschance=misschance(s,c,target,a.bonus);
	//		return Javelin.translatetochance(Math.round(20*misschance))+" to hit";
	//	}

	@Override
	public boolean perform(Combatant active){
		throw new UnsupportedOperationException();
	}
}