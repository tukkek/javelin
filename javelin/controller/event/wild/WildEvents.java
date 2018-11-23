package javelin.controller.event.wild;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.event.EventCard;
import javelin.controller.event.EventDealer;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;

/**
 * Manages {@link WildEvent}s.
 *
 * @author alex
 */
public class WildEvents extends EventDealer{
	/** @see StateManager */
	public static WildEvents instance=new WildEvents();

	static final Class<? extends WildEvent> DEBUG=null;

	@Override
	public EventCard generate(Squad s,int el,PointOfInterest l){
		if(Javelin.DEBUG&&WildEvents.DEBUG!=null) try{
			var card=DEBUG.getDeclaredConstructor().newInstance();
			if(!card.validate(s,el,l))
				throw new UnsupportedOperationException("Invalid #wildevent card ");
			card.define(s,el,l);
			return card;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
		return super.generate(s,el,l);
	}
}
