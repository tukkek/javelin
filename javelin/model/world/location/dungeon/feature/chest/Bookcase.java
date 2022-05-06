package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.table.dungeon.ChestTable;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * @see Scroll
 * @author alex
 */
public class Bookcase extends Chest{
  static final List<Recipe> RECIPES=Item.NONPRECIOUS.stream()
      .filter(i->!(i instanceof RuneGear)).map(Recipe::new)
      .collect(Collectors.toList());
  static final String INSTRUCTIONS="Use this recipe near a shop to allow the shop to craft the item on demand.";

  /** Adds an {@link Item} to a {@link Shop}'s {@link ItemSelection}. */
  public static class Recipe extends Item{
    /** The Item to add to a {@link Shop}. */
    public Item product;

    Recipe(Item i){
      super("Recipe for "+i.name.toLowerCase(),i.price*10,false);
      product=i.clone();
      product.identified=true;
      consumable=true;
      usedinbattle=false;
      usedoutofbattle=true;
      targeted=false;
    }

    Shop findshop(){
      if(WorldScreen.current.getClass()!=WorldScreen.class) return null;
      var s=Squad.active;
      var d=s.getdistrict();
      if(d==null) return null;
      return (Shop)d.getlocationtype(Shop.class).stream()
          .filter(shop->shop.distanceinsteps(s.x,s.y)<=1).findAny()
          .orElse(null);
    }

    @Override
    public boolean usepeacefully(Combatant user){
      var shop=findshop();
      if(shop==null) return false;
      shop.add(product);
      var t=shop.getdistrict().town.toString();
      var success=String.format("Added to the %s %s:\n%s!",t,
          shop.toString().toLowerCase(),product.name);
      Javelin.message(success,true);
      return true;
    }

    @Override
    public String describefailure(){
      return INSTRUCTIONS;
    }
  }

  /**
   * Bookcase that can only contain {@link Recipe}s. Not included in
   * {@link ChestTable} as it would be redundant with {@link Bookcase}.
   */
  public static class RecipeBookcase extends Bookcase{
    /** Constructor. */
    public RecipeBookcase(Integer gold,DungeonFloor f){
      super(gold,f);
    }

    @Override
    protected List<Item> getcandidates(){
      return new ArrayList<>(RECIPES);
    }

    @Override
    protected boolean allow(Item i){
      return i.is(Recipe.class)!=null;
    }
  }

  /** Constructor. */
  public Bookcase(Integer gold,DungeonFloor f){
    super(gold,f);
  }

  @Override
  protected boolean allow(Item i){
    return i.is(Scroll.class)!=null;
  }

  @Override
  public Image getimage(){
    return Images.get(List.of("dungeon","chest","bookcase"));
  }
}
