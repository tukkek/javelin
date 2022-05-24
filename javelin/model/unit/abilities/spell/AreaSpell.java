package javelin.model.unit.abilities.spell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
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
    this.radius=radius;
  }

  @Override
  public void filter(Combatant c,List<Combatant> targets,BattleState s){
    targets.retainAll(s.redteam);
  }

  /** @return Area of effect. */
  static public Set<Point> getarea(Point origin,int radius){
    radius/=5;
    var area=new HashSet<Point>(5);
    area.add(origin);
    var m=Fight.state.map;
    var width=m.length;
    var height=m[0].length;
    for(var i=0;i<radius;i++){
      var adjacent=area.stream()
          .flatMap(a->a.getorthogonallyadjacent().stream())
          .filter(a->a.validate(width,height)).toList();
      area.addAll(adjacent);
    }
    return area;
  }

  /** Affect a {@link Combatant#clone()}. */
  abstract protected String affect(Combatant target,Combatant caster,
      BattleState s);

  /**
   * @param area See {@link #getarea(Point, int)}.
   * @return All {@link Combatant}s in this area.
   */
  static public Combatants getcombatants(Set<Point> area,BattleState s){
    return new Combatants(s.getcombatants().stream()
        .filter(c->area.contains(c.getlocation())).toList());
  }

  /**
   * @param area See {@link #getarea(Point, int)}.
   * @param caster Can be <code>null</code> (if being cast from an item, etc).
   * @return Description of resulting from casting this spell.
   */
  public String cast(Set<Point> area,Combatant caster,BattleState s){
    return getcombatants(area,s).stream()
        .filter(c->!offensive||10+casterlevel>=c.source.sr)
        .map(c->affect(s.clone(c),caster,s)).collect(Collectors.joining(" "));
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    var area=getarea(target.getlocation(),radius);
    cn.overlay=new AiOverlay(area);
    return cast(area,target,s);
  }
}
