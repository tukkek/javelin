package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

public class GraveyardMap extends LocationMap{
	public GraveyardMap(){
		super("graveyard");
		wall=Images.get("terraintombstone");
		obstacle=Images.get("terrainbush");
		floor=Images.get("dungeonfloortempleevil");
	}

	@Override
	protected Square processtile(int x,int y,char c){
		Square s=super.processtile(x,y,c);
		if(!s.blocked&&RPG.chancein(20)) s.obstructed=true;
		return s;
	}
}
