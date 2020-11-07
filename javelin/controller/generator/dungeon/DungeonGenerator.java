package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap.Room;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.controller.table.Tables;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;
import javelin.view.screen.town.SelectScreen;

public class DungeonGenerator{
	public static final boolean DEBUG=false;
	public static final MapTemplate DEBUGTEMPLATE=DEBUG?null:null;
	public static final MapTemplate DEBUGCORRIDOR=DEBUG?null:null;
	public static final Mutator DEBUGMUTATOR=DEBUG?null:null;
	static final boolean DEBUGROOMS=true;
	static final int DEBUGSIZE=1;

	/**
	 * TODO temporary: will need to be refactored when more than one level can be
	 * generated (with one set of tables/parameters per level) and/or for
	 * multithreading. Should be as simple as passing an instance of this or of a
	 * new class GeneratorLevel to Templates.
	 */
	public static DungeonGenerator instance;

	static int ncorridors;
	static int ntemplates;

	public Tables tables;
	public VirtualMap map=new VirtualMap();
	public char[][] grid;
	public String ascii;

	LinkedList<Segment> segments=new LinkedList<>();
	ArrayList<MapTemplate> pool=new ArrayList<>();
	String templatesused="";
	private int minrooms;
	private int maxrooms;
	private int nrooms;

	static{
		setupparameters();
	}

	/**
	 * @param maxrooms
	 * @param minrooms
	 * @param tables2
	 * @param sizehint TOOD would be cool to have this handled built-in, not on
	 *          {@link #generate(int, int)}.
	 */
	private DungeonGenerator(int minrooms,int maxrooms,DungeonFloor f){
		this.minrooms=minrooms;
		this.maxrooms=maxrooms;
		tables=f.tables;
		instance=this;
		generatepool();
		draw();
		/* TODO make this a Table 5±10 */
		int connectionattempts=map.rooms.size()*RPG.r(0,10);
		for(int i=0;i<connectionattempts;i++)
			createconnection();
		finish();
	}

	MapTemplate generateroom(){
		MapTemplate t=null;
		while(t==null)
			t=RPG.pick(pool).create();
		return t;
	}

	/**
	 * TODO doesn't need necesarily to create only based on rooms
	 */
	void createconnection(){
		Room r=RPG.pick(map.rooms);
		Direction d=Direction.getrandom();
		Point exit=RPG.pick(d.getborder(r));
		if(map.countadjacent(MapTemplate.FLOOR,exit)==0) return;
		ArrayList<Point> connection=new ArrayList<>();
		int length=RPG.r(1,4)+RPG.r(1,4)+1;
		boolean connected=false;
		for(int i=0;i<length;i++){
			Point step=new Point(exit);
			step.x-=d.reverse.x*i;
			step.y-=d.reverse.y*i;
			if(map.countadjacent(MapTemplate.DOOR,step)>0) return;
			connection.add(step);
			Character tile=map.get(step);
			if(connection.size()>1&&map.countadjacent(MapTemplate.FLOOR,step)==1){
				connected=true;
				break;
			}
			if(MapTemplate.WALL.equals(tile)||tile==null) continue;
			return;
		}
		drawconnection(connection,connected);
	}

	void drawconnection(ArrayList<Point> connection,boolean connected){
		if(connected&&connection.size()>2){
			for(Point step:connection)
				map.set(MapTemplate.FLOOR,step);
			Point door=connection.get(connection.size()-1);
			map.set(MapTemplate.DOOR,door);
		}
	}

	public void finish(){
		ascii=map.rasterize(true).replaceAll(" ",
				Character.toString(MapTemplate.WALL));
		String[] grid=ascii.split("\n");
		this.grid=new char[grid.length][];
		for(int i=0;i<grid.length;i++)
			this.grid[i]=grid[i].toCharArray();
	}

	void draw(){
		MapTemplate start=generateroom();
		segments.add(new Segment(start,new Point(0,0)));
		map.draw(start,0,0);
		nrooms=RPG.r(minrooms,maxrooms);
		while(nrooms>0&&!segments.isEmpty()){
			Segment s=RPG.pick(segments);
			segments.remove(s);
			LinkedList<Point> doors=new LinkedList<>(s.room.getdoors());
			Collections.shuffle(doors);
			placingdoors:while(!doors.isEmpty()){
				Point door=doors.pop();
				for(int i=0;i<10;i++)
					if(expandroom(door,s)) continue placingdoors;
				if(map.get(s.cursor,door).equals(MapTemplate.DOOR))
					map.set(MapTemplate.WALL,s.cursor,door);
			}
		}
		for(Segment s:segments)
			for(Point door:s.room.getdoors())
				if(map.get(s.cursor,door).equals(MapTemplate.DOOR))
					map.set(MapTemplate.WALL,s.cursor,door);
	}

