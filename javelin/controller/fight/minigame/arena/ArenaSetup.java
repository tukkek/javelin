package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaAcademy;
import javelin.controller.fight.minigame.arena.building.ArenaBuilding;
import javelin.controller.fight.minigame.arena.building.ArenaFlagpole;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaTown;
import javelin.controller.fight.setup.BattleSetup;
import javelin.model.state.Square;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

public class ArenaSetup extends BattleSetup{
	static final int MAPSIZE=28;
	static final int[] ARENASIZE=new int[]{MAPSIZE-2,MAPSIZE-2};
	static final Point[] AREA=new Point[]{
			new Point((MAPSIZE-ARENASIZE[0])/2,(MAPSIZE-ARENASIZE[1])/2),
			new Point((MAPSIZE+ARENASIZE[0])/2,(MAPSIZE+ARENASIZE[1])/2)};

	final ArenaFight fight;

	ArenaSetup(ArenaFight f){
		fight=f;
	}

	@Override
	public void generatemap(Fight f){
		super.generatemap(f);
		Square[][] original=f.map.map;
		Square[][] map=new Square[MAPSIZE][];
		f.map.map=map;
		Fight.state.map=map;
		for(int i=0;i<MAPSIZE;i++)
			map[i]=Arrays.copyOfRange(original[i],0,MAPSIZE);
		for(int x=0;x<MAPSIZE;x++)
			for(int y=0;y<MAPSIZE;y++){
				Square s=map[x][y];
				if(!new Point(x,y).validate(AREA[0].x,AREA[0].y,AREA[1].x,AREA[1].y)){
					s.blocked=true;
					s.flooded=false;
				}else if(x==AREA[0].x||x==AREA[1].x-1||y==AREA[0].y||y==AREA[1].y-1)
					s.blocked=false;
			}
	}

	@Override
	public void place(){
		ArenaTown home=placebuildings();
		fight.enter(Fight.state.blueTeam,Fight.state.blueTeam,home.getlocation());
	}

	Point getcenterpoint(){
		return new Point(RPG.r(AREA[0].x,AREA[1].x),RPG.r(AREA[0].y,AREA[1].y));
	}

	ArenaTown placebuildings(){
		List<List<ArenaBuilding>> quadrants=new ArrayList<>();
		for(int i=0;i<4;i++)
			quadrants.add(new ArrayList<ArenaBuilding>());
		int homei=RPG.r(0,3);
		List<ArenaBuilding> home=quadrants.get(homei);
		ArenaTown t=new ArenaTown(homei);
		home.add(t);
		ArenaFountain fountain=new ArenaFountain();
		fountain.setspent(false);
		home.add(fountain);
		//		home.add(new ArenaShop());
		home.add(new ArenaAcademy());
		for(Building b:home)
			Fight.state.blueTeam.add(b);
		addflags(home,quadrants);
		for(int i=0;i<quadrants.size();i++)
			for(Building b:quadrants.get(i))
				place(b,i);
		definegateways(t);
		return t;
	}

	void definegateways(ArenaTown t){
		List<Combatant> gateways=Fight.state.redTeam;
		Point home=t.getlocation();
		gateways.sort((a,b)->{
			return 100*a.getlocation().distanceinsteps(home)
					-b.getlocation().distanceinsteps(home);
		});
		for(int i=0;i<4;i++){
			ArenaFlagpole g=(ArenaFlagpole)gateways.get(i);
			g.setlevel(Building.LEVELS[i]);
			g.hp=g.maxhp;
		}
	}

	void addflags(List<ArenaBuilding> home,List<List<ArenaBuilding>> quadrants){
		quadrants=new ArrayList<>(quadrants);
		quadrants.remove(home);
		Collections.shuffle(quadrants);
		for(int i=0;i<ArenaFlagpole.STARTING;i++){
			ArenaFlagpole g=new ArenaFlagpole();
			quadrants.get(i%quadrants.size()).add(g);
			Fight.state.redTeam.add(g);
		}
	}

	static public void place(Building b,int quadrant){
		int minx=AREA[0].x+1;
		int maxx=AREA[1].x-2;
		int midx=(minx+maxx)/2;
		int miny=AREA[0].y+1;
		int maxy=AREA[1].y-2;
		int midy=(miny+maxy)/2;
		Point p=null;
		while(p==null){
			int xa=RPG.r(minx,midx);
			int xb=RPG.r(midx,maxx);
			int ya=RPG.r(miny,midy);
			int yb=RPG.r(midy,maxy);
			if(quadrant==0)
				p=new Point(xa,ya);
			else if(quadrant==1)
				p=new Point(xa,yb);
			else if(quadrant==2)
				p=new Point(xb,ya);
			else if(quadrant==3) p=new Point(xb,yb);
			if(!validatesurroundings(p)){
				p=null;
				continue;
			}
		}
		for(int x=p.x-1;x<=p.x+1;x++)
			for(int y=p.y-1;y<=p.y+1;y++)
				Fight.state.map[x][y].clear();
		b.setlocation(p);
	}

	static public boolean validatesurroundings(Point p){
		for(int x=p.x-1;x<=p.x+1;x++)
			for(int y=p.y-1;y<=p.y+1;y++)
				if(Fight.state.getcombatant(p.x,p.y)!=null) return false;
		return true;
	}

	static boolean validate(Point p){
		return p.validate(AREA[0].x,AREA[0].y,AREA[1].x,AREA[1].y)
				&&!Fight.state.map[p.x][p.y].blocked
				&&Fight.state.getcombatant(p.x,p.y)==null;
	}
}