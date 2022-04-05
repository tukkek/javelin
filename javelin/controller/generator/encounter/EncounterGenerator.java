package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.Water;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.GaveUp;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;

/**
 * Generates an {@link Encounter}.
 *
 * If I'm not mistaken when manually converting {@link Organization} data on
 * monster.xml to a parseable format I only included monster groups up to 16
 * strong - anything other than that for generated encounters will need to be
 * worked upon or done through other means than only this class.
 *
 * @author alex
 */
public class EncounterGenerator{
  /** @see EncounterIndex#expand() */
  static final int EXPANSIONS=2;
  /**
   * So small it should be instant to {@link EncounterIndex#expand()}.
   *
   * If you divide all Index sizes in two before they get Generated here, the
   * lower half's ceiling is ~60 while the upper half's floor is much higer at
   * ~700. Basically this strategy is allowing that lower half to pre-expand
   * once but there is a lot of room for other indexes between 100-700 later on,
   * especially as more {@link Haunt}s are added - so let's be conservative here
   * and rise later as needed.
   */
  static final int PREEXPAND=100;
  static final boolean PRINTINFO=false;

  static int minel=Integer.MIN_VALUE;
  static int maxel=Integer.MAX_VALUE;

  /**
   * A helper class to abstract where the @link Monster} pool is coming from and
   * generate {@link Combatants} accordingly. This is helpful because
   * {@link Terrain} pools are vast and can generate any type of encounter level
   * easily while {@link EncounterIndex}es are usually more limited and thus
   * more slowly scan all available encounter pools.
   *
   * @author alex
   */
  public static class MonsterPool{
    List<Terrain> terrains=new ArrayList<>(0);
    EncounterIndex index=null;

    /** Constructor. */
    public MonsterPool(Terrain t){
      terrains.add(t);
    }

    /** Constructor. */
    public MonsterPool(EncounterIndex indexp){
      index=indexp;
    }

    /** Constructor. */
    public MonsterPool(List<Monster> pool){
      this(new EncounterIndex(pool));
    }

    /**
     * Calls either {@link EncounterGenerator#generate(int, EncounterIndex)} or
     * {@link EncounterGenerator#generate(int, List)} based on constructor data.
     */
    public Combatants generate(int el) throws GaveUp{
      return index==null?EncounterGenerator.generate(el,terrains)
          :EncounterGenerator.generate(el,index);
    }
  }

  //	static int checklimit(int baseline,int step){
  //		String failure=null;
  //		for(var el=baseline;;el+=step)
  //			for(var t:Terrain.NONWATER){
  //				if(Javelin.DEBUG&&PRINTINFO)
  //					failure=String.format("Failure: %s el%s",t,el);
  //				if(generate(el,t)==null){
  //					if(failure!=null) System.out.println(failure);
  //					return el-step;
  //				}
  //			}
  //	}

  static{
    /**
     * TODO ideally at some point here would use {@link Terrain#ALL} but
     * currently {@link Terrain#WATER} can't even reliably generate encounters
     * with EL less than 2, so clearly some work has got to go into adding
     * low-level aquatic enemies first.
     */
    var encounters=EncounterIndex.merge(Terrain.NONWATER.stream()
        .map(t->Organization.ENCOUNTERSBYTERRAIN.get(t.toString()))
        .collect(Collectors.toList()));
    var els=new ArrayList<>(encounters.keySet());
    els.sort(null);
    minel=els.get(0);
    maxel=els.get(els.size()-1)+4;
  }

  public static List<Integer> COUNT=new ArrayList<>();

  /**
   * @param el Target encounter level - will work around this is cannot generate
   *   exactly what is given.
   * @param index Usually {@link Terrain#current()} but not necessarily - for
   *   example not when generation a
   *   {@link javelin.model.world.location.Location#garrison}, which uses the
   *   local terrain instead.
   * @return Enemy units for an encounter. <code>null</code> should not be
   *   returned to external calls of this class, as Encounter Levels should be
   *   padded to safety - the exception would be when using a very limited pools
   *   like only {@link Water}, or a terran that happens to have an empty gap in
   *   EL for some reason. In most typical cases it should be safe to not expect
   *   a <code>null</code> return.
   */
  public static Combatants generatebyindex(int el,
      List<EncounterIndex> encounters){
    if(el<minel) el=minel;
    if(el>maxel) el=maxel;
    var index=EncounterIndex.merge(encounters);
    var attempts=1+EXPANSIONS;
    COUNT.add(index.count());
    while(attempts>1&&el>index.firstKey()&&index.count()<PREEXPAND){
      index.expand(el);
      attempts-=1;
    }
    for(var i=0;i<attempts;i++){
      if(i>0) index.expand(el);
      var encounter=select(el,index);
      if(encounter!=null) return encounter;
      if(el<index.firstKey()) break;
    }
    return null;
  }

  /** {@link #generatebyindex(int, List)} with {@link Terrain}s instead. */
  public static Combatants generate(int el,List<Terrain> terrains){
    var encounters=new ArrayList<EncounterIndex>(terrains.size());
    for(var t:terrains)
      encounters.add(Organization.ENCOUNTERSBYTERRAIN.get(t.toString()));
    while(encounters.remove(null)) continue;
    return generatebyindex(el,encounters);
  }

  static Combatants select(int elp,EncounterIndex encounters){
    var groups=encounters.get(elp);
    return groups==null||groups.isEmpty()?null:RPG.pick(groups).generate();
  }

  /** As {@link #generate(int, Terrain)} but for one terrain. */
  public static Combatants generate(int el,Terrain t){
    return generate(el,List.of(t));
  }

  /**
   * Generates an encounter given an EL. Even with {@link Haunt#getminimumel()},
   * it still may be hard to generate a particular EL from a pool, so we
   * iteratively look for the next-best thing before giving up, favoring lower
   * ELs before higher ELs.
   */
  public static Combatants generate(int el,EncounterIndex index) throws GaveUp{
    if(index.isEmpty()) throw new GaveUp();
    var indexes=List.of(index);
    var foes=generatebyindex(el,indexes);
    if(foes!=null) return foes;
    for(var delta=1;delta<=20;delta++){
      foes=generatebyindex(el-delta,indexes);
      if(foes!=null) return foes;
      foes=generatebyindex(el+delta,indexes);
      if(foes!=null) return foes;
    }
    throw new GaveUp();
  }
}
