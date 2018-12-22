package javelin.model.diplomacy.mandate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.diplomacy.mandate.influence.ImproveRelationship;
import javelin.model.diplomacy.mandate.influence.Insult;
import javelin.model.diplomacy.mandate.meta.RaiseHandSize;
import javelin.model.diplomacy.mandate.meta.Redraw;
import javelin.model.diplomacy.mandate.unit.RequestMercenaries;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.old.RPG;

/**
 * A {@link Diplomacy} action.
 *
 * @author alex
 */
public abstract class Mandate implements Serializable,Comparable<Mandate>{
	/** All types of mandates. */
	public static final List<Class<? extends Mandate>> MANDATES=new ArrayList<>(
			List.of(RaiseHandSize.class,Redraw.class,RequestGold.class,
					RequestMercenaries.class,RevealAlignment.class,
					ImproveRelationship.class,Insult.class,RequestTrade.class));

	/**
	 * Used for equality as well.
	 *
	 * @see #getname()
	 */
	public String name;
	/** May be ignored by cards that have no target. */
	protected Relationship target;

	/** Reflection constructor. */
	public Mandate(Relationship r){
		target=r;
	}

	/**
	 * @return Text to be shown to player describing this action and (possible)
	 *         target(s).
	 */
	public abstract String getname();

	/**
	 * @param diplomacy
	 * @return If <code>false</code>, will impede this from being drawn into the
	 *         Mandate hand. If already on hand, will remove it.
	 * @see #name
	 */
	public abstract boolean validate(Diplomacy d);

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o){
		return o instanceof Mandate&&name.equals(((Mandate)o).name);
	}

	@Override
	public int compareTo(Mandate o){
		return name.compareTo(o.name);
	}

	/** What to do once this card is played. */
	public abstract void act(Diplomacy d);

	/** Called once after a card is instantiated and validated. */
	public void define(){
		name=getname()+".";
	}

	/**
	 * @return A squad in the {@link #target}'s {@link District} or
	 *         <code>null</code> if none present.
	 */
	protected Squad getsquad(){
		var squads=target.town.getdistrict().getsquads();
		return squads.isEmpty()?null:RPG.pick(squads);
	}
}
