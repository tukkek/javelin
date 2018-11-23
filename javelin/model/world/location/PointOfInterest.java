package javelin.model.world.location;

import java.awt.Image;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.event.EventCard;
import javelin.controller.event.wild.WildEvent;
import javelin.controller.event.wild.WildEvents;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.view.Images;

/**
 * A mostly ad-hoc (scaled) type of Location meant to make the regions of the
 * game {@link World} more interesting to explore regardless of a
 * {@link Squad}'s current level. It also adds variety to the rest of the mostly
 * {@link Fight}-oriented encounters, with light strategic decision making and
 * subquests. For that reason, avoid designing {@link Fight}-based events.
 *
 * Events and {@link Hazard}s have a litte overlap but Events are more of a
 * one-in-a-lifetime occurence (at least stylistically) while hazards represent
 * the daily struggles of navigating a certain type of terrain.
 *
 * @see EventCard
 * @author alex
 */
public class PointOfInterest extends Actor{
	static final String DESCRIPTION="A point of interest.";

	WildEvent card=null;

	/** Constructor. */
	public PointOfInterest(){
		super();
		allowedinscenario=false;
		impermeable=true;
	}

	@Override
	public boolean interact(){
		if(card==null){
			int el=ChallengeCalculator.calculateel(Squad.active.members);
			card=(WildEvent)WildEvents.instance.generate(Squad.active,el,this);
		}
		card.happen(Squad.active,this);
		if(card.remove) remove();
		return true;
	}

	@Override
	public Boolean destroy(Incursion attacker){
		return null;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public Image getimage(){
		return Images.get("locationpointofinterest");
	}

	@Override
	public String describe(){
		return card==null?DESCRIPTION:card.name;
	}

	@Override
	public Integer getel(int attackerel){
		return null;
	}

	@Override
	public void place(){
		if(x==-1) Location.generate(this,false);
		super.place();
	}
}
