package javelin.model.unit.skill;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Dazzled;
import javelin.model.unit.feat.skill.Alertness;

/**
 * A mix of Search, Spot and Listen. Only use directly for basic perception
 * rolls that don't depent on {@link Weather}, {@link Javelin#getperiod()},
 * flying or similar modifiers.
 *
 * @see Squad#perceive(boolean, boolean, boolean)
 * @see Combatant#perceive(boolean, boolean, boolean)
 */
public class Perception extends Skill{
  static final String[] NAMES={"Perception","listen","spot","search"};

  Perception(){
    super(NAMES,Ability.WISDOM);
  }

  @Override
  public int getbonus(Combatant c){
    var bonus=super.getbonus(c);
    if(c.source.hasfeat(Alertness.SINGLETON)) bonus+=Alertness.BONUS;
    if(c.hascondition(Dazzled.class)!=null) bonus+=Dazzled.PENALTY;
    return bonus;
  }
}
