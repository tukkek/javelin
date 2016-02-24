package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A container of {@link QueueItem} for a {@link Town}.
 * 
 * @author alex
 */
public class TownQueue implements Serializable {

	/**
	 * Items being processed or done yet not reclaimed, from those that are
	 * going to finish first to those that will finish later.
	 */
	public ArrayList<QueueItem> queue = new ArrayList<QueueItem>(0);

	public boolean done() {
		return queue.isEmpty() || last().completed(Squad.active.hourselapsed);
	}

	public QueueItem last() {
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
	public List<QueueItem> reclaim(long force) {
		ArrayList<QueueItem> reclaimed = new ArrayList<QueueItem>();
		for (QueueItem i : (List<QueueItem>) queue.clone()) {
			if (i.completed(force)) {
				reclaimed.add(i);
				queue.remove(i);
			}
		}
		return reclaimed;
	}

	public QueueItem add(Serializable[] payload, int hoursofwork) {
		QueueItem i = new QueueItem(getnextslot() + hoursofwork, payload);
		queue.add(i);
		return i;
	}

	public void add(QueueItem item) {
		// item.completionat += getnextslot();
		queue.add(item);
	}

	public Long next() {
		return queue.isEmpty() ? null : queue.get(0).completionat;
	}
}
