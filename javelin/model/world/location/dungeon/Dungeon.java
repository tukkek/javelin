package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.EncountersByEl;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.table.Table;
import javelin.controller.table.Tables;
import javelin.controller.table.dungeon.door.DoorExists;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.controller.table.dungeon.feature.FeatureRarityTable;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.controller.table.dungeon.feature.SpecialTrapTable;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.item.Item;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.mappanel.dungeon.DungeonTile;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;

/**
 * A dungeon is an underground area of the world where the combats are harder
 * but have extra treasure laying around.
 *
 * The in-game logic for dungeons is that they are a hideout of bandits or
 * similar, which is why they are sacrificeable by {@link Incursion}s and are
 * removed from the game after a {@link Squad} leaves one (in this case it's
 * assumed the bandits packed their stuff and left).
 *
 * @author alex
 */
public class Dungeon extends Location{
	static final Class<? extends Feature> DEBUGFEATURE=null;
	static final int MAXTRIES=1000;
	static final int[] DELTAS={-1,0,1};

	/**
	 * A loose approximation of how many {@link DungeonTile}s are revealed with
	 * each step. Multiply by {@link #vision}.
	 */
	protected static final int DISCOVEREDPERSTEP=4;

	/**
	 * Current {@link Dungeon} or <code>null</code> if not in one. During a
	 * {@link #define()} operation, this can be set to any one instance being
	 * mapped.
	 */
	public static Dungeon active=null;

	/** All of this dungeon's {@link Feature}s. */
	public Features features=new Features(this);
	/**
	 * Explored squares in this dungeon.
	 *
	 * TODO why is there this and also {@link #discovered}?
	 */
	public boolean[][] visible;
	/**
	 * Current {@link Squad} location.
	 *
	 * TODO is this needed?
	 */
	public Point herolocation=null;
	/** File to use under 'avatar' folder. */
	public String tilefloor;
	/** File to use under 'avatar' folder. */
	public String tilewall;
	/** Tiles already revealed. */
	public HashSet<Point> discovered=new HashSet<>();
	/**
	 * A grid of characters representing dungeon objects.
	 *
	 * @see Template
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
	/** <code>false</code> if doors should be drawn without background. */
	public boolean doorbackground=true;
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
	/** How far a {@link Squad} can see inside a {@link Dungeon}. */
	public int vision=4;

	float ratiomonster=RPG.r(25,50)/100f;
	float ratiofeatures=RPG.r(50,95)/100f;
	float ratiotraps=RPG.r(10,25)/100f;
	float ratiotreasure=RPG.r(5,10)/100f;

	int revealed=0;
	Dungeon parent;

	transient boolean generated=false;

	/** All floors that make part of this dungeon. */
	protected List<? extends Dungeon> floors;

	transient int nrooms;

	/** Constructor. */
	public Dungeon(String name,Integer level,Dungeon parent,
			List<? extends Dungeon> floors){
		super(null);
		this.level=level;
		this.parent=parent;
		this.floors=floors;
		link=false;
		discard=false;
		impermeable=true;
		allowedinscenario=false;
		var tier=gettier();
		tilewall=tier.wall;
		tilefloor=tier.floor;
		description=name==null?baptize(tier.name):name;
		allowentry=false;
		unique=true;
	}

	/**
	 * @return Generates a name from {@link World#dungeonnames}, where a name will
	 *         be removed from permanently.
	 * @throws RuntimeException When out of names.
	 */
	protected String baptize(String suffix){
		LinkedList<String> names=World.getseed().dungeonnames;
		suffix=" "+suffix.toLowerCase();
		if(names.isEmpty()){
			if(Javelin.DEBUG) throw new RuntimeException("Out of dungeon names!");
			return "Nameless"+suffix;
		}
		String name=names.pop();
		name=name.substring(name.lastIndexOf(" ")+1,name.length());
		name+=name.charAt(name.length()-1)=='s'?"'":"'s";
		return name+suffix;
	}

