package javelin.old.underground;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.model.state.Square;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Dungeon.DungeonImage;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO should probably extract a DungeonMap out of this
 *
 * TODO make light levels one lower always
 *
 * @author alex
 */
public class Caves extends Map{
	protected static final int SIZE=DndMap.SIZE;
	static final int[] DELTAS=new int[]{-1,0,+1};
	int coresize=1;

	/** Constructor. */
	public Caves(String namep){
		super(namep,SIZE,SIZE);
		maxflooding=Weather.CLEAR;
		var d=Dungeon.active;
		if(d!=null){
			floor=Images.get(List.of("dungeon",d.images.get(DungeonImage.FLOOR)));
			wall=Images.get(List.of("dungeon",d.images.get(DungeonImage.WALL)));
		}else{
			floor=Images.get(List.of("terrain","dungeonfloor"));
			wall=Images.get(List.of("terrain","dungeonwall"));
		}
		obstacle=rock;
		flying=false;
	}

	public Caves(){
		this("Caves");
	}

	@Override
	public void generate(){
		setup();
		for(int buildloop=0;buildloop<200;buildloop++)
			build();
		// now do some decoration
		for(int i=0;i<13+6;i++){
			Square s=null;
			while(s==null||s.blocked)
				s=map[RPG.r(0,SIZE-1)][RPG.r(0,SIZE-1)];
			s.obstructed=true;
		}
		close();
	}

	/**
	 * Makes all map {@link Square} blocked and creates an inner room according to
	 * {@link #coresize}.
	 */
	protected void setup(){
		for(Square[] squares:map)
			for(Square s:squares)
				s.blocked=true;
		int center=SIZE/2;
		for(int x=center-coresize;x<=center+coresize;x++)
			for(int y=center-coresize;y<=center+coresize;y++)
				map[x][y].blocked=false;
	}

	void build(){
		// first choose a direction
		int dx=0;
		int dy=0;
		switch(RPG.r(1,4)){
			case 1:
				dx=1;
				break; // W
			case 2:
				dx=-1;
				break; // E
			case 3:
				dy=1;
				break; // N
			case 4:
				dy=-1;
				break; // S
		}

		Point p=findEdgeSquare(dx,dy);
		if(p==null) return;
		// advance onto blank square
		p.x+=dx;
		p.y+=dy;

		try{
			addfeature(dx,dy,p);
		}catch(IndexOutOfBoundsException e){
			return;
		}
	}

	/**
	 * find unblocked square with tile type c adjacent in specified direction
	 */
	public Point findEdgeSquare(final int dx,final int dy){
		for(int i=0;i<10*SIZE*SIZE;i++){
			int x=RPG.r(SIZE-2)+1;
			int y=RPG.r(SIZE-2)+1;
			if(!map[x][y].blocked&&map[x+dx][y+dy].blocked) return new Point(x,y);
		}
		return null;
	}

	void addfeature(int dx,int dy,Point p){
		// choose new feature to add
		switch(RPG.r(1,7)){
			case 1:
			case 2:
				makeThinPassage(p.x,p.y,dx,dy,false);
				break;
			case 3:
				makeThinPassage(p.x,p.y,dx,dy,true);
				break;
			case 4:
			case 5:
			case 6:
				makeOvalRoom(p.x,p.y,dx,dy);
				break;
			case 7:
				makeCrevice(p.x,p.y,dx,dy);
				break;
		}
	}

	void makeCrevice(int x,int y,int dx,int dy){
		if(isBlank(x+dy,y-dx,x-dy,y+dx)) return;
		map[x+dy][y-dx].blocked=true;
		map[x-dy][y+dx].blocked=true;

		map[x][y].blocked=false;
		if(RPG.rolldice(1,10)<=2) makeOvalRoom(x+dx,y+dy,dx,dy);
	}

	boolean isBlank(int x1,int y1,int x2,int y2){
		for(int x=x1;x<=x2;x++)
			for(int y=x1;y<=y2;y++)
				if(map[x][y].blocked) return false;
		return true;
	}

	void makeOvalRoom(int x,int y,int dx,int dy){
		int w=RPG.rolldice(2,3);
		int h=RPG.rolldice(2,3);
		int x1=x+(dx-1)*w;
		int y1=y+(dy-1)*h;
		int x2=x+(dx+1)*w;
		int y2=y+(dy+1)*h;
		if(!isBlank(x1,y1,x2,y2)) return;

		int cx=(x1+x2)/2;
		int cy=(y1+y2)/2;

		for(int lx=x1;lx<=x1+w*2;lx++)
			for(int ly=y1;ly<y1+h*2;ly++)
				if((lx-cx)*(lx-cx)*100/(w*w)+(ly-cy)*(ly-cy)*100/(h*h)<100)
					map[lx][ly].blocked=false;
		for(int clearx=cx;clearx<=x;clearx++)
			for(int cleary=cy;cleary<=y;cleary++)
				map[clearx][cleary].blocked=false;
	}

	private void makeThinPassage(int x,int y,int dx,int dy,boolean diagonals){
		int len=RPG.rolldice(2,12);
		int size=RPG.r(1,4);
		if(!isBlank(x+size*dy,y-size*dx,x-size*dy+len*dx,y+size*dx+len*dy)) return;

		int fromx=x+size*dy;
		int fromy=y-size*dx;
		int tox=x-size*dy+len*dx/2;
		int toy=y+size*dx+len*dy/2;
		for(int wallx=fromx;wallx<=tox;wallx++)
			for(int wally=fromy;wally<=toy;wally++)
				map[wallx][wally].blocked=true;
		int p=0;
		for(int i=0;i<=len;i++){
			int passagex=x+i*dx+p*dy;
			int passagey=y+i*dy-p*dx;
			map[passagex][passagey].blocked=false;
			map[passagex+DELTAS[RPG.r(DELTAS.length)]][passagey
					+DELTAS[RPG.r(DELTAS.length)]].blocked=false;
			if(RPG.r(1,2)==1) p=BigCave.middle(-size,p+RPG.r(2)-RPG.r(2),size);
			if(!diagonals) map[x+i*dx+p*dy][y+i*dy-p*dx].blocked=false;
		}
	}
}
