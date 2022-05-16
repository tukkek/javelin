package javelin.controller.content.terrain.hazard;

import javelin.Javelin;
import javelin.model.transport.Carriage;
import javelin.model.transport.Transport;
import javelin.model.unit.Squad;

/**
 * {@link Carriage}s are fragile vehicles and prone to breaking.
 *
 * @author alex
 */
public class Break extends Hazard{

  @Override
  public void hazard(int hoursellapsed){
    Javelin.message("The rough terrain damages your carriage!",true);
    Squad.active.transport=null;
    Squad.active.updateavatar();
  }

  @Override
  public boolean validate(){
    return Transport.CARRIAGE.equals(Squad.active.transport);
  }
}
