package javelin.controller.db;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.generator.encounter.Encounter;

/**
 * Mapping of {@link Encounter}s by encounter level.
 *
 * @author alex
 */
public class EncounterIndex extends TreeMap<Integer,List<Encounter>>{

	/**
	 * @param e Register this with the given Encounter Level.
	 */
	public void put(int el,Encounter e){
		List<Encounter> tier=get(el);
		if(tier==null){
			tier=new ArrayList<>();
			put(el,tier);
		}
		tier.add(e);
	}

	public int count(){
		int count=0;
		for(List<Encounter> encounters:values())
			count+=encounters.size();
		return count;
	}

	/** @param e Uses {@link Encounter#el}. */
	public void put(Encounter e){
		put(e.el,e);
	}
}
