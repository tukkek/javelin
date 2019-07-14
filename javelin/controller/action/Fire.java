package javelin.controller.action;

import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.action.target.Target;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;

/**
 * Basic ranged attack.
 *
 * TODO make all subclasses children of {@link Target} instead.
 *
 * @see RangedAttack
 * @author alex
 */
public class Fire extends Target{
	/** Unique instace of {@link Fire}. */
	public static final Fire SINGLETON=new Fire();

	/**
	 * @param confirm Usually the same as the action key so as to make pressing
	 *          the same key twice a "invoke action" + "confirm targeting".
	 * @see Action#Action(String, String).
	 */
	public Fire(final String name,final String key,char confirm){
		super(name,key);
		confirmkey=confirm;
	}

	private Fire(){
		this("Fire or throw ranged weapon","f",'f');
	}

	@Override
	protected void attack(Combatant combatant,Combatant target,BattleState state){
		combatant.rangedattacks(target,state);
	}

	@Override
	protected int predictchance(Combatant active,Combatant target,BattleState s){
		var attacktype=RangedAttack.INSTANCE;
		var a=active.source.ranged.get(0).get(0);
		return calculatehiddc(active,target,a,attacktype,s);
	}

	public static int calculatehiddc(Combatant active,final Combatant target,
			Attack a,AbstractAttack attacktype,BattleState s){
		final float dc=20*attacktype.misschance(s,active,target,a.bonus);
		return Math.round(dc);
	}

	@Override
	protected void checkhero(Combatant hero){
		hero.checkattacktype(false);
	}
}
