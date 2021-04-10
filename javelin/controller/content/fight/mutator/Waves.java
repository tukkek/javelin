package javelin.controller.content.fight.mutator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.haunt.Haunt;
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
	protected int waves=RPG.r(MINIMUM,MAXIMUM);
	/** Current wave (1 for first). */
	protected int wave=1;
	/** Encounter level for each wave. */
	protected int waveel;

	List<EncounterIndex> encounters;

	/** {@link EncounterIndex} constructor. */
	public Waves(int elp,List<EncounterIndex> encountersp){
		waveel=elp+Waves.ELMODIFIER.get(waves);
		encounters=encountersp;
	}

	@Override
	public Combatants generate(Fight f){
		return new Combatants(f.getfoes(waveel));
	}

	/**
	 * @param wave Add these units...
	 * @param team to this team...
	 * @param spawn and place each of them at one of these points.
	 * @param f Current fight.
	 */
	protected void add(Combatants wave,List<Combatant> team,List<Point> spawn,
			Fight f){
		Fight.state.next();
		for(var c:wave)
			c.rollinitiative(Fight.state.next.ap);
		team.addAll(wave);
		f.setup.place(wave,spawn);
	}

	@Override
	public void checkend(Fight f){
		var red=Fight.state.redteam;
		if(!red.isEmpty()) return;
		wave+=1;
		if(wave>waves) return;
		var wave=generate(f);
		if(wave==null) return;
		add(wave,red,f.map.getspawn(red),f);
		var p=RPG.pick(wave).getlocation();
		BattleScreen.active.center(p.x,p.y);
		Javelin.redraw();
		Javelin.message(message,true);
	}

	@Override
	public void draw(Fight f){
		checkend(f);
	}

	/**
	 * Generates a wave from {@link Encounter}s. Even with
	 * {@link Haunt#getminimumel()}, it still may be hard to generate a particular
	 * EL from a pool, so we iteratively look for the next-best thing before
	 * giving up, favoring lower ELs before higher ELs.
	 */
	public static Combatants generate(int el,EncounterIndex index) throws GaveUp{
		if(index.isEmpty()) throw new GaveUp();
		var indexes=List.of(index);
		var foes=EncounterGenerator.generatebyindex(el,indexes);
		if(foes!=null) return foes;
		for(var delta=1;delta<=20;delta++){
			foes=EncounterGenerator.generatebyindex(el-delta,indexes);
			if(foes!=null) return foes;
			foes=EncounterGenerator.generatebyindex(el+delta,indexes);
			if(foes!=null) return foes;
		}
		throw new GaveUp();
	}

	/** @return Generates at most {@link #MAXIMUM} waves and returns all units. */
	public static Combatants generatewaves(int elp,EncounterIndex index)
			throws GaveUp{
		var nwaves=RPG.r(MINIMUM,MAXIMUM);
		var waves=new Combatants();
		int el=elp+ELMODIFIER.get(nwaves);
		for(var i=0;i<nwaves;i++)
			waves.addAll(generate(el,index));
		return waves;
	}
}
