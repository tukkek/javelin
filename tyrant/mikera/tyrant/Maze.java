/*
 * Created on 26-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.RPG;

/**
 * @author Mike
 *
 * Maze implements methods for constructing maze-like dungeons
 */

public class Maze {
	public static void buildMaze(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1,y1,x2,y2,m.wall());
		buildInnerMaze(m,x1+1,y1+1,x2-1,y2-1);
	}
	
	public static void buildInnerMaze(BattleMap m, int x1, int y1, int x2, int y2) {
		int floor=m.floor();
		int wall=m.wall();
		
		m.fillArea(x1,y1,x2,y2,wall);
		
		int w=x2-x1+1; int rw=(w+1)/2;
		int h=y2-y1+1; int rh=(h+1)/2;
		
		int sx=x1+2*RPG.r(rw);
		int sy=y1+2*RPG.r(rh);
		m.setTile(sx,sy,floor);
		
		int finishedCount=0;
		for (int i=1; (i<(rw*rh*1000))&&(finishedCount<(rw*rh)); i++) {
			int x=x1+2*RPG.r(rw);
			int y=y1+2*RPG.r(rh);
			if (m.getTile(x,y)!=wall) continue;
			int dx=(RPG.d(2)==1)?(RPG.r(2)*2-1):0;
			int dy=(dx==0)      ?(RPG.r(2)*2-1):0;
			int lx=x+dx*2;
			int ly=y+dy*2;
			if ((lx>=x1)&&(lx<=x2)&&(ly>=y1)&&(ly<=y2)) {
				if (m.getTile(lx,ly)!=wall) {
					m.setTile(x,y,floor);
					m.setTile(x+dx,y+dy,floor);
					finishedCount++;
				}
			}
		}
	}
}
