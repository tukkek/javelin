package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Alignment;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;

/**
 * A group of generic monsters to be fought against.
 *
 * @author alex
 */
public class Encounter{
  /** An arbitrary number to help balance {@link Encounter} size. */
  public static final int BIG=9;

  /**
   * Units encountered.
   *
   * TODO turning this to {@link Monster} at some point would be huge on
   * performance
   */
  public List<Combatant> group;
  /** Encounter level as per OGL rules. */
  public int el;

  /** Constructor. */
  public Encounter(List<Combatant> groupp){
    group=groupp;
    el=ChallengeCalculator.calculateel(group);
  }

  /** One-unit constructor. */
  public Encounter(Combatant c){
    this(new ArrayList<>(List.of(c)));
  }

  /** Constructor for a given amount of {@link Monster} units. */
  public Encounter(Monster m,int amount){
    group=new Combatants(amount);
    for(var j=1;j<=amount;j++) group.add(new Combatant(m,true));
    el=ChallengeCalculator.calculateel(group);
  }

  /**
   * @return Copy of {@link #group}.
   */
  public Combatants generate(){
    var encounter=new Combatants(group.size());
    for(var m:group) encounter.add(new Combatant(m.source,true));
    return encounter;
  }

  @Override
  public String toString(){
    return Javelin.group(group)+" EL "+el+"";
  }

  /**
   * @return {@link #group} size.
   */
  public int size(){
    return group.size();
  }

  /** @return <code>true</code> if every unit is compatible. */
  public boolean iscompatible(Alignment a){
    return group.stream().allMatch(c->c.source.alignment.iscompatible(a));
  }
}
