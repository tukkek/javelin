package javelin.controller.fight.setup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.map.Map;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Given a {@link Map}, a {@link Squad} and a {@link Fight} setups an initial
 * battle state.
 *
 * @author alex
 */
public class BattleSetup{
	static final Point NOTPLACED=new Point(-1,-1);
	static final int MAXDISTANCE=6;
	/**
	 * Improving battle placement by not allowing more than 1 adjacent ally at a
	 * time (otherwise it's too easy to cramp an entire army together so that each
	 * army won't see each other and then fulfill the other position requirements)
	 */
	static final int MAXADJACENT=1;

	/** Starts the setup steps. */
	public void setup(){
		rollinitiative();
		Fight f=Javelin.app.fight;
		generatemap(f);
		try{
			place();
		}catch(GaveUp e){
			throw new RuntimeException("Could not place combatants!",e);
		}
		Weather.flood();
	}

	/** Allows greater control of {@link Map} generation. */
	public void generatemap(Fight f){
		if(f.map==null){
			Terrain t;
			if(Dungeon.active!=null)
				t=Terrain.UNDERGROUND;
			else if(f.terrain==null)
				t=Terrain.current();
			else
				t=f.terrain;
			f.map=t.getmaps().pick();
		}
		f.map.generate();
		Fight.state.map=f.map.map;
	}

	/** Rolls initiative for each {@link Combatant}. */
	public void rollinitiative(){
		for(final Combatant c:Fight.state.getcombatants()){
			c.ap=0;
			c.rollinitiative();
		}
	}

	/**
	 * Sets each {@link Combatant} in a sensible starting location.
	 *
	 * @throws GaveUp If exceeded maximum allowed attempts.
	 */
	public void place() throws GaveUp{
		var state=Fight.state;
		for(int i=0;i<1000;i++)
			try{
				place(state);
				return;
			}catch(GaveUp e){
				continue;
			}
		throw new GaveUp();
	}

	void place(BattleState s) throws GaveUp{
		for(var c:s.getcombatants())
			c.setlocation(NOTPLACED);
		var blueseed=RPG.chancein(2);
		var a=RPG.shuffle(new LinkedList<>(blueseed?s.blueTeam:s.redTeam));
		var b=RPG.shuffle(new LinkedList<>(blueseed?s.redTeam:s.blueTeam));
		var placeda=new ArrayList<Combatant>();
		var placedb=new ArrayList<Combatant>();
		var seeda=a.pop();
		var seedb=b.pop();
		seeda.setlocation(getrandompoint(s));
		placeda.add(seeda);
		placecombatant(seedb,seeda,null,null,s);
		placedb.add(seedb);
		while(!a.isEmpty()||!b.isEmpty()){
			var queue=RPG.chancein(2)?a:b;
			if(queue.isEmpty()) continue;
			var unit=queue.pop();
			var allies=queue==a?placeda:placedb;
			var enemies=queue==a?placedb:placeda;
			var success=false;
			for(var ally:RPG.shuffle(allies))
				if(placecombatant(unit,ally,allies,enemies,s)){
					success=true;
					break;
				}
			if(!success) throw new GaveUp();
			allies.add(unit);
		}
	}

	boolean placecombatant(Combatant c,Combatant reference,
			ArrayList<Combatant> allies,List<Combatant> enemies,BattleState s){
		var source=reference.getlocation();
		var vision=reference.calculatevision(s);
		var all=s.getcombatants();
		for(var combatant:all)
			vision.remove(combatant.getlocation());
		for(var p:RPG.shuffle(new ArrayList<>(vision))){
			if(p.distanceinsteps(source)>MAXDISTANCE||s.map[p.x][p.y].blocked)
				continue;
			if(allies!=null
					&&allies.stream().filter(a->a.getlocation().distanceinsteps(p)==1)
							.limit(MAXADJACENT+1).count()==MAXADJACENT)
				continue;
			if(enemies!=null&&cansee(enemies,p)) continue;
			c.setlocation(p);
			return true;
		}
		return false;
	}

	boolean cansee(List<Combatant> enemies,Point p){
		for(var enemy:enemies)
			if(Fight.state.haslineofsight(enemy,p)!=Vision.BLOCKED) return true;
		return false;
	}

	/**
	 * @return A free spot inside the given coordinates. Will loop infinitely if
	 *         given space is fully occupied.
	 */
	static Point getrandompoint(final BattleState s){
		var width=s.map.length;
		var height=s.map[0].length;
		Point p=null;
		while(p==null||s.map[p.x][p.y].blocked||s.getcombatant(p.x,p.y)!=null)
			p=new Point(RPG.r(2,width-3),RPG.r(2,height-3));
		return p;
	}
}