	boolean expandroom(Point door,Segment s){
		MapTemplate next=generateroom();
		Direction going=s.room.inborder(door.x,door.y);
		if(going==null){
			/* static template with internal door */
			map.set(MapTemplate.FLOOR,door.x,door.y);
			return true;
		}
		Direction coming=Direction.opposite(going);
		Point doorb=next.rotate(coming);
		Point cursorb=new Point(s.cursor);
		cursorb=going.connect(cursorb,s.room,next,door,doorb);
		StraightCorridor.clear(s.room,s.cursor,door,next,doorb,map);
		if(!map.draw(next,cursorb.x,cursorb.y)) return false;
		map.set(MapTemplate.FLOOR,cursorb,doorb);
		segments.add(new Segment(next,cursorb));
		nrooms-=1;
		return true;
	}

	void generatepool(){
		pool.addAll(selectrooms());
		pool.addAll(selectcorridors());
		if(RPG.chancein(2)&&DEBUGTEMPLATE==null) pool.add(StaticTemplate.FACTORY);
		for(MapTemplate t:pool)
			templatesused+=t.getClass().getSimpleName()+" ";
	}

	List<MapTemplate> selectrooms(){
		List<MapTemplate> templates;
		if(DEBUGTEMPLATE!=null){
			templates=new ArrayList<>(1);
			templates.add(DEBUGTEMPLATE);
			return templates;
		}
		templates=new ArrayList<>(Arrays.asList(MapTemplate.GENERATED));
		Collections.shuffle(templates);
		return templates.subList(0,Math.min(ntemplates,templates.size()));
	}

	List<MapTemplate> selectcorridors(){
		ArrayList<MapTemplate> corridors=new ArrayList<>();
		if(DEBUGCORRIDOR!=null){
			corridors.add(DEBUGCORRIDOR);
			return corridors;
		}
		if(ncorridors==0) return corridors;
		corridors.addAll(Arrays.asList(MapTemplate.CORRIDORS));
		Collections.shuffle(corridors);
		return corridors.subList(0,Math.min(ncorridors,corridors.size()));
	}

	void print(){
		String[] lines=ascii.split("\n");
		char[][] map=new char[lines.length][];
		for(int i=0;i<lines.length;i++)
			map[i]=lines[i].toCharArray();
		if(DEBUGROOMS){
			ArrayList<Room> rooms=this.map.rooms;
			for(int i=0;i<rooms.size();i++){
				Room r=rooms.get(i);
				for(int x=r.x;x<r.x+r.width;x++)
					for(int y=r.y;y<r.y+r.height;y++)
						if(map[x][y]==MapTemplate.FLOOR) map[x][y]=SelectScreen.getkey(i);
			}
		}
		StringBuilder builder=new StringBuilder();
		for(char[] line:map){
			builder.append(line);
			builder.append('\n');
		}
		System.out.println(ascii);
	}

	/**
	 * Called to set-up default parameters. You may call this method to "reset"
	 * and then provide your own before calling {@link #generate()}.
	 *
	 * This step is done in advance - otherwise the random generator will just
	 * naturally select "easy" parameters. This way parameters are "set" and the
	 * generator needs to rety as many times as necesar to achieve them.
	 */
	public static void setupparameters(){
		ntemplates=RPG.r(1,4);
		ncorridors=0;
		while(RPG.chancein(2))
			ncorridors+=1;
		ncorridors=Math.min(ncorridors,ntemplates);
	}

	/**
	 * @return A dungeon map, ready for drawing.
	 *
	 * @see VirtualMap#rooms
	 * @see #setupparameters()
	 */
	public static DungeonGenerator generate(int minrooms,int maxrooms,
			DungeonFloor f){
		StaticTemplate.load();
		DungeonGenerator dungeon=null;
		while(dungeon==null){
			dungeon=new DungeonGenerator(minrooms,maxrooms,f);
			int size=dungeon.map.rooms.size();
			if(!(minrooms<=size&&size<=maxrooms)) dungeon=null;
		}
		return dungeon;
	}

	public static void main(String[] args) throws IOException{
		int minrooms=3;
		int maxrooms=7;
		minrooms=13;
		maxrooms=13*2;
		DungeonGenerator dungeon=generate(minrooms,maxrooms,null);
		dungeon.print();
		System.out.println(dungeon.templatesused);
	}

	@Override
	public String toString(){
		return map.toString();
	}
}
