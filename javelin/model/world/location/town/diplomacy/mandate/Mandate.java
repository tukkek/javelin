package javelin.model.world.location.town.diplomacy.mandate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javelin.Javelin;
import javelin.controller.content.ContentSummary;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.mandate.ally.RequestChaoticAlly;
import javelin.model.world.location.town.diplomacy.mandate.ally.RequestEvilAlly;
import javelin.model.world.location.town.diplomacy.mandate.ally.RequestGoodAlly;
import javelin.model.world.location.town.diplomacy.mandate.ally.RequestLawfulAlly;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A {@link Diplomacy} action.
 *
 * @author alex
 */
public abstract class Mandate implements Serializable,Comparable<Mandate>{
	/**
	 * If {@link Javelin#DEBUG} and not-<code>null</code>, will prioritize this
	 * card type over others.
	 */
	static final Class<? extends Mandate> DEBUG=null;
	static final Map<String,List<Class<? extends Mandate>>> MANDATES=new HashMap<>();
	static final String DEFAULT="default";

	static{
		MANDATES.put(DEFAULT,List.of(RequestRuby.class));
		MANDATES.put(Trait.CRIMINAL,List.of(RequestEvilAlly.class));
		MANDATES.put(Trait.EXPANSIVE,
				List.of(RequestLocation.class,RequestTrade.class));
		MANDATES.put(Trait.MAGICAL,List.of(RequestItem.class));
		MANDATES.put(Trait.MERCANTILE,List.of(RequestGold.class));
		MANDATES.put(Trait.MILITARY,List.of(RequestLawfulAlly.class));
		MANDATES.put(Trait.NATURAL,List.of(RequestChaoticAlly.class));
		MANDATES.put(Trait.RELIGIOUS,List.of(RequestGoodAlly.class));
	}

	/**
	 * Used for equality as well.
	 *
	 * @see #getname()
	 */
	public String name;
	/** Town the mandate is being asked from. */
	protected Town town;

	/** Reflection constructor. */
	public Mandate(Town t){
		town=t;
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
	public abstract boolean validate();

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
	public abstract void act();

	/** Called once after a card is instantiated and validated. */
	public void define(){
		name=getname();
	}

	/**
	 * @return <code>true</code> if added a valid, non-repeated mandate card to
	 *         {@link Diplomacy#treaties}.
	 */
	public static Mandate generate(Diplomacy d){
		try{
			var deck=new ArrayList<>(MANDATES.get(DEFAULT));
			for(var t:d.town.traits)
				deck.addAll(MANDATES.get(t));
			if(Javelin.DEBUG&&DEBUG!=null){
				deck.clear();
				deck.add(DEBUG);
			}
			for(var type:RPG.shuffle(deck)){
				var card=type.getConstructor(Town.class).newInstance(d.town);
				card.define();
				if(card.validate()&&d.treaties.add(card)) return card;
			}
			return null;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/** @see ContentSummary */
	public static String printsummary(){
		var actions=0;
		for(var v:MANDATES.values())
			actions+=v.size();
		return actions+" diplomatic actions";
	}

	@Override
	public String toString(){
		return name;
	}
}
