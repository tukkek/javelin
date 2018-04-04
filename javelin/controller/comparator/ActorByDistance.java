package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.world.Actor;

/**
 * Sorts by distance, closest one will be first.
 * 
 * @author alex
 */
public class ActorByDistance implements Comparator<Actor> {
	final Actor reference;

	/**
	 * @param actor
	 */
	public ActorByDistance(Actor actor) {
		reference = actor;
	}

	@Override
	public int compare(Actor o1, Actor o2) {
		return o1.distanceinsteps(reference.x, reference.y)
				- o2.distanceinsteps(reference.x, reference.y);
	}
}