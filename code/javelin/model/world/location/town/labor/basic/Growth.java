package javelin.model.world.location.town.labor.basic;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * Adds 1 to {@link Town#population}.
 *
 * @author alex
 */
public class Growth extends Labor{
  /**
   * Ideally 20 should be the soft limit, but this offers a higher hard limit as
   * well.
   */
  public static final int MAXPOPULATION=30;
  /**
   * To be used for equality, such as for removing another instance from a list.
   *
   * @see Object#equals(Object)
   */
  public static final Growth INSTANCE=new Growth();

  /** Constructor. */
  public Growth(){
    super("Growth",-1,Rank.HAMLET);
  }

  @Override
  public void done(){
    town.population+=1;
    if(town.population>30) town.population=MAXPOPULATION;
  }

  @Override
  public boolean validate(District d){
    define();
    return super.validate(d)&&d.town.population<30;
  }

  @Override
  protected void define(){
    cost=town.population;
  }
}
