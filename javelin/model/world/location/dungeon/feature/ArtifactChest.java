package javelin.model.world.location.dungeon.feature;

import java.awt.Image;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.view.Images;

/**
 * Holds the {@link Artifact} for this temple. If for any reason the
 * {@link Artifact} is lost by the player it shall be available for pickup here
 * again.
 *
 * @author alex
 */
public class ArtifactChest extends Feature{
	Temple temple;

	/** Constructor. */
	public ArtifactChest(Temple temple){
		super("artifact chest");
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

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","artifact"));
	}
}
