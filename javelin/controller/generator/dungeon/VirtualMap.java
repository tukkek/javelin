package javelin.controller.generator.dungeon;

import java.util.ArrayList;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;

public class VirtualMap{
	public class Room implements DungeonArea{
		public int x;
		public int y;
		public int width;
		public int height;
		public ArrayList<Point> accessible;

		public Room(){

		}

		public Room(int x,int y,int width,int height){
			this.x=x;
			this.y=y;
			this.width=width;
			this.height=height;
		}

		@Override
		public String toString(){
			return x+":"+y+" ("+width+"x"+height+")";
		}

		@Override
		public int getwidth(){
			return width;
		}

		@Override
		public int getheight(){
			return height;
		}

		@Override
		public int getx(){
			return x;
		}

		@Override
		public int gety(){
			return y;
		}
	}

	/**
	 * This contains some rooms information that can be used post-generation to
	 * place monsters, treasure, counter the number of rooms in the dungeon, etc.
	 * Note that Corridors aren't considered rooms so not all map area is covered
	 * by this field.
	 *
	 * TODO should be easy enough to check doors after the map is ready and
	 * provide a "this room is connected with these other rooms" field. This could
	 * be useful as a graph for the consumers to determine solution paths, most
	 * distant rooms, etc.
	 */
	public ArrayList<Room> rooms=new ArrayList<>();
	public Character fill=FloorTile.WALL;

	HashMap<Point,Character> map=new HashMap<>();

	public boolean draw(FloorTile t,int rootx,int rooty,boolean check){
		for(int x=0;x<t.width;x++)
			for(int y=0;y<t.height;y++){
				Point p=new Point(x+rootx,y+rooty);
				char c=t.tiles[x][y];
				if(!check){
					map.put(p,c);
					continue;
				}
				Character old=map.get(p);
				if(old!=null&&(old!=FloorTile.WALL||c!=FloorTile.WALL))
					return false;
			}
		return true;
	}

	public String rasterize(boolean commit){
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		// int minx = Integer.MAX_VALUE;
		// int miny = Integer.MAX_VALUE;
		for(Point p:map.keySet()){
			min=Math.min(min,Math.min(p.x,p.y));
			max=Math.max(max,Math.max(p.x,p.y));
			// minx = Math.min(minx, p.x);
			// miny = Math.min(miny, p.y);
		}
		if(commit){
			int total=max-min;
			for(Room r:rooms){
				r.x-=min;
				r.y-=min;
			}
		}
		StringBuilder raster=new StringBuilder();
		for(int x=min;x<=max;x++){
			for(int y=min;y<=max;y++){
				Character c=map.get(new Point(x,y));
				raster.append(c==null?fill:c);
			}
			raster.append('\n');
		}
		return raster.toString();
	}

	@Override
	public String toString(){
		return rasterize(false);
	}

	public boolean draw(FloorTile t,int x,int y){
		if(!draw(t,x,y,true)) return false;
		draw(t,x,y,false);
		if(!t.corridor) rooms.add(new Room(x,y,t.width,t.height));
		return true;
	}

	public void set(char c,int x,int y){
		map.put(new Point(x,y),c);
	}

	public Character get(Point p){
		return map.get(p);
	}

	public Character get(Point cursor,Point p){
		return get(new Point(cursor.x+p.x,cursor.y+p.y));
	}

	public void set(char tile,Point cursor,Point p){
		set(tile,cursor.x+p.x,cursor.y+p.y);
	}

	public void set(char c,Point p){
		set(c,p.x,p.y);
	}

	public int countadjacent(Character tile,Point p){
		int found=0;
		for(int x=p.x-1;x<=p.x+1;x++)
			for(int y=p.y-1;y<=p.y+1;y++)
				if(tile.equals(get(new Point(x,y)))) found+=1;
		return found;
	}
}
