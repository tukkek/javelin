package javelin.controller.event.wild;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.event.EventDealer.EventDeck;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * An event that always validates as true and does nothing at all. Necessary
 * because there is a very small chance that no other events at all will
 * validate in some {@link EventDeck}.
 *
 * TODO remove once all decks in {@link WildEvents} have at least one
 * always-validating event at least.
 *
 * Despite being pointless, this still has some vaue as a neutral event - the
 * fact you might decide go out of your way for a chance of absolutely nothing
 * happening is, in itself a (admitedly small) strategic decision.
 *
 * @author alex
 */
public class FindNothing extends WildEvent{
	static final List<String> MESSAGES=List.of(
			"You thought you saw something interesting over here but you were wrong...",
			"A situation here seemed to be developing but quickly defused. Everything's back to normal.",
			"You unwittingly stump your toe against a large rock on the way - you have never been in so much pain!",
			"A bee comes flying by menacingly, intent on checking out some of your food!",
			"An annoying song gets stuck in your head, you try singing it out loud to see if it'll go away.",
			"For a moment you think you might have forgotten to lock your door, way back home... did you?",
			"You stop for a moment to recall a childhood story about one of your friends...",
			"You have a sudden feeling of deja-vu!");

	/** Reflection-friendly constructor. */
	public FindNothing(PointOfInterest l){
		super("Nothing",l);
	}

	@Override
	public void happen(Squad s){
		ArrayList<String> messages=new ArrayList<>(MESSAGES);
		var period=Period.now();
		messages.add("You suddenly notice how this is such a beatiful "
				+period.toString().toLowerCase()+".");
		if(Weather.current!=Weather.CLEAR)
			messages.add("It sure is raining a lot, isn't it..?");
		if(s.members.size()>=3) messages.add(RPG.pick(s.members)
				+" falls down and everyone else has a good laugh about it!");
		var terrain=Terrain.get(location.x,location.y).toString().toLowerCase();
		messages.add("For some reason, looking at the "+terrain
				+" around you fills you with determination!");
		Javelin.message(RPG.pick(messages),true);
	}
}
