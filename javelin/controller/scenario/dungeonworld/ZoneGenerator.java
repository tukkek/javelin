package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.transport.Airship;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.temple.AirTemple;
import javelin.model.world.location.dungeon.temple.EarthTemple;
import javelin.model.world.location.dungeon.temple.EvilTemple;
import javelin.model.world.location.dungeon.temple.FireTemple;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.model.world.location.dungeon.temple.MagicTemple;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.dungeon.temple.WaterTemple;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.cultural.MagesGuild.BuildMagesGuild;
import javelin.model.world.location.town.labor.expansive.Hub;
import javelin.model.world.location.town.labor.productive.Shop;
import javelin.old.RPG;

/**
 * Generates the {@link DungeonWorld} towns, {@link Gate}s and {@link District}
 * {@link Location}s.
 *
 * @author alex
 */
public class ZoneGenerator extends FeatureGenerator{
	static final double MINDISTANCE=5;
	static final ArrayList<Trait> TRAITS=new ArrayList<>(Deck.TRAITS);
	static final ArrayList<Realm> REALMS=new ArrayList<>(
			Arrays.asList(Realm.values()));

	static{
		Collections.shuffle(TRAITS);
		Collections.shuffle(REALMS);
	}

	class Zone{
		HashSet<Point> area=new HashSet<>();
		ArrayList<Point> arealist=new ArrayList<>();
		HashMap<Zone,HashSet<Point>> borders=new HashMap<>();
		ArrayList<Gate> gates=new ArrayList<>();
		Realm realm;
		int level;

		public Zone(Realm r){
			realm=r;
		}

		void add(Point p){
			if(area.add(p)) arealist.add(p);
		}

		void expand(){
			Point p=DungeonWorldGenerator.expand(arealist,null);
			if(!World.validatecoordinate(p.x,p.y)) return;
			int neighborindex=checkclaimed(p);
			if(neighborindex<0){
				add(p);
				claimed+=1;
			}else if(neighborindex!=Integer.MAX_VALUE
					&&neighborindex!=zones.indexOf(this)){
				Zone neighbor=zones.get(neighborindex);
				addborder(neighbor,p);
				neighbor.addborder(this,p);
				if(allborders.add(p)) claimed+=1;
			}
		}

		void addborder(Zone neighbor,Point p){
			area.remove(p);
			arealist.remove(p);
			HashSet<Point> border=borders.get(neighbor);
			if(border==null){
				border=new HashSet<>(1);
				borders.put(neighbor,border);
			}
			border.add(p);
		}

		HashSet<Point> enclose(){
			HashSet<Point> frontier=new HashSet<>();
			for(Point territory:area)
				for(Point p:Point.getadjacent2()){
					p.x+=territory.x;
					p.y+=territory.y;
					if(World.validatecoordinate(p.x,p.y)&&!frontier.contains(p)
							&&!allborders.contains(p)&&!area.contains(p))
						frontier.add(p);
				}
			return frontier;
		}
	}

	/**
	 * Keys in the order the player needs to find them to beat the game.
	 *
	 * @see #checksolvable()
	 */
	transient ArrayList<Realm> keys=new ArrayList<>();
	transient HashSet<Point> allborders=new HashSet<>();
	transient ArrayList<Zone> zones=new ArrayList<>();
	transient int claimed=0;
	transient int worldsize;
	transient World world;
	transient ArrayList<Town> towns=new ArrayList<>();

	@Override
	public void spawn(float chance,boolean generatingworld){
		// don't: static world
	}

	/**
	 * @return Same {@link Zone} if has been claimed by border.
	 */
	int checkclaimed(Point p){
		if(allborders.contains(p)) return Integer.MAX_VALUE;
		for(int i=0;i<zones.size();i++){
			Zone z=zones.get(i);
			if(z.area.contains(p)) return i;
		}
		return -1;
	}

