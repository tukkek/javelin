package javelin.model.world.location.town.labor.basic;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * TODO the constants are a mess, push {@link #TITLES} and {@link #IMAGES} and
 * {@link #LABOR} to {@link Lodging}
 *
 * @author alex
 */
public class Lodge extends Fortification{
  /** Smaller-tier lodge. */
  public static final Lodging LODGE=new Lodging("lodge",1,0);
  /** Medium-tier lodge. */
  public static final Lodging HOTEL=new Lodging("hotel",2,.5f);
  /** Highest-tier lodge. */
  public static final Lodging HOSPITAL=new Lodging("hospital",4,2);
  /** All tiers from lowest to biggest. */
  public static final Lodging[] LODGING={LODGE,HOTEL,HOSPITAL};
  /** More descriptive titles for {@link #LODGING} entries. */
  public static final String[] TITLES={"Traveller's lodge","Hotel","Hospital"};
  /** Avatar filenames for {@link #LODGING} tiers (without extension). */
  public static final String[] IMAGES={"inn","innhotel","innhospital"};
  /** Labor cost for each {@link #LODGING} tier. */
  public static final int[] LABOR={5,10,15};
  /** Standard amount of time for a rest. */
  public static final int RESTPERIOD=8;

  static final int WEEKLONGREST=24*7/RESTPERIOD;
  static final int MAXLEVEL=TITLES.length-1;

  static class Lodging{
    String name;
    private float fee;
    int quality;

    Lodging(String name,int quality,float fee){
      this.name=name;
      this.fee=fee;
      this.quality=quality;
    }

    public int getfee(){
      return fee==0?0:Math.round(Math.max(1,Squad.active.eat()*fee));
    }
  }

  /**
   * {@link Town} project.
   *
   * @author alex
   */
  public static class BuildLodge extends Build{
    /** Constructor. */
    public BuildLodge(){
      super("Build "+Lodge.TITLES[0].toLowerCase(),Lodge.LABOR[0],Rank.HAMLET,
          null);
    }

    @Override
    public Location getgoal(){
      return new Lodge();
    }

    @Override
    public boolean validate(District d){
      if(site==null
          &&(d.getlocation(Lodge.class)!=null||d.isbuilding(Lodge.class)))
        return false;
      return super.validate(d);
    }
  }

  class UpgradeLodge extends BuildingUpgrade{
    public UpgradeLodge(Lodge i){
      super(TITLES[i.level+1],LABOR[i.level+1],5,i,Rank.RANKS[i.level+1]);
    }

    @Override
    public Location getgoal(){
      return previous;
    }

    @Override
    public void done(Location l){
      var i=(Lodge)l;
      if(i.level<MAXLEVEL){
        i.level+=1;
        i.rename(TITLES[i.level]);
      }
      super.done(l);
    }

    @Override
    public boolean validate(District d){
      var i=(Lodge)previous;
      return d!=null&&i.level<MAXLEVEL&&super.validate(d);
    }
  }

  int level=0;

  /** Consstructor. */
  public Lodge(){
    super(TITLES[0],TITLES[0],1,5);
    gossip=true;
    neutral=true;
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    var price=LODGING[level].getfee();
    var weekprice=WEEKLONGREST*price;
    var s="Do you want to rest at the "+TITLES[level].toLowerCase()+"?\n";
    s+="\nENTER or s to stay ($"+price+"), w to stay for a week ($"+weekprice
        +")";
    s+="\nany other key to leave";
    var input=Javelin.prompt(s);
    if(input=='\n'||input=='s') return rest(price,level+1);
    if(input=='w') return rest(weekprice,WEEKLONGREST*(level+1));
    return false;
  }

  boolean rest(long price,int periods){
    if(Squad.active.gold<price){
      Javelin.message("You can't pay the $"+price+" fee!",false);
      return false;
    }
    Squad.active.gold-=price;
    rest(periods,RESTPERIOD*periods,true,LODGING[level]);
    return true;
  }

  @Override
  public List<Combatant> getcombatants(){
    return garrison;
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    var upgrades=super.getupgrades(d);
    if(level<MAXLEVEL) upgrades.add(new UpgradeLodge(this));
    return upgrades;
  }

  @Override
  public Image getimage(){
    return Images.get(List.of("world",IMAGES[level]));
  }

  /**
   * @param restperiods Normally 1 rest period equals to 8 hours of rest in
   *   normal conditions.
   * @param hours Number of hours elapsed.
   * @param accomodation Level of the resting environment.
   */
  public static void rest(int restperiods,int hours,boolean advancetime,
      Lodging l){
    var s=Squad.active;
    var factor=s.heal()>=15&&!l.equals(HOSPITAL)?2:1;
    for(var i=0;i<restperiods;i++){
      s.quickheal();
      for(var c:s) c.rest(factor,hours);
    }
    if(advancetime) s.delay(hours);
    identifyitems(s);
  }

  static void identifyitems(Squad s){
    var identified=new ArrayList<Item>(0);
    for(var i:s.equipment.getall()) if(!i.identified&&s.identify(i)){
      i.identified=true;
      identified.add(i);
    }
    if(!identified.isEmpty()){
      var message="The following items have been identified: %s!";
      message=String.format(message,Javelin.group(identified).toLowerCase());
      Javelin.message(message,true);
    }
  }

  /** @return {@link #LODGING} {@link #level}. */
  public Lodging lodge(){
    return LODGING[level];
  }
}
