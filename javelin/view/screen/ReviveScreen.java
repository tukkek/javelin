package javelin.view.screen;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.stream.Stream;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.item.consumable.Scroll;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.raise.RaiseDead;
import javelin.model.unit.abilities.spell.conjuration.healing.raise.Ressurect;

/**
 * {@link EndBattle} {@link RaiseDead} menu.
 *
 * TODO would be nice to have a more active way to do this, similarly to other
 * items
 *
 * @author alex
 */
public class ReviveScreen{
  static final String PROMPT="""
      How will you ressurect %s?

      Dead units: %s.
      """;
  static final String READ="%s will read a %s";
  static final String CAST="%s will cast %s";

  record Option(Combatant user,Object method,String action){
    //
  }

  List<Combatant> dead=new Combatants(Fight.state.dead);
  List<Combatant> alive=new Combatants(0);

  /** Constructor. */
  public ReviveScreen(List<Combatant> originalteam){
    alive.addAll(originalteam);
    alive.removeAll(dead);
    dead.retainAll(originalteam);
    dead=dead.stream().filter(d->d.hp<=Combatant.DEADATHP).toList();
  }

  Option cast(Class<? extends RaiseDead> type){
    for(var a:alive){
      var spell=a.spells.stream()
          .filter(s->!s.exhausted()&&type.getClass().equals(s.getClass()))
          .findAny().orElse(null);
      if(spell!=null){
        var action=CAST.formatted(a,spell.toString().toLowerCase());
        return new Option(a,spell,action);
      }
    }
    return null;
  }

  Option read(Class<? extends RaiseDead> type){
    var scroll=Squad.active.equipment.getall(Scroll.class).stream()
        .filter(s->s.identified&&type.equals(s.spell.getClass())).findAny()
        .orElse(null);
    if(scroll==null) return null;
    var reader=alive.stream().filter(c->scroll.canuse(c)==null).findAny()
        .orElse(null);
    if(reader==null) return null;
    var action=READ.formatted(reader,scroll.name.toLowerCase());
    return new Option(reader,scroll,action);
  }

  Option choose(Combatant target,List<Option> means){
    var choices=means.stream().map(m->m.action).toList();
    var dead=this.dead.stream().map(Combatant::toString).collect(joining(", "));
    var p=PROMPT.formatted(target,dead);
    var choice=Javelin.choose(p,choices,true,false);
    return choice>=0?means.get(choice):null;
  }

  boolean cast(Combatant user,Spell s,Combatant target){
    if(!s.validate(user,target)) return false;
    s.castpeacefully(user,target);
    return true;
  }

  /**
   * Will search for {@link Spell}s and {@link Scroll}s to {@link RaiseDead},
   * show the player a list of options and apply one or none of them.
   *
   * @return <code>true</code> if a method has been selected, used and spent.
   */
  public boolean show(Combatant target){
    var means=Stream.of(cast(Ressurect.class),cast(RaiseDead.class),
        read(Ressurect.class),read(RaiseDead.class)).filter(o->o!=null)
        .toList();
    if(means.isEmpty()) return false;
    var choice=choose(target,means);
    if(choice==null) return false;
    if(choice.method instanceof Spell s&&cast(choice.user,s,target)){
      s.used+=1;
      return true;
    }
    if(choice.method instanceof Scroll s&&cast(null,s.spell,target)){
      Squad.active.equipment.remove(s);
      return true;
    }
    return false;
  }
}
