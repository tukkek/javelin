package javelin.model.world.location.town.labor.expansive;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.terrain.Terrain;
import javelin.model.transport.FlyingNimbus;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.transmutation.OverlandFlight;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.view.Images;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

/**
 * {@link Transport} hub.
 *
 * @author alex
 */
public class Docks extends Location{
  static final Option REFUND=new Option("Return vehicle",0,'r');
  static final Option BLESSING=new Option("Blessing of speed",0,'b');
  static final Option SHIP=new Option("Ship",Transport.SHIP.price,'s');
  static final Option AIRSHIP=new Option("Airship",Transport.AIRSHIP.price,'a');

  class ShowTransport extends PurchaseScreen{
    Squad s=Squad.active;
    Transport current=s.transport instanceof FlyingNimbus?null:s.transport;
    int refund=current==null?0:Javelin.round(current.price*.9);

    public ShowTransport(){
      super("You enter the "+description.toLowerCase()+":",null);
      stayopen=false;
    }

    @Override
    public List<Option> getoptions(){
      if(s.transport!=null)
        return current==null?Collections.EMPTY_LIST:List.of(REFUND);
      BLESSING.price=Javelin
          .round(Shrine.price(OverlandFlight.INSTANCE)*s.members.size());
      var options=new ArrayList<>(List.of(REFUND,BLESSING));
      if(Terrain.search(getlocation(),Terrain.WATER,1,World.getseed())>0)
        options.add(SHIP);
      if(airdock) options.add(AIRSHIP);
      return options;
    }

    boolean refund(){
      if(current==null){
        print(text+"\nYou haven't rented a vehicle...");
        return false;
      }
      s.setlocation(getlocation());
      s.transport=null;
      s.gold+=refund;
      s.updateavatar();
      return true;
    }

    boolean bless(){
      for(var m:s.members) OverlandFlight.INSTANCE.castpeacefully(null,m);
      Javelin.message("Your units are blessed with flight for 24 hours!",true);
      return true;
    }

    @Override
    public boolean select(Option o){
      if(!super.select(o)) return false;
      if(o==REFUND) return refund();
      if(o==BLESSING) return bless();
      if(o==SHIP) s.transport=Transport.SHIP;
      else if(o==AIRSHIP) s.transport=Transport.AIRSHIP;
      else throw new UnsupportedOperationException("Unknown option");
      s.setlocation(getlocation());
      s.updateavatar();
      return true;
    }

    @Override
    public String printpriceinfo(Option o){
      if(o!=REFUND) return super.printpriceinfo(o);
      if(current==null) return " (90% refund upon vehicle return)";
      return " (refund $%s)".formatted(Javelin.format(refund));
    }
  }

  /** {@link Town} {@link Labor}. */
  public static class BuildAirdock extends Build{
    /** Constructor. */
    public BuildAirdock(){
      super("Build airdock",20,Rank.CITY,null);
    }

    BuildAirdock(String name,int cost,Rank r){
      super(name,cost,r,null);
    }

    @Override
    public Location getgoal(){
      var h=new Docks();
      h.upgrade();
      return h;
    }

    @Override
    public boolean validate(District d){
      return super.validate(d)&&d.getlocationtype(Docks.class).isEmpty()
          &&getsitelocation()!=null;
    }
  }

  /** {@link Town} {@link Labor}. **/
  public static class BuildDocks extends BuildAirdock{
    /** Constructor. */
    public BuildDocks(){
      super("Build docks",5,Rank.VILLAGE);
    }

    @Override
    public Location getgoal(){
      return new Docks();
    }

    @Override
    protected Point getsitelocation(){
      return town.getdistrict().getfreespaces().stream()
          .filter(f->Terrain.search(f,Terrain.WATER,1,World.getseed())>0)
          .findAny().orElse(null);
    }
  }

  class DocksUpgrade extends BuildingUpgrade{
    DocksUpgrade(){
      super("Upgrade docks to airdock",15,1,Docks.this,Rank.CITY);
    }

    @Override
    protected void define(){
      // already defined
    }

    @Override
    public void done(){
      super.done();
      var h=(Docks)previous;
      h.upgrade();
    }

    @Override
    public Location getgoal(){
      return previous;
    }
  }

  boolean airdock=false;

  /** Constructor. */
  public Docks(){
    super("Docks");
    allowentry=false;
    discard=false;
    gossip=true;
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }

  void upgrade(){
    description="Airdock";
    airdock=true;
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    new ShowTransport().show();
    return true;
  }

  @Override
  public Image getimage(){
    return Images.get(List.of("world",description.toLowerCase()));
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    return airdock?super.getupgrades(d)
        :new ArrayList<>(List.of(new DocksUpgrade()));
  }

  @Override
  public String describe(){
    return ishostile()?super.describe():description+".";
  }
}
