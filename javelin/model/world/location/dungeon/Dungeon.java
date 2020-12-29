package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.StateManager;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.template.Template;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Passage;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.SpecialChest;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.model.world.location.unique.DeepDungeon;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * A set of {@link DungeonFloor}s.
 *
 * @author alex
 */
public class Dungeon implements Serializable{
	/**
	 * This is a trade-off between having the most variety in {@link Encounter}s
	 * with how long that process takes to complete for {@link WorldGenerator}.
	 * This is only one of the optimization steps taken when it comes to applying
	 * {@link Template}s to encounters and it's only barely fast enough.
	 */
	static final int TEMPLATEENCOUNTERS=100/3;

	/** Current dungeon or <code>null</code> if not in one. */
	public static DungeonFloor active=null;

	/**
	 * All floors that make part of this dungeon. Each floor deeper is a
	 * {@link #level} higher.
	 */
	public LinkedList<DungeonFloor> floors=new LinkedList<>();
	/**
	 * Images that should be cohesive between {@link #floors}. {@link Chest}s,
	 * {@link Decoration}...
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
	public List<Terrain> terrains=new ArrayList<>(List.of(Terrain.UNDERGROUND));
	/** Usually either zero or two, except {@link Temple}s with one. */
	public List<Branch> branches=new ArrayList<>(2);

	/** Dungeon name. */
	protected String name;

	/**
	 * Top-level floor constructor.
	 *
	 * @param name If <code>null</code>, will {@link #baptize(String)}.
	 */
	public Dungeon(String name,int level,int nfloors){
		this.level=level;
		var t=gettier();
		this.name=name;
		images=new DungeonImages(t);
		for(var i=0;i<nfloors;i++)
			floors.add(createfloor(level+i));
	}

	/** @return Allows subclasses to instantiate custom {@link #floors}. */
	protected DungeonFloor createfloor(int level){
		return new DungeonFloor(level,this);
	}

	/**
	 * @return Chooses and removes a name from {@link World#dungeonnames}.
	 * @throws NoSuchElementException If out of names.
	 */
	synchronized protected String baptize(String base){
		if(!branches.isEmpty()){
			var prefix=branches.get(0).prefix;
			base=base.toLowerCase();
			var suffix=branches.get(1).suffix.toLowerCase();
			return String.format("%s %s %s",prefix,base,suffix);
		}
		var names=World.getseed().dungeonnames;
		if(names.isEmpty()){
			if(Javelin.DEBUG)
				throw new NoSuchElementException("Out of dungeon names!");
			return "Nameless "+base;
		}
		var name=names.pop();
		name=name.substring(name.lastIndexOf(" ")+1,name.length());
		name+=name.charAt(name.length()-1)=='s'?"'":"'s";
		return name+" "+base.toLowerCase();
	}

	/** @see Lore */
	void generatelore(){
		for(var f:floors)
			lore.addAll(Lore.generate(f));
		if(!Lore.DEBUG){
			var byvalue=RPG.shuffle(new ArrayList<>(lore));
			byvalue.sort((a,b)->-Integer.compare(a.value,b.value));
			var keep=RPG.randomize(3,1,byvalue.size());
			lore.retainAll(byvalue.subList(0,keep));
		}
	}

	List<EncounterIndex> indexencounters(){
		var terrain=new ArrayList<>(terrains);
		for(var b:branches)
			terrain.addAll(b.terrains);
		var indexes=terrain.stream()
				.map(t->Organization.ENCOUNTERSBYTERRAIN.get(t.toString()))
				.collect(Collectors.toList());
		var templates=branches.stream().flatMap(b->b.templates.stream())
				.collect(Collectors.toList());
		if(templates.isEmpty()) return indexes;
		var encounters=indexes.stream().flatMap(i->i.values().stream())
				.flatMap(es->es.stream())
				.filter(e->Difficulty.VERYEASY<=e.el-level
						&&e.el-level<=Difficulty.DIFFICULT&&validate(e.group))
				.collect(Collectors.toList());
		var modified=new EncounterIndex();
		var total=0;
		for(var e:RPG.shuffle(encounters)){
			for(var t:templates){
				var combatants=e.generate();
				if(t.apply(combatants,this)>0){
					modified.put(new Encounter(combatants));
					total+=1;
				}
			}
			if(total>=TEMPLATEENCOUNTERS) return List.of(modified);
		}
		//if TEMPLATEENCOUNTERS is done away with, remove this:
		indexes=new ArrayList<>(indexes);
		indexes.add(modified);
		return indexes;
	}

	/**
	 * Calls {@link #generate()} on all floors; then {@link Feature#define()} on
	 * each floor's {@link #features}; then generates {@link Lore}.
	 */
	public void generate(){
		var encounters=indexencounters();
		for(var f:floors)
			f.generate(encounters);
		for(var f:floors)
			for(var feature:f.features.getall())
				feature.define(f,floors);
		generatelore();
		generateappearance();
		for(var b:branches)
			b.define(this);
		name=baptize(name);
		if(entrance!=null) entrance.set(this);
	}

	/**
	 * If there are {@link #branches}, pick between them and the base image for
	 * each tile, meaning there are 9 tileset variations (per
	 * {@link DungeonTier})! Simple but pretty effective!
	 */
	protected void generateappearance(){
		var nbranches=branches.size();
		var floor=RPG.r(0,nbranches);
		if(floor<nbranches)
			images.put(DungeonImages.FLOOR,branches.get(floor).floor);
		var wall=RPG.r(0,nbranches);
		if(wall<nbranches){
			var b=branches.get(wall);
			images.put(DungeonImages.WALL,b.floor);
			doorbackground=b.doorbackground;
		}
	}

	/**
	 * If there are already explored {@link Passage}s in this dungeon which lead
	 * outside, allows the player to use them as entrances.
	 *
	 * @return Floor with location set.
	 * @throws RepeatTurn If cancelled.
	 * @see DungeonFloor#squadlocation
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
		if(choice<0) throw new RepeatTurn();
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
			if(Javelin.DEBUG) throw new RuntimeException(this+" not generated!");
			BattleScreen.active.messagepanel.clear();
			Javelin.message("Generating dungeon, please wait...",Delay.NONE);
			BattleScreen.active.messagepanel.repaint();
			generate();
		}
		chooseentrance().enter();
	}

	/**
	 * Whenever possible, it's preferrable to use {@link DungeonFloor#gettier()}
	 * instead, as it may be overriden.
	 *
	 * @see DeepDungeon
	 */
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

	/** @see RandomDungeonEncounter */
	public RandomDungeonEncounter fight(){
		var f=new RandomDungeonEncounter(active);
		for(var b:RPG.shuffle(new ArrayList<>(branches)))
			f.mutators.addAll(b.mutators);
		return f;
	}

	/** @return <code>false</code> to discard this {@link Encounter}. */
	public boolean validate(List<Combatant> group){
		for(var b:branches)
			if(!b.validate(group)) return false;
		return true;
	}

	/** @see SpecialChest */
	protected Feature generatespecialchest(DungeonFloor f){
		if(f==floors.getLast()) return new SpecialChest(f,new Ruby());
		var value=RewardCalculator.getgold(f.level);
		var items=RPG.shuffle(new ArrayList<>(Item.ITEMS));
		var item=items.stream().filter(i->value/2<=i.price&&i.price<=value*2)
				.findAny().orElse(null);
		return new SpecialChest(f,item==null?new Ruby():item);
	}
}
