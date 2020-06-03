package javelin.controller.fight;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.setup.LocationFightSetup;
import javelin.controller.map.location.LocationMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.Location;
import javelin.old.RPG;

/**
 * Baseline class for a fight that is composed of distinct "waves", with each
 * one coming after the last wave is cleared.
 *
 * TODO is woefully not self-contained, being tied to a {@link LocationFight}
 * and not even having the number of waves being fought, etc.
 *
 * @author alex
 */
public abstract class WavesFight extends LocationFight{
	/** EL modifier by number of waves. */
	public static final Map<Integer,Integer> ELMODIFIER=new TreeMap<>();
	/** Shown when a new wave appears. */
	protected String message="A new wave of enemies appears!";

	static{
		ELMODIFIER.put(1,0);
		ELMODIFIER.put(2,-2);
		ELMODIFIER.put(3,-3);
		ELMODIFIER.put(4,-4);
	}

	/** Total waves. */
	protected int waves=RPG.r(1,4);
	/** Current wave. 0 before any wave, 1 at first #generatewave(). */
	protected int wave=0;
	int el;

	/** Constructor. */
	public WavesFight(Location l,LocationMap map,int el){
		super(l,map);
		this.el=el+getelmodifier();
	}

	/** @return Proper {@link #ELMODIFIER} given {@link #waves}. */
	protected Integer getelmodifier(){
		return WavesFight.ELMODIFIER.get(waves);
	}

	/**
	 * @param el Target Encounter Level.
	 * @return New enemies to enter the fight.
	 * @throws GaveUp If cannot generate. Will probably end the fight immediately
	 *           in this case.
	 */
	abstract protected Combatants generatewave(int el) throws GaveUp;

	@Override
	public void checkend(){
		try{
			if(!Fight.state.redTeam.isEmpty()) return;
			wave+=1;
			if(wave>waves) return;
			var wave=generatewave(el);
			if(wave==null) return;
			add(wave,Fight.state.redTeam,((LocationMap)map).spawnred);
			Javelin.redraw();
			Javelin.message(message,true);
		}catch(GaveUp e){
			if(Javelin.DEBUG) throw new RuntimeException(e);
		}finally{
			super.checkend();
		}
	}

	protected void add(Combatants wave,List<Combatant> team,List<Point> spawn){
		for(var c:wave)
			c.rollinitiative(Fight.state.next.ap);
		team.addAll(wave);
		(team==Fight.state.redTeam?Fight.originalredteam:Fight.originalblueteam)
				.addAll(wave);
		((LocationFightSetup)setup).place(wave,spawn);
	}

	@Override
	public void draw(){
		checkend();
	}
}