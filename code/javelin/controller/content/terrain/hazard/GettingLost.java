package javelin.controller.content.terrain.hazard;

import javelin.Javelin;
import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;

/**
 * A party that is lost will wander around and waste time.
 *
 * @author alex
 */
public class GettingLost extends Hazard{
  static final boolean ANNOY=false;

  int dc;

  /**
   * @param dc Survival check difficulty class.
   */
  public GettingLost(int dc){
    this.dc=dc;
  }

  @Override
  public boolean validate(){
    if(Squad.active.fly()||!Squad.active.lastterrain.equals(Terrain.current())||(Terrain.current().describeweather()!=Desert.SANDSTORM)) return false;
    return Squad.active.getbest(Skill.SURVIVAL).roll(Skill.SURVIVAL)<dc;
  }

  @Override
  public void hazard(int hoursellapsed){
    getlost(ANNOY?"Squad got lost!":null,hoursellapsed);
  }

  /**
   * {@link Squad#displace()} on the active squad.
   *
   * @param message Shows this message in a prompt.
   * @param hoursellapsed This many more hours will be spent.
   */
  public static void getlost(String message,int hoursellapsed){
    Squad.active.displace();
    Squad.active.place();
    Squad.active.delay(hoursellapsed);
    if(message!=null) Javelin.message(message,false);
  }
}
