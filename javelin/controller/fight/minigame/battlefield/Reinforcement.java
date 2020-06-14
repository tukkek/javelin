package javelin.controller.fight.minigame.battlefield;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.old.RPG;

public class Reinforcement{
	public Combatants commander=new Combatants();
	public Combatants elites;
	public Combatants footsoldiers=new Combatants();
	List<Terrain> terrains;

	public Reinforcement(int el,List<Terrain> t){
		terrains=t;
		el=Math.min(el,Battlefield.HIGHESTEL);
		generatecommander(el);
		generateelites(el);
		generatefootsoldiers(el);
	}

	public Reinforcement(float el,List<Terrain> t){
		this(Math.round(el),t);
	}

	void generatecommander(int el){
		for(float cr=ChallengeCalculator.eltocr(el);commander.isEmpty();cr--){
			List<Monster> tier=Monster.BYCR.get(cr);
			if(tier!=null) commander.add(new Combatant(RPG.pick(tier),true));
		}
	}

	void generateelites(int el){
		for(int target=el;elites==null;){
			elites=EncounterGenerator.generate(target,terrains);
			if(elites.size()==1) elites=null;
		}
	}

	void generatefootsoldiers(int elp){
		int el=elp+(RPG.chancein(2)?-2:-3);
		if(el<-1) el=RPG.chancein(2)?-1:0;
		Combatants footsoldiers=null;
		while(footsoldiers==null||footsoldiers.size()>9)
			footsoldiers=EncounterGenerator.generate(el,terrains);
		this.footsoldiers.addAll(footsoldiers);
		while(ChallengeCalculator.calculateel(this.footsoldiers)<elp){
			Combatant c=RPG.pick(footsoldiers);
			this.footsoldiers.add(new Combatant(c.source,true));
		}
	}

	/**
	 * @return List with {@link #commander}, {@link #elites} and
	 *         {@link #footsoldiers}.
	 */
	public List<Combatants> getchoices(){
		return List.of(commander,elites,footsoldiers);
	}
}