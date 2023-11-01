package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.fight.RandomEncounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Monsters;
import javelin.old.RPG;

/**
 * Adds or removes {@link Combatant}s while maintaining Challenge Rating. Keeps
 * {@link RandomEncounter}s fresh by avoiding hard static numbers.
 */
public class EncounterMixer{
  public List<Monsters> possibilites=new ArrayList<>();

  HashMap<String,Monster> units=new HashMap<>();
  int el;

  /** Constructor. */
  public EncounterMixer(Combatants encounter){
    possibilites
        .add(new Monsters(encounter.stream().map(e->e.source).toList()));
    for(var e:encounter) units.put(e.toString(),e.source);
    el=ChallengeCalculator.calculateel(encounter);
  }

  List<Monsters> register(List<Monsters> generated){
    generated=generated.stream().filter(g->g.getel()==el).toList();
    possibilites.addAll(generated);
    return generated;
  }

  void lower(List<Monsters> reference){
    if(reference.size()==0) return;
    var lower=new LinkedList<Monsters>();
    for(var r:reference) for(var name:units.keySet()){
      var u=r.stream().filter(m->m.toString().equals(name)).findAny()
          .orElse(null);
      if(u!=null){
        var monsters=new Monsters(r);
        monsters.remove(u);
        lower.add(monsters);
      }
    }
    lower(register(lower));
  }

  private void raise(List<Monsters> reference){
    if(reference.size()==0) return;
    var higher=new LinkedList<Monsters>();
    for(var r:reference) for(var u:units.values()){
      var monsters=new Monsters(r);
      monsters.add(u);
      higher.add(monsters);
    }
    raise(register(higher));
  }

  private List<Monsters> filter(List<Monsters> possibilites){
    var bygroup=new HashMap<String,Monsters>(possibilites.size());
    for(var p:possibilites) bygroup.put(Javelin.group(p),p);
    return bygroup.values().stream().map(Monsters::new).toList();
  }

  /** @return An encounter out of all {@link #possibilites}. */
  public Combatants mix(){
    lower(possibilites);
    raise(possibilites);
    possibilites=filter(possibilites);
    return Combatants.from(RPG.pick(possibilites));
  }
}
