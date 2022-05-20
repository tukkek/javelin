package javelin.model.world.location.town.labor.religious;

import java.util.ArrayList;

import javelin.controller.content.kit.Cleric;
import javelin.controller.content.kit.Paladin;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.model.world.location.Location;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.academy.Guild;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * {@link Academy} for {@link Cleric} and (after upgraded) {@link Paladin}s.
 *
 * @author alex
 */
public class Sanctuary extends Guild{
  static final String CATHEDRAL="Cathedral";
  static final int MAXUPGRADES=15;

  /**
   * {@link Town} {@link Labor}.
   *
   * @author alex
   */
  public static class BuildSanctuary extends BuildAcademy{
    /** Constructor. */
    public BuildSanctuary(){
      super(Rank.VILLAGE);
    }

    @Override
    protected Academy generateacademy(){
      return new Sanctuary();
    }
  }

  static class UpǵradeSanctuary extends BuildingUpgrade{
    public UpǵradeSanctuary(Location previous){
      super(CATHEDRAL,getupgradecost(previous),getupgradecost(previous),
          previous,Rank.TOWN);
    }

    static int getupgradecost(Location previous){
      var s=(Sanctuary)previous;
      return Math.max(MAXUPGRADES,
          s.upgrades.size()+Paladin.INSTANCE.getupgrades().size());
    }

    @Override
    public Location getgoal(){
      return previous;
    }

    @Override
    protected void done(Location goal){
      super.done(goal);
      var s=(Sanctuary)goal;
      s.upgrade();
    }
  }

  boolean upgraded=false;

  /** Constructor. */
  public Sanctuary(){
    super("Sanctuary",Cleric.INSTANCE);
  }

  /** Turn sanctuary into cathedral. */
  public void upgrade(){
    upgrades.addAll(Paladin.INSTANCE.getupgrades());
    while(upgrades.size()>MAXUPGRADES){
      var u=RPG.pick(new ArrayList<>(upgrades));
      if(u instanceof ClassLevelUpgrade) continue;
      upgrades.remove(u);
    }
    upgraded=true;
    descriptionknown=CATHEDRAL;
    descriptionunknown=CATHEDRAL;
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    var upgrades=super.getupgrades(d);
    if(!upgraded) upgrades.add(new UpǵradeSanctuary(this));
    return upgrades;
  }

  @Override
  protected void generatehires(){
    if(upgraded) kit=RPG.chancein(2)?Cleric.INSTANCE:Paladin.INSTANCE;
    super.generatehires();
  }
}
