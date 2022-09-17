package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.EncountersByEl;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.template.Template;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.db.EncounterIndex;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.table.Table;
import javelin.controller.table.Tables;
import javelin.controller.table.dungeon.ChestTable;
import javelin.controller.table.dungeon.door.DoorExists;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.table.dungeon.feature.DecorationTable;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.controller.table.dungeon.feature.FeatureRarityTable;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.controller.table.dungeon.feature.SpecialTrapTable;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.feature.BranchPortal;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.Crate;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.mappanel.dungeon.DungeonTile;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;

/**
 * A floor of a {@link Dungeon}.
 *
 * @author alex
 */
public class DungeonFloor implements Serializable{
  static final Class<? extends Feature> DEBUGFEATURE=null;
  static final int ENCOUNTERATTEMPTS=1000;

  /**
   * A loose approximation of how many {@link DungeonTile}s are revealed with
   * each step. Multiply by {@link Dungeon#vision}.
   */
  protected static final int DISCOVEREDPERSTEP=4;

  /** All of this dungeon's {@link Feature}s. */
  public Features features=new Features(this);
  /**
   * Explored squares in this dungeon.
   *
   * TODO why is there this and also {@link #discovered}?
   */
  public boolean[][] visible;
  /** Current {@link Squad} location. */
  public Point squadlocation=null;
  /** Tiles already revealed. */
  public HashSet<Point> discovered=new HashSet<>();
  /** @see FloorTile */
  public char[][] map=null;
  /**
   * {@link #map} size (width and height).
   *
   * TODO support non-square maps
   */
  public int size;
  /** @see #generate(List) */
  public int stepsperencounter;
  /** Dungeon encounter level. -1 if not initialized. */
  public int level=-1;
  /**
   * Table of {@link Encounter}s to roll from when generating
   * {@link RandomDungeonEncounter}s.
   *
   * Entries can be set to <code>null</code> when certain encounters are
   * pacified. If rolled, these will result in skipped encounters (ie: the
   * {@link Squad} met them but they weren't hostile).
   *
   * @see Leader
   * @see Dungeon#fight()
   */
  public List<Combatants> encounters=new ArrayList<>();
  /** Dungeon this floor is a part of. */
  public Dungeon dungeon;

  int knownfeatures=0;
  Tables tables;

  transient Integer nrooms=1;

  /** Constructor for top floor. */
  public DungeonFloor(Integer level,Dungeon d){
    this.level=level;
    dungeon=d;
  }

  /**
   * This function generates the dungeon map using {@link DungeonGenerator} and
   * then {@link #populate()}. One notable thing that happens here is the
   * determination of how many {@link RandomDungeonEncounter}s should take for
   * the player to explore the whole level.
   *
   * Currently, the calculation is done by setting a goal of one fight per room
   * on average (so naturally, larger {@link DungeonTier}s will have more fights
   * than smaller ones). The formula takes into account
   * {@link DungeonScreen#VIEWRADIUS} instead of counting each step as a single
   * tile.
   *
   * Since a Squad of the dungeon's intended level cannot hope to clear a
   * dungeon if it's large (in average they can only take 4-5 encounters of the
   * same EL), this is then offset by placing {@link Feature}s like
   * {@link Fountain}s and {@link Campfire}s that would theoretically allow them
   * to do the floor in one go - not counting backtracking out of the dungeon or
   * finding their way back to {@link Town} safely, so this naturally makes the
   * dungeon more challenging (hopefully being offset by the cool rewards).
   *
   * @param index {@link Encounter}s, possibly {@link Template}-based.
   *
   * @see #stepsperencounter
   */
  public void generate(List<EncounterIndex> index){
    if(map!=null) return;
    var p=getparent();
    if(p==null) p=dungeon.exit;
    tables=p==null?new Tables():p.tables.clone();
    map=map();
    size=map.length;
    generatedoors();
    stepsperencounter=calculateencounterrate();
    if(stepsperencounter<2) stepsperencounter=2;
    generateencounters(index);
    populate();
    visible=new boolean[size][size];
    for(var x=0;x<size;x++) for(var y=0;y<size;y++) visible[x][y]=false;
  }

  /** @return Floor above this one or <code>null</code> if top floor. */
  protected DungeonFloor getparent(){
    var floor=getdepth()-1;
    return floor==0?null:dungeon.floors.get(floor-1);
  }

