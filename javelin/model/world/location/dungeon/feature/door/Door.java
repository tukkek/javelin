package javelin.model.world.location.dungeon.feature.door;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.table.dungeon.door.DoorType;
import javelin.controller.table.dungeon.door.HiddenDoor;
import javelin.controller.table.dungeon.door.LockedDoor;
import javelin.controller.table.dungeon.door.StuckDoor;
import javelin.controller.table.dungeon.door.TrappedDoor;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.trap.Alarm;
import javelin.model.world.location.dungeon.feature.door.trap.ArcaneLock;
import javelin.model.world.location.dungeon.feature.door.trap.DoorTrap;
import javelin.model.world.location.dungeon.feature.door.trap.HoldPortal;
import javelin.old.RPG;

/**
 * A {@link Dungeon} door, which may be {@link #locked}, {@link #stuck}, contain
 * a {@link #trap} or not. If none of these are true, it may be discarded.
 *
 * @see Key
 * @author alex
 */
public class Door extends Feature{
  static final List<DoorTrap> TRAPS=Arrays.asList(Alarm.INSTANCE,
      ArcaneLock.INSTANCE,HoldPortal.INSTANCE);
  static final String FORCE="""
      The door is stuck (difficulty: %s)... Do you want to force it?

      Press ENTER to try to force it once, b to force until it's broken or any other key to cancel...
      """;
  static final String BEYONDSTUCK="This door is stuck beyond hope of being budged by %s...";
  static final String FORCED="%s breaks the door down!";
  static final String FORCEFAIL="%s could not break the door...";
  static final String TOOCOMPLEX="The lock is too complex for %s to pick...";
  /** TODO switch instead of map */
  static final Map<Character,Integer> ATTEMPTS=new HashMap<>();

  record Outcome(Combatant opener,boolean open,String messsage){}

  static{
    ATTEMPTS.put('\n',1);
    ATTEMPTS.put('b',Integer.MAX_VALUE);
  }

  /** DC of {@link DisableDevice} to unlock. */
  public int unlockdc;
  /** Used if {@link #hidden}. TODO */
  public int searchdc=RPG.r(20,30);
  /** DC of {@link Monster#strength} to break down. */
  public int breakdc;
  /** Type of key used to unlock. */
  public Class<? extends Key> key;
  /** Trap present or <code>null</code>. */
  public DoorTrap trap;
  /** <code>true</code> if stuck. */
  public boolean stuck;
  /** <code>true</code> if locked. */
  public boolean locked;
  /** @see #searchdc */
  /** TODO use only #draw, remove */
  boolean hidden;

  Door(String avatar,int breakdcstuck,int breakdclocked,
      Class<? extends Key> key,DungeonFloor f){
    super("door",avatar);
    this.key=key;
    enter=false;
    trap=f.gettable(TrappedDoor.class).rollboolean()?RPG.pick(TRAPS):null;
    stuck=f.gettable(StuckDoor.class).rollboolean();
    locked=f.gettable(LockedDoor.class).rollboolean();
    hidden=f.gettable(HiddenDoor.class).rollboolean();
    breakdc=locked?breakdclocked:breakdcstuck;
    breakdc=Math.max(2,breakdc+RPG.randomize(5));
    unlockdc=RPG.r(20,30)+f.gettable(FeatureModifierTable.class).roll();
    if(unlockdc<2) unlockdc=2;
    if(trap!=null) trap.generate(this);
    draw=!hidden;
  }

  Combatant getunlocker(){
    var expert=Squad.active.getbest(Skill.DISABLEDEVICE);
    return expert.taketen(Skill.DISABLEDEVICE)>=unlockdc?expert:null;
  }

  Combatant getforcer(){
    return Squad.active.members.stream()
        .collect(Collectors.maxBy(Comparator.comparing(c->c.source.strength)))
        .orElseThrow();
  }

  Outcome force(){
    var forcer=getforcer();
    var s=Monster.getbonus(forcer.source.strength);
    if(20+s<breakdc){
      Javelin.message(BEYONDSTUCK.formatted(forcer),true);
      return new Outcome(forcer,false,null);
    }
    var prompt=FORCE.formatted(Javelin.describe(s,breakdc));
    var attempts=ATTEMPTS.getOrDefault(Javelin.prompt(prompt),0);
    if(attempts==0) return new Outcome(forcer,false,null);
    Fight f=null;
    for(var i=0;i<attempts&&stuck&&f==null;i++){
      if(10+s>=breakdc||RPG.r(1,20)+s>=breakdc) stuck=false;
      if(RPG.chancein(10)) f=Dungeon.active.dungeon.fight();
    }
    Javelin.message((stuck?FORCEFAIL:FORCED).formatted(forcer),false);
    if(f!=null){
      Javelin.message("The noise draws someone's attention!",true);
      throw new StartBattle(f);
    }
    return new Outcome(forcer,!stuck,null);
  }

  Outcome open(){
    var u=getunlocker();
    if(u!=null) return new Outcome(u,true,u+" unlocks the door!");
    var f=getforcer();
    if(10+Monster.getbonus(f.source.strength)>breakdc)
      return new Outcome(f,true,f+" forces the lock!");
    var keys=Squad.active.equipment.getall(key);
    var key=keys.stream().filter(k->k.open(Dungeon.active)).findAny();
    if(key.isPresent()) return new Outcome(null,true,null);
    return new Outcome(u,false,TOOCOMPLEX.formatted(f));
  }

  @Override
  public boolean activate(){
    if(Debug.bypassdoors) return true;
    if(hidden&&!draw) return false;
    Combatant opening=null;
    if(locked){
      var outcome=open();
      if(outcome.messsage!=null) Javelin.message(outcome.messsage,false);
      if(!outcome.open) return false;
      opening=outcome.opener;
    }
    locked=false;
    if(stuck){
      var outcome=force();
      if(!outcome.open) return false;
      opening=outcome.opener;
    }
    enter=true;
    remove();
    if(trap!=null) spring(opening);
    return true;
  }

  void spring(Combatant opening){
    if(opening==null) opening=Squad.active.members.stream()
        .collect(Collectors.maxBy(Comparator.comparing(c->c.hp))).orElseThrow();
    trap.activate(opening);
  }

  /**
   * @param f Dungeon to place door in.
   * @param p Position of the door.
   * @return A randomly-chosen type of door.
   */
  @SuppressWarnings("unchecked")
  public static Door generate(DungeonFloor f,Point p){
    try{
      var type=(Class<? extends Door>)f.gettable(DoorType.class).roll();
      var d=type.getDeclaredConstructor(DungeonFloor.class).newInstance(f);
      if(!d.stuck&&!d.locked&&!(d.trap instanceof Alarm)) return null;
      d.x=p.x;
      d.y=p.y;
      d.unlockdc+=f.level;
      return d;
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void place(DungeonFloor d,Point p){
    super.place(d,p);
    if(hidden) d.map[x][y]=FloorTile.WALL;
  }

  @Override
  public boolean discover(Combatant searching,int searchroll){
    if(draw||searchroll<searchdc) return false;
    Dungeon.active.map[x][y]=FloorTile.FLOOR;
    draw=true;
    hidden=false;
    Javelin.redraw();
    if(searching!=null) Javelin.message("You find a hidden door!",true);
    return true;
  }

  @Override
  public String toString(){
    return getClass().getSimpleName();
  }

  @Override
  public void remove(){
    super.remove();
    Dungeon.active.map[x][y]=FloorTile.FLOOR;
  }
}
