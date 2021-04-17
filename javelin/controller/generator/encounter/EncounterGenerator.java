package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.Water;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.GaveUp;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;

/**
 * Generates an {@link Encounter}.
 *
 * If I'm not mistaken when manually converting {@link Organization} data on
 * monster.xml to a parseable format I only included monster groups up to 16
 * strong - anything other than that for generated encounters will need to be
 * worked upon or done through other means than only this class.
 *
 * @author alex
 */
public class EncounterGenerator{
	static final int MAXSIZEDIFFERENCE=5;
	static final int MAXTRIES=1000;
	static final boolean PRINTINFO=false;

	static int minel=Integer.MIN_VALUE;
	static int maxel=Integer.MAX_VALUE;

	/**
	 * A helper class to abstract where the @link Monster} pool is coming from and
	 * generate {@link Combatants} accordingly. This is helpful because
	 * {@link Terrain} pools are vast and can generate any type of encounter level
	 * easily while {@link EncounterIndex}es are usually more limited and thus
	 * more slowly scan all available encounter pools.
	 *
	 * @author alex
	 */
	public static class MonsterPool{
		List<Terrain> terrains=new ArrayList<>(0);
		EncounterIndex index=null;

		/** Constructor. */
		public MonsterPool(Terrain t){
			terrains.add(t);
		}

		/** Constructor. */
		public MonsterPool(List<Monster> pool){
			index=new EncounterIndex(pool);
		}

		/**
		 * Calls either {@link EncounterGenerator#generate(int, EncounterIndex)} or
		 * {@link EncounterGenerator#generate(int, List)} based on constructor data.
		 */
		public Combatants generate(int el) throws GaveUp{
			return index==null?EncounterGenerator.generate(el,terrains)
					:EncounterGenerator.generate(el,index);
		}
	}

	/**
	 * TODO ideally at some point here would use {@link Terrain#ALL} but currently
	 * {@link Terrain#WATER} can't even reliably generate encounters with EL less
	 * than 2, so clearly some work has got to go into adding low-level aquatic
	 * enemies first.
	 */
	static int checklimit(int baseline,int step){
		String failure=null;
		for(var el=baseline;;el+=step)
			for(var t:Terrain.NONWATER){
				if(Javelin.DEBUG&&PRINTINFO)
					failure=String.format("Failure: %s el%s",t,el);
				if(generate(el,t)==null){
					if(failure!=null) System.out.println(failure);
					return el-step;
				}
			}
	}

	static{
		minel=checklimit(0,-1);
		maxel=checklimit(0,+1);
	}

	/**
	 * @param el Target encounter level - will work around this is cannot generate
	 *          exactly what is given.
	 * @param encounters Usually {@link Terrain#current()} but not necessarily -
	 *          for example not when generation a
	 *          {@link javelin.model.world.location.Location#garrison}, which uses
	 *          the local terrain instead.
	 * @return Enemy units for an encounter. <code>null</code> should not be
	 *         returned to external calls of this class, as Encounter Levels
	 *         should be padded to safety - the exception would be when using a
	 *         very limited pools like only {@link Water}, or a terran that
	 *         happens to have an empty gap in EL for some reason. In most typical
	 *         cases it should be safe to not expect a <code>null</code> return.
	 */
	public static Combatants generatebyindex(int el,
			List<EncounterIndex> encounters){
		if(el<minel) el=minel;
		if(el>maxel) el=maxel;
		Combatants encounter=null;
		for(int i=0;i<MAXTRIES;i++){
			encounter=select(el,encounters);
			if(encounter!=null) return encounter;
		}
		return null;
	}

	/** {@link #generatebyindex(int, List)} with {@link Terrain}s instead. */
	public static Combatants generate(int el,List<Terrain> terrains){
		var encounters=new ArrayList<EncounterIndex>(terrains.size());
		for(var t:terrains)
			encounters.add(Organization.ENCOUNTERSBYTERRAIN.get(t.toString()));
		while(encounters.remove(null))
			continue;
		return generatebyindex(el,encounters);
	}

	static Combatants select(int elp,List<EncounterIndex> encounters){
		ArrayList<Integer> popper=new ArrayList<>();
		popper.add(elp);
		while(RPG.chancein(2)){
			Integer pop=popper.get(RPG.r(0,popper.size()-1));
			popper.remove(popper.indexOf(pop));
			pop-=2;
			popper.add(pop);
			popper.add(pop);
		}
		final Combatants foes=new Combatants();
		for(final int el:popper){
			List<Combatant> group=makeencounter(el,encounters);
			if(group==null) return null;
			for(Combatant invitee:group)
				if(!validatecreature(invitee,foes)) return null;
			for(Combatant invitee:group)
				foes.add(invitee);
		}
		if(!new AlignmentDetector(foes).check()) return null;
		return foes.size()>getmaxenemynumber()?null:foes;
	}

	private static boolean validatecreature(Combatant invitee,
			ArrayList<Combatant> foes){
		if(foes.indexOf(invitee)>=0) return false;
		final boolean underground=Dungeon.active!=null;
		return !invitee.source.nightonly||underground
				||Period.NIGHT.is()&&Period.EVENING.is();
	}

	/**
	 * See {@link EncounterGenerator}'s main javadoc description for mote info on
	 * enemy group size.
	 *
	 * @return The recommended number of enemies to face at most in one battle.
	 *         Other modules may differ from this but this is a suggestion to
	 *         avoid the computer player taking a long time to act while the human
	 *         player has to wait (for example: 1 human unit against 20 enemies).
	 */
	public static int getmaxenemynumber(){
		int current=4;
		if(Fight.state==null){
			if(Squad.active!=null) current=Squad.active.members.size();
		}else if(!Fight.state.blueteam.isEmpty())
			current=Fight.state.blueteam.size();
		return MAXSIZEDIFFERENCE+current;
	}

	static List<Combatant> makeencounter(final int el,
			List<EncounterIndex> encounters){
		List<Encounter> possibilities=new ArrayList<>();
		for(var index:encounters){
			List<Encounter> tier=index.get(el);
			if(tier!=null) possibilities.addAll(tier);
		}
		return possibilities.isEmpty()?null:RPG.pick(possibilities).generate();
	}

	/** As {@link #generate(int, Terrain)} but for one terrain. */
	public static Combatants generate(int el,Terrain t){
		return generate(el,List.of(t));
	}

	/**
	 * Generates an encounter given an EL. Even with {@link Haunt#getminimumel()},
	 * it still may be hard to generate a particular EL from a pool, so we
	 * iteratively look for the next-best thing before giving up, favoring lower
	 * ELs before higher ELs.
	 */
	public static Combatants generate(int el,EncounterIndex index) throws GaveUp{
		if(index.isEmpty()) throw new GaveUp();
		var indexes=List.of(index);
		var foes=generatebyindex(el,indexes);
		if(foes!=null) return foes;
		for(var delta=1;delta<=20;delta++){
			foes=generatebyindex(el-delta,indexes);
			if(foes!=null) return foes;
			foes=generatebyindex(el+delta,indexes);
			if(foes!=null) return foes;
		}
		throw new GaveUp();
	}
}
