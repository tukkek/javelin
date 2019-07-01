package javelin.model.world.location.dungeon;

import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.table.Tables;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.item.Tier;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A type of {@link Location} that plays like a {@link Dungeon} but is instead
 * meant to be more relaxed and exploration-focused than anything. Maps are
 * bigger and based on {@link Fight} maps instead. Encounters are always
 * {@link Difficulty#EASY}.
 *
 * As far as {@link #fight()}s go, we assume that each encounter wll take 10% of
 * a {@link Squad}'s resources. Also that it will take around 1d4 attemps to
 * fully explore the area.
 *
 * Chests here will be largely for show only, since the area itself has little
 * challenge that isn't rewarded per-se. Other features however, are fully
 * operational which can either give decent boons (like a {@link LearningStone})
 * or an even unfairer advantage in exploring the area.
 *
 * TODO could {@link Hazard}s be used here instead of a {@link #fight()} some of
 * the time?
 *
 * @author alex
 */
public class Wilderness extends Dungeon{
	/** Placeholder to prevent an uneeded call {@link #baptize(String)}.p */
	static final String DESCRIPTION="Wilderness";
	static final Set<Class<? extends Feature>> FORBIDDEN=Set.of(Brazier.class,
			Mirror.class,Throne.class);

	class Entrance extends StairsUp{
		Entrance(Point p){
			super(p);
			avatarfile="locationwilderness";
		}

		@Override
		protected String prompt(){
			return "Leave area?";
		}
	}

	/** Terrain type (not {@link Terrain#WATER} or {@link Terrain#UNDERGROUND}. */
	public Terrain type;
	public int attemptstoclear;

	static int determinelevel(){
		var i=0;
		while(RPG.chancein(2))
			i+=1;
		i=Math.min(i,Tier.TIERS.size()-1);
		var t=Tier.TIERS.get(i);
		return RPG.r(t.minlevel,t.maxlevel);
	}

	/** Constructor. */
	public Wilderness(int level){
		super(DESCRIPTION,determinelevel(),null,null);
		floors=List.of(this);
		vision*=2;
		tables=new Tables();
	}

	//	List<Point> getborder(char[][] map){
	//		var edge=new ArrayList<Point>(size*4);
	//		var width=map.length;
	//		var height=map[0].length;
	//		for(var x=0;x<width;x++)
	//			for(var y=0;y<height;y++)
	//				if(x==0||y==0||x==width-1||y==height-1) edge.add(new Point(x,y));
	//		return edge;
	//	}

	void generateentrance(char[][] map) throws GaveUp{
		//		var border=RPG.shuffle(getborder(map));
		//		var entrance=border.stream().filter(p->map[p.x][p.y]==Template.FLOOR)
		//				.findAny().orElse(null);
		//		if(entrance==null) throw new GaveUp();
		//		border.remove(entrance);
		//		border.forEach(p->map[p.x][p.y]=Template.WALL);
		herolocation=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
		if(RPG.chancein(2))
			herolocation.x=RPG.chancein(2)?0:map.length-1;
		else
			herolocation.y=RPG.chancein(2)?0:map[0].length-1;
		map[herolocation.x][herolocation.y]=Template.FLOOR;
		new Entrance(herolocation).place(this,herolocation);
	}

	@Override
	protected char[][] map(){
		type=World.seed.map[x][y];
		try{
			var map=RPG.pick(type.getmaps());
			map.generate();
			var width=map.map.length;
			int height=map.map[0].length;
			var dmap=new char[width][height];
			for(var x=0;x<width;x++)
				for(var y=0;y<height;y++)
					dmap[x][y]=map.map[x][y].blocked?Template.WALL:Template.FLOOR;
			generateentrance(dmap);
			tilefloor=Images.NAMES.get(map.floor);
			tilewall=Images.NAMES.get(map.wall);
			description=baptize(map.name);
			return dmap;
		}catch(GaveUp e){
			return map();
		}
	}

	@Override
	protected int calculateencounterfrequency(){
		var totalsteps=countfloor()/(DISCOVEREDPERSTEP*vision);
		attemptstoclear=RPG.r(1,4);
		return totalsteps/attemptstoclear;
	}

	@Override
	protected void generateencounters(){
		var target=RPG.rolldice(2,10);
		while(encounters.size()<target)
			try{
				var el=level+Difficulty.get()+makeeasy();
				encounters.add(EncounterGenerator.generate(el,type));
			}catch(GaveUp e){
				if(!encounters.isEmpty()) return;
			}
	}

	int makeeasy(){
		return -RPG.r(1,6)+1;
	}

	@Override
	public Fight fight(){
		var e=new RandomDungeonEncounter(this);
		e.map.wall=Images.get(tilewall);
		e.map.floor=Images.get(tilefloor);
		return e;
	}

	@Override
	public String getimagename(){
		return "locationwilderness";
	}

	@Override
	protected void populatedungeon(){
		var target=RPG.rolldice(2,10);
		while(features.size()<target){
			var p=getrandompoint();
			if(RPG.chancein(6)){
				var justgold=RPG.chancein(2);
				var gold=RewardCalculator.getgold(level+makeeasy());
				var c=new Chest(justgold?0:gold,p);
				if(justgold) c.gold=Javelin.round(gold);
				c.place(this,p);
				continue;
			}
			try{
				var f=createfeature();
				if(f!=null&&!FORBIDDEN.contains(f.getClass())) f.place(this,p);
			}catch(ReflectiveOperationException e){
				if(Javelin.DEBUG) throw new RuntimeException(e);
			}
		}
	}
}
