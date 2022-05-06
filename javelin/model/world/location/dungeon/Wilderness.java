package javelin.model.world.location.dungeon;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.db.EncounterIndex;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.item.Tier;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.Crate;
import javelin.model.world.location.dungeon.feature.chest.GemDisplay;
import javelin.model.world.location.dungeon.feature.common.Brazier;
import javelin.model.world.location.dungeon.feature.common.LoreNote;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.dungeon.feature.rare.LearningStone;
import javelin.model.world.location.dungeon.feature.rare.Mirror;
import javelin.model.world.location.dungeon.feature.rare.Throne;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Prisoner;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.dungeon.DungeonWalker;

/**
 * A type of {@link Location} that plays like a {@link Dungeon} but is instead
 * meant to be more relaxed and exploration-focused. Maps are bigger and based
 * on non-Underground {@link Fight} {@link Map}s instead.
 *
 * Fights are {@link Difficulty#EASY} and we assume that each encounter wll take
 * 10% of a {@link Squad}'s resources - also that it will take around 1d4
 * attemps to fully explore the entire map area.
 *
 * Chests here will be largely for show only, since the area itself has little
 * challenge that isn't rewarded per-se. Other {@link Feature}s however, while
 * limited by types that would make sense outdoors, are fully operational which
 * can either give decent boons (like a {@link LearningStone}) or an even bigger
 * advantage in exploring the area.
 *
 * This {@link Location}'s {@link Tier} is first {@link Tier#LOW}, with a 50%
 * recursive chance of increasing a tier. This makes Wildernesses good leveling
 * areas with little risk (but also less reward) than typical {@link Dungeon}s.
 *
 * {@link Path}s are partially drawn between {@link Feature}s, representing
 * either roads or markings from recent/recurrent travel. Althought generally
 * helpful for navigation, this is mostly for flavor - becoming more important
 * for {@link Portal}s.
 *
 * TODO could {@link Hazard}s be used here instead of a {@link #fight()} some of
 * the time?
 *
 * TODO a cool Feature would be "boss" encouners, probably signified by a skull.
 * could also have a RareTable for Dungeons and other for Wilderness, with a
 * small change of taking from the other instead, giving each more personality.
 * Common would be common to both.
 *
 * TODO could have town quests place trinkets to be retrieved from wildernessesn
 *
 * @author alex
 */
public class Wilderness extends Dungeon{
  /** {@link DungeonFloor#features} that are not relevant to Wildernesses. */
  public static final Set<Class<? extends Feature>> FORBIDDEN=Set.of(
      Brazier.class,Mirror.class,Throne.class,Fountain.class,Prisoner.class,
      Crate.class,LoreNote.class);

  static final String DESCRIPTION="Wilderness";

  class Path extends Feature{
    int tile=RPG.r(1,14);

    public Path(){
      super("path");
      remove=false;
    }

    @Override
    public boolean activate(){
      return false;
    }

    @Override
    public Image getimage(){
      return Images.get(List.of("dungeon","path","path"+tile));
    }
  }

  class PathWalker extends DungeonWalker{
    PathWalker(Point from,Point to,DungeonFloor f){
      super(from,to,f);
      pathing=new DirectPath();
      discoveredonly=false;
    }
  }

  class WildernessFloor extends DungeonFloor{
    public WildernessFloor(Integer level,Dungeon d){
      super(level,d);
    }

    /** Places {@link Exit} and {@link Squad} on a border {@link Tile}. */
    void generateentrance(char[][] map){
      var top=floors.getFirst();
      var width=map.length;
      var height=map[0].length;
      top.squadlocation=null;
      while(top.squadlocation==null){
        top.squadlocation=new Point(RPG.r(0,width-1),RPG.r(0,height-1));
        if(RPG.chancein(2)) top.squadlocation.x=RPG.chancein(2)?0:width-1;
        else top.squadlocation.y=RPG.chancein(2)?0:height-1;
        var empty=top.squadlocation.getadjacent().stream().filter(
            p->p.validate(0,0,width,height)&&map[p.x][p.y]==FloorTile.FLOOR);
        if(empty.count()<3) top.squadlocation=null;
      }
      map[top.squadlocation.x][top.squadlocation.y]=FloorTile.FLOOR;
      new Exit(top.squadlocation).place(this,top.squadlocation);
    }

    @Override
    protected char[][] map(){
      var m=RPG.pick(dungeon.terrains).getmap();
      m.generate();
      var width=m.map.length;
      var height=m.map[0].length;
      var map=new char[width][height];
      for(var x=0;x<width;x++) for(var y=0;y<height;y++)
        map[x][y]=m.map[x][y].blocked?FloorTile.WALL:FloorTile.FLOOR;
      for(var i=0;i<entrances;i++) generateentrance(map);
      dungeon.images.put(DungeonImages.FLOOR,Images.NAMES.get(m.floor));
      dungeon.images.put(DungeonImages.WALL,Images.NAMES.get(m.wall));
      dungeon.name=m.name;
      return map;
    }

