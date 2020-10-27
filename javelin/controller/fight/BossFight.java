package javelin.controller.fight;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * A Fight with one major enemy and a group of ever-replenishing nuisances. This
 * is only recommended for EL>=5, as it can be complicated to generate lesser
 * ELs with current contnet.
 *
 * TODO use this somehwere!
 *
 * TODO expand this to work with at least EL 1 - 20
 *
 * @author alex
 */
public class BossFight extends Fight{
	List<Monster> enemies=new ArrayList<>();

	/**
	 * @param targetel Intended Encounter Level.
	 * @param terrains Indicates which Monsters can be used.
	 */
	public BossFight(int targetel,List<Terrain> terrains){
		terrain=RPG.pick(terrains);
		int bossel=targetel-2;
		for(;enemies.isEmpty()&&bossel>=Monster.BYCR.firstKey();bossel--){
			var b=findboss(bossel,terrains);
			if(b!=null) enemies.add(b);
		}
		if(enemies.isEmpty()) throw new InvalidParameterException();
		var minionsel=bossel-2;
		//		var minionsamount=2;
		while(minionsel>=Monster.BYCR.firstKey()){
			var minions=findminions(minionsel,terrains);
			if(minions!=null){
				while(getenemiesel()<targetel)
					for(var m:minions){
						enemies.add(m);
						if(getenemiesel()>=targetel) break;
					}
				break;
			}
			minionsel-=2;
			//			minionsamount*=2;
		}
		if(enemies.size()==1) enemies.add(enemies.get(0));
	}

	int getenemiesel(){
		return ChallengeCalculator.calculateelfromcrs(
				enemies.stream().map(e->e.cr).collect(Collectors.toList()));
	}

	Monster findboss(int el,List<Terrain> terrains){
		var canditates=new ArrayList<Monster>();
		for(var m:Monster.MONSTERS){
			if(!ChallengeCalculator.eltocr(el).equals(m.cr)) continue;
			var terrainnames=m.getterrains();
			for(var t:terrains){
				if(terrainnames.contains(t.name)) canditates.add(m);
				break;
			}
		}
		return canditates.isEmpty()?null:RPG.pick(canditates);
	}

	List<Monster> findminions(int el,List<Terrain> terrains){
		for(var i=0;i<10;i++){
			var minions=EncounterGenerator.generate(el,terrains);
			if(minions!=null) for(var m:minions)
				if(!m.source.alignment.iscompatible(enemies.get(0).alignment)){
					minions=null;
					break;
				}
			if(minions!=null)
				return minions.stream().map(m->m.source).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return new ArrayList<>(enemies.stream().map(e->new Combatant(e,true))
				.collect(Collectors.toList()));
	}

	/** Console-output helper to check valid EL range. */
	public static void test(){
		for(var el=5;el<=20+Difficulty.DEADLY;el++)
			for(var t:Terrain.NONWATER)
				try{
					var f=new BossFight(el,List.of(t));
					var foes=f.getfoes(el);
					System.out.println("Success: "+t+" "+el+" ("+Javelin.group(foes)
							+") el "+ChallengeCalculator.calculateel(foes));
				}catch(InvalidParameterException e){
					System.out.println("Failure: "+t+" "+el);
				}
	}

	/**
	 * @return Uses the Boss Fight mechanic to just generate an encounter instead.
	 */
	public List<Combatant> generate(int el,List<Terrain> terrains){
		return new BossFight(el,terrains).getfoes(el);
	}
}