  /**
   * @see DungeonGenerator
   * @see #map
   */
  protected char[][] map(){
    var t=gettier();
    if(dungeon.exit!=null){
      t=dungeon.exit.dungeon.gettier();
      var smaller=DungeonTier.TIERS.indexOf(t)-1;
      t=DungeonTier.TIERS.get(Math.max(0,smaller));
    }
    var generator=DungeonGenerator.generate(t.minrooms,t.maxrooms,this);
    nrooms=generator.map.rooms.size();
    return generator.grid;
  }

  /** * Defines {@link #encounters}. */
  protected void generateencounters(List<EncounterIndex> index){
    var p=getparent();
    if(p!=null){
      encounters=new ArrayList<>(p.encounters);
      while(encounters.remove(null)) continue;
      encounters.sort(EncountersByEl.INSTANCE);
      var crop=RPG.r(1,encounters.size());
      encounters.removeAll(encounters.subList(0,crop));
    }
    var target=DungeonTier.TIERS.indexOf(gettier())+RPG.randomize(6,4,7);
    for(var i=0;encounters.size()<target;i++){
      var el=level+Difficulty.get();
      var e=EncounterGenerator.generatebyindex(el,index);
      if(e!=null) encounters.add(e);
      if(i>ENCOUNTERATTEMPTS){
        if(!Javelin.DEBUG) return;
        var error="Cannot generate encounters for %s [EL %s]!";
        throw new RuntimeException(String.format(error,this,level));
      }
    }
  }

  void generatedoors(){
    for(var x=0;x<size;x++) for(var y=0;y<size;y++)
      if(map[x][y]==FloorTile.DOOR&&gettable(DoorExists.class).rollboolean()){
        var d=Door.generate(this,new Point(x,y));
        if(d!=null) d.place(this,d.getlocation());
      }
  }

  /**
   * Tries to come up with a number roughly similar to what you'd have if you
   * explored all rooms, fought all monsters and then left (plus a 10% random
   * encounter chance).
   *
   * @return {@link #stepsperencounter}.
   */
  protected int calculateencounterrate(){
    var encounters=nrooms*1.1f*dungeon.ratiomonster;
    var tilesperroom=countfloor()/nrooms;
    var steps=encounters*tilesperroom/(DISCOVEREDPERSTEP*dungeon.vision);
    return Math.round(steps*2);
  }

  int countfloor(){
    var floortiles=0;
    for(var x=0;x<size;x++)
      for(var y=0;y<size;y++) if(map[x][y]==FloorTile.FLOOR) floortiles+=1;
    return floortiles;
  }

  /** @return Generated decoration or <code>null</code> if Dungeon doesn't. */
  protected LinkedList<Decoration> generatedecoration(int minimum){
    var unnocupied=new ArrayList<Point>(size*size/10);
    for(var x=0;x<size;x++) for(var y=0;y<size;y++){
      var p=new Point(x,y);
      if(!isoccupied(p)) unnocupied.add(p);
    }
    RPG.shuffle(unnocupied);
    var target=RPG.randomize(gettier().minrooms,minimum,unnocupied.size());
    var d=new LinkedList<Decoration>();
    var t=gettable(DecorationTable.class);
    for(var i=0;i<target;i++){
      var f=new Decoration((String)t.roll(),this);
      d.add(f);
      f.place(this,unnocupied.get(i));
    }
    return d;
  }

  /** Generates {@link BranchPortal}s. */
  protected void generatebranches(DungeonZoner z){
    while(RPG.chancein(dungeon.branchchance)){
      var p=z.getpoint();
      if(p!=null) new BranchPortal(this).place(this,p);
    }
  }

  /** Place {@link Features}s and defines {@link #squadlocation}. */
  protected void populate(){
    /* if placed too close to the edge, the carving in #createstairs() will make
     * it look weird as the edge will look empty without walls */
    while(squadlocation==null||!squadlocation.validate(2,2,size-3,size-3))
      squadlocation=getunnocupied();
    var zoner=new DungeonZoner(this,squadlocation);
    generatestairs(zoner);
    generatekeys(zoner);
    var ntraps=getfeaturequantity(nrooms,dungeon.ratiotraps);
    var ntreasure=getfeaturequantity(nrooms,dungeon.ratiotreasure);
    var d=generatedecoration(ntraps+ntreasure+1);
    var traps=generatetraps(ntraps,d);
    var pool=0;
    for(var t:traps) pool+=RewardCalculator.getgold(t.cr);
    generatechests(ntreasure,pool,zoner,d);
    generatefeatures(getfeaturequantity(nrooms,dungeon.ratiofeatures),zoner);
    generatebranches(zoner);
  }

