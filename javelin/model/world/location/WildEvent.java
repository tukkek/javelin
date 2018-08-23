package javelin.model.world.location;

import java.awt.Image;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.event.wild.WildEventCard;
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
 * {@link Fight}-oriented encounters with lighter strategic decision making and
 * subquests. For that same reason, avoid designing {@link Fight}-based events.
 *
 * Events and {@link Hazard}s have a litte overlap but Events are more of a
 * one-in-a-lifetime occurence (at least stylistically) while hazards represent
 * the daily struggles of naviagting a certain type of terrain.
 *
 * TODO should probably be an {@link Actor}, not a location.
 *
 * @see WildEventCard
 * @author alex
 */
public class WildEvent extends Actor{
	static final String DESCRIPTION="A point of interest.";
	WildEventCard card=null;

	/** Constructor. */
	public WildEvent(){
		super();
		allowedinscenario=false;
		impermeable=true;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(card==null){
			int el=ChallengeCalculator.calculateel(Squad.active.members);
			card=WildEventCard.generate(Squad.active,el,this);
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
		return Images.get("locationwildevent");
	}

	@Override
	public String describe(){
		return card==null?DESCRIPTION:card.name;
	}

	@Override
	public Integer getel(int attackerel){
		return null;
	}
}
