package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.relic.Relic;
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
	public Altar(Temple temple){
		super("dungeonchestrelic","altar");
		this.temple=temple;
		remove=false;
	}

	@Override
	public boolean activate(){
		var r=temple.relic;
		if(Item.getplayeritems().contains(r))
			Javelin.message("The "+r+" is not here anymore...",true);
		else{
			String text="This chest contains the "+r+"!";
			text+="\nIf it is lost for any reason it shall be teleported back to safety here.";
			Javelin.message(text,true);
			r.clone().grab();
		}
		return true;
	}

	@Override
	public String toString(){
		return temple.relic.name;
	}
}
