package javelin.model.world.location.unique;

import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.Siege;
import javelin.controller.content.map.location.LocationMap;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.table.dungeon.BranchTable;
import javelin.controller.table.dungeon.feature.DecorationTable;
import javelin.model.item.Item;
import javelin.model.item.consumable.Ruby;
import javelin.model.item.key.door.IronKey;
import javelin.model.item.key.door.StoneKey;
import javelin.model.item.key.door.WoodenKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Period;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.branch.CatacombBranch;
import javelin.model.world.location.dungeon.feature.BranchPortal;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.SpecialChest;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.dungeon.feature.door.ExcellentWoodenDoor;
import javelin.model.world.location.dungeon.feature.door.IronDoor;
import javelin.model.world.location.dungeon.feature.door.StoneDoor;
import javelin.model.world.location.dungeon.feature.rare.LearningStone;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Trader;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * A horror-themed {@link Dungeon} minigame. It has a static hub (main floor)
 * with access to 4 areas, one per {@link Tier} - each having one unique
 * {@link Branch} applied to it.
 *
 * @author alex
 */
public class Catacombs extends Wilderness{
  static final List<Point> STONES=List.of(new Point(26,5),new Point(26,4),
      new Point(30,4),new Point(30,5),new Point(26,10),new Point(26,11),
      new Point(30,11),new Point(30,10),new Point(4,10),new Point(4,11),
      new Point(8,11),new Point(8,10),new Point(4,5),new Point(4,4),
      new Point(8,4),new Point(8,5));
  static final List<Point> DOORS=List.of(new Point(28,6),new Point(28,9),
      new Point(6,9),new Point(6,6));
  static final Image WALL=Images.get(List.of("dungeon","wallcatacombs"));

  class Catacomb extends Dungeon{
    Item goal;

    Catacomb(Tier t,Item i){
      super("Catacomb",t.minlevel,5);
      goal=i;
      branches.add(CatacombBranch.INSTANCE);
      branchchance=0;
    }

    @Override
    protected synchronized String baptize(String base){
      return base;
    }

    @Override
    protected Feature generatespecialchest(DungeonFloor f){
      if(f==floors.getLast()) return new SpecialChest(f,goal);
      return super.generatespecialchest(f);
    }

    Catacomb place(Point p){
      var f=Catacombs.this.floors.get(0);
      new BranchPortal(f,this).place(f,p);
      return this;
    }

    void set(Branch b){
      branches.add(b);
      name+=" "+b.suffix.toLowerCase();
    }
  }

  class Entrance extends DungeonEntrance{
    Entrance(Dungeon d){
      super(d);
    }

    @Override
    public boolean interact(){
      return garrison.isEmpty()?super.interact():Location.interact(this);
    }

    @Override
    protected Fight fight(){
      var s=new Siege(this);
      s.period=Period.NIGHT;
      s.map=new LocationMap("Catacombs"){
        @Override
        public void generate(){
          super.generate();
          for(var d:DOORS) map[d.x][d.y].blocked=true;
          var plants=floors.get(0).features.getall(Decoration.class);
          var f=plants.get(0).avatarfile;
          obstacle=Images.get(List.of("dungeon","decoration",f));
          for(var d:plants) map[d.x][d.y].obstructed=true;
        }
      };
      s.map.wall=WALL;
      return s;
    }

    @Override
    public Integer getel(){
      return garrison.getel();
    }

    @Override
    public String getimagename(){
      return "catacombs";
    }

    @Override
    public void turn(long time,WorldScreen world){
      super.turn(time,world);
      var features=floors.get(0).features;
      Tier t;
      if(features.get(IronDoor.class)==null) t=Tier.EPIC;
      else if(features.get(StoneDoor.class)==null) t=Tier.HIGH;
      else if(features.get(ExcellentWoodenDoor.class)==null) t=Tier.MID;
      else t=Tier.LOW;
      var encounters=features.getall(BranchPortal.class).stream()
          .flatMap(p->p.destination.floors.stream())
          .filter(f->f.level==t.minlevel).findAny().orElseThrow().encounters;
      entrance.garrison=RPG.pick(encounters);
    }
  }

