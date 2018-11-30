package javelin.controller.event.wild.positive;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.controller.terrain.Hill;
import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.PointOfInterest;

/**
 * Reveals nearby area and lets you select a landing spot. Only happens in
 * {@link Plains}, {@link Hill}s and if there's no evil member in the current
 * {@link Squad}.
 *
 * @author alex
 */
public class PegasusRide extends WildEvent{
	private static final String PROMPT="A herd of Pegasus is grazing nearby. They seem pretty docile.\n"
			+"Do you want to try to ride them?\n"
			+"Press r to ride and i to ignore the herd...";

	/** Reflection-friendly constructor. */
	public PegasusRide(){
		super("Pegasus ride");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		if(s.members.size()>10) return false;
		var terrain=Terrain.get(l.x,l.y);
		if(!(terrain.equals(Terrain.HILL)||terrain.equals(Terrain.PLAIN)))
			return false;
		for(var member:s)
			if(Boolean.FALSE.equals(member.source.good)) return false;
		return true;
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		var input=Javelin.prompt(PROMPT,Set.of('r','i'));
		if(input=='i') return;
		var vision=s.seesurroundings(s.perceive(true,true,true)+4);
		Javelin.redraw();
		Javelin.message("Your group shoots up into the air!",true);
		var destinations=new ArrayList<>(World.getactors().stream()
				.filter(a->a instanceof Location&&a.distanceinsteps(l.x,l.y)<=vision)
				.collect(Collectors.toList()));
		destinations.add(0,s);
		var names=destinations.stream().map(a->"Nearby to: "+a.describe()).map(
				name->(name.contains("\n")?name.substring(0,name.indexOf('\n')):name))
				.collect(Collectors.toList());
		names.set(0,"Back where you were before");
		var landing=destinations
				.get(Javelin.choose("Where do you want to land?",names,true,true));
		if(landing==s) return;
		s.x=landing.x;
		s.y=landing.y;
		s.displace();
	}
}
