package javelin.model.item.consumable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javelin.Javelin.Delay;
import javelin.controller.Audio;
import javelin.controller.Point;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.Fire;
import javelin.controller.content.action.ai.attack.RangedAttack;
import javelin.model.item.Item;
import javelin.model.item.consumable.potion.Potion;
import javelin.model.item.consumable.potion.Vaporizer;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.AreaSpell;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.RPG;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Similar to a {@link Vaporizer} but is a hostile splash weapon instead.
 *
 * @author alex
 */
public class Bomb extends Item{
  static final String OUTCOME="""
      %s %s the %s!
      %s
      """.trim();
  static final int RANGE=20/5;

  class FireBomb extends Fire{
    public FireBomb(){
      super(null,"i",'i');
    }

    @Override
    protected Overlay overlay(Combatant active, Combatant target){
      return new AiOverlay(getarea(target.getlocation()));
    }

    @Override
    protected int predictchance(Combatant bomber,Combatant target,
        BattleState s){
      var attack=bomber.source.getbab()
          +Monster.getbonus(bomber.source.dexterity)
          -RangedAttack.INSTANCE.getpenalty(bomber,target,s)+RangedAttack
              .penalize(RangedAttack.countincrements(RANGE,bomber,target));
      return target.gettouchac()-attack;
    }

    boolean hit(Combatant bomber,Combatant target,BattleState s){
      var r=RPG.r(1,20);
      if(r==20) return true;
      if(r==1) return false;
      return r>=predictchance(bomber,target,s);
    }

    Point miss(Combatant bomber,Combatant target,BattleState s){
      var i=RangedAttack.countincrements(RANGE,bomber,target);
      var v=bomber.calculatevision(s);
      var t=target.getlocation();
      var area=t.getadjacent(i);
      area.retainAll(v);
      RPG.shuffle(area).sort(Comparator.comparing(a->a.distanceinsteps(t)));
      return area.get(area.size()-1);
    }

    Set<Point> getarea(Point p){
      return AreaSpell.getarea(p,10);
    }

    @Override
    protected void attack(Combatant caster,Combatant target,BattleState s){
      var h=hit(caster,target,s);
      var center=h?target.getlocation():miss(caster,target,s);
      var area=getarea(center);
      String result;
      if(spell instanceof AreaSpell a) result=a.cast(area,caster,s);
      else{
        var targets=AreaSpell.getcombatants(area,s);
        var effects=new ArrayList<String>(targets.size());
        for(var t:targets){
          var bonus=t.getlocation().equals(center)?-4:+2;
          var saved=RPG.r(1,20)+bonus<=spell.save(caster,target);
          effects.add(spell.cast(caster,t,saved,s,null));
        }
        result=String.join(" ",effects);
      }
      var item=Bomb.this.toString().toLowerCase();
      var success=h?"hits":"misses";
      var o=OUTCOME.formatted(caster,success,item,result).trim();
      var n=new ChanceNode(s,1,o,Delay.BLOCK);
      n.audio=new Audio("cast");
      n.overlay=new AiOverlay(area);
      Action.outcome(List.of(n));
    }
  }

  Spell spell;

  /** Constructor. */
  public Bomb(Spell s){
    super("Bomb of "+s.name.toLowerCase(),price(s),true);
    if(!s.isbomb&&!s.isrod&&!s.iswand||!s.castinbattle)
      throw new IllegalArgumentException(s+" is not a bomb!");
    spell=s.clone();
    identified=false;
    usedoutofbattle=false;
  }

  static int price(Spell s){
    if(!s.isbomb) s=Vaporizer.scale(s);
    return Potion.appraise(s);
  }

  @Override
  public boolean use(Combatant user){
    return new FireBomb().perform(user);
  }
}
