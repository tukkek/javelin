package javelin.model.world.location.town.diplomacy.mandate;

import javelin.model.world.Caravan;
import javelin.model.world.location.town.Town;

/** @see Caravan */
public class RequestCaravan extends Mandate{
  /** Constructor. */
  public RequestCaravan(Town t){
    super(t);
  }

  @Override
  public String getname(){
    return "Request caravan";
  }

  @Override
  public boolean validate(){
    return town.population>1;
  }

  @Override
  public void act(){
    Caravan.spawn(town,true);
  }
}
