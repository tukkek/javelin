package javelin.controller.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.collection.CountingSet;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/**
 * Mulit-threaded {@link World} generation. Also does
 * {@link Dungeon#generate(javelin.model.world.location.dungeon.Dungeon.GenerationReport)}
 * once a valid world is generated.
 *
 * @see World#seed
 * @author alex
 */
public class WorldGenerator extends Thread{
	public static final Terrain[] GENERATIONORDER=new Terrain[]{Terrain.MOUNTAINS,
			Terrain.MOUNTAINS,Terrain.DESERT,Terrain.PLAIN,Terrain.HILL,Terrain.WATER,
			Terrain.WATER,Terrain.MARSH,Terrain.FOREST};
	/**
	 * Arbitrary number to serve as guideline for {@link Terrain} generation.
	 */
	public static final int NREGIONS=16;
	/** @see Debug */
	public static final CountingSet RESETS=Javelin.DEBUG?new CountingSet():null;

	static final int MAXRETRIES=1000*2;
	static final int NOISEAMOUNT=World.scenario.size*World.scenario.size/10;
	static final Terrain[] NOISE=new Terrain[]{Terrain.PLAIN,Terrain.HILL,
			Terrain.FOREST,Terrain.MOUNTAINS};
	static final int REFRESH=100;
	static final String PROGRESSHEADER="Building world, using %S thread(s)...";
	static final String GENERATINGDUNGEONS="Generating dungeons: %s%%.";
	static final int NTHREADS=Math.max(1,Preferences.maxthreads);
	static final List<WorldGenerator> WORLDTHREADS=new ArrayList<>(NTHREADS);
	static final boolean DEBUG=false;

	static int discarded=0;

	static class ProgressScreen extends InfoScreen{
		LinkedList<String> reports=new LinkedList<>();

		public ProgressScreen(String header){
			super(header);
			reports.add(header);
			reports.add("");
			reports.add(null);
		}

		@Override
		public void print(String line){
			reports.removeLast();
			reports.add(line);
			super.print(String.join("\n",reports));
		}

		void fix(){
			reports.add(null);
		}
	}

	public int retries=0;
	public World world;

	@Override
	public final void run(){
		while(World.seed==null)
			try{
				generate();
			}catch(RestartWorldGeneration e){
				continue;
			}
	}

	/** Creates {@link World} geography and {@link Location}s. */
	protected void generate(){
		try{
			retries=0;
			world=new World();
			var realms=RPG.shuffle(new LinkedList<>(Realm.REALMS));
			var regions=new ArrayList<HashSet<Point>>(realms.size());
			generategeography(realms,regions,world);
			world.featuregenerator=World.scenario.locationgenerator
					.getDeclaredConstructor().newInstance();
			var start=world.featuregenerator.generate(realms,regions,world);
			finish(start,world);
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	public synchronized void finish(Location start,World w){
		if(World.seed!=null) return;
		World.seed=w;
		Squad.active.x=start.x;
		Squad.active.y=start.y;
		Squad.active.displace();
		Squad.active.place();
		if(start instanceof Town) Squad.active.lasttown=(Town)start;
	}

	/**
	 * Handles when {@link World} generation is taking too long.
	 *
	 * @throws RestartWorldGeneration
	 */
	public synchronized final void bumpretry(){
		if(World.seed!=null) throw new RestartWorldGeneration();
		retries+=1;
		if(retries>=MAXRETRIES||Thread.interrupted()){
			retries=0;
			discarded+=1;
			if(DEBUG){
				var e=new RuntimeException("Reset");
				e.fillInStackTrace();
				RESETS.add(JavelinApp.printstacktrace(e));
			}
			throw new RestartWorldGeneration();
		}
	}

	public static void retry(){
		Thread t=Thread.currentThread();
		if(t instanceof WorldGenerator){
			if(World.seed!=null) throw new RestartWorldGeneration();
			WorldGenerator builder=(WorldGenerator)t;
			builder.bumpretry();
		}
	}

	protected void generategeography(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		int size=World.scenario.size;
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				w.map[i][j]=Terrain.FOREST;
		for(Terrain t:WorldGenerator.GENERATIONORDER)
			regions.add(t.generate(w));
		Point nw=new Point(0,0);
		Point sw=new Point(0,size-1);
		Point se=new Point(size-1,size-1);
		Point ne=new Point(size-1,0);
		floodedge(nw,sw,+1,0,w);
		floodedge(sw,se,0,-1,w);
		floodedge(ne,se,-1,0,w);
		floodedge(nw,ne,0,+1,w);
	}

	void floodedge(Point from,Point to,int deltax,int deltay,World w){
		ArrayList<Point> edge=new ArrayList<>(World.scenario.size);
		edge.add(from);
		edge.add(to);
		if(from.x!=to.x)
			for(int x=from.x+1;x!=to.x;x++)
				edge.add(new Point(x,from.y));
		else
			for(int y=from.y+1;y!=to.y;y++)
				edge.add(new Point(from.x,y));
		final Terrain[][] map=w.map;
		for(Point p:edge){
			map[p.x][p.y]=Terrain.WATER;
			if(RPG.random()<=.5f){
				map[p.x+deltax][p.y+deltay]=Terrain.WATER;
				if(RPG.random()<=.33f) map[p.x+deltax*2][p.y+deltay*2]=Terrain.WATER;
			}
		}
	}

	static void generateworld(InfoScreen s){
		try{
			for(var i=0;i<NTHREADS;i++)
				startthread();
			var lastdiscarded=-1;
			while(World.seed==null){
				if(lastdiscarded!=discarded){
					s.print("Worlds discarded: "+discarded+'.');
					lastdiscarded=discarded;
				}
				Thread.sleep(REFRESH);
			}
			for(var t:WORLDTHREADS){
				t.interrupt();
				t.join();
			}
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

	static void generatedungeons(InfoScreen s){
		s.print(String.format(GENERATINGDUNGEONS,0));
		var dungeons=World.getactors().stream()
				.filter(a->a instanceof DungeonEntrance)
				.map(a->((DungeonEntrance)a).dungeon)
				.sorted((a,b)->Integer.compare(a.floors.size(),b.floors.size()))
				.collect(Collectors.toList());
		var pool=Executors.newFixedThreadPool(NTHREADS);
		var ndungeons=dungeons.size();
		var tasks=new ArrayList<Future<?>>(ndungeons);
		for(var d:dungeons)
			tasks.add(pool.submit(()->d.generate()));
		for(var i=0;i<ndungeons;i++)
			try{
				tasks.get(i).get();
				var progress=100.0*(i+1)/ndungeons;
				s.print(String.format(GENERATINGDUNGEONS,Math.round(progress)));
			}catch(Exception e){
				throw new RuntimeException(e);
			}
	}

	static void startthread() throws ReflectiveOperationException{
		var generator=World.scenario.worldgenerator;
		var thread=generator.getDeclaredConstructor().newInstance();
		thread.start();
		WORLDTHREADS.add(thread);
	}

	/**
	 * Multi-threaded steps to generate {@link World} and {@link Dungeon}s.
	 * Reports on progress through an {@link InfoScreen}. Blocks synchronously.
	 *
	 * @see LocationGenerator
	 * @see Dungeon#generate()
	 */
	public static void build(){
		var header=String.format(PROGRESSHEADER,NTHREADS);
		var s=new ProgressScreen(header);
		generateworld(s);
		s.fix();
		generatedungeons(s);
	}
}
