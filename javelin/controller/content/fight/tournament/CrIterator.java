package javelin.controller.content.fight.tournament;

import java.util.Iterator;
import java.util.List;

import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * Picks a random enemy from each available challenge rating.
 *
 * @author alex
 */
public class CrIterator implements Iterable<Monster>,Iterator<Monster>{
	Iterator<List<Monster>> i;

	/** Constructor. */
	public CrIterator(java.util.Map<Float,List<Monster>> m){
		i=m.values().iterator();
	}

	@Override
	public boolean hasNext(){
		return i.hasNext();
	}

	@Override
	public Monster next(){
		return RPG.pick(i.next());
	}

	@Override
	public Iterator<Monster> iterator(){
		return this;
	}

}