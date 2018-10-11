package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.view.Images;

public class AbandonedManorMap extends LocationMap{
	public AbandonedManorMap(){
		super("Abandoned manor");
		floor=Images.get("dungeonfloortempleevil");
		wall=Images.get("terrainmoldwall");
		obstacle=Images.get("terraintreeforest");
		flying=false;
	}
}
