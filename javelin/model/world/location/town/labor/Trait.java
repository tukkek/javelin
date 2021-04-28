package javelin.model.world.location.town.labor;

import java.util.List;
import java.util.TreeSet;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * A trait represents one of the "personality" qualities of a town. for example,
 * a Town can be mercantile (interested in commerce), natural
 * (ecologically-oriented), both or neither.
 *
 * Each Trait comes with its own {@link LaborDeck} of {@link Labor} projects.
 * This is the basis for the deck-building town-management game loop.
 *
 * @author alex
 */
public class Trait extends Labor{
	/** Trait name. */
	public static final String CRIMINAL="criminal";
	/** Trait name. */
	public static final String EXPANSIVE="expansive";
	/** Trait name. */
	public static final String MAGICAL="magical";
	/** Trait name. */
	public static final String MERCANTILE="mercantile";
	/** Trait name. */
	public static final String MILITARY="military";
	/** Trait name. */
	public static final String NATURAL="natural";
	/** Trait name. */
	public static final String RELIGIOUS="religious";
	/** Ordered set of all {@link Trait} names. */
	public static final TreeSet<String> ALL=new TreeSet<>(List.of(CRIMINAL,
			EXPANSIVE,MAGICAL,MERCANTILE,MILITARY,RELIGIOUS,NATURAL));

	String traitname;

	/**
	 * @param name Trait name.
	 * @param deck {@link Labor}s this trait unlocks.
	 */
	public Trait(String name,LaborDeck deck){
		super("Trait: "+name.toLowerCase(),deck.size(),Rank.HAMLET);
		traitname=name;
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
		return super.validate(d)&&!town.traits.contains(traitname)
				&&town.traits.size()<town.getrank().rank;
	}

	/** @param t Add this trait to {@link Town#traits}. */
	public void addto(Town t){
		t.traits.add(traitname);
	}
}
