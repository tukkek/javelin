package javelin.model.world.location.unique;

import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;

/**
 * An unique location is a {@link Location} that is only featured in a single
 * location in the {@link World} map during a game. They are usually places of
 * high power, which makes them {@link Location#impermeable} by default.
 *
 * @author alex
 */
public abstract class UniqueLocation extends Fortification{
  /**
   * Constructor. See {@link Location#WorldPlace(String)}.
   *
   * @param minel
   * @param maxel
   */
  public UniqueLocation(String descriptionknown,String descriptionunknown,
      int minel,int maxel){
    super(descriptionknown,descriptionunknown,minel,maxel);
    impermeable=true;
    allowentry=false;
    gossip=true;
    vision=0;
    unique=true;
  }

  @Override
  public Boolean destroy(Incursion attacker){
    return impermeable?Incursion.ignore(attacker):super.destroy(attacker);
  }

  @Override
  public Integer getel(){
    return impermeable?null:super.getel();
  }

}
