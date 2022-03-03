package javelin.controller.content.fight.mutator.mode;

import java.util.Map;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatants;
import javelin.old.RPG;
import javelin.test.TestHaunt;
import javelin.test.TestWaves;
import javelin.view.screen.BattleScreen;

/**
 * A {@link Fight} mode that is composed of distinct waves, with each coming
 * after the previous one is cleared. Currently assumes the EL total is the same
 * for separate waves as one big {@link Encounter} which is the rules
 * by-the-book (see: resources used per encounter table) - but seems like it
 * should be EL-1, at least.
 *
 * @see TestHaunt
 * @see TestWaves
 * @author alex
 */
public class Waves extends FightMode{
	/** Least amount of waves. */
	public static final int MINIMUM=1;
	/** Most amount of waves. */
	public static final int MAXIMUM=4;
	/** EL modifier by number of waves. */
	public static final Map<Integer,Integer> ELMODIFIER=new TreeMap<>();

	static final int ATTEMPTS=10_000;

	static{
		ELMODIFIER.put(MINIMUM,0);
		ELMODIFIER.put(2,-2);
		ELMODIFIER.put(3,-3);
		ELMODIFIER.put(MAXIMUM,-4);
	}

	/** Shown when a new wave appears. */
	protected String message="A new wave of enemies appears!";
	/** Total waves. */
	protected int waves;
	/** Current wave (1 for first). */
	protected int wave=1;
	/** Encounter level for each wave. */
	protected int waveel;

	public Waves(int elp,int waves){
		this.waves=waves;
		waveel=elp+Waves.ELMODIFIER.get(waves);
	}

	public Waves(int elp){
		this(elp,RPG.r(MINIMUM,MAXIMUM));
	}

	@Override
	public Combatants generate(Fight f){
		return new Combatants(f.getfoes(waveel));
	}

	@Override
	public void checkend(Fight f){
		var red=Fight.state.redteam;
		if(!red.isEmpty()) return;
		wave+=1;
		if(wave>waves) return;
		var wave=generate(f);
		if(wave==null) return;
		f.add(wave,red);
		var p=RPG.pick(wave).getlocation();
		BattleScreen.active.center(p.x,p.y);
		Javelin.redraw();
		Javelin.message(message,true);
	}

	@Override
	public void draw(Fight f){
		checkend(f);
	}

	/** @return Generates at most {@link #MAXIMUM} waves and returns all units. */
	public static Combatants generatewaves(int elp,EncounterIndex index)
			throws GaveUp{
		var nwaves=RPG.r(MINIMUM,MAXIMUM);
		var waves=new Combatants();
		int el=elp+ELMODIFIER.get(nwaves);
		for(var i=0;i<nwaves;i++)
			waves.addAll(EncounterGenerator.generate(el,index));
		return waves;
	}
}
