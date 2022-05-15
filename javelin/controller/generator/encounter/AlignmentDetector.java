package javelin.controller.generator.encounter;

import java.util.List;

import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;

/**
 * Checks if a group of {@link Combatant}s is aligned enough to work as a team.
 *
 * @author alex
 */
public class AlignmentDetector{
  public boolean good=false;
  public boolean evil=false;
  public boolean lawful=false;
  public boolean chaotic=false;

  /** @param units All combatants that need to be compatible. */
  public AlignmentDetector(List<Combatant> units){
    for(Combatant c:units) register(c);
  }

  void register(Combatant c){
    var a=c.source.alignment;
    if(a.morals!=Morals.NEUTRAL) if(a.isgood()) good=true;
    else evil=true;
    if(a.ethics!=Ethics.NEUTRAL) if(a.islawful()) lawful=true;
    else chaotic=true;
  }

  /**
   * @return <code>false</code> if there are good and evil (or lawful and
   *   chaotic) creatures coexisting on the given group. <code>true</code> if
   *   all alignments match.
   */
  public boolean check(){
    if(good&&evil||lawful&&chaotic) return false;
    return true;
  }

  /**
   * @return <code>true</code> if {@link #good} and {@link #evil} or
   *   {@link #lawful} and {@link #chaotic} are actively opposing each other.
   */
  public boolean antagonize(AlignmentDetector enemy){
    return good&&enemy.evil||evil&&enemy.good||lawful&&enemy.chaotic
        ||chaotic&&enemy.evil;
  }

  /** @see #antagonize(AlignmentDetector) */
  public static boolean antagonize(Combatants groupa,Combatants groupb){
    var a=new AlignmentDetector(groupa);
    return a.antagonize(new AlignmentDetector(groupb));
  }
}