  void generatekeys(DungeonZoner zoner){
    var generated=new HashSet<Class<? extends Key>>();
    var area=new ArrayList<Point>();
    for(var z:zoner.zones){
      area.addAll(z.area);
      for(var door:z.doors){
        if(!door.locked||!generated.add(door.key)) continue;
        Point p=null;
        while(p==null||isoccupied(p)) p=RPG.pick(area);
        try{
          var k=door.key.getConstructor(DungeonFloor.class).newInstance(this);
          new Chest(k,this).place(this,p);
        }catch(ReflectiveOperationException e){
          throw new RuntimeException(e);
        }
      }
    }
  }

  /** * @return A random, free {@link DungeonTile}. */
  public Point getunnocupied(){
    Point p=null;
    while(p==null||isoccupied(p)) p=new Point(RPG.r(0,size-1),RPG.r(0,size-1));
    return p;
  }

  void generatestairs(DungeonZoner zoner){
    new StairsUp(squadlocation,this).place(this,squadlocation);
    for(var x=squadlocation.x-1;x<=squadlocation.x+1;x++)
      for(var y=squadlocation.y-1;y<=squadlocation.y+1;y++)
        map[x][y]=FloorTile.FLOOR;
    if(!isdeepest()) new StairsDown(this).place(this,zoner.getpoint());
  }

  /** @return <code>true</code> if lowest floor. */
  public boolean isdeepest(){
    return this==dungeon.floors.getLast();
  }

  /** @param nfeatures Target quantity of {@link Feature}s to place. */
  protected void generatefeatures(int nfeatures,DungeonZoner zoner){
    while(nfeatures>0){
      zoner.place(generatefeature(),this);
      nfeatures-=1;
    }
  }

