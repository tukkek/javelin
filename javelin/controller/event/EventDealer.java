package javelin.controller.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.controller.InfiniteList;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * Draws {@link EventCard}s. Some care is taken so that the available decks
 * ({@link EventDealer#positive}, {@link EventDealer#neutral} and
 * {@link EventDealer#negative}) will remember which cards are drawn or not.
 * This is to ensure that, as much as possible, events don't repeat. If decks
 * ever run out of valid cards to generate, used cards will be entirely
 * reshuffled.
 *
 * @author alex
 */
public abstract class EventDealer implements Serializable{
	class EventDeck extends InfiniteList<Class<? extends EventCard>>{
		EventDeck(Collection<Class<? extends EventCard>> cards){
			super(cards,true);
		}
	}

	/** Events that benefit the {@link Squad}. */
	EventDeck positive=new EventDeck(List.of());
	/**
	 * Events that aren't clearly {@link positive} or
	 * {@link EventDealer#negative}.
	 */
	EventDeck neutral=new EventDeck(List.of());
	/** Events that detriment the {@link Squad}. */
	EventDeck negative=new EventDeck(List.of());

	/**
	 * @param s Usually {@link Squad#active}.
	 * @param el Encounter level of the given Squad.
	 * @param l The {@link PointOfInterest} Location.
	 * @return A valid, defined event ready to be used with equal chances of being
	 *         positive, neutral or negative.
	 */
	public EventCard generate(Squad s,int el,PointOfInterest l){
		EventCard card=draw(s,el,l,RPG.pick(List.of(positive,neutral,negative)));
		card.define(s,el,l);
		return card;
	}

	/**
	 * Draws until a valid card is found. Returns any invalid cards to the deck
	 * or, whenever spent, reshuffles the entire deck.
	 */
	static EventCard draw(Squad s,int el,PointOfInterest l,EventDeck deck){
		try{
			var drawn=new ArrayList<Class<? extends EventCard>>();
			EventCard card=null;
			while(card==null){
				Class<? extends EventCard> type=deck.pop();
				if(deck.isempty())
					drawn.clear();
				else
					drawn.add(type);
				card=type.getDeclaredConstructor().newInstance();
				if(!card.validate(s,el,l)){
					card=null;
					continue;
				}
			}
			deck.addcontent(drawn);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