	@Override
	public Town generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		world=w;
		worldsize=World.scenario.size*World.scenario.size;
		while(zones.isEmpty())
			generatezones(7);
		while(claimed<worldsize)
			RPG.pick(zones).expand();
		createborders();
		placegates(zones.get(0),new HashSet<Zone>(zones.size()));
		shufflegates();
		if(!checksolvable()) throw new RestartWorldGeneration();
		for(Zone z:zones){
			z.level=1+keys.indexOf(z.realm)*19/(realms.size()-1);
			placefeatures(z);
		}
		createmagicdocks();
		if(counttemples()!=7) throw new RestartWorldGeneration();
		return towns.get(0);
	}

	/**
	 * Make sure there's at least one place to get a kewl {@link Airship}!
	 */
	void createmagicdocks(){
		ArrayList<Hub> hubs=new ArrayList<>();
		for(Actor a:World.getactors())
			if(a instanceof Hub){
				Hub hub=(Hub)a;
				if(hub.level==2) return;
				hubs.add(hub);
			}
		if(!hubs.isEmpty()){
			RPG.pick(hubs).level=2;
			return;
		}
		Zone z=RPG.pick(zones.subList(1,zones.size()));
		Town t=towns.get(zones.indexOf(z));
		Hub hub=new Hub();
		hub.upgradetomagicdocks();
		placeintown(hub,t.getdistrict(),z);
	}

	void shufflegates(){
		for(Zone z:zones){
			if(z.gates.size()<=1) continue;
			ArrayList<Realm> keys=new ArrayList<>(z.gates.size());
			for(Gate g:z.gates)
				keys.add(g.key);
			Collections.shuffle(keys);
			for(int i=0;i<z.gates.size();i++)
				z.gates.get(i).setkey(keys.get(i));
		}
	}

	boolean checksolvable(){
		HashSet<Zone> visited=new HashSet<>();
		visited.add(zones.get(0));
		keys.add(zones.get(0).realm);
		while(true){
			int oldvisited=visited.size();
			for(Zone z:new ArrayList<>(visited))
				for(Gate g:z.gates)
					if(keys.contains(g.key)){
						Zone to=g.to;
						visited.add(to);
						if(!keys.contains(to.realm)) keys.add(to.realm);
					}
			int newvisited=visited.size();
			if(newvisited==REALMS.size())
				return true;
			else if(newvisited==oldvisited) return false;
		}
	}

	/**
	 * TODO make it throw a fatal error so we can determine why someitmes temples
	 * are missing. a similar problem was found before in the feature-placing
	 * function.
	 */
	int counttemples(){
		int temples=0;
		for(Actor a:World.getactors())
			if(a instanceof Temple) temples+=1;
		return temples;
	}

	void placefeatures(Zone z){
		placefeature(createtemple(z.realm,z.level),z);
		int tiers=RPG.r(1,4)+RPG.r(1,4);
		createtown(z);
		while(tiers>0){
			Dungeon d=placefeature(createdungeon(z),z);
			tiers-=DungeonTier.TIERS.indexOf(d.gettier())+1;
		}
	}

	Dungeon createdungeon(Zone z){
		int level=-1;
		while(level<1||level>20)
			level=z.level+RPG.randomize(6);
		return Dungeon.generate(level);
	}

	Town createtown(Zone z){
		Town t=placefeature(new Town((Point)null,z.realm),z);
		int size=z.level;
		if(towns.isEmpty()){
			size=11;
			District d=t.getdistrict();
			placeintown(new BuildMagesGuild().generateacademy(),d,z);
			placeintown(RPG.pick(Kit.KITS).createguild(),d,z);
			placeintown(new Shop(true),d,z);
			placeintown(new Lodge(),d,z);
		}
		towns.add(t);
		TRAITS.get(zones.indexOf(z)).addto(t);
		size=Math.min(15,size);
		while(t.population<size)
			t.getgovernor().work(1,t.getdistrict());
		for(Labor l:new ArrayList<>(t.getgovernor().getprojects()))
			l.cancel();
		return t;
	}

	void placeintown(Location l,District d,Zone z){
		ArrayList<Actor> actors=World.getactors();
		Point p=null;
		while(p==null||checkclutter(p,actors)||!z.area.contains(p))
			p=RPG.pick(d.getfreespaces());
		l.setlocation(p);
		l.place();
	}

	<K extends Location> K placefeature(K l,Zone z){
		Point p=null;
		ArrayList<Actor> actors=World.getactors();
		while(p==null||world.map[p.x][p.y]==Terrain.WATER
				||World.get(p.x,p.y,actors)!=null||checkclutter(p,actors)){
			p=RPG.pick(z.arealist);
			WorldGenerator.retry();
		}
		l.setlocation(p);
		l.place();
		return l;
	}

	boolean checkclutter(Point target,ArrayList<Actor> actors){
		int worldsize=World.scenario.size;
		for(Point p:Point.getadjacent2()){
			p.x+=target.x;
			p.y+=target.y;
			if(!p.validate(0,0,worldsize,worldsize)
					||world.map[p.x][p.y]==Terrain.WATER||World.get(p.x,p.y,actors)!=null)
				return true;
		}
		return false;
	}

	Temple createtemple(Realm r,int level){
		if(r==Realm.AIR) return new AirTemple(level);
		if(r==Realm.EARTH) return new EarthTemple(level);
		if(r==Realm.EVIL) return new EvilTemple(level);
		if(r==Realm.FIRE) return new FireTemple(level);
		if(r==Realm.GOOD) return new GoodTemple(level);
		if(r==Realm.MAGIC) return new MagicTemple(level);
		if(r==Realm.WATER) return new WaterTemple(level);
		return null;
	}

	void placegates(Zone from,HashSet<Zone> cache){
		if(cache.contains(from)) return;
		cache.add(from);
		for(Zone to:from.borders.keySet())
			if(!cache.contains(to)){
				placegate(from,to,from.realm);
				placegates(to,cache);
			}else if(RPG.chancein(2)) placegate(from,to,RPG.pick(REALMS));
		clearmap();
	}

	void clearmap(){
		for(int x=0;x<World.scenario.size;x++)
			for(int y=0;y<World.scenario.size;y++){
				Point p=new Point(x,y);
				if(checkclaimed(p)<0) world.map[p.x][p.y]=Terrain.WATER;
			}
	}

	void placegate(Zone from,Zone to,Realm key){
		int limit=World.scenario.size;
		Point gate=null;
		while(gate==null){
			gate=RPG.pick(new ArrayList<>(from.borders.get(to)));
			if(gate.x==0||gate.y==0||gate.x==limit-1||gate.y==limit-1){
				gate=null;
				continue;
			}
			boolean reacha=false;
			boolean reachb=false;
			for(Point p:Point.getadjacent2()){
				p.x+=gate.x;
				p.y+=gate.y;
				if(world.map[p.x][p.y]==Terrain.WATER) continue;
				if(from.area.contains(p)) reacha=true;
				if(to.area.contains(p)) reachb=true;
			}
			if(!reacha||!reachb){
				gate=null;
				WorldGenerator.retry();
			}
		}
		Gate g=new Gate(key,to);
		from.gates.add(g);
		g.setlocation(gate);
		g.place();
		clearterrain(gate);
	}

	void clearterrain(Point gate){
		List<Point> ajacent=Arrays.asList(Point.getadjacent2());
		Collections.shuffle(ajacent);
		for(Point p:ajacent){
			p.x+=+gate.x;
			p.y+=+gate.y;
			if(!World.validatecoordinate(p.x,p.y)) continue;
			Terrain t=world.map[p.x][p.y];
			if(t!=Terrain.WATER){
				world.map[gate.x][gate.y]=t;
				break;
			}
		}
	}

	void createborders(){
		for(Point p:allborders)
			world.map[p.x][p.y]=Terrain.WATER;
		for(Zone z:zones){
			HashSet<Point> frontier=z.enclose();
			allborders.addAll(frontier);
			for(Point p:frontier)
				world.map[p.x][p.y]=Terrain.WATER;
		}
	}

	void generatezones(int nzones){
		ArrayList<Point> zones=new ArrayList<>(nzones);
		while(zones.size()<nzones){
			Point p=new Point(RPG.r(1,World.scenario.size-2),
					RPG.r(1,World.scenario.size-2));
			if(!zones.contains(p)) zones.add(p);
		}
		for(int i=0;i<nzones;i++)
			for(int j=i+1;j<nzones;j++)
				if(zones.get(i).distance(zones.get(j))<MINDISTANCE){
					WorldGenerator.retry();
					return;
				}
		for(int i=0;i<nzones;i++){
			Zone z=new Zone(REALMS.get(i));
			z.add(zones.get(i));
			this.zones.add(z);
		}
	}

}