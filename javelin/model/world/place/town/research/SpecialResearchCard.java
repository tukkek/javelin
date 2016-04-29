package javelin.model.world.place.town.research;

import javelin.model.world.place.town.Town;

/**
 * These are supposed to be a stack of research cards each with a unique effect.
 * 
 * They have #aiable as <code>false</code> by default. And sometimes make use of
 * #immediate as well.
 * 
 * @author alex
 */
public abstract class SpecialResearchCard extends Research {

	public SpecialResearchCard(String name, double price) {
		super(name, price);
		aiable = false;
	}

	@Override
	protected boolean isrepeated(Town t) {
		return false;
	}
}
