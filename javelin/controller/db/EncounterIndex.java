package javelin.controller.db;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.collection.CountingSet;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;

/**
 * Mapping of {@link Encounter}s by Encounter Level.
 *
 * @author alex
 */
public class EncounterIndex extends TreeMap<Integer,List<Encounter>>{
	/** Constructor. */
	public EncounterIndex(){
		super();
	}

	/**
	 * Generates an index from a Monster pool, including NPCs and mixed
	 * encounters.
	 */
	public EncounterIndex(List<Monster> pool){
		for(var m:pool){
			for(var i=1;i<=9;i++){
				var group=new Combatants(i);
				for(var j=0;j<i;j++)
					group.add(new Combatant(m,true));
				put(new Encounter(group));
			}
			var from=Math.round(m.cr)+1;
			var to=Math.max(20,from+10);
			IntStream.rangeClosed(from,to)
					.mapToObj(level->NpcGenerator.generate(m,level))
					.filter(npc->npc!=null).forEach(npc->put(new Encounter(npc)));
		}
		var all=values().stream().flatMap(e->e.stream())
				.collect(Collectors.toList());
		for(var i=0;i<all.size();i++)
			for(var j=i+1;j<all.size();j++){
				var a=all.get(i).group;
				var b=all.get(j).group;
				var size=a.size()+b.size();
				if(size>9) continue;
				var mixed=new Combatants(size);
				mixed.addAll(a);
				mixed.addAll(b);
				try{
					ChallengeCalculator.calculateelsafe(mixed);
					if(new CountingSet(mixed).getcount().size()>1)
						put(new Encounter(mixed));
				}catch(UnbalancedTeams e){
					continue;
				}
			}
	}

	/** @param e Register this with the given Encounter Level. */
	public void put(int el,Encounter e){
		var tier=get(el);
		if(tier==null){
			tier=new ArrayList<>();
			put(el,tier);
		}
		tier.add(e);
	}

	/** @return Total number of {@link Encounter}s. */
	public int count(){
		int count=0;
		for(var encounters:values())
			count+=encounters.size();
		return count;
	}

	/** Calls {@link #put(int, Encounter)} with {@link Encounter#el}. */
	public void put(Encounter e){
		put(e.el,e);
	}
}
