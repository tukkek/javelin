package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaBuilding;
import javelin.controller.fight.minigame.arena.building.ArenaFlagpole;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaShop;
import javelin.controller.fight.minigame.arena.building.ArenaTown;
import javelin.controller.fight.setup.BattleSetup;
import javelin.model.state.Square;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

public class ArenaSetup extends BattleSetup{
	static final int MAPSIZE=28;

	final Arena fight;

	ArenaSetup(Arena f){
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
				if(x==0||y==0||x==MAPSIZE-1||y==MAPSIZE-1) s.blocked=true;
				s.flooded=false;
			}
	}

	@Override
	public void place(){
		ArenaTown home=placebuildings();
		var blue=Arena.get().getallies();
		fight.enter(blue,Fight.state.blueTeam,home.getlocation());
	}

	ArenaTown placebuildings(){
		var buildings=new ArrayList<ArenaBuilding>();
		buildings.add(new ArenaTown());
		var f=new ArenaFountain();
		f.setspent(false);
		buildings.add(f);
		buildings.add(new ArenaShop());
		for(Building b:buildings){
			Fight.state.blueTeam.add(b);
			place(b);
		}
		for(int i=0;i<4;i++){
			ArenaFlagpole g=new ArenaFlagpole();
			Fight.state.redTeam.add(g);
			place(g);
		}
		definegateways(new ArenaTown());
		return new ArenaTown();
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

	/**
	 * @param b Puts a building on the map, selecting a randomly-selected but
	 *          viable location.
	 */
	static public void place(Building b){
		var max=Fight.state.map.length-3;
		Point p=null;
		while(p==null||!validate(b,p))
			p=new Point(RPG.r(2,max),RPG.r(2,max));
		for(int x=p.x-1;x<=p.x+1;x++)
			for(int y=p.y-1;y<=p.y+1;y++)
				Fight.state.map[x][y].clear();
		b.setlocation(p);
	}

	static boolean validate(Building b,Point p){
		for(var c:Fight.state.getcombatants()){
			if(c.equals(b)) continue;
			var l=c.getlocation();
			if(l.equals(p)) return false;
			if(c instanceof ArenaBuilding&&p.distanceinsteps(l)<=2) return false;
		}
		return true;
	}

}