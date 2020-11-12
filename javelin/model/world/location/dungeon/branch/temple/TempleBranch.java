package javelin.model.world.location.dungeon.branch.temple;

import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.model.world.location.dungeon.feature.chest.SpecialChest;

/**
 * See {@link #temple}.
 *
 * @author alex
 */
public class TempleBranch extends Branch{
	/**
	 * If provided, generates {@link ArtifactChest} with
	 * {@link #generatespecialchest()}.
	 */
	public Temple temple;

	/** Constructor. */
	public TempleBranch(String floor,String wall){
		super(floor,wall);
	}

	@Override
	public SpecialChest generatespecialchest(){
		return temple==null?null:new ArtifactChest(temple.artifact);
	}
}