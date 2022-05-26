package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.controller.Calendar;
import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.collection.WeightedList;
import javelin.controller.comparator.ActorsByDistance;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;

/**
 * A figure that travels from one {@link Town} to another. It can be visited to
 * buy {@link Item}s. If originating from a larger city than its destination,
 * once it reaches a human {@link Town} it grows by 1 {@link Town#population} -
 * this is an incentive for the player to protect merchants against
 * {@link Incursion}s.
 *
 * Unlike Shop {@link Item}s, these are not crafted but sold as-is, and as such
 * are removed after purchase.
 *
 * Caravans provide an important late-game funciton: they are a reliable, common
 * and varied source of purchasing {@link Item}s, since every {@link Fight} on
 * later {@link Tier}s gives a lot of gold but {@link Shop}s are usually a
 * long-term affair and {@link Dungeon} {@link Chest}s rewards are generally
 * poor compared to the amount of gold from {@link Encounter}s. This is why
 * Caravans are more likely to spawn from higher-rank Towns. Having a reocurring
 * selection of items to choose from is much more strategic as well than finding
 * random loot somewhere (althouth that has its own value as well).
 *
 * Caravans move slowly - only once every other day on average but they also can
 * instantly "jump" over other {@link Actor}s in their path (which can be
 * interpreted as them bivouacking for a day or two before moving on).
 *
 * TODO players should have the option to {@link Fight} the caravans for their
 * loot. It should be a {@link Difficulty#DEADLY} fight in realtion to even
 * {@link #level} but if they win, they take all of the {@link #inventory} and
 * probably some bad relations with the {@link Town} it was originally from.
 *
 * TODO could become a {@link Labor}
 *
 * @author alex
 */
public class Caravan extends Actor{
  static final boolean ALLOW=true;
  static final Map<Tier,Integer> FREQUENCY=new HashMap<>(4);

  static{
    FREQUENCY.put(Tier.LOW,Calendar.YEAR);
    FREQUENCY.put(Tier.MID,Calendar.SEASON);
    FREQUENCY.put(Tier.HIGH,Calendar.MONTH);
    FREQUENCY.put(Tier.EPIC,Calendar.WEEK);
  }

  class CaravanScreen extends ShoppingScreen{
    /** Constructor. */
    public CaravanScreen(){
      super("You reach a trading caravan:",null);
    }

    @Override
    protected void afterpurchase(PurchaseOption o){
      inventory.remove(o.i);
      o.i.clone().grab();
    }

    @Override
    protected ItemSelection getitems(){
      return inventory;
    }
  }

  /** Selection of {@link Item}s available for purchase. */
  public ItemSelection inventory=new ItemSelection();

  Town from=null;
  Point to=null;
  int level;

  /** Creates a merchant in the world map but doesn't {@link #place()} it. */
  public Caravan(Point p,int level){
    x=p.x;
    y=p.y;
    this.level=level;
    displace();
    to=determinedestination(Town.gettowns());
    stock();
  }

  /**
   * @see Town#getlocation()
   * @see Town#population
   */
  public Caravan(Town t){
    this(t.getlocation(),t.population);
    from=t;
    to=determinedestination(Town.gettowns());
  }

  void stock(){
    var all=new ArrayList<>(Item.NONPRECIOUS);
    var t=from==null?Tier.MID:Tier.get(from.population);
    var size=RPG.randomize(t.maxlevel/2,1,t.maxlevel);
    while(inventory.size()<size){
      var level=RPG.r(1,this.level);
      var min=RewardCalculator.getgold(level-1);
      var max=RewardCalculator.getgold(level+1);
      var item=Item.randomize(all).stream()
          .filter(i->min<=i.price&&i.price<=max).findAny().orElse(null);
      if(item==null) continue;
      item.identified=true;
      inventory.add(item);
    }
  }

  /** Reflection-friendly constructor. */
  public Caravan(){
    this(RPG.pick(Town.gettowns()));
  }

  Point determinedestination(List<Town> towns){
    if(towns!=null){
      towns.remove(from);
      towns=towns.stream().filter(t->!Incursion.crosseswater(this,t.x,t.y))
          .collect(Collectors.toList());
      if(!towns.isEmpty()){
        if(from==null) return RPG.pick(towns).getlocation();
        towns.sort(new ActorsByDistance(from).reversed());
        var chances=new WeightedList<>(towns).distribution;
        return RPG.pick(chances).getlocation();
      }
    }
    while(to==null||Incursion.crosseswater(this,to.x,to.y))
      to=new Point(RPG.r(World.SIZE),RPG.r(World.SIZE));
    return to;
  }

  @Override
  public void turn(long time,WorldScreen world){
    if(RPG.chancein(2)) move();
  }

  void move(){
    var x=this.x+calculatedelta(this.x,to.x);
    var y=this.y+calculatedelta(this.y,to.y);
    if(Terrain.get(x,y).equals(Terrain.WATER)){
      to=determinedestination(null);
      return;
    }
    var here=World.get(x,y,World.getactors());
    this.x=x;
    this.y=y;
    place();
    if(x==to.x&&y==to.y){
      remove();
      if(here instanceof Town) arrive((Town)here);
    }else if(here!=null) move();
  }

  void arrive(Town t){
    if(from==null||t.population>=from.population||t.ishostile()) return;
    t.population+=1;
    MessagePanel.active.clear();
    var notification="A caravan arrives at "+t+", city grows.";
    t.events.add(notification);
  }

  int calculatedelta(int from,int to){
    if(to==from) return 0;
    return to>from?+1:-1;
  }

  @Override
  public Boolean destroy(Incursion attacker){
    return Incursion.fight(attacker.getel(),level);
  }

  @Override
  public boolean interact(){
    new CaravanScreen().show();
    return true;
  }

  @Override
  public Image getimage(){
    return Images.get(List.of("world","caravan"));
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }

  @Override
  public String describe(){
    return "Caravan.";
  }

  @Override
  public Integer getel(){
    return level;
  }

  /** To be called daily from every Town. */
  public static void spawn(Town t){
    if(!ALLOW||!RPG.chancein(FREQUENCY.get(Tier.get(t.population)))) return;
    var c=new Caravan(t);
    c.place();
    c.displace();
    if(!t.getdistrict().getsquads().isEmpty())
      t.events.add(String.format("A caravan leaves %s!",t));
  }
}
