package javelin.controller.content.wish;

import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;

public class RevealFloor extends Wish{

	public RevealFloor(Character keyp,WishScreen screen){
		super("reveal dungeon map",keyp,1,false,screen);
	}

	@Override
	boolean wish(Combatant target){
		reveal(Dungeon.active);
		return true;
	}

	@Override
	String validate(){
		return Dungeon.active==null?"Can only be used inside dungeons...":null;
	}

	/**
	 * TODO move to {@link DungeonFloor}
	 */
	public static void reveal(DungeonFloor d){
		for(int x=0;x<d.visible.length;x++)
			for(int y=0;y<d.visible[x].length;y++)
				d.setvisible(x,y);
		for(Feature f:d.features)
			f.draw=true;
	}
}
