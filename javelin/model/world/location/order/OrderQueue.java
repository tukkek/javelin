package javelin.model.world.location.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;

/**
 * A container of {@link Order} for a {@link Town}.
 * 
 * @author alex
 */
public class OrderQueue implements Serializable {

	/**
	 * Items being processed or done yet not reclaimed, from those that are
	 * going to finish first to those that will finish later.
	 */
	public ArrayList<Order> queue = new ArrayList<Order>(0);

	public boolean done() {
		return queue.isEmpty() || last().completed(Squad.active.hourselapsed);
	}

	public Order last() {
		return queue.get(queue.size() - 1);
	}

	public long getnextslot() {
		return queue.isEmpty() ? Squad.active.hourselapsed
				: last().completionat;
	}

	/**
	 * @param force
	 * @return Item which is in queue and should be removed manually upon
	 *         confirmation.
	 */
	public List<Order> reclaim(long force) {
		ArrayList<Order> reclaimed = new ArrayList<Order>();
		for (Order i : (List<Order>) queue.clone()) {
			if (i.completed(force)) {
				reclaimed.add(i);
				queue.remove(i);
			}
		}
		return reclaimed;
	}

	public void add(Order item) {
		queue.add(item);
	}

	public Long next() {
		return queue.isEmpty() ? null : queue.get(0).completionat;
	}

	public void clear() {
		queue.clear();
	}

	/**
	 * @return <code>true</code> if there is an {@link Order} waiting to be
	 *         reclaimed.
	 * @see #reclaim(long)
	 */
	public boolean ready() {
		return !queue.isEmpty()
				&& queue.get(0).completed(Squad.active.hourselapsed);
	}
}
