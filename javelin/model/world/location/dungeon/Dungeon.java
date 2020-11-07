package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.db.StateManager;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.model.world.location.dungeon.feature.Passage;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;

/**
 * A set of {@link DungeonFloor}s.
 *
 * @author alex
 */
public class Dungeon implements Serializable{
	/**
	 * Current dungeon or <code>null</code> if not in one. Also set during
	 * {@link #generate()}, pointing to the relevant instance.
	 */
	public static DungeonFloor active=null;

	/**
	 * Simple callback to report generation progress.
	 *
	 * @author alex
	 */
	public interface GenerationReport{
		/**
		 * @param d Floor that just finished generation.
		 */
		void report(double d);
	}

	/**
	 * All floors that make part of this dungeon. Each floor deeper is a
	 * {@link #level} higher.
	 */
	public List<DungeonFloor> floors=new ArrayList<>();
	/**
	 * Images that should be cohesive between {@link #floors}. {@link Chest}s,
	 * {@link Furniture}...
	 */
	public DungeonImages images;
	/** All available lore about this dungeon. */
	public Set<Lore> lore=new HashSet<>();
	/** How far a {@link Squad} can see inside a {@link DungeonFloor}. */
	public int vision=4;
	/** <code>false</code> if {@link Door}s should be drawn without background. */
	public boolean doorbackground=true;
	/** Estimate chance of having a {@link Fight} in a room. */
	public float ratiomonster=RPG.r(25,50)/100f;
	/** Estimate chance of having a {@link Feature} in a room. */
	public float ratiofeatures=RPG.r(50,95)/100f;
	/** Estimate chance of having a {@link Trap} in a room. */
	public float ratiotraps=RPG.r(10,25)/100f;
	/** Estimate chance of having a {@link Chest} in a room. */
	public float ratiotreasure=RPG.pick(List.of(5,10,20))/100f;
	/** @see Location */
	public DungeonEntrance entrance=null;
	/** @see Tier */
	public int level;
	/** A message to be shown when entering the dungeon or <code>null</code>. */
	public String fluff=null;
	/** Usually {@link Terrain#UNDERGROUND}. */
	public List<Terrain> terrains=List.of(Terrain.UNDERGROUND);

	String name;

	/**
	 * Top-level floor constructor.
	 *
	 * @param name If <code>null</code>, will {@link #baptize(String)}.
	 */
	public Dungeon(String name,int level,int nfloors){
		this.level=level;
		var tier=gettier();
		if(name==null) name=baptize(tier.name);
		this.name=name;
		images=new DungeonImages(tier);
		floors.add(createfloor(level));
		for(var i=1;i<nfloors;i++)
			floors.add(createfloor(level+i));
	}

	/** @return Allows subclasses to instantiate custom {@link #floors}. */
	protected DungeonFloor createfloor(int level){
		return new DungeonFloor(level,this);
	}

	/**
	 * @return Chooses and removes a name from {@link World#dungeonnames}.
	 * @throws RuntimeException When out of names.
	 */
	protected String baptize(String suffix){
		var names=World.getseed().dungeonnames;
		suffix=" "+suffix.toLowerCase();
		if(names.isEmpty()){
			if(Javelin.DEBUG) throw new RuntimeException("Out of dungeon names!");
			return "Nameless "+suffix;
		}
		var name=names.pop();
		name=name.substring(name.lastIndexOf(" ")+1,name.length());
		name+=name.charAt(name.length()-1)=='s'?"'":"'s";
		return name+suffix;
	}

	/** @see Lore */
	void generatelore(){
		for(var f:floors)
			lore.addAll(Lore.generate(this,f));
		if(!Lore.DEBUG){
			var byvalue=RPG.shuffle(new ArrayList<>(lore));
			byvalue.sort((a,b)->Integer.compare(b.value,a.value));
			var keep=RPG.randomize(3,1,byvalue.size());
			lore.retainAll(byvalue.subList(0,keep));
		}
	}

	/**
	 * Calls {@link #generate()} on all floors; then {@link Feature#define()} on
	 * each floor's {@link #features}; then generates {@link Lore}. Should be
	 * called only once, from top-level.
	 */
	public void generate(GenerationReport callback){
		var nfloors=floors.size();
		for(int i=0;i<nfloors;i++){
			Dungeon.active=floors.get(i);
			Dungeon.active.generate();
			if(callback!=null) callback.report(i/(nfloors*2));
		}
		for(int i=0;i<nfloors;i++){
			var floor=floors.get(i);
			Dungeon.active=floor;
			for(var feature:floor.features.getall())
				feature.define(floor,floors);
			if(callback!=null) callback.report(.5+i/nfloors);
		}
		generatelore();
	}

	/**
	 * If there are already explored {@link Passage}s in this dungeon which lead
	 * outside, allow the player to use them as entrances.
	 *
	 * @return Dungeon to open a {@link DungeonScreen} on or <code>null</code> to
	 *         cancel.
	 */
	protected DungeonFloor chooseentrance(){
		if(Dungeon.active!=null) return Dungeon.active;
		var entrances=new ArrayList<Feature>(floors.stream().sequential()
				.flatMap(f->f.features.getall(Passage.class).stream())
				.filter(p->p.destination==null&&p.found).collect(Collectors.toList()));
		var top=floors.get(0);
		if(entrances.isEmpty()) return top;
		var stairs=top.features.get(StairsUp.class);
		entrances.add(0,stairs);
		var choice=Javelin.choose("Use which entrance?",entrances,true,false);
		if(choice<0) return null;
		if(choice==0){
			top.squadlocation=stairs.getlocation();
			return top;
		}
		var p=(Passage)entrances.get(choice);
		p.floor.squadlocation=p.getlocation();
		return p.floor;
	}

	/** Create or recreate dungeon on {@link StateManager#load()}. */
	public void enter(){
		if(floors.get(0).features.isEmpty()){
			if(Javelin.DEBUG) throw new RuntimeException("Dungeon not generated!");
			generate((i)->{
				// should be handled by WorldGenerator but just in case...
				var report="Generating dungeon (%s%%)...";
				BattleScreen.active.messagepanel.clear();
				var progress=100*i/floors.size();
				Javelin.message(String.format(report,progress),Delay.NONE);
				BattleScreen.active.messagepanel.repaint();
			});
		}
		var f=chooseentrance();
		if(f==null) throw new RepeatTurn();
		f.enter();
	}

	/** @return Tier object. */
	public DungeonTier gettier(){
		return DungeonTier.get(level);
	}

	@Override
	public String toString(){
		return name;
	}

	/** @see Location#getimagename() */
	public String getimagename(){
		return gettier().name.toLowerCase();
	}

	/**
	 * @return <code>false</code> if any units aren't theme or
	 *         gameplay-appropriate. <code>true</code> by default.
	 */
	public boolean validate(List<Monster> monsters){
		return true;
	}

	/** @return A RandomDungeonEncounter. */
	public Fight fight(){
		return new RandomDungeonEncounter(active);
	}
}
