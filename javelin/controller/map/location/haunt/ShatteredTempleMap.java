package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

public class ShatteredTempleMap extends LocationMap{
	public ShatteredTempleMap(){
		super("Shattered temple");
		floor=Images.get("dungeonfloortempleevil");
		wall=Images.get("terrainrockwall2");
		obstacle=Images.get("terrainbush");
	}

	@Override
	protected Square processtile(int x,int y,char c){
		Square s=super.processtile(x,y,c);
		if(!s.blocked&&RPG.r(1,6)==1) s.obstructed=true;
		return s;
	}
}
