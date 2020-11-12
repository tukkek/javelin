package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.temple.GoodTemple;
import javelin.old.RPG;
import javelin.view.mappanel.dungeon.DungeonTile;
import javelin.view.screen.BattleScreen;

/**
 * Shows and reveals the (best guessed) closest {@link Feature} in this
 * {@link DungeonFloor} level, if any.
 *
 * @see GoodTemple
 * @see Feature#discover(javelin.model.unit.Combatant, int)
 * @author alex
 */
public class Spirit extends Feature{

	/** Constructor. */
	public Spirit(DungeonFloor f){
		super("spirit");
	}

	@Override
	public boolean activate(){
		var undiscovered=Dungeon.active.features.getallundiscovered();
		if(undiscovered.isEmpty()){
			Javelin.message("The spirit flees from your presence in shame...",false);
			return true;
		}
		var closest=undiscovered.stream()
				.min((a,b)->a.getlocation().distanceinsteps(b.getlocation()))
				.orElse(null);
		reveal(RPG.chancein(2)?"'Hey, look!'":"'Hey, listen!'",closest);
		return true;
	}

	/**
	 * Fully reveals a {@link Feature} and {@link DungeonTile}, along with an
	 * appropriate messsage.
	 */
	static public void reveal(String message,Feature f){
		Dungeon.active.discover(f);
		BattleScreen.active.center(f.x,f.y);
		Javelin.redraw();
		Javelin.message(message,false);
		Point p=JavelinApp.context.getsquadlocation();
		JavelinApp.context.view(p.x,p.y);
	}
}
