package javelin.controller.content.scenario;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.event.urban.UrbanEvents;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.exception.UnbalancedTeams;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.old.RPG;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.WorldScreen;

/**
 * Main game mode, full on strategy-RPG.
 *
 * @author alex
 */
public class Campaign extends Scenario{
  /** Minimum starting party encounter level. */
  public static final float INITIALEL;

  static final ArrayList<Point> ALLTILES=new ArrayList<>();

  static{
    var crs=new ArrayList<Float>(4);
    for(var i=0;i<4;i++) crs.add(1f);
    try{
      INITIALEL=ChallengeCalculator.calculateelfromcrs(crs,false);
    }catch(UnbalancedTeams e){
      throw new RuntimeException(e);
    }
  }

  /** <code>true</code> to allow {@link Diplomacy}. */
  protected boolean diplomacy=true;

  /** Constructor. */
  public Campaign(){
    allowallactors=true;
    allowlabor=true;
    asksquadnames=true;
    desertradius=2;
    fogofwar=true;
    helpfile="Campaign";
    easystartingtown=true;
    minigames=true;
    normalizemap=false;
    record=true;
    respawnlocations=true;
    size=30;
    startingdungeons=20;
    statictowns=false;
    towns=Realm.REALMS.size();
    worldencounters=true;
    worldhazards=true;
    dominationwin=false;
    startingfeatures=Math.round(size*size/(5*1.5f));
    simpletroves=false;
    boost=1;
    randomrealms=false;
    quests=true;
    diplomacy=true;
    urbanevents=UrbanEvents.ENABLED;
  }

  @Override
  public void upgradesquad(Squad s){
    var members=s.members;
    var crsum=Math.round(Math.round(
        members.stream().collect(Collectors.summingDouble(c->c.source.cr))));
    var boost=1f/members.size();
    while(ChallengeCalculator.calculateelfromcrs(
        members.stream().map(c->c.source.cr+c.xp.floatValue())
            .collect(Collectors.toList()))<INITIALEL)
      for(var m:members) m.xp=m.xp.add(new BigDecimal(boost));
    for(var m:members){
      var classes=Kit.getpreferred(m.source,false).stream()
          .flatMap(k->k.getupgrades().stream())
          .filter(u->u instanceof ClassLevelUpgrade).collect(toList());
      if(classes.isEmpty()) continue;
      var c=RPG.pick(classes);
      while(m.xp.floatValue()>=1){
        c.upgrade(m,true);
        ChallengeCalculator.calculatecr(m.source);
      }
    }
    s.gold+=RewardCalculator.getgold(4-crsum);
  }

  @Override
  public boolean checkfullsquad(ArrayList<Combatant> squad){
    return ChallengeCalculator.calculateel(squad)>=INITIALEL;
  }

  @Override
  public boolean win(){
    return false;
  }

  @Override
  public Item openspecialchest(){
    return new Ruby();
  }

  @Override
  public void endday(){
    cover(2);
  }

  /** Covers an amount of {@link WorldTile} per day with fog of war. */
  void cover(int amount){
    if(Debug.showmap) return;
    if(ALLTILES.isEmpty()) ALLTILES.addAll(Point.getrange(0,0,size,size));
    var stayrevealed=new HashSet<Point>();
    var mappanel=WorldScreen.current.mappanel;
    for(Actor a:World.getactors()){
      if(!mappanel.tiles[a.x][a.y].discovered) continue;
      var point=a.getlocation();
      stayrevealed.add(point);
      var l=a instanceof Location?(Location)a:null;
      if(l==null) continue;
      var vision=l.watch();
      if(vision>0) for(var x=point.x-vision;x<=point.x+vision;x++)
        for(var y=point.y-vision;y<=point.y+vision;y++)
          stayrevealed.add(new Point(x,y));
    }
    for(Point p:RPG.shuffle(ALLTILES)){
      var t=mappanel.tiles[p.x][p.y];
      if(!t.discovered||World.seed.roads[t.x][t.y]||stayrevealed.contains(p))
        continue;
      t.cover();
      amount-=1;
      if(amount==0) break;
    }
    Squad.updatevision();
  }

  @Override
  public void populate(Town t,boolean starting){
    t.setgovernor(starting?new HumanGovernor(t):new MonsterGovernor(t));
    if(!starting) t.populate(RPG.r(1,4));
  }
}
