package javelin.model.world.location.dungeon;

import java.util.Set;

import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.item.Tier;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.LoreNote;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.model.world.location.dungeon.feature.chest.Crate;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.Tile;

/**
 * A type of {@link Location} that plays like a {@link Dungeon} but is instead
 * meant to be more relaxed and exploration-focused than anything. Maps are
 * bigger and based on non-Underground {@link Fight} maps instead. Encounters
 * are always {@link Difficulty#EASY}.
 *
 * As far as {@link #fight()}s go, we assume that each encounter wll take 10% of
 * a {@link Squad}'s resources. Also that it will take around 1d4 attemps to
 * fully explore the area.
 *
 * Chests here will be largely for show only, since the area itself has little
 * challenge that isn't rewarded per-se. Other features however, while limited
 * by types that would make sense outdoors, are fully operational which can
 * either give decent boons (like a {@link LearningStone}) or an even unfairer
 * advantage in exploring the area.
 *
 * This {@link Location}'s {@link Tier} is {@link Tier#LOW}, with a 50%
 * recursive chance of increasing a tier. This makes Wildernesses good leveling
 * areas with little risk (but also less reward) than typical {@link Dungeon}s.
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
	/**
	 * {@link DungeonFloor} {@link Feature}s that are not relevant to Wilderness
	 * {@link Location}s.
	 */
	public static final Set<Class<? extends Feature>> FORBIDDEN=Set.of(
			Brazier.class,Mirror.class,Throne.class,Fountain.class,Prisoner.class,
			Crate.class,LoreNote.class);

	/** Placeholder to prevent an uneeded call {@link #baptize(String)}.p */
	static final String DESCRIPTION="Wilderness";

	class WildernessFloor extends DungeonFloor{
		public WildernessFloor(Integer level,Dungeon d){
			super(level,d);
			terrain=type;
		}

		/** Places {@link Entrance} and {@link Squad} on a border {@link Tile}. */
		void generateentrance(char[][] map){
			var top=floors.get(0);
			top.squadlocation=null;
			var width=map.length;
			var height=map[0].length;
			while(top.squadlocation==null){
				top.squadlocation=new Point(RPG.r(0,width-1),RPG.r(0,height-1));
				if(RPG.chancein(2))
					top.squadlocation.x=RPG.chancein(2)?0:width-1;
				else
					top.squadlocation.y=RPG.chancein(2)?0:height-1;
				var empty=top.squadlocation.getadjacent().stream().filter(
						p->p.validate(0,0,width,height)&&map[p.x][p.y]==MapTemplate.FLOOR);
				if(empty.count()<3) top.squadlocation=null;
			}
			map[top.squadlocation.x][top.squadlocation.y]=MapTemplate.FLOOR;
			new Entrance(top.squadlocation).place(this,top.squadlocation);
		}

		@Override
		protected char[][] map(){
			var e=Wilderness.this.entrance.getlocation();
			type=World.seed.map[e.x][e.y];
			var fightmap=RPG.pick(type.getmaps());
			fightmap.generate();
			var width=fightmap.map.length;
			int height=fightmap.map[0].length;
			var map=new char[width][height];
			for(var x=0;x<width;x++)
				for(var y=0;y<height;y++)
					map[x][y]=fightmap.map[x][y].blocked?MapTemplate.WALL
							:MapTemplate.FLOOR;
			generateentrance(map);
			dungeon.images.put(DungeonImages.FLOOR,Images.NAMES.get(fightmap.floor));
			dungeon.images.put(DungeonImages.WALL,Images.NAMES.get(fightmap.wall));
			dungeon.name=dungeon.baptize(fightmap.name);
			return map;
		}

		@Override
		protected int calculateencounterrate(){
			var totalsteps=countfloor()/(DISCOVEREDPERSTEP*dungeon.vision);
			var attemptstoclear=RPG.r(1,4);
			return 2*totalsteps/attemptstoclear;
		}

		@Override
		protected void generateencounters(){
			var target=RPG.randomize(6,1,Integer.MAX_VALUE);
			while(encounters.size()<target){
				var el=level+Difficulty.get()+Difficulty.EASY;
				var e=EncounterGenerator.generate(el,type);
				if(e!=null) encounters.add(e);
			}
		}

		@Override
		protected void populate(){
			var nfeatures=RPG.randomize(5,0,Integer.MAX_VALUE);
			for(var i=0;i<nfeatures;i++)
				generatefeature().place(this,getunnocupied());
			var gold=RewardCalculator.getgold(level);
			var ncrates=RPG.randomize(3,0,Integer.MAX_VALUE);
			for(var i=0;i<ncrates;i++){
				var c=new Crate(RPG.randomize(gold/ncrates,1,Integer.MAX_VALUE));
				c.place(this,getunnocupied());
			}
		}
	}

	class Entrance extends StairsUp{
		Entrance(Point p){
			super(p,Wilderness.this.floors.get(0));
		}

		@Override
		protected String prompt(){
			return "Leave area?";
		}
	}

	/** Terrain type (not {@link Terrain#WATER} or {@link Terrain#UNDERGROUND}. */
	public Terrain type;

	/** Constructor. */
	public Wilderness(){
		super(DESCRIPTION,-1,1);
		vision*=2;
		var tier=0;
		while(RPG.chancein(2)&&tier<Tier.TIERS.size()-1)
			tier+=1;
		var t=Tier.TIERS.get(tier);
		level=RPG.r(t.minlevel,t.maxlevel);
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
	public Fight fight(){
		var e=super.fight();
		e.map.floor=Images.get(images.get(DungeonImages.FLOOR));
		e.map.wall=Images.get(images.get(DungeonImages.WALL));
		e.map.wallfloor=e.map.floor;
		return e;
	}
}
