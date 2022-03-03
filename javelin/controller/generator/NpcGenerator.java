package javelin.controller.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.old.RPG;

public class NpcGenerator{
	static final boolean DEBUG=false;
	static final int MAXCR=25;
	static final double RATIO=.1;

	ArrayList<Monster> candidates=new ArrayList<>();
	int totalregistered=0;

	void register(Combatant c,List<?> terrains){
		if(c.source.isaquatic()) return;
		for(Object t:terrains){
			ArrayList<Combatant> encounter=new ArrayList<>(1);
			encounter.add(c);
			Organization.ENCOUNTERSBYTERRAIN.get(t.toString().toLowerCase()).put(
					ChallengeCalculator.crtoel(c.source.cr),new Encounter(encounter));
			totalregistered+=1;
		}
	}

	void generatenpcs(){
		for(float cr:getcrs()){
			List<Monster> tier=Monster.BYCR.get(cr);
			if(1<=cr&&cr<=MAXCR&&!candidates.isEmpty()){
				double npcs=gettarget(tier);
				for(int i=0;i<npcs;i++){
					Monster m=RPG.pick(candidates);
					Combatant c=generatenpc(m,cr);
					if(c!=null) register(c,Terrain.NONWATER);
				}
			}
			if(tier!=null) for(Monster m:tier)
				if(m.think(-1)) candidates.add(m);
		}
	}

	void generateelites(){
		List<Monster> last=null;
		for(float cr:getcrs()){
			if(cr>=5) return;
			List<Monster> tier=Monster.BYCR.get(cr);
			if(tier==null) continue;
			if(last!=null){
				double elites=gettarget(tier);
				for(int i=0;i<elites;i++){
					var elite=generateelite(RPG.pick(last),cr);
					if(elite.source.cr>cr) register(elite,elite.source.getterrains());
				}
			}
			last=tier;
		}
	}

	@SuppressWarnings("unused")
	public void generate(){
		generatenpcs();
		generateelites();
		if(Javelin.DEBUG&&DEBUG){
			float total=0;
			for(EncounterIndex encounters:Organization.ENCOUNTERSBYTERRAIN.values())
				total+=encounters.count();
			int percent=Math.round(100*totalregistered/total);
			System.out
					.println("Total registered: "+totalregistered+" ("+percent+"%)");
		}
	}

	static Combatant generateelite(Monster m,float targetcr){
		Combatant c=new Combatant(m,true);
		c.source.customName="Elite "+c.source.name.toLowerCase();
		while(c.source.cr<targetcr&&Commoner.SINGLETON.upgrade(c))
			ChallengeCalculator.calculatecr(c.source);
		c.source.elite=true;
		return c;
	}

	double gettarget(List<Monster> tier){
		double npcs;
		if(tier==null||tier.size()<2)
			npcs=2;
		else
			npcs=Math.max(1,tier.size()*RATIO);
		return npcs;
	}

	TreeSet<Float> getcrs(){
		TreeSet<Float> crs=new TreeSet<>(Monster.BYCR.keySet());
		for(int cr=1;cr<=MAXCR;cr++)
			crs.add((float)cr);
		return crs;
	}

	/**
	 * @return As {@link #generatenpc(Monster, Kit, float)} but uses
	 *         {@link Kit#getpreferred(Monster, boolean)}t.
	 */
	public static final Combatant generatenpc(Monster m,float cr){
		var kits=Kit.getpreferred(m,true);
		if(kits.isEmpty()) return null;
		var kit=RPG.pick(kits);
		return generatenpc(m,kit,cr);
	}

	/**
	 * @param m Monster to generate a NPC off of.
	 * @param k Kit to be used for {@link Upgrade}s.
	 * @param targetcr Target challenge rating.
	 * @return A fully generated NPC or <code>null</code> if failed to generate
	 *         one with the given parameters.
	 */
	public static Combatant generatenpc(Monster m,Kit k,float targetcr){
		Float originalcr=m.cr;
		Combatant c=new Combatant(m,true);
		float base=c.source.cr+(targetcr-c.source.cr)/2;
		while(c.source.cr<base&&k.classlevel.apply(c))
			ChallengeCalculator.calculatecr(c.source);
		while(c.source.cr<targetcr)
			if(!k.upgrade(c)) return null;
		if(c.source.cr<=originalcr) return c;
		k.rename(c.source);
		c.source.elite=true;
		return c;
	}

	/**
	 * If intelligent, {@link #generatenpc(Monster, float)}, else
	 * {@link #generateelite(Monster, float)}.
	 *
	 * @see Monster#think(int)
	 */
	public static Combatant generate(Monster m,int cr){
		return m.think(-1)?generatenpc(m,cr):generateelite(m,cr);
	}

	/**
	 * @param el Attempts to match the Encounter Level on a best-effort basis: may
	 *          be higher if an applied {@link Upgrade} was too strong or lower if
	 *          failed to {@link Kit#upgrade(Combatant)} any unit.
	 * @param weakest If <code>true</code> will prioritize the weakest
	 *          {@link Combatant} with each {@link Upgrade} - random otherwise.
	 * @return The same {@link Combatant} instances, upgraded to the target EL and
	 *         renamed according to each assigned {@link Kit} per unit.
	 */
	public static Combatants upgrade(Combatants npcs,int el,boolean weakest){
		var kits=new HashMap<Combatant,Kit>(npcs.size());
		while(ChallengeCalculator.calculateel(npcs)<el){
			var w=weakest?npcs.getweakest():RPG.pick(npcs);
			var k=kits.get(w);
			if(k==null){
				k=RPG.pick(Kit.getpreferred(w.source,true));
				kits.put(w,k);
				k.rename(w.source);
			}
			if(!k.upgrade(w)) break;
		}
		return npcs;
	}
}
