package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.EncountersByEl;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.table.Table;
import javelin.controller.table.Tables;
import javelin.controller.table.dungeon.ChestTable;
import javelin.controller.table.dungeon.door.DoorExists;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.controller.table.dungeon.feature.FeatureRarityTable;
import javelin.controller.table.dungeon.feature.FurnitureTable;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.controller.table.dungeon.feature.SpecialTrapTable;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.Crate;
import javelin.model.world.location.dungeon.feature.chest.RubyChest;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.trap.Trap;
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
	 * each step. Multiply by {@link #vision}.
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
	/**
	 * A grid of characters representing dungeon objects.
	 *
	 * @see MapTemplate
	 */
	public char[][] map=null;
	/**
	 * {@link #map} size (width and height).
	 *
	 * TODO support non-square maps
	 */
	public int size;
	/** Chance for a {@link RandomDungeonEncounter}. */
	public int stepsperencounter;
	/** Dungeon encounter level. -1 if not initialized. */
	public int level=-1;
	/** @see Table */
	public Tables tables;
	/**
	 * Table of encounters to roll from when generating
	 * {@link RandomDungeonEncounter}s.
	 *
	 * Entries can be set to <code>null</code> when certain encounters are
	 * pacified. If rolled, these will result in skipped encounters (ie: the
	 * {@link Squad} met them but they weren't hostile). Pacified encounters do
	 * not carry over to the next level.
	 *
	 * @see Leader
	 */
	public List<Combatants> encounters=new ArrayList<>();
	/** Dungeon this floor is a part of. */
	public Dungeon dungeon;
	/** @see Fight#map */
	public Terrain terrain=Terrain.UNDERGROUND;

	int revealed=0;

	transient int nrooms;

	/** Constructor for top floor. */
	public DungeonFloor(Integer level,Dungeon d){
		this.level=level;
		dungeon=d;
	}

	/**
	 * This function generates the dungeon map using {@link DungeonGenerator} and
	 * then {@link #createfeatures(int)}. One notable thing that happens here is
	 * the determination of how many {@link RandomDungeonEncounter}s should take
	 * for the player to explore the whole level.
	 *
	 * Currently, the calculation is done by setting a goal of one fight per room
	 * on average (so naturally, larger {@link DungeonTier}s will have more fights
	 * than smaller ones). The formula takes into account
	 * {@link DungeonScreen#VIEWRADIUS} instead of counting each step as a single
	 * tile.
	 *
	 * Since a Squad of the dungeon's intended level cannot hope to clear a
	 * dungeon if it's large (in average they can only take 4-5 encounters of the
	 * same EL), this is then offset by placing enough fountains that would
	 * theoretically allow them to do the one dungeon in one go.
	 *
	 * This is currently not counting backtracking out of the dungeon or finding
	 * your way back to town safely, so this naturally makes the dungeon more
	 * challenging (hopefully being offset by the rewards inside).
	 */
	public void generate(){
		if(map!=null) return;
		var previous=Dungeon.active;
		Dungeon.active=this;
		var p=getparent();
		tables=p==null?new Tables():p.tables.clone();
		map=map();
		size=map.length;
		generatedoors();
		stepsperencounter=calculateencounterrate();
		if(stepsperencounter<2) stepsperencounter=2;
		generateencounters();
		populate();
		visible=new boolean[size][size];
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				visible[x][y]=false;
		Dungeon.active=previous;
	}

	/** @return Floor above this one or <code>null</code> if top floor. */
	protected DungeonFloor getparent(){
		var floor=getfloor()-1;
		return floor==0?null:dungeon.floors.get(floor-1);
	}

	/**
	 * Generates Dungeon area.
	 *
	 * @return {@link #map}.
	 */
	protected char[][] map(){
		var tier=gettier();
		var generator=DungeonGenerator.generate(tier.minrooms,tier.maxrooms,this);
		nrooms=generator.map.rooms.size();
		return generator.grid;
	}

	/** Define {@link #encounters}. */
	protected void generateencounters(){
		var target=3+RPG.r(1,4)+DungeonTier.TIERS.indexOf(dungeon.gettier());
		var parent=getparent();
		if(parent!=null){
			encounters=new ArrayList<>(parent.encounters);
			while(encounters.contains(null))
				encounters.remove(null);
			encounters.sort(EncountersByEl.INSTANCE);
			var crop=RPG.r(1,encounters.size());
			encounters.removeAll(encounters.subList(0,crop));
		}
		var attempts=0;
		while(encounters.size()<target){
			var el=level+Difficulty.get();
			var encounter=generateencounter(el,dungeon.terrains);
			if(encounter!=null&&!encounters.contains(encounter))
				encounters.add(encounter);
			if(Javelin.DEBUG){
				attempts+=1;
				if(attempts>ENCOUNTERATTEMPTS){
					var error="Cannot generate encounters for level $s %s!";
					System.out.println(String.format(error,level,this));
				}
			}
		}
	}

	protected Combatants generateencounter(int level,List<Terrain> terrains){
		return EncounterGenerator.generate(level,terrains);
	}

	void generatedoors(){
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				if(map[x][y]==MapTemplate.DOOR
						&&gettable(DoorExists.class).rollboolean()){
					Door d=Door.generate(this,new Point(x,y));
					if(d!=null) d.place(this,d.getlocation());
				}
	}

	/**
	 * Tries to come up with a number roughly similar to what you'd have if you
	 * explored all rooms, fought all monsters and then left (plus random
	 * encounters).
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
		int floortiles=0;
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				if(map[x][y]==MapTemplate.FLOOR) floortiles+=1;
		return floortiles;
	}

	/**
	 * @return Generated furniture or <code>null</code> if Dungeon doesn't have
	 *         any.
	 */
	protected LinkedList<Furniture> generatefurniture(int minimum){
		var table=gettable(FurnitureTable.class);
		var unnocupied=new ArrayList<Point>(size*size/2);
		for(var x=0;x<size;x++)
			for(var y=0;y<size;y++){
				var p=new Point(x,y);
				if(!isoccupied(p)) unnocupied.add(p);
			}
		var target=RPG.randomize(dungeon.gettier().minrooms,minimum,
				unnocupied.size());
		RPG.shuffle(unnocupied);
		var furniture=new LinkedList<Furniture>();
		for(var i=0;i<target;i++){
			var f=new Furniture((String)table.roll());
			furniture.add(f);
			f.place(this,unnocupied.get(i));
		}
		return furniture;
	}

	/**
	 * Place {@link StairsUp}, deifne {@link #squadlocation} and create
	 * {@link Features}.
	 */
	protected void populate(){
		/*if placed too close to the edge, the carving in #createstairs() will make it look weird as the edge will look empty without walls */
		while(squadlocation==null||!squadlocation.validate(2,2,size-3,size-3))
			squadlocation=getunnocupied();
		var zoner=new DungeonZoner(this,squadlocation);
		generatestairs(zoner);
		generatekeys(zoner);
		var ntraps=getfeaturequantity(nrooms,dungeon.ratiotraps);
		int ntreasure=getfeaturequantity(nrooms,dungeon.ratiotreasure);
		var furniture=generatefurniture(ntraps+ntreasure+1);
		var traps=generatetraps(ntraps,furniture);
		var pool=0;
		for(var t:traps)
			pool+=RewardCalculator.getgold(t.cr);
		generatechests(ntreasure,pool,zoner,furniture);
		generatefeatures(getfeaturequantity(nrooms,dungeon.ratiofeatures),zoner);
	}

	void generatekeys(DungeonZoner zoner){
		try{
			var generated=new HashSet<Class<? extends Key>>();
			var area=new ArrayList<Point>();
			for(var zone:zoner.zones){
				area.addAll(zone.area);
				for(var door:zone.doors){
					if(!door.locked||!generated.add(door.key)) continue;
					Point p=null;
					while(p==null||isoccupied(p))
						p=RPG.pick(area);
					var key=door.key.getConstructor(DungeonFloor.class).newInstance(this);
					new Chest(key).place(this,p);
				}
			}
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/** * @return A free space in this dungeon floor. */
	public Point getunnocupied(){
		Point p=null;
		while(p==null||isoccupied(p))
			p=new Point(RPG.r(0,size-1),RPG.r(0,size-1));
		return p;
	}

	void generatestairs(DungeonZoner zoner){
		new StairsUp(squadlocation,this).place(this,squadlocation);
		for(int x=squadlocation.x-1;x<=squadlocation.x+1;x++)
			for(int y=squadlocation.y-1;y<=squadlocation.y+1;y++)
				map[x][y]=MapTemplate.FLOOR;
		if(!isdeepest()) new StairsDown().place(this,zoner.getpoint());
	}

	/** @return <code>true</code> if final floor. */
	public boolean isdeepest(){
		return dungeon.floors.indexOf(this)==dungeon.floors.size()-1;
	}

	/** @param nfeatures Target quantity of Features to place. */
	protected void generatefeatures(int nfeatures,DungeonZoner zoner){
		while(nfeatures>0){
			var f=generatefeature();
			f.place(this,zoner.getpoint());
			nfeatures-=1;
		}
	}

	/**
	 * @return A feature chosen from {@link #DEBUGFEATURE},
	 *         {@link RareFeatureTable} or {@link CommonFeatureTable}. May return
	 *         <code>null</code>, in which case it should usually be possible to
	 *         try again.
	 */
	protected Feature generatefeature(){
		if(Javelin.DEBUG&&DEBUGFEATURE!=null) try{
			return DEBUGFEATURE.getDeclaredConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			if(Javelin.DEBUG) throw new RuntimeException(e);
			return null;
		}
		var t=gettable(FeatureRarityTable.class).roll();
		return t.rollfeature(this);
	}

	static int getfeaturequantity(int quantity,float ratio){
		return RPG.randomize(Math.round(quantity*ratio),0,Integer.MAX_VALUE);
	}

	List<Trap> generatetraps(int ntraps,LinkedList<Furniture> furniture){
		var modifier=gettable(FeatureModifierTable.class);
		var special=gettable(SpecialTrapTable.class);
		var traps=new ArrayList<Trap>(ntraps);
		for(var i=0;i<ntraps;i++){
			var cr=level+Difficulty.get()+modifier.roll();
			var t=Trap.generate(cr,special.rollboolean());
			if(t!=null){
				traps.add(t);
				if(furniture==null)
					t.place(this,getunnocupied());
				else
					furniture.pop().hide(t);
			}
		}
		return traps;
	}

	/**
	 * @return <code>true</code> if given point is between 0 and {@link #SIZE}.
	 */
	public boolean valid(int coordinate){
		return 0<=coordinate&&coordinate<=size;
	}

	void generatechest(Class<? extends Chest> type,int gold,DungeonZoner zoner,
			Furniture f){
		var percentmodifier=gettable(FeatureModifierTable.class).roll()*2;
		gold=Math.round(gold*(100+percentmodifier)/100f);
		try{
			var c=type.getConstructor(Integer.class).newInstance(gold);
			if(!c.generateitem()){
				c=new Chest(gold);
				c.generateitem();
				if(f!=null){
					f.hide(c);
					return;
				}
			}
			c.place(this,zoner.getpoint());
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	void generatechests(int chests,int pool,DungeonZoner zoner,
			LinkedList<Furniture> furniture){
		generatespecialchest().place(this,zoner.getpoint());
		if(pool==0) return;
		if(chests<1) chests=1;
		var hidden=Math.max(2,chests/10);
		hidden=RPG.randomize(hidden,0,chests);
		if(furniture!=null&&hidden>0){
			chests-=hidden;
			var hiddenpool=pool/2;
			pool-=hiddenpool;
			for(var i=0;i<hidden;i++)
				generatechest(Chest.class,pool/hidden,zoner,furniture.pop());
		}
		var t=gettable(ChestTable.class);
		for(var i=0;i<chests;i++)
			generatechest(t.roll(),pool/chests,zoner,null);
		generatecrates(zoner);
	}

	/** @see Crate */
	protected void generatecrates(DungeonZoner zoner){
		var freebie=RewardCalculator.getgold(level);
		var ncrates=RPG.randomize(dungeon.gettier().minrooms,0,Integer.MAX_VALUE);
		for(int i=0;i<ncrates;i++){
			var gold=RPG.randomize(freebie/ncrates,1,Integer.MAX_VALUE);
			generatechest(Crate.class,gold,zoner,null);
		}
	}

	/** @return Most special chest here. */
	protected Feature generatespecialchest(){
		return new RubyChest();
	}

	public boolean isoccupied(Point p){
		if(map[p.x][p.y]==MapTemplate.WALL) return true;
		for(Feature f:features)
			if(f.x==p.x&&f.y==p.y) return true;
		return false;
	}

	/** Exit and destroy this dungeon. */
	public void leave(){
		WorldMove.abort=true;
		JavelinApp.context=new WorldScreen(true);
		BattleScreen.active=JavelinApp.context;
		Squad.active.place();
		Dungeon.active=null;
	}

	/**
	 * Called when reaching {@link StairsUp}
	 */
	public void goup(){
		Squad.active.delay(1);
		int floor=dungeon.floors.indexOf(this);
		if(floor==0)
			Dungeon.active.leave();
		else{
			var up=dungeon.floors.get(floor-1);
			up.squadlocation=up.features.get(StairsDown.class).getlocation();
			up.enter();
		}
	}

	/**
	 * Called when reaching {@link StairsDown}.
	 */
	public void godown(){
		Squad.active.delay(1);
		var floor=dungeon.floors.get(dungeon.floors.indexOf(this)+1);
		floor.squadlocation=floor.features.get(StairsUp.class).getlocation();
		floor.enter();
	}

	/**
	 * Akin to terrain {@link Hazard}s.
	 *
	 * @return <code>true</code> if a hazard happens.
	 */
	public boolean hazard(){
		// no hazards in normal dungeons
		return false;
	}

	public void setvisible(int x,int y){
		if(!valid(x)||!valid(y)) return;
		visible[x][y]=true;
		BattleScreen.active.mappanel.tiles[x][y].discovered=true;
	}

	public void discover(Feature f){
		setvisible(f.x,f.y);
		f.discover(null,9000);
	}

	public static <K extends Table> K gettable(Class<K> table){
		return Dungeon.active.tables.get(table);
	}

	public List<Combatant> rasterizenecounters(){
		ArrayList<Combatant> enemies=new ArrayList<>();
		for(Combatants encounter:encounters)
			enemies.addAll(encounter);
		Collections.shuffle(enemies);
		return enemies;
	}

	/**
	 * @param to Moves {@link Squad} location to these coordinates.
	 */
	public void teleport(Point to){
		squadlocation=to;
		JavelinApp.context.view(to.x,to.y);
		WorldMove.abort=true;
	}

	/**
	 * @return A human-intended count of how deep this floor is (1 for first, 2
	 *         for the one below that, etc).
	 * @see #floors
	 */
	public int getfloor(){
		return dungeon.floors.indexOf(this)+1;
	}

	@Override
	public String toString(){
		return dungeon.name+" (floor "+getfloor()+")";
	}

	/** Enters thsi particular floor. */
	public void enter(){
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
}
