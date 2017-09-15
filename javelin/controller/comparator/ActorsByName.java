package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.world.Actor;

public class ActorsByName implements Comparator<Actor> {
	public static final ActorsByName INSTANCE = new ActorsByName();

	private ActorsByName() {
		// prevents instantiation
	}

	@Override
	public int compare(Actor a, Actor b) {
		return a.toString().compareTo(b.toString());
	}
}