    @Override
    protected int calculateencounterrate(){
      var totalsteps=countfloor()/(DISCOVEREDPERSTEP*dungeon.vision);
      var attemptstoclear=RPG.randomize(3,1,Integer.MAX_VALUE);
      return 2*totalsteps/attemptstoclear;
    }

    @Override
    protected void generateencounters(List<EncounterIndex> index){
      var target=RPG.randomize(6,1,Integer.MAX_VALUE);
      var easy=Difficulty.EASY;
      while(encounters.size()<target){
        var el=level+Difficulty.get()+easy;
        var e=EncounterGenerator.generatebyindex(el,index);
        if(e==null) easy+=1;
        else encounters.add(e);
      }
    }

    void generatepaths(){
      var nodes=new ArrayList<>(features.stream().map(Feature::getlocation)
          .collect(Collectors.toList()));
      var w=map.length;
      var h=map[0].length;
      var extra=RPG.randomize(2,0,Integer.MAX_VALUE);
      for(var i=0;i<extra;i++){
        var p=new Point(RPG.r(0,w-1),RPG.r(0,h-1));
        if(!nodes.contains(p)&&!isoccupied(p)) nodes.add(p);
      }
      var paths=new HashSet<Point>();
      for(var i=0;i<nodes.size();i++) for(var j=i+1;j<nodes.size();j++){
        var walker=new PathWalker(nodes.get(i),nodes.get(j),this).walk();
        if(walker!=null) paths.addAll(walker);
      }
      paths.stream()
          .filter(p->RPG.chancein(2)&&p.validate(0,0,w,h)&&!isoccupied(p))
          .forEach(p->new Path().place(this,p));
    }

    @Override
    protected void populate(){
      var nfeatures=RPG.randomize(5,0,Integer.MAX_VALUE);
      var z=new DungeonZoner(this,squadlocation);
      for(var i=0;i<nfeatures;i++){
        var p=z.getpoint();
        if(p==null) break;
        generatefeature().place(this,p);
      }
      generatepaths();
      var pool=RewardCalculator.getgold(level);
      var ncrates=RPG.randomize(3,0,Integer.MAX_VALUE);
      for(var i=0;i<ncrates;i++){
        var gold=RPG.randomize(pool/ncrates,1,Integer.MAX_VALUE);
        var c=new Crate(gold,this);
        var p=z.getpoint();
        if(p!=null) c.place(this,p);
      }
      generatebranches(z);
    }

    @Override
    public String toString(){
      return dungeon.name;
    }
  }

  class Exit extends StairsUp{
    Exit(Point p){
      super(p,Wilderness.this.floors.getFirst());
      prompt="Leave area?";
    }
  }

  /** Number of {@link Exit}s. */
  protected int entrances=1;

  /** Subclass constructor. */
  protected Wilderness(int level){
    super(DESCRIPTION,level,1);
    vision*=2;
    goals=List.of(GemDisplay.class);
  }

  /** Public constructor. */
  public Wilderness(){
    this(getlevel());
  }

  private static int getlevel(){
    var tier=0;
    while(RPG.chancein(2)&&tier<Tier.TIERS.size()-1) tier+=1;
    var t=Tier.TIERS.get(tier);
    return RPG.r(t.minlevel,t.maxlevel);
  }

  @Override
  public void generate(){
    var e=entrance.getlocation();
    var t=World.getseed().map[e.x][e.y];
    terrains.clear();
    terrains.add(t);
    super.generate();
  }

  @Override
  public String getimagename(){
    return "wilderness";
  }

  @Override
  protected DungeonFloor createfloor(int level){
    return new WildernessFloor(level,this);
  }

  @Override
  void generatelore(){
    //don't
  }

  @Override
  public RandomDungeonEncounter fight(){
    var e=super.fight();
    e.set(RPG.pick(terrains));
    e.map.floor=Images.get(images.get(DungeonImages.FLOOR));
    e.map.wall=Images.get(images.get(DungeonImages.WALL));
    e.map.wallfloor=e.map.floor;
    return e;
  }

  /** @return All {@link DungeonEntrance}s to a wilderness. */
  public static List<DungeonEntrance> getwildernesses(){
    return World.getactors().stream().filter(a->a instanceof DungeonEntrance)
        .map(a->(DungeonEntrance)a).filter(e->e.dungeon instanceof Wilderness)
        .collect(Collectors.toList());
  }

  @Override
  protected void generateappearance(){
    // don't, as would mix avatar/dungeon/ and avatar/terrain/ folders
  }
}
