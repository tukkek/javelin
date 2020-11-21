package javelin.controller.fight.mutator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.EncounterIndex;
import javelin.controller.fight.Fight;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * A fight that is composed of distinct waves, with each coming after the
 * previous one is cleared.
 *
 * @author alex
 */
public class Waves extends Mutator{
	/** Least amount of waves. */
	public static final int MINIMUM=1;
	/** Most amount of waves. */
	public static final int MAXIMUM=4;

	/** EL modifier by number of waves. */
	public static final Map<Integer,Integer> ELMODIFIER=new TreeMap<>();
	/** Shown when a new wave appears. */
	protected String message="A new wave of enemies appears!";

	static{
		ELMODIFIER.put(MINIMUM,0);
		ELMODIFIER.put(2,-2);
		ELMODIFIER.put(3,-3);
		ELMODIFIER.put(MAXIMUM,-4);
	}

	/** Total waves. */
	protected int waves=RPG.r(MINIMUM,MAXIMUM);
	/** Current wave. 0 before any wave, 1 at first #generatewave(). */
	protected int wave=0;
	/** Given {@link Encounter}s. */
	protected List<EncounterIndex> encounters;

	int waveel;

	/** {@link EncounterIndex} constructor. */
	public Waves(int elp,List<EncounterIndex> encountersp){
		waveel=elp+Waves.ELMODIFIER.get(waves);
		encounters=encountersp;
	}

	@Override
	public void prepare(Fight f){
		super.prepare(f);
		Fight.state.redTeam=generatewave(waveel,f);
		wave=1;
	}

	/**
	 * @param el Target Encounter Level.
	 * @return New enemies to enter the fight.
	 */
	protected Combatants generatewave(int el,Fight f){
		return new Combatants(f.getfoes(el));
	}

	/**
	 * @param wave Add these units...
	 * @param team to this team...
	 * @param spawn and place eac of them at one of these points.
	 * @param f Current fight.
	 */
	protected void add(Combatants wave,List<Combatant> team,List<Point> spawn,
			Fight f){
		for(var c:wave)
			c.rollinitiative(Fight.state.next.ap);
		team.addAll(wave);
		var original=team==Fight.state.redTeam?Fight.originalredteam
				:Fight.originalblueteam;
		original.addAll(wave);
		f.setup.place(wave,spawn);
	}

	@Override
	public void checkend(Fight f){
		var red=Fight.state.redTeam;
		if(!red.isEmpty()) return;
		wave+=1;
		if(wave>waves) return;
		var wave=generatewave(waveel,f);
		if(wave==null) return;
		add(wave,red,f.map.getspawn(red),f);
		Javelin.redraw();
		var p=RPG.pick(wave).getlocation();
		BattleScreen.active.center(p.x,p.y);
		Javelin.message(message,true);
	}

	@Override
	public void draw(Fight f){
		checkend(f);
	}
}
