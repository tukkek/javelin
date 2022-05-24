package javelin.model.world.location.town.labor.basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.comparator.OptionsByPriority;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.consumable.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bane;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.abilities.spell.evocation.MagicMissile;
import javelin.model.unit.abilities.spell.transmutation.Longstrider;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.order.CraftingOrder;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;
import javelin.view.screen.town.SelectScreen;

/**
 * A {@link World} location where you can buy and sell a selection of
 * {@link Item}s.
 *
 * @see ShoppingScreen
 * @author alex
 */
public class Shop extends Location{
  static final Option SELL=new Option("Sell items",0,'s');

  class SellingScreen extends SelectScreen{
    HashMap<Option,Item> selling=new HashMap<>();
    int buylimit=0;

    public SellingScreen(){
      super("Sell which items?",null);
      var d=getdistrict();
      if(d!=null){
        buylimit=RewardCalculator.getgold(d.town.population);
        buylimit=Javelin.round(buylimit);
      }
    }

    @Override
    public String getCurrency(){
      return "$";
    }

    @Override
    public String printinfo(){
      var limit=Javelin.format(buylimit);
      var info="The shop will pay at most $"+limit+" for an item.\n";
      return info+"Your squad has $"+Javelin.format(Squad.active.gold)+".";
    }

    @Override
    public List<Option> getoptions(){
      var options=new ArrayList<Option>();
      for(var c:Squad.active.members)
        for(var i:Squad.active.equipment.get(c)) if(i.sell()){
          var listing="["+c+"] "+i.describe(c);
          var sellingprice=Math.round(Math.min(buylimit,i.price*i.sellvalue));
          if(!i.identified) sellingprice=1;
          var o=new Option(listing,sellingprice);
          selling.put(o,i);
          options.add(o);
        }
      return options;
    }

    @Override
    public boolean select(Option o){
      Squad.active.gold+=o.price;
      Squad.active.equipment.remove(selling.get(o));
      return true;
    }
  }

  /**
   * {@link Town} {@link Labor}.
   *
   * @author alex
   */
  public static class BuildShop extends Build{
    Class<? extends Shop> type;

    /** Constructor. */
    public BuildShop(){
      super("Build",5,Rank.HAMLET,null);
      var g=getgoal();
      name+=" "+g.toString().toLowerCase();
      type=g.getClass();
    }

    @Override
    public Shop getgoal(){
      return new Shop();
    }

    @Override
    public boolean validate(District d){
      return super.validate(d)&&d.getlocationtype(type).isEmpty();
    }
  }

  class ShowShop extends ShoppingScreen{
    Shop s;

    ShowShop(Shop s){
      super("You enter the shop.",null);
      this.s=s;
    }

    @Override
    protected ItemSelection getitems(){
      return selection;
    }

    @Override
    protected void afterpurchase(PurchaseOption o){
      s.crafting.add(new CraftingOrder(o.i,crafting));
    }

    @Override
    public String printinfo(){
      var info=super.printinfo();
      if(!crafting.queue.isEmpty())
        info+="\n\nCurrently crafting: "+crafting+'.';
      return info;
    }

    @Override
    public String printpriceinfo(Option o){
      return o.price==0?"":super.printpriceinfo(o);
    }

    @Override
    public List<Option> getoptions(){
      var options=super.getoptions();
      if(cansell()){
        SELL.priority=0;
        options.add(SELL);
      }
      return options;
    }

    private boolean cansell(){
      if(getdistrict()==null) return false;
      for(ArrayList<Item> bag:Squad.active.equipment.values())
        for(Item i:bag) if(i.sell()) return true;
      return false;
    }

    @Override
    public boolean select(Option o){
      if(o==SELL){
        new SellingScreen().show();
        return true;
      }
      return super.select(o);
    }

    @Override
    protected Comparator<Option> sort(){
      return (a,b)->{
        if(!(a instanceof PurchaseOption itema
            &&b instanceof PurchaseOption itemb))
          return OptionsByPriority.INSTANCE.compare(a,b);
        var difference=Math.round(Math.round(itemb.price-itema.price));
        return difference==0?a.name.compareTo(b.name):difference;
      };
    }
  }

