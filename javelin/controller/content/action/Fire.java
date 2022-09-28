package javelin.controller.content.action;

import javelin.controller.content.action.ai.attack.AbstractAttack;
import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.RangedAttack;
import javelin.controller.content.action.target.Target;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

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
   *   the same key twice a "invoke action" + "confirm targeting".
   * @see Action#Action(String, String)
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
    var sequence=active.source.ranged.get(0);
    return calculatehiddc(active,target,sequence.get(0),sequence,attacktype,s);
  }

  /** @return Difficulty Class-equivalent to hit this enemy. */
  public static int calculatehiddc(Combatant c,final Combatant target,Attack a,
      AttackSequence sequence,AbstractAttack attacktype,BattleState s){
    var r=new AttackResolver(attacktype,c,target,sequence,s);
    r.preview(target);
    return Math.round(20*r.misschance);
  }

  @Override
  protected void checkhero(Combatant hero){
    hero.checkattacktype(false);
  }
}
