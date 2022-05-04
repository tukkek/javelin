package javelin.controller.content.fight.setup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.terrain.plain.Field;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.GaveUp;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Given a {@link Map}, a {@link Squad} and a {@link Fight} setups an initial
 * battle state.
 *
 * @author alex
 */
public class BattleSetup{
  /** 1 = strict pass, 2 = relaxed pass, 3 {@link Field} override. */
  static final int PLACEMENTSPASSES=3;
  static final int PLACEMENTATTEMPTS=150/PLACEMENTSPASSES;
  static final Point NOTPLACED=new Point(-1,-1);
  static final int MAXDISTANCE=6;

  /**
   * Improving battle placement by not allowing more than 1 adjacent ally at a
   * time (otherwise it's too easy to cramp an entire army together so that each
   * army won't see each other and then fulfill the other position requirements)
   */
  static final int MAXADJACENT=1;

  /** Exposes which placement pass the last setup ended on. */
  static public int pass;

  /** Starts the setup steps. */
  public void setup(){
    rollinitiative();
    var f=Fight.current;
    Fight.state.map=generatemap(f,null).map;
    for(pass=1;pass<=PLACEMENTSPASSES;pass++) try{
      if(pass==3) generatemap(f,new Field());
      place(pass!=2);
      Weather.flood();
      return;
    }catch(GaveUp e){
      if(pass<PLACEMENTSPASSES) continue;
      var info="\nMap: "+f.map.name;
      var blue=Fight.originalblueteam;
      info+="\nBlue team ("+blue.size()+"): "+blue;
      var red=Fight.originalredteam;
      info+="\nRed team ("+red.size()+"): "+red;
      throw new RuntimeException("Could not place combatants!"+info,e);
    }
  }

  /** Allows greater control of {@link Map} generation. */
  public Map generatemap(Fight f,Map m){
    if(f.map==null){
      var t=Dungeon.active==null?RPG.pick(f.terrains):Terrain.UNDERGROUND;
      f.map=t.getmap();
    }
    if(m!=null){
      if(f.map!=null){
        m.flooded=f.map.flooded;
        m.floor=f.map.floor;
        m.obstacle=f.map.obstacle;
      }
      f.map=m;
    }
    f.map.generate();
    return f.map;
  }

  /** Rolls initiative for each {@link Combatant}. */
  public void rollinitiative(){
    for(final Combatant c:Fight.state.getcombatants()){
      c.ap=0;
      c.rollinitiative();
    }
  }

  /**
   * Sets each {@link Combatant} in a sensible starting location.
   *
   * @param strict If <code>false</code> will relax some of the placement
   *   constraints.
   * @throws GaveUp If exceeded maximum allowed attempts.
   */
  protected void place(boolean strict) throws GaveUp{
    for(var i=0;i<PLACEMENTATTEMPTS;i++) try{
      place(Fight.state,strict);
      return;
    }catch(GaveUp e){
      continue;
    }
    throw new GaveUp();
  }

  void place(BattleState s,boolean strict) throws GaveUp{
    for(var c:s.getcombatants()) c.setlocation(NOTPLACED);
    var blueseed=RPG.chancein(2);
    var a=RPG.shuffle(new LinkedList<>(blueseed?s.blueteam:s.redteam));
    var b=RPG.shuffle(new LinkedList<>(blueseed?s.redteam:s.blueteam));
    var placeda=new ArrayList<Combatant>();
    var placedb=new ArrayList<Combatant>();
    var seeda=a.pop();
    var seedb=b.pop();
    seeda.setlocation(getrandompoint(s));
    placeda.add(seeda);
    placecombatant(seedb,seeda,null,null,s,true);
    placedb.add(seedb);
    while(!a.isEmpty()||!b.isEmpty()){
      var queue=RPG.chancein(2)?a:b;
      if(queue.isEmpty()) continue;
      var unit=queue.pop();
      var allies=queue==a?placeda:placedb;
      var enemies=queue==a?placedb:placeda;
      var success=false;
      for(var ally:RPG.shuffle(allies))
        if(placecombatant(unit,ally,allies,enemies,s,strict)){
          success=true;
          break;
        }
      if(!success) throw new GaveUp();
      allies.add(unit);
    }
  }

  boolean placecombatant(Combatant c,Combatant reference,
      ArrayList<Combatant> allies,List<Combatant> enemies,BattleState s,
      boolean strict){
    var source=reference.getlocation();
    var vision=reference.calculatevision(s);
    var all=s.getcombatants();
    for(var combatant:all) vision.remove(combatant.getlocation());
    for(var p:RPG.shuffle(new ArrayList<>(vision))){
      if(p.distanceinsteps(source)>MAXDISTANCE||s.map[p.x][p.y].blocked)
        continue;
      if(strict){
        if((allies!=null
            &&allies.stream().filter(a->a.getlocation().distanceinsteps(p)==1)
                .limit(MAXADJACENT+1).count()==MAXADJACENT)||(enemies!=null&&cansee(enemies,p))) continue;
      }
      c.setlocation(p);
      return true;
    }
    return false;
  }

  boolean cansee(List<Combatant> enemies,Point p){
    for(var enemy:enemies)
      if(Fight.state.haslineofsight(enemy,p)!=Vision.BLOCKED) return true;
    return false;
  }

  /**
   * @return A free spot inside the given coordinates. Will loop infinitely if
   *   given space is fully occupied.
   */
  static Point getrandompoint(final BattleState s){
    var width=s.map.length;
    var height=s.map[0].length;
    Point p=null;
    while(p==null||s.map[p.x][p.y].blocked||s.getcombatant(p.x,p.y)!=null)
      p=new Point(RPG.r(2,width-3),RPG.r(2,height-3));
    return p;
  }

  /** Place units, using the given points as hints. */
  public void place(List<Combatant> team,List<Point> spawn){
    var m=Fight.current.map;
    teamplacement:for(var c:RPG.shuffle(team)){
      for(var s:RPG.shuffle(new ArrayList<>(spawn))){
        var area=s.getadjacent();
        area.add(s);
        for(var p:RPG.shuffle(area)) if(Fight.state.isempty(p.x,p.y)){
          c.setlocation(p);
          spawn.add(p);
          continue teamplacement;
        }
      }
      var error="Couldn't place all combatants on map "+m.name;
      throw new RuntimeException(error);
    }
  }
}
