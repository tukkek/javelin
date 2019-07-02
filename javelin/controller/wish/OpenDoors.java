package javelin.controller.wish;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;

public class OpenDoors extends Wish{
	public OpenDoors(Character keyp,WishScreen screen){
		super("open doors",keyp,getprice(),false,screen);
	}

	static int getprice(){
		return getdoors().size();
	}

	static ArrayList<Door> getdoors(){
		ArrayList<Door> doors=new ArrayList<>(0);
		for(Feature d:Dungeon.active.features)
			if(d instanceof Door&&d.draw
					&&new Point(d.x,d.y).distanceinsteps(Dungeon.active.squadlocation)==1)
				doors.add((Door)d);
		return doors;
	}

	@Override
	String validate(){
		return getdoors().isEmpty()?"No nearby doors!":null;
	}

	@Override
	boolean wish(Combatant target){
		for(Door d:getdoors())
			d.remove();
		return true;
	}
}
