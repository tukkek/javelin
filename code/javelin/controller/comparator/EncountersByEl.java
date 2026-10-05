package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatants;

/**
 * Compares in increasing Encounter Level order.
 *
 * @author alex
 */
public class EncountersByEl implements Comparator<Combatants>{
  /** Singleton. */
  public static final EncountersByEl INSTANCE=new EncountersByEl();

  EncountersByEl(){
    // singleton
  }

  @Override
  public int compare(Combatants a,Combatants b){
    return Integer.compare(ChallengeCalculator.calculateel(a),
        ChallengeCalculator.calculateel(b));
  }
}
