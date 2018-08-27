package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.WorldScreen;

/**
 * Game world overview. This is focused on generating the initial game state.
 *
 * TODO would be nice to have tiles reflect the official d20 terrains (add
 * desert and hill)
 *
 * @see WorldScreen
 * @author alex
 */
public class World implements Serializable{
	/** Ruleset of the current game. */
	public static Scenario scenario=null;
	/**
	 * Randomly generated world map.
	 *
	 * @see #getseed()
	 */
	public static World seed=null;
	/** Facilitate movement. */
	public boolean[][] roads;
	/** Upgraded {@link #roads}. */
	public boolean[][] highways;
	/** Map of terrain tiles by [x][y] coordinates. */
	public Terrain[][] map;

	/** Contains all actor instances still in the game. */
	public final HashMap<Class<? extends Actor>,ArrayList<Actor>> actors=new HashMap<>();
	/**
	 * Used o speed-up coordinate-based searches. Useful for long debugging
	 * operations as it speeds things up, safe to remove if troublesome.
	 *
	 * Don't bother registering non {@link Location}s because they can change
	 * coordinates often and are a minority. The performance gains would be
	 * irrelevant and definitely not worth the trouble of get it working right.
	 */
	public final HashMap<Point,Location> locations=new HashMap<>();
	public final ArrayList<String> townnames=new ArrayList<>();
	/**
	 * Intermediary for {@link WorldTile} while loading.
	 *
	 * TODO remove
	 *
	 * @see Tile#discovered
	 */
	public final HashSet<Point> discovered=new HashSet<>();
	/** Tribute to masters in of tabletop RPGs, literature, video games... */
	public final LinkedList<String> dungeonnames=new LinkedList<>(
			Arrays.asList(new String[]{"Frank Herbert","Lao Tze","Robert Monroe",
					"Gary Gygax","Dave Arneson","Ed Greenwood","Tracy Hickman",
					"Margaret Weis","Monte Cook","Tony DiTerlizzi","Brian Fargo",
					"Tim Cain","John Romero","Chris Avellone","Feargus Urquhart",
					"Jon Van Caneghem","John Carmack","Richard Garriott","Guido Henkel",
					"Mark Rein Hagen","Norman Sirotek","George Lucas","Mark Morgan",
					"Colin McComb","Jordan Weisman","Nobue Uematsu","Michiel van den Bos",
					"Alexander Brandon","Hironobu Sakaguchi","Shinji Mikami","Ben Houge",
					"Jeremy Soule","Yoshitaka Amano","Takashi Tokita","Koji Kondo",
					"Katsuhiro Otomo","Shigeru Miyamoto","Yoshinori Kitase",
					"Hiroyuki Ito","Robert Howard","Érica Awano","André Vazzios",
					"Marcelo Cassaro","J. M. Trevisan","Rogério Saladino",
					"Marcelo Del Debbio","Evandro Gregório","Steve Jackson",
					"Clyde Caldwell","Jeff Easley","Sandy Petersen","Stan Lee",
					"Hideaki Anno","Patrick Wyatt","Bill Roper","Michiru Yamane",
					"Sid Meier","William Gibson","Julie Bell","Boris Vallejo",
					"Lee Salzman","Johannes Bonitz"}));

	public FeatureGenerator featuregenerator;

	public World(){
		map=new Terrain[scenario.size][scenario.size];
		initroads();
		inittownnames();
		Collections.shuffle(dungeonnames);
	}

