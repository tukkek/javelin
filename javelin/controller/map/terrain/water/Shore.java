package javelin.controller.map.terrain.water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.controller.terrain.Water;
import javelin.model.world.World;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Map to be used in a {@link Water} tile that meets some type of non-water
 * terrain adjacent to it. {@link DeepWaters} aren't particularly interesting or
 * fun so we leave that full-flooded approach to where it makes the most sense
 * in the game {@link World}.
 *
 * No effort is put into determining which side the shore(s) should be from at
 * this point in time - especially considering this might be used from a
 * {@link Minigame} or similar and not a proper {@link World} map. Players are
 * left to imagine that the NSWE positions of the {@link BattleScreen} aren't
 * necessarily aligned with that of the {@link WorldScreen}.
 *
 * Having some sort of seeded map generation here so that approaching the same
 * tile from the same angle would always result in the same map would be
 * interesting, but not particularly desirable or urgent - especially
 * considering Javelin has a larger {@link World} scale than almost any other
 * game, meaning there's plenty of opportunity to access an area from the same
 * angle and still end up in a different internal region of that tile-crossing.
 *
 * @author alex
 */
public class Shore extends Map{
	boolean shoretop=false;
	boolean shorebottom=false;
	boolean shoreleft=false;
	boolean shoreright=false;
	int swing=RPG.r(2,4);
	float debris=RPG.pick(List.of(0f,.1f,.25f,.5f,.75f));
	Integer verticallimit=null;
	Integer verticaldirection=null;

	/** Constructor. */
	public Shore(){
		super("Shore",DndMap.SIZE,DndMap.SIZE);
		flooded=Images.get(List.of("terrain","aquatic"));
		floor=Images.get(List.of("terrain","desert"));
		obstacle=Images.get(List.of("terrain","rock"));
	}

	@Override
	public void generate(){
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map.length;y++)
				map[x][y].flooded=true;
		defineparameters();
		if(shoretop)
			placeverticalshore(+1);
		else if(shorebottom) placeverticalshore(-1);
		if(shoreleft)
			placehorizontalshore(+1);
		else if(shoreright) placehorizontalshore(-1);
		placedebris();
	}

	void placedebris(){
		ArrayList<Point> shore=new ArrayList<>();
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map[0].length;y++)
				if(!map[x][y].flooded) shore.add(new Point(x,y));
		Collections.shuffle(shore);
		int debris=Math.round(shore.size()*this.debris);
		for(int i=0;i<debris;i++){
			Point p=shore.get(i);
			map[p.x][p.y].obstructed=true;
		}
	}

	public void defineparameters(){
		boolean both=RPG.chancein(4);
		boolean vertical=both||RPG.chancein(2);
		boolean horizontal=both||!vertical;
		if(vertical){
			shoretop=RPG.chancein(2);
			shorebottom=!shoretop;
		}
		if(horizontal){
			shoreleft=RPG.chancein(2);
			shoreright=!shoreleft;
		}
	}

	void placeverticalshore(int direction){
		float coverage=RPG.r(25,50)/100f;
		int from=direction==+1?0:map.length-1;
		int to=direction==+1?map.length-1:0;
		int limit=Math.round(from+(to-from)*coverage);
		for(int y=from;y!=limit+direction;y+=direction)
			for(int x=0;x<map.length;x++)
				map[x][y].flooded=false;
		placeverticalswing(limit,direction==-1);
		verticallimit=limit;
		verticaldirection=direction;
	}

	void placehorizontalshore(int direction){
		float coverage=RPG.r(25,50)/100f;
		int from=direction==+1?0:map[0].length-1;
		int to=direction==+1?map[0].length-1:0;
		int limit=Math.round(from+(to-from)*coverage);
		for(int x=from;x!=limit+direction;x+=direction)
			for(int y=0;y<map.length;y++)
				map[x][y].flooded=false;
		placehorizontalswing(limit,direction==-1);
	}

	void placeverticalswing(int y,boolean flood){
		int wave=RPG.r(0,swing);
		int direction=RPG.chancein(2)?+1:-1;
		for(int x=0;x<map[0].length;x++){
			for(int i=0;i<=wave;i++){
				Point p=new Point(x,y+i);
				if(p.validate(0,0,map.length,map[0].length))
					map[p.x][p.y].flooded=flood;
			}
			if(direction==+1&&wave==swing)
				direction=-1;
			else if(direction==-1&&wave==0) direction=+1;
			wave+=direction;
		}
	}

	void placehorizontalswing(int x,boolean flood){
		int wave=RPG.r(0,swing);
		int direction=RPG.chancein(2)?+1:-1;
		for(int y=0;y<map[0].length;y++){
			if(verticaldirection!=null){
				if(verticaldirection==+1&&y<=verticallimit) continue;
				if(verticaldirection==-1&&y>=verticallimit) continue;
			}
			for(int i=0;i<=wave;i++){
				Point p=new Point(x+i,y);
				if(p.validate(0,0,map.length,map[0].length))
					map[p.x][p.y].flooded=flood;
			}
			if(direction==+1&&wave==swing)
				direction=-1;
			else if(direction==-1&&wave==0) direction=+1;
			wave+=direction;
		}
	}
}
