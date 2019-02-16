package javelin.controller.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.CountingSet;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

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

	static final int MAXRETRIES=100000;
	static final int NOISEAMOUNT=World.scenario.size*World.scenario.size/10;
	static final Terrain[] NOISE=new Terrain[]{Terrain.PLAIN,Terrain.HILL,
			Terrain.FOREST,Terrain.MOUNTAINS};

	static int discarded=0;

	public int retries=0;
	public World world;

	@Override
	public final void run(){
		try{
			generate();
		}catch(RestartWorldGeneration e){
			if(World.seed==null) try{
				startthread();
			}catch(ReflectiveOperationException e2){
				throw new RuntimeException(e2);
			}
		}
	}

	protected void generate(){
		world=new World();
		LinkedList<Realm> realms=new LinkedList<>();
		for(Realm r:Realm.values())
			realms.add(r);
		Collections.shuffle(realms);
		ArrayList<HashSet<Point>> regions=new ArrayList<>(realms.size());
		generategeography(realms,regions,world);
		try{
			FeatureGenerator generator=World.scenario.featuregenerator
					.getDeclaredConstructor().newInstance();
			world.featuregenerator=generator;
			Location start=generator.generate(realms,regions,world);
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
		retries+=1;
		if(retries<=MAXRETRIES) return;
		retries=0;
		discarded+=1;
		if(Javelin.DEBUG){
			RuntimeException e=new RuntimeException("Reset");
			e.fillInStackTrace();
			RESETS.add(JavelinApp.printstacktrace(e));
		}
		throw new RestartWorldGeneration();
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

	public static void build(){
		int threads=Math.max(1,Preferences.maxthreads);
		final String info="Building world, using "+threads
				+" thread(s)...\n\nWorlds discarded: ";
		try{
			for(int i=0;i<threads;i++)
				startthread();
			int lastdiscarded=-1;
			InfoScreen screen=new InfoScreen("");
			while(World.seed==null){
				if(lastdiscarded!=discarded){
					screen.print(info+discarded+'.');
					lastdiscarded=discarded;
				}
				Thread.sleep(1000);
			}
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

	static void startthread() throws ReflectiveOperationException{
		Class<? extends WorldGenerator> generator=World.scenario.worldgenerator;
		generator.getDeclaredConstructor().newInstance().start();
	}
}
