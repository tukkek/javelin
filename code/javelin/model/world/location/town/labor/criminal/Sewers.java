package javelin.model.world.location.town.labor.criminal;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Monsters will inhabit the sewers periodically, can be upgraded for tougher
 * fights.
 *
 * @author alex
 */
public class Sewers extends Fortification{
  static final String SEWERS="Sewers";

  /**
   * {@link Town} laabor.
   *
   * @author alex
   */
  public static class BuildSewers extends Build{
    /** Constructor. */
    public BuildSewers(){
      super("Build sewers",5,Rank.HAMLET,null);
    }

    @Override
    public Location getgoal(){
      return new Sewers();
    }

    @Override
    public boolean validate(District d){
      return super.validate(d)&&d.getlocation(Sewers.class)==null;
    }

    @Override
    protected void done(Location goal){
      super.done(goal);
      var s=(Sewers)goal;
      s.populate();
    }
  }

  class UpgradeSewers extends BuildingUpgrade{
    Sewers s;

    public UpgradeSewers(Sewers s){
      super("",5,5,s,Rank.RANKS[s.level+1]);
      this.s=s;
      name="Upgrade sewers";
    }

    @Override
    public void done(){
      super.done();
      s.level+=1;
      s.populate();
    }

    @Override
    public Location getgoal(){
      return s;
    }

    @Override
    public boolean validate(District d){
      return super.validate(d)&&!s.ishostile();
    }
  }

  int level=0;

  /** Constructor. */
  public Sewers(){
    super(SEWERS,SEWERS,1,5);
    terrain.add(Terrain.UNDERGROUND);
    sacrificeable=false;
  }

  void populate(){
    var level=1+this.level*5;
    generategarrison(level,level+4);
  }

  @Override
  public void turn(long time,WorldScreen world){
    super.turn(time,world);
    if(garrison.isEmpty()&&RPG.chancein(30)) populate();
  }

  @Override
  public List<Combatant> getcombatants(){
    return garrison;
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    var upgrades=super.getupgrades(d);
    if(level<3) upgrades.add(new UpgradeSewers(this));
    return upgrades;
  }

  @Override
  public boolean interact(){
    try{
      if(!super.interact()) return false;
    }catch(StartBattle e){
      e.fight.map=Terrain.UNDERGROUND.getmap();
      throw e;
    }
    Javelin.message("The sewers are empty and safe right now...",false);
    return true;
  }
}