  class UpgradeShop extends BuildingUpgrade{
    public UpgradeShop(Shop s,int newlevel){
      super("",5,newlevel,s,Rank.HAMLET);
      name="Upgrade "+description.toLowerCase();
    }

    @Override
    public Location getgoal(){
      return previous;
    }

    @Override
    public boolean validate(District d){
      return cost>0&&crafting.queue.isEmpty()&&super.validate(d);
    }

    @Override
    public void done(){
      super.done();
      level=upgradelevel;
      stock();
    }
  }

  /** Items for sale. */
  protected ItemSelection selection=new ItemSelection();
  /** Roughly equivalent to {@link Rank#rank}. */
  protected int level=Rank.HAMLET.rank;

  OrderQueue crafting=new OrderQueue();

  /**
   * @param r Determines selection of {@link Item}s sold.
   * @see Realm#getitems()
   */
  protected Shop(Integer level){
    super("Shop");
    if(level!=null) this.level=level;
    allowentry=false;
    discard=false;
    gossip=true;
    stock();
  }

  /** Constructor. */
  public Shop(){
    this(null);
  }

  /**
   * Adds more items to this shop's {@link #selection} when first built or when
   * upgrading.
   */
  protected void stock(){
    var tier=Tier.TIERS.get(level-1);
    var items=Item.randomize(Item.BYTIER.get(tier));
    for(var i:filter(items)){
      if(selection.size()>=tier.maxlevel) break;
      selection.add(i);
    }
  }

  /**
   * @return A version of the {@link #stock()} candidates with any undesired
   *   elements removed.
   */
  protected List<Item> filter(List<Item> items){
    items.retainAll(Item.NONPRECIOUS);
    return items;
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }

  @Override
  public boolean interact(){
    if(!super.interact()
        ||!isopen(List.of(Period.MORNING,Period.AFTERNOON),this))
      return false;
    for(Order o:crafting.reclaim(Period.gettime())){
      var done=(CraftingOrder)o;
      done.item.grab();
    }
    new ShowShop(this).show();
    return true;
  }

  @Override
  public boolean hascrafted(){
    return !ishostile()&&crafting.reportanydone();
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    var upgrades=super.getupgrades(d);
    if(d.town.getrank().rank>level) upgrades.add(new UpgradeShop(this,level+1));
    return upgrades;
  }

  @Override
  public boolean isworking(){
    return !crafting.queue.isEmpty()&&!crafting.reportalldone();
  }

  @Override
  public boolean canupgrade(){
    return super.canupgrade()&&crafting.isempty();
  }

  /** @param i Add this as a {@link PurchaseOption}. */
  public void add(Item i){
    selection.add(i);
  }

  /**
   * Starting {@link Location} with a fixed selection of {@link Tier#LOW} items
   * to prevent players from restarting new games until they end up with a
   * perceived "optimal" choice of items (thus encouraging them to bore
   * theselves, which goes against the DCSS philosophy document). This can still
   * be upgraded as a normal shop later on and as such the initial selection
   * must be kept fairly small.
   *
   * TODO ideally have a {@link Potion}, a {@link Scroll}, an {@link Eidolon}
   * and two other distinct item types (a grenade)... At that point Scroll could
   * be {@link Bane}, which feels the most "arcane" while now it's a
   * defensive/offensive pair.
   *
   * @see Academy#makebasic()
   */
  static public Shop makebasic(){
    var eidolons=Item.randomize(Item.NONPRECIOUS.stream()
        .filter(i->i.price<100&&i instanceof Eidolon).limit(5).toList());
    var items=new ItemSelection(List.of(RPG.pick(eidolons)));
    for(var spell:List.of(new CureLightWounds(),new Longstrider()))
      items.add(new Potion(spell));
    for(var spell:List.of(new Bless(),new MagicMissile()))
      items.add(new Scroll(spell));
    for(var i:items) i.identified=true;
    items.sort();
    var s=new Shop();
    s.selection=items;
    return s;
  }
}
