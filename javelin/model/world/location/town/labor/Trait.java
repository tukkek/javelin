package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

public class Trait extends Labor{
	/** Trait name. */
	public static final String EXPANSIVE="expansive";
	/** Trait name. */
	public static final String MERCANTILE="mercantile";
	/** Trait name. */
	public static final String MILITARY="military";
	/** Trait name. */
	public static final String MAGICAL="magical";
	/** Trait name. */
	public static final String CRIMINAL="criminal";
	/** Trait name. */
	public static final String RELIGIOUS="religious";
	/** Trait name. */
	public static final String NATURAL="natural";

	String trait;

	public Trait(String trait,Deck deck){
		super("Trait: "+trait.toLowerCase(),deck.size(),Rank.HAMLET);
		this.trait=trait;
	}

	@Override
	protected void define(){
		// nothing to update
	}

	@Override
	public void done(){
		addto(town);
	}

	@Override
	public boolean validate(District d){
		return super.validate(d)&&!town.traits.contains(trait)
				&&town.traits.size()<town.getrank().rank;
	}

	public void addto(Town t){
		t.traits.add(trait);
	}
}
