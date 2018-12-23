package javelin.controller.event;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;

import javelin.controller.ContentSummary;
import javelin.controller.InfiniteList;
import javelin.controller.event.urban.UrbanEvents;
import javelin.controller.event.wild.WildEvents;
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
	/**
	 * An infinitely-drawing deck of {@link EventCard}s.
	 *
	 * @author alex
	 */
	public static class EventDeck
			extends InfiniteList<Class<? extends EventCard>>{
		EventDeck(){
			super(null,true);
		}
	}

	/** Events that benefit the {@link Squad}. */
	protected EventDeck positive=new EventDeck();
	/**
	 * Events that aren't clearly {@link positive} or
	 * {@link EventDealer#negative}.
	 */
	protected EventDeck neutral=new EventDeck();
	/** Events that detriment the {@link Squad}. */
	protected EventDeck negative=new EventDeck();

	/**
	 * @param s Usually {@link Squad#active}.
	 * @param el Encounter level of the given Squad.
	 * @param l The {@link PointOfInterest} Location.
	 * @return A valid, defined event ready to be used with equal chances of being
	 *         positive, neutral or negative.
	 */
	public EventCard generate(Squad s,int el){
		var deck=choosedeck();
		var card=draw(s,el,deck);
		card.define(s,el);
		return card;
	}

	/**
	 * @return By default, equal chances of {@link #positive}, {@link #neutral}
	 *         and {@link #negative}.
	 */
	protected EventDeck choosedeck(){
		return RPG.pick(List.of(positive,neutral,negative));
	}

	/** @see ContentSummary */
	public String printsummary(String title){
		var positive=this.positive.getcontentsize();
		var neutral=this.neutral.getcontentsize();
		var negative=this.negative.getcontentsize();
		var total=positive+neutral+negative;
		var summary=total+" "+title.toLowerCase()+" ";
		summary+="("+positive+" positive, ";
		summary+=neutral+" neutral, ";
		summary+=negative+" negative)";
		return summary;
	}

	/**
	 * Draws until a valid card is found. Returns any invalid cards to the deck
	 * or, whenever spent, reshuffles the entire deck.
	 */
	EventCard draw(Squad s,int el,EventDeck deck){
		try{
			var drawn=new HashSet<Class<? extends EventCard>>();
			EventCard card=null;
			while(card==null){
				if(deck.isempty()) drawn.clear();
				Class<? extends EventCard> type=deck.pop();
				card=newinstance(type);
				if(!card.validate(s,el)){
					card=null;
					drawn.add(type);
					continue;
				}
			}
			deck.addcontent(drawn);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Given a type of card, instantiate it. Subclasses may have different
	 * {@link Constructor}s so this allows the execution of the proper reflection
	 * operations.
	 *
	 * This is only responsible for returning a
	 * {@link Constructor#newInstance(Object...)}. Other operations, such as
	 * validation are performed elsewhere.
	 *
	 * @see WildEvents#generating
	 * @see UrbanEvents#generating
	 */
	protected EventCard newinstance(Class<? extends EventCard> type)
			throws ReflectiveOperationException{
		return type.getDeclaredConstructor().newInstance();
	}
}
