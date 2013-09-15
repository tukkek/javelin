/*
 * Created on 10-Jan-2005
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class GoblinVillage {
	public static BattleMap makeGoblinVillage() {
		int w=61;
		int h=61;		
		BattleMap m=new BattleMap(w,h);
		m.setTheme("goblins");
		m.set("IsHostile",0);
		m.set("Description","Goblin Village");
		m.set("EnterMessage","You hear the sound of goblin drums...");

		m.set("Level",3);
		

		
		
		// entrance
		Thing ent=Portal.create("invisible portal");
		m.addThing(ent,30,0);
		m.setEntrance(ent);
		
		m.makeRandomPath(w/2,0,w/2,h/2,0,0,w-1,h-1,m.floor(),false);
		
		// create disjoint 9*9 areas
		Point[] ps=new Point[RPG.d(2,10)];
		ps[0]=new Point(w/2,h/2);
		int count=1;
		for (int i=1; i<ps.length; i++) {
			for (int q=0; q<20; q++) {
				Point p=new Point(6+RPG.r(w-12),6+RPG.r(h-12));
				ps[i]=p;
				boolean found=true;
				for (int j=0; j<i; j++) {
					if (ps[j]==null) continue;
					if (ps[j].hvDistance(p)<9) {
						found=false;
						break;
					}
				}
				if (!found) {
					ps[i]=null;
					continue;
				}
				break;
			}
			if (ps[i]!=null) count++;
		}
		
		// compress array
		if (count<ps.length){
			Point[] ts=new Point[count];
			
			count=0;
			for (int i=0; i<ps.length; i++) {
				if (ps[i]!=null) {
					ts[count++]=ps[i];
				}
			}
			ps=ts;
		}
		
		for (int i=1; i<ps.length; i++) {
			int j=RPG.r(i);
			m.makeRandomPath(ps[i].x,ps[i].y,ps[j].x,ps[j].y,0,0,w-1,h-1,m.floor(),false);			
		}
 		
		makeChiefHut(m,ps[0].x,ps[0].y);
		
		for (int i=1; i<ps.length; i++) {
			makeRandomFeature(m,ps[i].x,ps[i].y);			
		}
		
		for (int i=0; i<30; i++) {
			m.addThing(Lib.createType("IsGoblinoid",RPG.d(10)));
		}
		
		for (int x=0; x<w; x++) {
			for (int y=0; y<h; y++) {
				Thing gob=m.getMobile(x,y);
				if (gob!=null) {
					AI.setNeutral(gob);
				}
				int d=RPG.distSquared(x,y,w/2,h/2);
				if (m.isBlank(x,y)&&(RPG.d(2500)<d)) {
					m.addThing(Lib.create("withered tree"),x,y);
				}
			}
		}
		
		m.completeArea(0,0,w-1,h-1,Tile.GUNK);
		
		return m;
	}
	
	public static void makeChiefHut(BattleMap m,int x, int y) {
		m.fillOval(x-6,y-6,x+6,y+6,m.floor());
		m.fillArea(x-4,y-2,x+4,y+2,m.wall());
		m.fillArea(x-3,y-3,x+3,y+3,m.wall());
		m.fillArea(x-3,y-1,x+3,y+1,m.floor());
		m.fillArea(x-2,y-2,x+2,y+2,m.floor());
		m.fillArea(x,y-4,x,y,m.floor());
		
		// the boss with an artifact
		Thing wb=Lib.create("goblin war-boss");
		AI.name(wb,Name.createGoblinName()+" the goblin war-boss");
		m.addThing(wb,x,y);
		AI.setGuard(wb,m,x,y+2,x,y+2);
		wb.addThing(Lib.createArtifact(20));
		
		// goblin guard
		for (int i=RPG.d(2,4); i>0; i--) {
			Thing g=Lib.create(RPG.pick(new String[] {"goblin chieftain","goblin shaman","goblin hero"}));
			AI.name(g,Name.createGoblinName()+" "+g.getTheName());
			m.addThing(g,x-3,y-2,x+3,y+2);
			AI.setGuard(g,m,g.x,g.y);
		}
	}
	
	public static void makeRandomFeature(BattleMap m,int cx, int cy) {
		switch (RPG.d(10)) {
			case 1:
				// archer barricade
				m.fillOval(cx-4,cy-4,cx+4,cy+4,m.floor());
				for (int x1=-2; x1<=2; x1++) {
					for (int y1=-2; y1<=2; y1++) {
						if ((x1==2)||(x1==-2)||(y1==2)||(y1==-2)) {
							m.addThing("barricade",cx+x1,cy+y1);
						} else {
							if (RPG.d(2)==1) {
								m.addThing("goblin archer",cx+x1,cy+y1);
							} else {
								m.addThing("[IsStoreItem]",cx+x1,cy+y1);
							}
						}
					}
				}
				
				return;
				
			case 2: 
				// meeting
				m.fillOval(cx-3,cy-3,cx+3,cy+3,m.floor());
				for (int x1=-2; x1<=2; x1++) {
					for (int y1=-2; y1<=2; y1++) {
						Thing gob=Lib.createType("IsGoblinoid",RPG.d(15));
						m.addThing(gob,cx+x1,cy+y1);
						AI.setGuard(gob,m,cx+x1,cy+y1);
					}
				}
				return;
				
			case 3: 
				m.fillArea(cx-2,cy-2,cx+2,cy+2,m.floor());
				for (int x1=-2; x1<=2; x1++) {
					for (int y1=-2; y1<=2; y1++) {
						if (RPG.d(3)==1) {
							Thing tent=Lib.create("tent");
							m.addThing(tent,cx+x1,cy+y1);
						}
					}
				}
				return;
				
			case 4: 
				m.fillOval(cx-2,cy-2,cx+2,cy+2,m.floor());
				for (int x1=-1; x1<=1; x1++) {
					for (int y1=-1; y1<=1; y1++) {
						if ((x1==0)&&(y1==0)) {
							m.addThing(Fire.create(10),cx,cy);
						} else if (RPG.d(2)==1) {
							Thing gob=Lib.createType("IsGoblinoid",RPG.d(10));
							m.addThing(gob,cx+x1,cy+y1);
							AI.setGuard(gob,m,cx+x1,cy+y1);
						}
					}
				}
				return;
				
			default:
				m.fillOval(cx-2,cy-2,cx+2,cy+2,m.floor());
				Thing gob=Lib.createType("IsGoblinoid",10+RPG.d(10));
				m.addThing(gob,cx,cy);
				AI.setGuard(gob,m,cx,cy,cx,cy);			
				return;
				
		}
	}
	
	public static void init() {
		Thing t;
		
		t=Lib.extend("goblin tribesman","goblin");
		t.set("Frequency",0);
		AI.setNeutral(t);
		t.set("LevelMin",7);
		Lib.add(t);
		
		t=Lib.extend("goblin guard","goblin tribesman");
		Monster.strengthen(t,3);
		t.set("Image",243);
		t.set("LevelMin",15);
		Lib.add(t);

		
		
	}
}
