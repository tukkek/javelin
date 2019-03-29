package javelin.controller.fight.setup;

import java.util.ArrayList;
import java.util.LinkedList;

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
	private static final int MAXDISTANCE=9;

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

	/**
	 * Sets each {@link Combatant} in a sensible starting location.
	 *
	 * @throws GaveUp If exceeded maximum allowed attempts.
	 */
	public void place() throws GaveUp{
		var state=Fight.state;
		var width=state.map.length;
		var height=state.map[0].length;
		for(int i=0;i<1000;i++)
			try{
				place(state,width,height);
				return;
			}catch(GaveUp e){
				continue;
			}
		throw new GaveUp();
	}

	void place(BattleState s,int width,int height) throws GaveUp{
		var redseed=RPG.chancein(2);
		var a=RPG.shuffle(new LinkedList<>(redseed?s.blueTeam:s.redTeam));
		var b=RPG.shuffle(new LinkedList<>(redseed?s.redTeam:s.blueTeam));
		var placeda=new ArrayList<Combatant>();
		var placedb=new ArrayList<Combatant>();
		var seeda=a.pop();
		var seedb=b.pop();
		add(seeda,getrandompoint(s,0,width-1,0,height-1));
		placeda.add(seeda);
		placecombatant(seedb,seeda,s);
		placedb.add(seedb);
		while(!a.isEmpty()||!b.isEmpty()){
			var queue=RPG.chancein(2)?a:b;
			if(queue.isEmpty()) continue;
			var unit=queue.pop();
			var placed=queue==a?placeda:placedb;
			placecombatant(unit,RPG.pick(placed),s);
			placed.add(unit);
		}
	}

	/** Rolls initiative for each {@link Combatant}. */
	public void rollinitiative(){
		for(final Combatant c:Fight.state.getcombatants()){
			c.ap=0;
			c.rollinitiative();
		}
	}

	void placecombatant(final Combatant placing,final Combatant reference,
			final BattleState s) throws GaveUp{
		int vision=placing.view(s.period);
		if(vision>8||vision>MAXDISTANCE) vision=MAXDISTANCE;
		final ArrayList<Point> possibilities=mappossibilities(reference,vision,s);
		while(!possibilities.isEmpty()){
			Point p=RPG.pick(possibilities);
			placing.location[0]=p.x;
			placing.location[1]=p.y;
			Vision path=s.haslineofsight(placing,reference);
			if(path==Vision.CLEAR){
				add(placing,p);
				break;
			}
			possibilities.remove(p);
		}
		if(possibilities.isEmpty()) throw new GaveUp();
	}

	ArrayList<Point> mappossibilities(final Combatant reference,int vision,
			final BattleState s){
		final ArrayList<Point> possibilities=new ArrayList<>();
		for(int x=reference.location[0]-vision;x<=reference.location[0]+vision;x++)
			if(isbound(x,s.map))
				for(int y=reference.location[1]-vision;y<=reference.location[1]
						+vision;y++)
				if(isbound(y,s.map[0])&&!s.map[x][y].blocked&&s.getcombatant(x,y)==null)
					possibilities.add(new Point(x,y));
		return possibilities;
	}

	/**
	 * @return <code>true</code> if inside battle map.
	 */
	public static boolean isbound(final int y,final Object[] squares){
		return 0<y&&y<=squares.length-1;
	}

	/**
	 * @param c Sets location to given {@link Point}.
	 */
	void add(final Combatant c,final Point p){
		c.location[0]=p.x;
		c.location[1]=p.y;
	}

	/**
	 * @return A free spot inside the given coordinates. Will loop infinitely if
	 *         given space is fully occupied.
	 */
	static public Point getrandompoint(final BattleState s,int minx,int maxx,
			int miny,int maxy){
		minx=Math.max(minx,0);
		miny=Math.max(miny,0);
		maxx=Math.min(maxx,s.map.length-1);
		maxy=Math.min(maxy,s.map[0].length-1);
		Point p=null;
		while(p==null||s.map[p.x][p.y].blocked||s.getcombatant(p.x,p.y)!=null)
			p=new Point(RPG.r(minx,maxx),RPG.r(miny,maxy));
		return p;
	}

	/**
	 * @return Same as {@link #getrandompoint(BattleState, int, int, int, int)}
	 *         but near to a given point.
	 */
	public static Point getrandompoint(BattleState state,Point p){
		return getrandompoint(state,p.x-2,p.x+2,p.y-2,p.y+2);
	}

	/**
	 * @return Same as {@link #getrandompoint(BattleState, int, int, int, int)}
	 *         but receives two Points as parameters, forming a square area.
	 */
	public static Point getrandompoint(BattleState state,Point min,Point max){
		return getrandompoint(state,min.x,max.x,min.y,max.y);
	}

}
