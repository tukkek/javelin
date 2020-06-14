package javelin.model.world.location.dungeon;

import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
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
import javelin.model.world.location.dungeon.feature.Crate;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.Tile;

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
	/** Placeholder to prevent an uneeded call {@link #baptize(String)}.p */
	static final String DESCRIPTION="Wilderness";
	static final Set<Class<? extends Feature>> FORBIDDEN=Set.of(Brazier.class,
			Mirror.class,Throne.class,Fountain.class,Prisoner.class,Crate.class);

	class Entrance extends StairsUp{
		Entrance(Point p){
			super(p,Wilderness.this);
			avatarfile="locationwilderness";
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
		super(DESCRIPTION,-1,null,null);
		floors=List.of(this);
		squadvision*=2;
		tables=new Tables();
		var tieri=0;
		var last=Tier.TIERS.size()-1;
		while(RPG.chancein(2)&&tieri<last)
			tieri+=1;
		var t=Tier.TIERS.get(tieri);
		level=RPG.r(t.minlevel,t.maxlevel);
	}

	/** Places {@link Entrance} and {@link Squad} on a border {@link Tile}. */
	void generateentrance(char[][] map){
		squadlocation=null;
		var width=map.length;
		var height=map[0].length;
		while(squadlocation==null){
			squadlocation=new Point(RPG.r(0,width-1),RPG.r(0,height-1));
			if(RPG.chancein(2))
				squadlocation.x=RPG.chancein(2)?0:width-1;
			else
				squadlocation.y=RPG.chancein(2)?0:height-1;
			var empty=squadlocation.getadjacent().stream().filter(
					p->p.validate(0,0,width,height)&&map[p.x][p.y]==Template.FLOOR);
			if(empty.count()==0) squadlocation=null;
		}
		map[squadlocation.x][squadlocation.y]=Template.FLOOR;
		new Entrance(squadlocation).place(this,squadlocation);
	}

	@Override
	protected char[][] map(){
		type=World.seed.map[x][y];
		var fightmap=RPG.pick(type.getmaps());
		fightmap.generate();
		var width=fightmap.map.length;
		int height=fightmap.map[0].length;
		var map=new char[width][height];
		for(var x=0;x<width;x++)
			for(var y=0;y<height;y++)
				map[x][y]=fightmap.map[x][y].blocked?Template.WALL:Template.FLOOR;
		generateentrance(map);
		tilefloor=Images.NAMES.get(fightmap.floor);
		tilewall=Images.NAMES.get(fightmap.wall);
		description=baptize(fightmap.name);
		return map;
	}

	@Override
	protected int calculateencounterrate(){
		var totalsteps=countfloor()/(DISCOVEREDPERSTEP*squadvision);
		var attemptstoclear=RPG.r(1,4);
		return 2*(totalsteps/attemptstoclear);
	}

	@Override
	protected void generateencounters(){
		var target=RPG.rolldice(2,10);
		var failures=100;
		while(encounters.size()<target){
			var el=level+Difficulty.get()+makeeasy();
			encounters.add(EncounterGenerator.generate(el,type));
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
		e.map.wallfloor=e.map.floor;
		return e;
	}

	@Override
	public String getimagename(){
		return "locationwilderness";
	}

	void makechest(){
		var gold=RewardCalculator.getgold(level+makeeasy());
		var c=new Chest(gold);
		if(RPG.chancein(2)) c.nitems=0;
		c.place(this,getunnocupied());
	}

	@Override
	protected void populatedungeon(){
		var target=RPG.rolldice(2,10);
		while(features.size()<target){
			if(RPG.chancein(6)){
				makechest();
				continue;
			}
			var f=createfeature();
			if(f!=null&&!FORBIDDEN.contains(f.getClass()))
				f.place(this,getunnocupied());
		}
	}

	@Override
	protected Dungeon chooseentrance(){
		return this;
	}
}
