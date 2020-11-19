package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.view.Images;

/**
 * Holds the {@link Artifact} for this temple. If for any reason the
 * {@link Artifact} is lost by the player it shall be available for pickup here
 * again.
 *
 * @author alex
 */
public class ArtifactChest extends Feature{
	static final String INSTRUCTIONS="This chest contains the %s!\n"
			+"If it is lost for any reason it shall be teleported back to safety here.";
	Artifact artifact;

	/** Constructor. */
	public ArtifactChest(Artifact a){
		super("artifact chest");
		artifact=a;
		remove=false;
	}

	@Override
	public boolean activate(){
		if(Item.getplayeritems().contains(artifact))
			Javelin.message("The "+artifact+" is not here anymore...",true);
		else{
			Javelin.message(String.format(INSTRUCTIONS,artifact),true);
			artifact.clone().grab();
		}
		return true;
	}

	@Override
	public String toString(){
		return artifact.toString();
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","artifact"));
	}
}
