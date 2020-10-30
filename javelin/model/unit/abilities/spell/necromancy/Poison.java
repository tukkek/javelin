package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Poisoned;

/**
 * Deals 1d10 points of temporary Constitution damage immediately and another
 * 1d10 points of temporary Constitution damage 1 hour later.
 *
 * @see Poisoned
 * @author alex
 */
public class Poison extends Touch{
	public static Poison instance=new Poison();
	boolean nonmagical=false;
	public int dcbonus=0;

	/** Constructor. */
	public Poison(){
		super("Poison",4,ChallengeCalculator.ratespell(4));
		castinbattle=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		if(saved) return target+" resists!";
		final int dc=getdc(caster);
		float ratio=(dc-target.source.getfortitude())/Float.valueOf(dc);
		if(ratio<0)
			ratio=0;
		else if(ratio>1) ratio=1;
		final int damage=Math.round(3*ratio);
		if(damage<=0) return target+" resists the poison!";
		Poisoned p=new Poisoned(target,damage,dc,nonmagical?null:this);
		if(p.neutralized) return target+" is immune to poison.";
		target.addcondition(p);
		if(target.hp<1) target.hp=1;
		return target+" is poisoned!";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		int fort=target.source.getfortitude();
		if(fort==Integer.MAX_VALUE||target.source.immunitytopoison)
			return Integer.MIN_VALUE;
		return getdc(caster)-fort;
	}

	int getdc(Combatant caster){
		return 10+casterlevel/2+Monster.getbonus(caster.source.wisdom)+dcbonus;
	}

	@Override
	public void setdamageeffect(){
		nonmagical=true;
	}
}
