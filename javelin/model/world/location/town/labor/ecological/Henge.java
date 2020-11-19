package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.kit.Druid;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * {@link Druid} {@link Academy}.
 *
 * @author alex
 */
public class Henge extends Guild{
	static final String DESCRIPTIION="Henge";

	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildHenge extends BuildAcademy{
		/** Constructor. */
		public BuildHenge(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return new Henge();
		}
	}

	/** Constructor. */
	public Henge(){
		super(DESCRIPTIION,Druid.INSTANCE);
	}

	ArrayList<Monster> fill(int newlevel){
		ArrayList<Monster> animals=new ArrayList<>();
		for(Float tier:Monster.BYCR.keySet()){
			if(tier>level) break;
			for(Monster m:Monster.BYCR.get(tier))
				if(m.type==MonsterType.ANIMAL&&!contains(m)) animals.add(m);
		}
		if(animals.isEmpty()) return animals;
		Collections.shuffle(animals);
		while(animals.size()+upgrades.size()>newlevel)
			animals.remove(0);
		return animals;
	}

	boolean contains(Monster m){
		for(Upgrade u:upgrades){
			Summon s=u instanceof Summon?(Summon)u:null;
			if(s!=null&&s.monstername.equals(m.name)) return true;
		}
		return false;
	}
}