  /**
   * @return A feature chosen from {@link #DEBUGFEATURE},
   *   {@link RareFeatureTable} or {@link CommonFeatureTable}. May return
   *   <code>null</code> in event of failure, in which case it should usually be
   *   possible to try again.
   */
  protected Feature generatefeature(){
    if(Javelin.DEBUG&&DEBUGFEATURE!=null) try{
      return DEBUGFEATURE.getDeclaredConstructor(DungeonFloor.class)
          .newInstance(this);
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
    var t=gettable(FeatureRarityTable.class).roll();
    return t.rollfeature(this);
  }

  static int getfeaturequantity(int quantity,float ratio){
    return RPG.randomize(Math.round(quantity*ratio),0,Integer.MAX_VALUE);
  }

  List<Trap> generatetraps(int ntraps,LinkedList<Decoration> d){
    var modifier=gettable(FeatureModifierTable.class);
    var special=gettable(SpecialTrapTable.class);
    var traps=new ArrayList<Trap>(ntraps);
    for(var i=0;i<ntraps;i++){
      var cr=level+Difficulty.get()+modifier.roll();
      var t=Trap.generate(cr,special.rollboolean(),this);
      if(t==null) continue;
      traps.add(t);
      if(d==null) t.place(this,getunnocupied());
      else d.pop().hide(t);
    }
    return traps;
  }

  Chest generatechest(Class<? extends Chest> type,int gold,DungeonZoner z,
      Decoration f){
    var percentmodifier=gettable(FeatureModifierTable.class).roll()*2;
    gold=Math.round(gold*(100+percentmodifier)/100f);
    try{
      var c=type.getConstructor(Integer.class,DungeonFloor.class)
          .newInstance(gold,this);
      if(!c.generateitem()){
        c=new Chest(gold,this);
        c.generateitem();
        if(f!=null){
          f.hide(c);
          return c;
        }
      }
      var p=z.getpoint();
      if(p==null) return null;
      c.place(this,p);
      return c;
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
  }

  void generatechests(int chests,int pool,DungeonZoner z,
      LinkedList<Decoration> d){
    var p=z.getpoint();
    if(p==null) return;
    dungeon.generatespecialchest(this).place(this,p);
    if(pool==0) return;
    if(chests<1) chests=1;
    var hidden=Math.max(2,chests/10);
    hidden=RPG.randomize(hidden,0,chests);
    var t=gettable(ChestTable.class);
    if(d!=null&&hidden>0){
      chests-=hidden;
      var hiddenpool=pool/2;
      pool-=hiddenpool;
      for(var i=0;i<hidden;i++) generatechest(t.roll(),pool/hidden,z,d.pop());
    }
    for(var i=0;i<chests;i++) generatechest(t.roll(),pool/chests,z,null);
    generatecrates(z);
  }

  /** @see Crate */
  protected void generatecrates(DungeonZoner zoner){
    var freebie=RewardCalculator.getgold(level);
    var ncrates=RPG.randomize(gettier().minrooms,0,Integer.MAX_VALUE);
    for(var i=0;i<ncrates;i++){
      var gold=RPG.randomize(freebie/ncrates,1,Integer.MAX_VALUE);
      generatechest(Crate.class,gold,zoner,null);
    }
  }

  /**
   * @return <code>true</code> if a {@link FloorTile#WALL} or {@link Feature}.
   */
  public boolean isoccupied(Point p){
    if(map[p.x][p.y]==FloorTile.WALL) return true;
    for(Feature f:features) if(f.x==p.x&&f.y==p.y) return true;
    return false;
  }

  /** Exit and destroy this dungeon. */
  public void leave(){
    var e=dungeon.exit;
    if(e!=null){
      var p=e.features.getall(BranchPortal.class).stream()
          .filter(f->f.destination==dungeon).findAny().orElseThrow();
      e.squadlocation=p.getlocation();
      e.enter();
      return;
    }
    WorldMove.abort=true;
    Dungeon.active=null;
    Javelin.app.switchScreen(new WorldScreen(true));
    Squad.active.place();
  }

  /** @see StairsUp */
  public void goup(){
    Squad.active.delay(1);
    var f=dungeon.floors.indexOf(this);
    if(f==0) Dungeon.active.leave();
    else{
      var up=dungeon.floors.get(f-1);
      up.squadlocation=up.features.get(StairsDown.class).getlocation();
      up.enter();
    }
  }

  /** @see StairsDown */
  public void godown(){
    Squad.active.delay(1);
    var f=dungeon.floors.get(dungeon.floors.indexOf(this)+1);
    f.squadlocation=f.features.get(StairsUp.class).getlocation();
    f.enter();
  }

  /**
   * Akin to terrain {@link Hazard}s.
   *
   * @return <code>true</code> if a hazard has happened.
   */
  public boolean triggerhazard(){
    var hazards=dungeon.branches.stream().flatMap(b->b.hazards.stream())
        .filter(h->h!=null).collect(Collectors.toList());
    for(var h:hazards){
      var steps=Math.round(stepsperencounter*hazards.size()/h.chancemodifier);
      if(RPG.chancein((int)steps)) return h.trigger();
    }
    return false;
  }

  /** Makes this {@link Point} visible to the player. */
  public void setvisible(int x,int y){
    if(0<=x&&x<=size&&0<=y&&y<=size){
      visible[x][y]=true;
      BattleScreen.active.mappanel.tiles[x][y].discovered=true;
    }
  }

  /** @param f {@link #setvisible(int, int)} and reveals {@link Feature}. */
  public void discover(Feature f){
    setvisible(f.x,f.y);
    f.discover(null,9000);
  }

  /** @see Tables */
  public <K extends Table> K gettable(Class<K> table){
    return tables.get(table,this);
  }

  /** @return All {@link #encounters} {@link Combatants}. */
  public List<Combatant> getcombatants(){
    var combatants=encounters.stream().flatMap(Combatants::stream)
        .collect(Collectors.toList());
    return RPG.shuffle(new ArrayList<>(combatants));
  }

  /** @param to Moves {@link Squad} location to these coordinates. */
  public void teleport(Point to){
    squadlocation=to;
    JavelinApp.context.view(to.x,to.y);
    WorldMove.abort=true;
  }

  /**
   * @return A human-intended count of how deep this floor is (1 for first, 2
   *   for the one below that)...
   * @see Dungeon#floors
   */
  public int getdepth(){
    return dungeon.floors.indexOf(this)+1;
  }

  @Override
  public String toString(){
    return dungeon.name+" (floor "+getdepth()+")";
  }

  /** Enters thsi particular floor. */
  public void enter(){
    if(BattleScreen.active instanceof DungeonScreen s&&s.floor==this) return;
    Dungeon.active=this;
    JavelinApp.context=new DungeonScreen(this);
    BattleScreen.active=JavelinApp.context;
    Squad.active.updateavatar();
    BattleScreen.active.mappanel.center(squadlocation.x,squadlocation.y,true);
    features.getknown();
  }

  /** @see Dungeon#gettier() */
  public DungeonTier gettier(){
    return dungeon.gettier();
  }

  /** @see DungeonScreen#explore(int, int) */
  public boolean explore(){
    RandomEncounter.encounter(1f/stepsperencounter);
    return !triggerhazard();
  }
}
