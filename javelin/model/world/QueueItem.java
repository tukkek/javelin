package javelin.model.world;

import java.io.Serializable;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;

/**
 * Represents a training {@link Combatant} or a forging {@link Item}.
 * 
 * @author alex
 */
public class QueueItem implements Serializable {
	public long completionat;
	public Serializable[] payload;

	public QueueItem(long completionat, Serializable[] payload) {
		this.completionat = completionat;
		this.payload = payload;
	}

	public boolean completed(long time) {
		return completionat <= time;
	}
}
