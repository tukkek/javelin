package javelin.controller.content.event.wild;

import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.content.event.EventCard;
import javelin.controller.content.event.EventDealer;
import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.event.wild.negative.ConfusingFairies;
import javelin.controller.content.event.wild.negative.FindHazard;
import javelin.controller.content.event.wild.negative.FindIncursion;
import javelin.controller.content.event.wild.negative.MercenariesLeave;
import javelin.controller.content.event.wild.neutral.FindCaravan;
import javelin.controller.content.event.wild.neutral.FindMob;
import javelin.controller.content.event.wild.neutral.WanderingHalflings;
import javelin.controller.content.event.wild.neutral.WanderingMercenary;
import javelin.controller.content.event.wild.neutral.WeatherChange;
import javelin.controller.content.event.wild.positive.FindAlly;
import javelin.controller.content.event.wild.positive.FindRuby;
import javelin.controller.content.event.wild.positive.FindSignpost;
import javelin.controller.content.event.wild.positive.RevealRegion;
import javelin.controller.content.event.wild.positive.WanderingPegasus;
import javelin.controller.content.event.wild.positive.WanderingPriest;
import javelin.controller.content.event.wild.positive.WanderingTraveller;
import javelin.controller.content.event.wild.positive.skill.FindWounded;
import javelin.controller.content.event.wild.positive.skill.MysticLock;
import javelin.controller.content.event.wild.positive.skill.RockClimb;
import javelin.controller.db.StateManager;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;

/**
 * Manages {@link WildEvent}s.
 *
 * @author alex
 */
public class WildEvents extends EventDealer{
	/** @see StateManager */
	public static EventDealer instance=new WildEvents();
	public static PointOfInterest generating;
	/** TODO use something similar to {@link Debug}'s {@link UrbanEvent}. */
	static final Class<? extends WildEvent> DEBUG=null;

	/** Constructor. */
	private WildEvents(){
		positive.addcontent(List.of(FindNothing.class,WanderingPriest.class,
				WanderingTraveller.class,RevealRegion.class,FindRuby.class,
				WanderingPegasus.class,FindSignpost.class,RockClimb.class,
				MysticLock.class,FindWounded.class,FindAlly.class));
		neutral.addcontent(
				List.of(FindNothing.class,WanderingMercenary.class,WeatherChange.class,
						FindCaravan.class,WanderingHalflings.class,FindMob.class));
		negative.addcontent(List.of(FindNothing.class,FindIncursion.class,
				ConfusingFairies.class,MercenariesLeave.class,FindHazard.class));
	}

	@Override
	public EventCard generate(Squad s,int squadel){
		/*TODO use a Debug Helper for this instead, like UrbanEvents*/
		if(Javelin.DEBUG&&WildEvents.DEBUG!=null) try{
			var card=newinstance(DEBUG);
			if(!card.validate(s,squadel))
				throw new UnsupportedOperationException("Invalid #wildevent card ");
			card.define(s,squadel);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
		return super.generate(s,squadel);
	}

	@Override
	protected EventCard newinstance(Class<? extends EventCard> type)
			throws ReflectiveOperationException{
		return type.getDeclaredConstructor(PointOfInterest.class)
				.newInstance(generating);
	}
}
