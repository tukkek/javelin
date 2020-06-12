package javelin.controller.event.urban.diplomatic;

import java.util.List;

import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.scenario.Scenario;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * Makes sure that {@link Diplomacy} is enabled for this {@link Scenario} during
 * {@link #validate(Squad, int)}.
 *
 * @author alex
 */
public abstract class DiplomaticEvent extends UrbanEvent{
	/** Full constructor. */
	public DiplomaticEvent(Town t,List<String> traits,Rank minimum){
		super(t,traits,minimum);
	}
}
