package javelin.model.unit.abilities.spell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Area-of-effect spell.
 *
 * @author alex
 */
public abstract class AreaSpell extends Spell{
  boolean offensive=true;
  int radius;

  /** Constructor. */
  public AreaSpell(String name,int level,float cr,int radius){
    super(name,level,cr);
    this.radius=radius/5;
  }

  @Override
  public void filter(Combatant c,List<Combatant> targets,BattleState s){
    targets.retainAll(s.redteam);
  }

  /** @return Area of effect. */
  static public Set<Point> getarea(Point origin,int radius){
    var area=new HashSet<Point>(5);
    area.add(origin);
    for(var i=0;i<radius;i++){
      var adjacent=area.stream()
          .flatMap(a->a.getorthogonallyadjacent().stream()).toList();
      area.addAll(adjacent);
    }
    return area;
  }

  /** Affect a {@link Combatant#clone()}. */
  abstract protected String affect(Combatant target,Combatant caster,
      BattleState s);

  /**
   * @param area See {@link #getarea(Point, int)}.
   * @param caster Can be <code>null</code> (if being cast from an item, etc).
   * @return Description of resulting from casting this spell.
   */
  public String cast(Set<Point> area,Combatant caster,BattleState s){
    return s.getcombatants().stream()
        .filter(c->!offensive||10+casterlevel>=c.source.sr)
        .filter(c->area.contains(c.getlocation()))
        .map(c->affect(s.clone(c),caster,s)).collect(Collectors.joining(" "));
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    radius=4;
    var area=getarea(target.getlocation(),radius);
    cn.overlay=new AiOverlay(area);
    return cast(area,target,s);
  }
}
