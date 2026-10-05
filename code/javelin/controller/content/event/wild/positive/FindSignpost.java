package javelin.controller.content.event.wild.positive;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.content.event.wild.WildEvent;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Reveals a {@link Town} location.
 *
 * @author alex
 */
public class FindSignpost extends WildEvent{
	Town town=null;

	/** Reflection-friendly constructor. */
	public FindSignpost(PointOfInterest l){
		super("Find signpost",l);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(Weather.current==Weather.STORM&&s.roll(Skill.PERCEPTION)<15)
			return false;
		var towns=Town.gettowns();
		if(towns.isEmpty()) return false;
		towns.sort((a,b)->Double.compare(a.distance(location.x,location.y),b.distance(location.x,location.y)));
		for(var i=0;i<2&&i<towns.size();i++){
			var town=towns.get(i);
			if(!WorldScreen.see(town.getlocation())) this.town=town;
		}
		return town!=null;
	}

	@Override
	public void happen(Squad s){
		WorldScreen.discover(town.x,town.y);
		Javelin.redraw();
		Javelin.message("A roadsign shows the direction towards a "
				+town.getrank().toString().toLowerCase()+".",true);
	}
}
