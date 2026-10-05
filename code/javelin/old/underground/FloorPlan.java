package javelin.old.underground;

import javelin.controller.Point;
import javelin.old.RPG;

/**
 * @author alex
 */
public class FloorPlan extends Caves{
	float density=0.05f;

	/** Constructor. */
	public FloorPlan(){
		super("Dungeon floor");
		coresize=0;
	}

	@Override
	public void generate(){
		setup();
		// create a central room
		int from=(0+SIZE-1)/2-RPG.r(1,3);
		int to=(0+SIZE-1)/2+RPG.r(1,3);
		for(int x=from;x<=to;x++)
			for(int y=from;y<=to;y++)
				map[x][y].blocked=false;
		for(int buildloop=0;buildloop<SIZE*SIZE*density;buildloop++){
			// first choose a direction
			Point d=randomdirection();
			int dx=d.x;
			int dy=d.y;
			// now find a free extension point
			Point p=findEdgeSquare(dx,dy);
			if(p==null) continue;
			// advance onto blank square
			p.x+=dx;
			p.y+=dy;
			try{
				extendDungeon(p.x,p.y,dx,dy);
			}catch(IndexOutOfBoundsException e){
				continue;
			}
		}
		close();
	}

	/* choose new feature to add */
	void extendDungeon(int x,int y,int dx,int dy){
		switch(RPG.r(1,8)){
			case 1:
				makeCorridor(x,y,dx,dy);
				break;
			case 2:
			case 3:
				makeOvalRoom(x,y,dx,dy);
				break;
			case 4:
				makeRoom(x,y,dx,dy);
				break;
			case 5:
			case 6:
			case 7:
				makeCorridorToRoom(x,y,dx,dy);
				break;
			case 8:
				makeTunnel(x,y,dx,dy);
				break;
		}
	}

	boolean makeTunnel(int x,int y,int dx,int dy){
		if(map[x][y].blocked) return false;
		if(!(0<=x&&x<SIZE&&0<=y&&y<SIZE)) return false;
		int ndx=dx;
		int ndy=dy;
		map[x][y].blocked=false;
		if(RPG.r(1,3)==1){
			ndx=-dy;
			ndy=dx;
		}
		if(RPG.r(1,4)==1){
			ndx=dy;
			ndy=-dx;
		}
		makeTunnel(x+ndx,y+ndy,ndx,ndy);
		return true;
	}

	boolean makeCorridorToRoom(int x,int y,int dx,int dy){
		// random dimesions and offset
		int cl=RPG.rolldice(2,10);
		// check corridor is clear (3 wide)
		if(isBlank(x-dy,y-dx,x+cl*dx+dy,y+cl*dy+dx)) return false;
		// build room
		if(!makeRoom(x+cl*dx,y+cl*dy,dx,dy)) return false;
		for(int fillx=x;fillx<=x+cl*dx;fillx++)
			for(int filly=y;filly<=y+cl*dy;filly++)
				map[fillx][filly].blocked=false;
		map[x][y].blocked=false;
		map[x+dy][y-dx].blocked=true;
		map[x-dy][y+dx].blocked=true;
		int j1=BigCave.rspread(1,cl-1);
		makeRoom(x+j1*dx,y+j1*dy,dy,-dx);
		int j2=BigCave.rspread(1,cl-1);
		makeRoom(x+j2*dx,y+j2*dy,-dy,dx);
		return true;
	}

	// make a long corridor
	boolean makeCorridor(int x,int y,int dx,int dy){
		int l=RPG.rolldice(2,8);
		if(!isBlank(x,y,x+dx*l,y+dy*l)) return false;
		for(int i=0;i<l;i++)
			map[x+i*dx][y+i*dy].blocked=false;
		// add a door if there is space
		if(l>4&&RPG.r(1,2)==1) if(map[x+dy][y+dx].blocked&&map[x-dy][y-dx].blocked)
			map[x][y].blocked=false;
		// try adding a room to end
		if(l>3) makeRoom(x+dx*l,y+dy*l,dx,dy);
		return true;
	}

	boolean makeRoom(int x,int y,int dx,int dy){
		// random dimesions and offset
		int x1=x-RPG.rolldice(abs(dx-1),5);
		int y1=y-RPG.rolldice(abs(dy-1),5);
		int x2=x+RPG.rolldice(abs(dx+1),5);
		int y2=y+RPG.rolldice(abs(dy+1),5);
		if(x2-x1<3||y2-y1<3||!isBlank(x1,y1,x2,y2)) return false;
		// draw the floor
		for(int floorx=x1+1;floorx<=x2-1;floorx++)
			for(int floory=y1+1;floory<=y2-1;floory++)
				map[floorx][floory].blocked=false;
		// make the door
		map[x][y].blocked=false;
		return true;
	}

	private Point randomdirection(){
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
		return new Point(dx,dy);
	}

	public static final int abs(final int a){
		return a>=0?a:-a;
	}
}
