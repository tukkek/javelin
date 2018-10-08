package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;

/**
 * A group of monsters to be fought against.
 *
 * @author alex
 */
public class Encounter{
	/** Units encountered. */
	public final List<Combatant> group;
	public final int el;

	/** Constructor. */
	public Encounter(List<Combatant> groupp){
		group=groupp;
		el=ChallengeCalculator.calculateel(group);
	}

	/**
	 * @return Copy of {@link #group}.
	 */
	public ArrayList<Combatant> generate(){
		final ArrayList<Combatant> encounter=new ArrayList<>(group.size());
		for(final Combatant m:group)
			encounter.add(new Combatant(m.source,true));
		return encounter;
	}

	@Override
	public String toString(){
		final HashMap<String,Integer> count=new HashMap<>();
		for(final Combatant m:group){
			final Integer n=count.get(m.source.toString());
			count.put(m.source.toString(),n==null?1:n+1);
		}
		return count.toString();
	}

	/**
	 * @return {@link #group} size.
	 */
	public int size(){
		return group.size();
	}
}