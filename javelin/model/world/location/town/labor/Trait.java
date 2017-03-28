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
		if (trait.equals(Deck.NAMECRIMINAL)
				&& town.traits.contains(Deck.NAMERELIGIOUS)) {
			return false;
		}
		if (trait.equals(Deck.NAMERELIGIOUS)
				&& town.traits.contains(Deck.NAMECRIMINAL)) {
			return false;
		}
		return !town.traits.contains(trait);
	}

	@Override
	public boolean equals(Object obj) {
		Labor t = (Labor) obj;
		return name.equals(t.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
