package javelin.controller.generator.dungeon.template.mutator;

import java.util.Arrays;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.old.RPG;

public class Alcoves extends Mutator{
	public Alcoves(){
		chance=0.1;
	}

	@Override
	public void apply(FloorTile t){
		HashSet<Direction> directions=new HashSet<>();
		while(directions.isEmpty())
			for(Direction d:Direction.DIRECTIONS)
				if(RPG.chancein(4)) directions.add(d);
		for(Direction d:directions){
			int depth=1;
			while(RPG.chancein(2))
				depth+=1;
			carve(d,depth,t);
		}
	}

	void carve(Direction d,int depth,FloorTile t){
		grow(d,depth,t);
		int nalcoves=RPG.r(1,4);
		for(int i=0;i<nalcoves;i++){
			Point p=RPG.pick(d.getborder(t));
			while(p.validate(0,0,t.width,t.height)&&t.tiles[p.x][p.y]==FloorTile.WALL){
				t.tiles[p.x][p.y]=FloorTile.FLOOR;
				Point step=d.takestep();
				p.x+=step.x;
				p.y+=step.y;
			}
		}
	}

	void grow(Direction d,int depth,FloorTile t){
		char[][] grown;
		if(d==Direction.NORTH){
			grown=new char[t.width][t.height+depth];
			copy(t,0,grown,0);
			for(int x=0;x<t.width;x++)
				for(int y=t.height;y<t.height+depth;y++)
					grown[x][y]=FloorTile.WALL;
		}else if(d==Direction.SOUTH){
			grown=new char[t.width][t.height+depth];
			copy(t,0,grown,depth);
			for(int x=0;x<t.width;x++)
				for(int y=0;y<depth;y++)
					grown[x][y]=FloorTile.WALL;
		}else if(d==Direction.EAST){
			grown=new char[t.width+depth][t.height];
			System.arraycopy(t.tiles,0,grown,0,t.tiles.length);
			for(int x=t.width;x<t.width+depth;x++)
				Arrays.fill(grown[x],FloorTile.WALL);
		}else{
			grown=new char[t.width+depth][t.height];
			System.arraycopy(t.tiles,0,grown,depth,t.tiles.length);
			for(int x=0;x<depth;x++)
				Arrays.fill(grown[x],FloorTile.WALL);
		}
		t.tiles=grown;
		t.width=t.tiles.length;
		t.height=t.tiles[0].length;
	}

	void copy(FloorTile from,int positionfrom,char[][] to,int positionto){
		for(int x=0;x<from.tiles.length;x++){
			char[] sub=from.tiles[x];
			System.arraycopy(sub,positionfrom,to[x],positionto,sub.length);
		}
	}
}
