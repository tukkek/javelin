package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.item.Item;
import javelin.model.item.relic.Relic;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.temple.Temple;

/**
 * Holds the {@link Relic} for this temple. If for any reason the {@link Relic}
 * is lost by the player it shall be available for pickup here again.
 *
 * @author alex
 */
public class Altar extends Feature{
	Temple temple;

	/** Constructor. */
	public Altar(Point p,Temple temple){
		super(p.x,p.y,"dungeonchestrelic");
		this.temple=temple;
		remove=false;
	}

	@Override
	public boolean activate(){
		Item reward=World.scenario.openaltar(temple);
		if(Item.getplayeritems().contains(reward))
			Javelin.message("The "+reward+" is not here anymore...",true);
		else{
			String text="This chest contains the "+reward+"!";
			text+="\nIf it is lost for any reason it shall be teleported back to safety here.";
			Javelin.message(text,true);
			reward.clone().grab();
		}
		return true;
	}
}
