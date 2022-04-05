package javelin.controller.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.collection.CountingSet;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.AlignmentDetector;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * Mapping of {@link Encounter}s by Encounter Level.
 *
 * Depending on the operation, this structure can easily hold hundreds of
 * thousands of {@link Encounter}s in total, even when only a handful per EL are
 * more than enough to generate something like{@link DungeonFloor#encounters}.
 * The {@link #filter(int)} and {@link #limit(int)} functions are there to
 * reduce massively the complexity of such tasks.
 *
 * @author alex
 */
public class EncounterIndex extends TreeMap<Integer,List<Encounter>>{
  /** Constructor. */
  public EncounterIndex(){}

  /** Constructor from {@link Monster}s, with NPCs and mixed encounters. */
  public EncounterIndex(List<Monster> pool){
    for(var m:pool){
      for(var i=1;i<=Encounter.BIG;i++) put(new Encounter(m,i));
      var from=Math.round(m.cr)+1;
      var to=Math.max(20,from+10);
      IntStream.rangeClosed(from,to)
          .mapToObj(level->NpcGenerator.generate(m,level))
          .filter(npc->npc!=null).forEach(npc->put(new Encounter(npc)));
    }
    var all=values().stream().flatMap(List::stream)
        .collect(Collectors.toList());
    for(var i=0;i<all.size();i++) for(var j=i+1;j<all.size();j++){
      var a=all.get(i).group;
      var b=all.get(j).group;
      var size=a.size()+b.size();
      if(size>Encounter.BIG) continue;
      var mixed=new Combatants(size);
      mixed.addAll(a);
      mixed.addAll(b);
      try{
        ChallengeCalculator.calculateelsafe(mixed);
      }catch(UnbalancedTeams e){
        continue;
      }
      if(new CountingSet(mixed).getcount().size()>1) put(new Encounter(mixed));
    }
  }

  /** @param e Register this with the given Encounter Level. */
  public void put(int el,Encounter e){
    var tier=get(el);
    if(tier==null){
      tier=new ArrayList<>();
      put(el,tier);
    }
    tier.add(e);
  }

  /** @return Total number of {@link Encounter}s. */
  public int count(){
    var count=0;
    for(var encounters:values()) count+=encounters.size();
    return count;
  }

  /** Calls {@link #put(int, Encounter)} with {@link Encounter#el}. */
  public void put(Encounter e){
    put(e.el,e);
  }

  /** Adds all of the entires of the given Index to this one. */
  public void merge(EncounterIndex i){
    for(var el:i.keySet()) merge(el,new ArrayList<>(i.get(el)),(a,b)->{
      a.addAll(b);
      return a;
    });
  }

  /**
   * @return A new index with only {@link Encounter}s in the given Encounter
   *   Level range (inclusive).
   */
  public EncounterIndex filter(int from,int to){
    var i=new EncounterIndex();
    for(var el:keySet()) if(from<=el&&el<=to) i.put(el,get(el));
    return i;
  }

  /**
   * @return As {@link #filter(int, int)} but in the {@link Difficulty#VERYEASY}
   *   to {@link Difficulty#DIFFICULT} range.
   */
  public EncounterIndex filter(int el){
    var from=el+Difficulty.VERYEASY;
    var to=el+Difficulty.DIFFICULT;
    return filter(from,to);
  }

  /** @return All {@link Encounter}s. */
  public List<Encounter> getall(){
    return values().stream().flatMap(List::stream).collect(Collectors.toList());
  }

  /**
   * @return A new index, truncating each Encounter Level list of
   *   {@link Encounter}s to a maximum, rnadomly-chosen given limit of entries.
   */
  public EncounterIndex limit(int limit){
    var i=new EncounterIndex();
    for(var el:keySet()){
      var encounters=get(el);
      if(encounters.size()>limit) encounters=new ArrayList<>(
          RPG.shuffle(new ArrayList<>(encounters).subList(0,limit)));
      i.put(el,encounters);
    }
    return i;
  }

  /** {@link #merge(EncounterIndex)} many indexes into one. */
  public static EncounterIndex merge(Collection<EncounterIndex> merge){
    var i=new EncounterIndex();
    for(var m:merge) i.merge(m);
    return i;
  }

  /**
   * Combines all current {@link Encounter}s to maximize encounters by Ecnounter
   * Level. Uses {@link AlignmentDetector}.
   *
   * Can be called multiple times but naturally can only generate higher ELs,
   * with {@link Encounter}s acting as the building blocks.
   *
   * @param targetel Used for culling, we don't need to expand
   *   {@link Encounter}s outside a range that would produce a relevant EL.
   */
  public void expand(int targetel){
    var encounters=keySet().stream()
        .filter(el->targetel+Difficulty.MODERATE<el&&el<=targetel)
        .flatMap(el->get(el).stream()).collect(Collectors.toList());
    var nencounters=encounters.size();
    for(var i=0;i<nencounters;i++) for(var j=i+1;j<nencounters;j++){
      var e=new Combatants(encounters.get(i).group);
      e.addAll(encounters.get(j).group);
      if(new AlignmentDetector(e).check()) put(new Encounter(e));
    }
  }
}
