package javelin.controller.event.wild;

import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.event.EventCard;
import javelin.controller.event.EventDealer;
import javelin.controller.event.wild.negative.ConfusingFairies;
import javelin.controller.event.wild.negative.FindIncursion;
import javelin.controller.event.wild.neutral.FindMob;
import javelin.controller.event.wild.neutral.WanderingMercenary;
import javelin.controller.event.wild.neutral.WeatherChange;
import javelin.controller.event.wild.positive.FindCaravan;
import javelin.controller.event.wild.positive.FindRuby;
import javelin.controller.event.wild.positive.FindSignpost;
import javelin.controller.event.wild.positive.RevealRegion;
import javelin.controller.event.wild.positive.WanderingHalflings;
import javelin.controller.event.wild.positive.WanderingPegasus;
import javelin.controller.event.wild.positive.WanderingPriest;
import javelin.controller.event.wild.positive.WanderingTraveller;
import javelin.controller.event.wild.positive.skill.FindWounded;
import javelin.controller.event.wild.positive.skill.MysticLock;
import javelin.controller.event.wild.positive.skill.RockClimb;
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
	static final Class<? extends WildEvent> DEBUG=null;

	/** Constructor. */
	public WildEvents(){
		positive.addcontent(List.of(FindNothing.class,WanderingPriest.class,
				WanderingTraveller.class,RevealRegion.class,FindRuby.class,
				WanderingPegasus.class,FindSignpost.class,RockClimb.class,
				MysticLock.class,FindWounded.class));
		neutral.addcontent(
				List.of(FindNothing.class,WanderingMercenary.class,WeatherChange.class,
						FindCaravan.class,WanderingHalflings.class,FindMob.class));
		negative.addcontent(
				List.of(FindNothing.class,FindIncursion.class,ConfusingFairies.class));
	}

	@Override
	public EventCard generate(Squad s,int squadel,PointOfInterest l){
		if(Javelin.DEBUG&&WildEvents.DEBUG!=null) try{
			var card=DEBUG.getDeclaredConstructor().newInstance();
			if(!card.validate(s,squadel,l))
				throw new UnsupportedOperationException("Invalid #wildevent card ");
			card.define(s,squadel,l);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
		return super.generate(s,squadel,l);
	}
}
