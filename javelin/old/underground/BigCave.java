package javelin.old.underground;

import javelin.controller.Point;
import javelin.model.state.Square;
import javelin.old.RPG;

/**
 * @author alex
 */
public class BigCave extends Caves{
	/** Constructor. */
	public BigCave(){
		super("Big cave");
		coresize=3;
		flying=false;
	}

	@Override
	public void generate(){
		init();
		for(int i=0;i<SIZE*2/3;i++){
			Point p=findfreesquare();
			int x1=BigCave.rspread(1,SIZE-2);
			int y1=BigCave.rspread(1,SIZE-2);
			makerandompath(p.x,p.y,x1,y1,1,1,SIZE-2,SIZE-2,false);
		}

		for(int i=0;i<SIZE*SIZE/80;i++){
			Point p=findfreesquare();
			int x1=p.x-RPG.r(1,6);
			int y1=p.y-RPG.r(1,6);
			int x2=p.x+RPG.r(1,6);
			int y2=p.y+RPG.r(1,6);
			if(x1<1||x2>SIZE-2||y1<1||y2>SIZE-2) continue;
			clearoval(x1,y1,x2,y2);
		}
		close();
	}

	/**
	 * make a random path from (x1,y1) to (x2,y2) staying within region
	 * (x3,y3,x4,y4)
	 */
	public void makerandompath(int x1,int y1,final int x2,final int y2,
			final int x3,final int y3,final int x4,final int y4,
			final boolean diagonals){
		while(x1!=x2||y1!=y2){
			map[x1][y1].blocked=false;
			int dx;
			int dy;
			if(RPG.r(1,3)==1){
				dx=BigCave.sign(x2-x1);
				dy=BigCave.sign(y2-y1);
			}else{
				dx=RPG.r(3)-1;
				dy=RPG.r(3)-1;
			}
			switch(RPG.r(1,diagonals?3:2)){
				case 1:
					dx=0;
					break;
				case 2:
					dy=0;
					break;
			}
			x1+=dx;
			y1+=dy;
			x1=BigCave.middle(x3,x1,x4);
			y1=BigCave.middle(y3,y1,y4);
		}
		map[x2][y2].blocked=false;
	}

	Point findfreesquare(){
		Point p=null;
		while(p==null||map[p.x][p.y].blocked)
			p=new Point(RPG.r(0,SIZE-1),RPG.r(0,SIZE-1));
		return p;
	}

	@Override
	protected void init(){
		for(Square[] squares:map)
			for(Square s:squares)
				s.blocked=true;
		int center=SIZE/2;
		for(int x=center-1;x<=center+1;x++)
			for(int y=center-1;y<=center+1;y++)
				map[x][y].blocked=false;
	}

	/** Clears oval area. */
	void clearoval(int x1,int y1,int x2,int y2){
		if(x1>x2){
			final int t=x1;
			x1=x2;
			x2=t;
		}
		if(y1>y2){
			final int t=y1;
			y1=y2;
			y2=t;
		}
		final double cx=(x1+x2)/2.0;
		final double cy=(y1+y2)/2.0;
		final double cw=(cx-x1)*1.005;
		final double ch=(cy-y1)*1.005;
		for(int x=x1;x<=x2;x++)
			for(int y=y1;y<=y2;y++)
				if((x-cx)*(x-cx)/(cw*cw)+(y-cy)*(y-cy)/(ch*ch)<1)
					map[x][y].blocked=false;
	}

	public static final int sign(final int a){
		return a<0?-1:a>0?1:0;
	}

	// return integer evenly distributed in range
	public static int rspread(int a,int b){
		if(a>b){
			final int t=a;
			a=b;
			b=t;
		}
		return RPG.rand.nextInt(b-a+1)+a;
	}

	// return the middle value
	public static final int middle(final int a,final int b,final int c){
		if(a>b){
			if(b>c) return b;
			if(a>c) return c;
			return a;
		}
		if(a>c) return a;
		if(b>c) return c;
		return b;
	}
}
