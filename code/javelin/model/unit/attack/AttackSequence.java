package javelin.model.unit.attack;

import java.util.Collection;
import java.util.Comparator;

import javelin.Javelin;
import javelin.model.unit.CloneableList;
import javelin.model.unit.Combatant;
import javelin.model.unit.feat.attack.PowerAttack;
import javelin.model.unit.feat.attack.shot.RapidShot;

/**
 * One of the possible mêlée or ranged full-attack options of a
 * {@link Combatant}.
 *
 * @author alex
 */
public class AttackSequence extends CloneableList<Attack>{
  static final Comparator<Attack> ATTACKSBYDESCENDINGBONUS=(a,b)->b.bonus
      -a.bonus;

  /** @see PowerAttack */
  public boolean powerful=false;
  /** @see RapidShot */
  public boolean rapid=false;

  /** Constructor. */
  public AttackSequence(){}

  /** Copy constructor. */
  public AttackSequence(Collection<Attack> attacks){
    super(attacks);
  }

  @Override
  public String toString(){
    return Javelin.group(stream().map(Attack::toString).toList());
  }

  @Override
  public AttackSequence clone(){
    return (AttackSequence)super.clone();
  }

  /** Guarantees that attacks are sorted by descending {@link Attack#bonus}. */
  public void sort(){
    sort(ATTACKSBYDESCENDINGBONUS);
  }
}