	void initroads(){
		int size=scenario.size;
		roads=new boolean[size][size];
		highways=new boolean[size][size];
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++){
				roads[x][y]=false;
				highways[x][y]=false;
			}
	}

	/**
	 * @return <code>true</code> if given coordinates are within the world map.
	 */
	public static boolean validatecoordinate(int x,int y){
		return 0<=x&&x<scenario.size&&0<=y&&y<scenario.size;
	}

	@Override
	public String toString(){
		String s="";
		for(int y=0;y<scenario.size;y++){
			for(int x=0;x<scenario.size;x++)
				s+=map[x][y].representation;
			s+="\n";
		}
		return s;
	}

	/**
	 * Populates {@link NAMES}. This may be needed if restarting {@link World}
	 * generation.
	 *
	 * @see retry
	 */
	public void inittownnames(){
		townnames.clear();
		townnames.add("Alexandria"); // my name :)
		townnames.add("Lindblum"); // final fantasy 9
		townnames.add("Sigil"); // planescape: torment
		townnames.add("Reno");// fallout 2
		townnames.add("Marrymore");// super mario rpg
		townnames.add("Kakariko"); // zelda
		townnames.add("The Citadel"); // mass effect
		townnames.add("Tristam");// diablo
		townnames.add("Midgar"); // final fantasy 7
		townnames.add("Medina");// chrono trigger
		townnames.add("Figaro"); // final fantasy 6
		townnames.add("Balamb"); // final fantasy 8
		townnames.add("Zanarkand"); // final fantasy 10
		townnames.add("Cornelia"); // final fantasy 1
		townnames.add("Vivec");// morrowind
		townnames.add("Termina");// chrono cross
		townnames.add("Tarant");// arcanum
		Collections.shuffle(townnames);
	}

	/**
	 * Note that this returns the canonical list from {@link World#actors}.
	 *
	 * @return All actors of the given type.
	 */
	public static ArrayList<Actor> getall(Class<? extends Actor> type){
		ArrayList<Actor> all=getseed().actors.get(type);
		if(all==null){
			all=new ArrayList<>();
			getseed().actors.put(type,all);
		}
		return all;
	}

	/**
	 * @return A new list with all existing {@link Actor}s.
	 */
	public static ArrayList<Actor> getactors(){
		ArrayList<Actor> actors=new ArrayList<>();
		for(ArrayList<Actor> instances:getseed().actors.values()){
			if(instances.isEmpty()||instances.get(0) instanceof Squad) continue;
			actors.addAll(instances);
		}
		/* squads added at end */
		actors.addAll(getall(Squad.class));
		return actors;
	}

	static Location getlocation(int x,int y){
		return getseed().locations.get(new Point(x,y));
	}

	/**
	 * @return Actor of the given set that occupies these coordinates.
	 */
	public static Actor get(int x,int y,List<? extends Actor> actors){
		final Location l=getlocation(x,y);
		if(l!=null) return actors.contains(l)?l:null;
		for(Actor a:actors)
			if(a.x==x&&a.y==y) return a;
		return null;
	}

	/**
	 * @return Any actor on these coordinates.
	 * @deprecated This loops through all of {@link #getactors()} with every call.
	 *             Makes it really inefficient.
	 */
	@Deprecated
	public static Actor get(int x,int y){
		final Location l=getlocation(x,y);
		if(l!=null) return l;
		return World.get(x,y,World.getactors());
	}

	/**
	 * @return Actor of the given type that occupies the given coordinates, or
	 *         <code>null</code>.
	 */
	public static Actor get(int x,int y,Class<? extends Actor> type){
		final Location l=getlocation(x,y);
		if(l!=null) return type.isInstance(l)?l:null;
		return World.get(x,y,World.getall(type));
	}

	/**
	 * Needs to be called during world building, as each {@link WorldGenerator}
	 * thread has a different world. During normal gameplay, {@link #seed} can be
	 * accessed directly.
	 *
	 * TODO make sure this is only being used where necessary, to avoid the
	 * overhead
	 *
	 * @return If {@link #building}, the thread-relevant world instance, otherwise
	 *         {@link #seed}.
	 */
	public static World getseed(){
		Thread t=Thread.currentThread();
		if(t instanceof WorldGenerator) return ((WorldGenerator)t).world;
		return seed;
	}
}
