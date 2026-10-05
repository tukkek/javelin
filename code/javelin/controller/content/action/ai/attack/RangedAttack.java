package javelin.controller.content.action.ai.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.target.RangedTarget;
import javelin.controller.walker.Walker;
import javelin.model.item.consumable.Bomb;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.unit.Size;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Prone;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;

/**
 * needs to take into account range increments, each full range increment
 * imposes a cumulative -2 penalty on the attack roll. Actually doesn't sound
 * too important since most weapons used have really long ranges for our combats
 *
 * also -4 if in melée with another enemy (many more details @
 * http://www.d20pfsrd.com/gamemastering/combat
 *
 * @author alex
 * @see RangedTarget
 */
public class RangedAttack extends AbstractAttack{
  static public RangedAttack INSTANCE=new RangedAttack(null);
  /**
   * Currently if the AI sees an infinitesimal chance to hit, it'll rather
   * damage any unit for immediate utility. This is very statoc, with units
   * always attacking from far away if they have ranged attacks. Let's disable
   * this for {@link #getoutcomes(Combatant, BattleState)} for now.
   */
  static boolean AISKIPUNLIKELY=true;

  /** Constructor. */
  public RangedAttack(Strike m){
    super("Ranged attack",m,"ranged-hit","ranged-miss");
  }

  @Override
  List<AttackSequence> getattacks(Combatant active){
    return active.source.ranged;
  }

  /**
   * TODO Attacker's {@link Size} modifier?
   */
  @Override
  public int getpenalty(Combatant attacker,Combatant target,BattleState s){
    var penalty=super.getpenalty(attacker,target,s);
    if(!attacker.source.hasfeat(PreciseShot.SINGLETON)&&s.isengaged(target))
      penalty+=4;
    if(!attacker.source.hasfeat(ImprovedPreciseShot.SINGLETON)
        &&iscovered(s.haslineofsight(attacker,target),target,s))
      penalty+=4;
    if(target.hascondition(Prone.class)!=null) penalty+=2;
    if(ispointblankshot(attacker,target)) penalty-=1;
    return penalty;
  }

  public static boolean iscovered(Vision sight,Combatant target,BattleState s){
    return sight==Vision.COVERED||sight==Vision.CLEAR
        &&s.map[target.location[0]][target.location[1]].obstructed;
  }

  static boolean ispointblankshot(Combatant attacker,Combatant target){
    return attacker.source.hasfeat(PointBlankShot.SINGLETON)
        &&Walker.distance(attacker,target)<=6;
  }

  @Override
  public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
    if(s.isengaged(c)) return Collections.EMPTY_LIST;
    var successors=new ArrayList<List<ChanceNode>>();
    for(var target:s.gettargets(c)) for(var attacks:c.source.ranged){
      var r=new AttackResolver(this,c,target,attacks,s);
      if(!skip(r,c,target)) successors.add(r.attack(c,target,s));
    }
    return successors;
  }

  /**
   * @return <code>true</code> if this ranged attack has a very unlikely chance
   *   to hit and only as long as the active {@link Combatant} cannot
   *   potentially move to reposition himself for a mêlée attack.
   *
   * @see #AISKIPUNLIKELY
   */
  static boolean skip(AttackResolver r,Combatant c,Combatant target){
    if(!AISKIPUNLIKELY||c.source.melee.isEmpty()) return false;
    r.preview(target);
    return r.misschance>Javelin.HARD/20f;
  }

  @Override
  protected int getdamagebonus(Combatant attacker,Combatant target){
    return ispointblankshot(attacker,target)?1:0;
  }

  /**
   * Utility method. This is not calculated with normal ranged attacks because
   * the increment tends to be much bigger than typical {@link Map} distances in
   * {@link Javelin} - but for things like {@link Bomb}s it can be relevant.
   *
   * @return Range increments counted (1 or more).
   */
  public static int countincrements(int range,Combatant c,Combatant target){
    var d=c.getlocation().distanceinsteps(target.getlocation());
    var i=range;
    while(i<d) i+=range;
    return i/range;
  }

  /** {@link #countincrements(int, Combatant, Combatant)} to attack penalty. */
  public static int penalize(int increments){
    return -2*(increments-1);
  }
}
