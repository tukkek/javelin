package javelin.controller.generator.dungeon;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.old.RPG;

public abstract class Direction{
	public static final Direction NORTH=new Direction("North",0,+1){
		@Override
		public ArrayList<Point> getborder(DungeonArea t){
			ArrayList<Point> border=new ArrayList<>();
			for(int x=0;x<t.getwidth();x++)
				border.add(new Point(t.getx()+x,t.gety()+t.getheight()-1));
			return border;
		}

		@Override
		public Point connect(Point cursor,FloorTile from,FloorTile to,Point fromdoor,
				Point todoor){
			cursor.x+=fromdoor.x-todoor.x;
			cursor.y+=from.height;
			return cursor;
		}
	};
	public static final Direction SOUTH=new Direction("South",0,-1){
		@Override
		public ArrayList<Point> getborder(DungeonArea t){
			ArrayList<Point> border=new ArrayList<>();
			for(int x=0;x<t.getwidth();x++)
				border.add(new Point(t.getx()+x,t.gety()));
			return border;
		}

		@Override
		public Point connect(Point cursor,FloorTile from,FloorTile to,Point fromdoor,
				Point todoor){
			cursor.x+=fromdoor.x-todoor.x;
			cursor.y-=to.height;
			return cursor;
		}
	};
	public static final Direction EAST=new Direction("East",+1,0){
		@Override
		public ArrayList<Point> getborder(DungeonArea t){
			ArrayList<Point> border=new ArrayList<>();
			for(int y=0;y<t.getheight();y++)
				border.add(new Point(t.getx()+t.getwidth()-1,t.gety()+y));
			return border;
		}

		@Override
		public Point connect(Point cursor,FloorTile from,FloorTile to,Point fromdoor,
				Point todoor){
			cursor.x+=from.width;
			cursor.y+=fromdoor.y-todoor.y;
			return cursor;
		}
	};
	public static final Direction WEST=new Direction("West",-1,0){
		@Override
		public ArrayList<Point> getborder(DungeonArea t){
			ArrayList<Point> border=new ArrayList<>();
			for(int y=0;y<t.getheight();y++)
				border.add(new Point(t.getx(),t.gety()+y));
			return border;
		}

		@Override
		public Point connect(Point cursor,FloorTile from,FloorTile to,Point fromdoor,
				Point todoor){
			cursor.x-=to.width;
			cursor.y+=fromdoor.y-todoor.y;
			return cursor;
		}
	};
	public static final Direction[] DIRECTIONS=new Direction[]{NORTH,SOUTH,WEST,
			EAST};

	public String name;
	public Point reverse;
	public Point following;

	private Direction(String name,int x,int y){
		this.name=name;
		following=new Point(x,y);
		reverse=new Point(-x,-y);
	}

	public abstract ArrayList<Point> getborder(DungeonArea t);

	public static Direction getrandom(){
		return DIRECTIONS[RPG.r(0,3)];
	}

	@Override
	public String toString(){
		return name;
	}

	public static Direction opposite(Direction d){
		if(d==null) throw new RuntimeException("Opposite of null?");
		if(d==NORTH) return SOUTH;
		if(d==SOUTH) return NORTH;
		if(d==EAST) return WEST;
		return EAST;
	}

	public abstract Point connect(Point cursor,FloorTile from,FloorTile to,
			Point fromdoor,Point todoor);

	public Point takestep(){
		Point step=null;
		while(step==null||following.x!=0&&step.x==following.x
				||following.y!=0&&step.y==following.y||step.x==0&&step.y==0)
			step=new Point(RPG.r(-1,+1),RPG.r(-1,+1));
		return step;
	}
}