  class Camp extends Campfire{
    public Camp(DungeonFloor f){
      super(f);
    }

    @Override
    protected boolean compromise(){
      return false;
    }
  }

  class Guide extends Trader{
    Guide(DungeonFloor f){
      super(f);
    }

    @Override
    public Combatant select(DungeonFloor f){
      var m=Monster.get("Trumpet archon");
      return NpcGenerator.generate(m,Math.round(m.cr+10));
    }

    @Override
    protected List<Item> stock(int goldpool){
      var stock=new ArrayList<Item>(20);
      for(var t:Tier.TIERS){
        var from=RewardCalculator.getgold(t.minlevel);
        var to=RewardCalculator.getgold(t.maxlevel);
        var tier=new ArrayList<>(Item.NONPRECIOUS.stream()
            .filter(i->from<=i.price&&i.price<=to).toList());
        for(var i:RPG.shuffle(tier).subList(0,5)){
          i=i.clone();
          i.identified=true;
          stock.add(i);
        }
      }
      return stock;
    }
  }

  class Hub extends WildernessFloor{
    Hub(){
      super(1,Catacombs.this);
    }

    @Override
    protected char[][] map(){
      var m=new LocationMap("Catacombs");
      m.wall=WALL;
      return super.map(m);
    }

    void close(){
      var doors=List.of(new ExcellentWoodenDoor(this),new StoneDoor(this),
          new IronDoor(this));
      var openings=new LinkedList<>(DOORS.subList(1,4));
      for(var d:doors){
        d.locked=true;
        d.stuck=false;
        d.draw=true;
        d.place(this,openings.pop());
      }
    }

    @Override
    protected void populate(){
      close();
      var portals=List.of(
          new Catacomb(Tier.LOW,new WoodenKey(this)).place(new Point(28,4)),
          new Catacomb(Tier.MID,new StoneKey(this)).place(new Point(28,11)),
          new Catacomb(Tier.HIGH,new IronKey(this)).place(new Point(6,11)),
          new Catacomb(Tier.EPIC,new Ruby()).place(new Point(6,4)));
      var branches=RPG.shuffle(BranchTable.BRANCHES,true);
      branches.remove(CatacombBranch.INSTANCE);
      for(var i=0;i<4;i++) portals.get(i).set(branches.get(i));
      var camp=new Point(17,8);
      new Camp(this).place(this,camp);
      new Guide(this).place(this,RPG.pick(camp.getadjacent(2)));
      for(var s:STONES)
        if(RPG.chancein(2)) new LearningStone(this).place(this,s);
      var s=features.get(StairsUp.class).getlocation();
      var free=new LinkedList<>(new DungeonZoner(this,s).zones.get(0).area);
      var features=this.features.getall();
      free.removeAll(features.stream().map(Feature::getlocation).toList());
      var p=DecorationTable.PLANT.roll();
      for(var a:free) if(RPG.chancein(4)) new Decoration(p,this).place(this,a);
    }

    @Override
    protected void generateencounters(EncounterIndex index){
      encounters.add(null);
    }
  }

  /** Constructor. */
  public Catacombs(){
    name="Catacombs";
    images.put(DungeonImages.FLOOR,"floorcatacombs");
    images.put(DungeonImages.WALL,"wallcatacombs");
    entrance=new Entrance(this);
    terrains.clear();
    terrains.add(Terrain.MARSH);
  }

  /** @see DungeonEntrance#place(Point) */
  public void place(Point p){
    entrance.place(p);
  }

  @Override
  protected DungeonFloor createfloor(int level){
    return new Hub();
  }

  @Override
  protected synchronized String baptize(String base){
    return base;
  }
}
