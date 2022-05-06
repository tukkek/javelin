package javelin.model.world.location.town.diplomacy.quest.fetch;

import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.chest.Bookcase.Recipe;
import javelin.model.world.location.dungeon.feature.chest.Bookcase.RecipeBookcase;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Shop;

/**
 * Fetch a {@link Recipe} from a {@link Dungeon}.
 *
 * @see Trait#MAGICAL
 * @see Dungeon#goals
 * @author alex
 */
public class FetchRecipe extends FetchQuest{
  /** Constructor. */
  public FetchRecipe(){
    super(RecipeBookcase.class);
    sellitem=false;
  }

  Shop getshop(){
    return (Shop)town.getdistrict().getlocation(Shop.class);
  }

  @Override
  public boolean validate(){
    return super.validate()&&getshop()!=null;
  }

  @Override
  protected boolean complete(){
    if(!super.complete()) return false;
    var r=(Recipe)goal;
    getshop().add(r.product);
    return true;
  }
}
