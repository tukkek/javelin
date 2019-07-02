package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.old.RPG;
import javelin.view.mappanel.dungeon.DungeonTile;
import javelin.view.screen.BattleScreen;

/**
 * @see GoodTemple
 * @author alex
 */
public class Spirit extends Feature{

	/** Constructor. */
	public Spirit(){
		super("dungeonspirit");
	}

	@Override
	public boolean activate(){
		Feature show=Dungeon.active.features.getundiscovered();
		if(show==null){
			Javelin.message("The spirit flees from your presence in shame...",false);
			return true;
		}
		reveal(RPG.chancein(2)?"'Hey, look!'":"'Hey, listen!'",show);
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
