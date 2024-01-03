package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.world.location.unique.MercenariesGuild;

public class Combatants extends ArrayList<Combatant>
    implements Cloneable,Serializable{

  public Combatants(){}

  public Combatants(int size){
    super(size);
  }

  public Combatants(Collection<Combatant> list){
    super(list);
  }

  @Override
  public int hashCode(){
    return toString().hashCode();
  }

  @Override
  public String toString(){
    return Javelin.group(this);
  }

  @Override
  public boolean equals(Object o){
    return o instanceof Combatants&&hashCode()==o.hashCode();
  }

  @Override
  public Combatants clone(){
    var clone=(Combatants)super.clone();
    for(var i=0;i<size();i++) clone.set(i,get(i).clone());
    return clone;
  }

  public List<Monster> getmonsters(){
    var monsters=new ArrayList<Monster>(size());
    for(Combatant c:this) monsters.add(c.source);
    return monsters;
  }

  /**
   * Unlike {@link #clone()}, this returns a new group of {@link Combatant}s,
   * with new {@link Combatant#id}s.
   */
  public Combatants generate(){
    var encounter=new Combatants(size());
    for(final Combatant m:this) encounter.add(new Combatant(m.source,true));
    return encounter;
  }

  /** @return Lowest {@link Monster#cr}. */
  public Combatant getweakest(){
    return stream().sorted((a,b)->Float.compare(a.source.cr,b.source.cr))
        .findFirst().orElse(null);
  }

  /** Generates {@link Combatant}s. */
  public static Combatants from(Collection<Monster> monsters){
    var c=new Combatants(monsters.size());
    for(var m:monsters) c.add(new Combatant(m,true));
    return c;
  }

  /** @see ChallengeCalculator#calculateel(List) */
  public int getel(){
    return ChallengeCalculator.calculateel(this);
  }

  /** @return Total {@link MercenariesGuild#getfee(Monster)}. */
  public int pay(){
    return stream().mapToInt(c->MercenariesGuild.getfee(c.source)).sum();
  }
}
