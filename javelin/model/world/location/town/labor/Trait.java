package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;

public class Trait extends Labor {
	String trait;

	public Trait(String trait, Deck deck) {
		super("Trait: " + trait.toLowerCase());
		cost = deck.size();
		this.trait = trait;
	}

	@Override
	protected void define() {
		// nothing to update
	}

	@Override
	public void done() {
		town.traits.add(trait);
	}

	@Override
	public boolean validate(District d) {
		if (trait.equals(Deck.CRIMINAL) && town.traits.contains(Deck.RELIGIOUS)) {
			return false;
		}
		if (trait.equals(Deck.RELIGIOUS) && town.traits.contains(Deck.CRIMINAL)) {
			return false;
		}
		return !town.traits.contains(trait);
	}
}