	@Override
	public boolean interact(){
		if(Javelin.prompt("You are about to enter: "+describe()+".\n"
				+"Press ENTER to continue or any other key to cancel...")!='\n')
			return true;
		activate(false);
		return true;
	}

	/** Create or recreate dungeon. */
	public void activate(boolean loading){
		active=this;
		while(features.isEmpty()){
			MessagePanel.active.clear();
			Javelin.message("Generating dungeon map...",Javelin.Delay.NONE);
			define();
		}
		regenerate(loading);
		JavelinApp.context=new DungeonScreen(this);
		BattleScreen.active=JavelinApp.context;
		Squad.active.updateavatar();
		BattleScreen.active.mappanel.center(herolocation.x,herolocation.y,true);
		features.getknown();
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
	public void define(){
		if(map!=null) return;
		var previous=Dungeon.active;
		Dungeon.active=this;
		map=map();
		size=map.length;
		stepsperencounter=calculateencounterfrequency();
		if(stepsperencounter<2) stepsperencounter=2;
		generateencounters();
		populatedungeon();
		size=map.length;
		visible=new boolean[size][size];
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				visible[x][y]=false;
		Dungeon.active=previous;
	}

	/**
	 * Generates Dungeon area.
	 *
	 * @return {@link #map}.
	 */
	protected char[][] map(){
		DungeonTier tier=gettier();
		var tables=parent==null?null:parent.tables;
		var generator=DungeonGenerator.generate(tier.minrooms,tier.maxrooms,tables);
		this.tables=generator.tables;
		createdoors();
		nrooms=generator.map.rooms.size();
		return generator.grid;
	}

	/** Define {@link #encounters}. */
	protected void generateencounters(){
		var target=3+RPG.r(1,4)+DungeonTier.TIERS.indexOf(gettier());
		if(parent!=null){
			encounters=new ArrayList<>(parent.encounters);
			while(encounters.contains(null))
				encounters.remove(null);
			encounters.sort(EncountersByEl.INSTANCE);
			var crop=RPG.r(1,encounters.size());
			for(;crop>0&&!encounters.isEmpty();crop--)
				encounters.remove(0);
		}
		var terrains=Arrays.asList(new Terrain[]{Terrain.UNDERGROUND});
		while(encounters.size()<target)
			try{
				var el=level+Difficulty.get();
				var encounter=generateencounter(el,terrains);
				if(encounter!=null&&!encounters.contains(encounter))
					encounters.add(encounter);
			}catch(GaveUp e){
				continue;
			}
	}

	protected Combatants generateencounter(int level,List<Terrain> terrains)
			throws GaveUp{
		return EncounterGenerator.generate(level,terrains);
	}

	void createdoors(){
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				if(map[x][y]==Template.DOOR&&gettable(DoorExists.class).rollboolean()){
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
	protected int calculateencounterfrequency(){
		float encounters=nrooms*1.1f*ratiomonster;
		float tilesperroom=countfloor()/nrooms;
		float steps=encounters*tilesperroom/(DISCOVEREDPERSTEP*vision);
		return Math.round(steps/2);
	}

	int countfloor(){
		int floortiles=0;
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
				if(map[x][y]==Template.FLOOR) floortiles+=1;
		return floortiles;
	}

	/**
	 * Place {@link StairsUp}, deifne {@link #herolocation} and create
	 * {@link Features}.
	 */
	protected void populatedungeon(){
		/*if placed too close to the edge, the carving in #createstairs() will make it look weird as the edge will look empty without walls */
		while(herolocation==null||!herolocation.validate(2,2,size-3,size-3))
			herolocation=getrandompoint();
		var zoner=new DungeonZoner(this,herolocation);
		createstairs(zoner);
		createkeys(zoner);
		int goldpool=createtraps(getfeaturequantity(nrooms,ratiotraps));
		createchests(getfeaturequantity(nrooms,ratiotreasure),goldpool,zoner);
		createfeatures(getfeaturequantity(nrooms,ratiofeatures),zoner);
	}

	void createkeys(DungeonZoner zoner){
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
					var key=door.key.getConstructor(Dungeon.class).newInstance(this);
					new Chest(p.x,p.y,key).place(this,p);
				}
			}
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	Point getrandompoint(){
		Point p=null;
		while(p==null||isoccupied(p))
			p=new Point(RPG.r(0,size-1),RPG.r(0,size-1));
		return p;
	}

	void createstairs(DungeonZoner zoner){
		new StairsUp(herolocation).place(this,herolocation);
		for(int x=herolocation.x-1;x<=herolocation.x+1;x++)
			for(int y=herolocation.y-1;y<=herolocation.y+1;y++)
				map[x][y]=Template.FLOOR;
		if(floors.indexOf(this)<floors.size()-1){
			var p=zoner.getpoint();
			new StairsDown(p).place(this,p);
		}
	}

	protected void createfeatures(int nfeatures,DungeonZoner zoner){
		try{
			while(nfeatures>0){
				Feature f=createfeature();
				if(f!=null){
					f.place(this,zoner.getpoint());
					nfeatures-=1;
				}
			}
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return A feature chosen from {@link #DEBUGFEATURE},
	 *         {@link RareFeatureTable} or {@link CommonFeatureTable}.
	 */
	protected Feature createfeature() throws ReflectiveOperationException{
		if(Javelin.DEBUG&&DEBUGFEATURE!=null)
			return DEBUGFEATURE.getDeclaredConstructor().newInstance();
		var table=gettable(FeatureRarityTable.class).rollboolean()
				?RareFeatureTable.class
				:CommonFeatureTable.class;
		return tables.get(table).rollfeature(this);
	}

	static int getfeaturequantity(int quantity,float ratio){
		quantity=Math.round(quantity*ratio);
		return quantity+RPG.randomize(quantity);
	}

	int createtraps(int ntraps){
		int gold=0;
		for(int i=0;i<ntraps;i++){
			int cr=level+Difficulty.get()
					+gettable(FeatureModifierTable.class).rollmodifier();
			boolean special=gettable(SpecialTrapTable.class).rollboolean();
			var p=getrandompoint();
			Trap t=Trap.generate(cr,special,p);
			if(t==null) continue;
			t.place(this,p);
			if(cr>0) gold+=RewardCalculator.getgold(t.cr);
		}
		return gold;
	}

	/**
	 * @return <code>true</code> if given point is between 0 and {@link #SIZE}.
	 */
	public boolean valid(int coordinate){
		return 0<=coordinate&&coordinate<=size;
	}

	void createchests(int chests,int pool,DungeonZoner zoner){
		var p=zoner.getpoint();
		createspecialchest(p).place(this,p);
		var hiddenchests=0;
		for(int i=0;i<chests;i++)
			if(RPG.chancein(10)) hiddenchests+=1;
		int hiddenpool=RewardCalculator.getgold(level)*chests;
		hiddenchests=0; //TODO have actually hidden chests
		chests+=hiddenchests; //TODO have actually hidden chests
		chests+=RPG.r(-1,+1);
		if(hiddenchests==0) pool+=hiddenpool;
		for(;chests>0;chests--){
			int gold=chests==1?pool:pool/RPG.r(2,chests);
			pool-=gold;
			createchest(gold,zoner,false);
		}
		for(;hiddenchests>0;hiddenchests--){
			int gold=hiddenchests==1?hiddenpool:hiddenpool/RPG.r(2,chests);
			hiddenpool-=gold;
			createchest(gold,zoner,true);
		}
	}

	/**
	 * TODO hidden chests would probably require copious amounts of decoration to
	 * hide the actual chests as.
	 */
	void createchest(int gold,DungeonZoner zoner,boolean hidden){
		int percentmodifier=gettable(FeatureModifierTable.class).rollmodifier()*2;
		gold=gold*(100+percentmodifier)/100;
		Dungeon toplevel=this;
		while(toplevel.parent!=null)
			toplevel=toplevel.parent;
		var chest=new Chest(gold,zoner.getpoint());
		chest.place(this,chest.getlocation());
	}

	/**
	 * @param p Chest's location.
	 * @return Most special chest here.
	 */
	protected Feature createspecialchest(Point p){
		Item i=World.scenario.openspecialchest();
		Chest c=new Chest(p.x,p.y,i);
		c.setspecial();
		return c;
	}

	public boolean isoccupied(Point p){
		if(map[p.x][p.y]==Template.WALL) return true;
		for(Feature f:features)
			if(f.x==p.x&&f.y==p.y) return true;
		return false;
	}

	/** Exit and destroy this dungeon. */
	public void leave(){
		JavelinApp.context=new WorldScreen(true);
		BattleScreen.active=JavelinApp.context;
		Squad.active.place();
		Dungeon.active=null;
		if(World.scenario.expiredungeons&&expire()) remove();
	}

	protected boolean expire(){
		for(Feature f:features){
			Chest c=f instanceof Chest?(Chest)f:null;
			if(c!=null&&c.special) return false;
		}
		return true;
	}

	void regenerate(boolean loading){
		if(!generated){
			for(Feature f:features)
				f.generate();
			generated=true;
		}
	}

	@Override
	public Integer getel(Integer attackel){
		return attackel-3;
	}

	/**
	 * Called when reaching {@link StairsUp}
	 */
	public void goup(){
		Squad.active.ellapse(1);
		int floor=floors.indexOf(this);
		if(floor==0)
			Dungeon.active.leave();
		else
			floors.get(floor-1).activate(false);
	}

	/**
	 * Called when reaching {@link StairsDown}.
	 */
	public void godown(){
		Squad.active.ellapse(1);
		floors.get(floors.indexOf(this)+1).activate(false);
	}

	/** See {@link WorldScreen#encounter()}. */
	@Override
	public Fight fight(){
		return new RandomDungeonEncounter(this);
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

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	public void setvisible(int x,int y){
		visible[x][y]=true;
		BattleScreen.active.mappanel.tiles[x][y].discovered=true;
	}

	static public List<Dungeon> getdungeons(){
		ArrayList<Actor> actors=World.getall(Dungeon.class);
		ArrayList<Dungeon> dungeons=new ArrayList<>(actors.size());
		for(Actor a:actors)
			dungeons.add((Dungeon)a);
		return dungeons;
	}

	public DungeonTier gettier(){
		for(DungeonTier t:DungeonTier.TIERS)
			if(level<=t.tier.maxlevel) return t;
		return DungeonTier.HIGHEST;
	}

	@Override
	public String describe(){
		int squadel=ChallengeCalculator.calculateel(Squad.active.members);
		String difficulty=Difficulty.describe(level-squadel);
		return description+" ("+difficulty+").";
	}

	@Override
	public String getimagename(){
		return "location"+gettier().name.toLowerCase();
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
		herolocation=to;
		JavelinApp.context.view(to.x,to.y);
		WorldMove.abort=true;
	}

	/**
	 * @return A human-intended count of how deep this floor is (1 for first, 2
	 *         for the one below that, etc).
	 * @see #floors
	 */
	public int getfloor(){
		return floors.indexOf(this)+1;
	}

	/**
	 * @return Builds a baseline dungeon with one or more {@link #floors} based on
	 *         its {@link DungeonTier}.
	 */
	public static Dungeon generate(int level){
		var floors=new ArrayList<Dungeon>();
		var top=new Dungeon(null,level,null,floors);
		floors.add(top);
		var tier=DungeonTier.get(level);
		var maxdepth=DungeonTier.TIERS.indexOf(tier)+1;
		var parent=top;
		while(floors.size()<maxdepth&&RPG.chancein(2)){
			var floor=new Dungeon(null,parent.level+1,parent,floors);
			floors.add(floor);
			parent=floor;
		}
		return top;
	}
}
