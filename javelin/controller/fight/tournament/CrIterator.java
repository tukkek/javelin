package javelin.controller.fight.tournament;

import java.util.Iterator;
import java.util.List;

import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

/**
 * Picks a random enemy from each available challenge rating.
 * 
 * @author alex
 */
public class CrIterator implements Iterable<Monster>, Iterator<Monster> {
	private Iterator<List<Monster>> i;

	public CrIterator(java.util.Map<Float, List<Monster>> m) {
		i = m.values().iterator();
	}

	@Override
	public boolean hasNext() {
		return i.hasNext();
	}

	@Override
	public Monster next() {
		return RPG.pick(i.next());
	}

	@Override
	public Iterator<Monster> iterator() {
		return this;
	}

}