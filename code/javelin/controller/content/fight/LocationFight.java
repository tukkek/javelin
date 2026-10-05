package javelin.controller.content.fight;

import javelin.controller.content.fight.setup.LocationFightSetup;
import javelin.controller.content.map.location.LocationMap;
import javelin.model.world.location.Location;

public class LocationFight extends Siege{
	public LocationFight(Location l,LocationMap map){
		super(l);
		this.map=map;
		setup=new LocationFightSetup(map);
		bribe=false;
		hide=false;
	}
}
