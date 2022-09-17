package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.content.map.location.LocationMap;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.table.dungeon.feature.DecorationTable;
import javelin.model.item.Item;
import javelin.model.item.key.door.IronKey;
import javelin.model.item.key.door.StoneKey;
import javelin.model.item.key.door.WoodenKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.door.ExcellentWoodenDoor;
import javelin.model.world.location.dungeon.feature.door.IronDoor;
import javelin.model.world.location.dungeon.feature.door.StoneDoor;
import javelin.model.world.location.dungeon.feature.rare.LearningStone;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Trader;
import javelin.old.RPG;
import javelin.view.Images;

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

  class Entrance extends DungeonEntrance{
    Entrance(Dungeon d){
      super(d);
    }

    @Override
    public String getimagename(){
      return "catacombs";
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
      m.wall=Images.get(List.of("dungeon","wallcatacombs"));
      return super.map(m);
    }

    @Override
    protected void populate(){
      new ExcellentWoodenDoor(this).place(this,new Point(28,9));
      new StoneDoor(this).place(this,new Point(6,9));
      new IronDoor(this).place(this,new Point(6,6));
      for(var d:features.getall(Door.class)){
        d.locked=true;
        d.stuck=false;
        d.draw=true;
      }
      new WoodenKey(this);
      new StoneKey(this);
      new IronKey(this);
      //TODO Branches
      var camp=new Point(17,8);
      new Camp(this).place(this,camp);
      new Guide(this).place(this,RPG.pick(camp.getadjacent(2)));
      //      var stones=RPG.high(1,8);
      for(var s:STONES)
        if(RPG.chancein(2)) new LearningStone(this).place(this,s);
      //        else new Decoration(DecorationTable.ROCK.roll(),this).place(this,s);
      var s=features.get(StairsUp.class).getlocation();
      var free=new LinkedList<>(new DungeonZoner(this,s).zones.get(0).area);
      var features=this.features.getall();
      free.removeAll(features.stream().map(Feature::getlocation).toList());
      //      RPG.shuffle(free);
      //      for(var i=0;i<stones;i++) new LearningStone(this).place(this,free.pop());
      var p=DecorationTable.PLANT.roll();
      for(var a:free) if(RPG.chancein(4)) new Decoration(p,this).place(this,a);
    }

    @Override
    protected void generateencounters(List<EncounterIndex> index){
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
