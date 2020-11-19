package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.template.KitTemplate;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.old.RPG;

/**
 * A {@link Branch} that also instantiates an {@link ArtifactChest} if the
 * {@link Dungeon} is a {@link Temple}. It is compatible with any type of
 * dungeon, simply behaving as any other typical branch otherwise.
 *
 * @author alex
 */
public class TempleBranch extends Branch{
	/**
	 * If provided, generates {@link ArtifactChest} with
	 * {@link #generatespecialchest(DungeonFloor)}.
	 */
	Realm realm;

	/** Constructor. */
	public TempleBranch(Realm r,String floor,String wall){
		super(floor,wall);
		realm=r;
		templates.add(new KitTemplate());
	}

	@Override
	public Feature generatespecialchest(DungeonFloor f){
		if(realm==null||f!=f.dungeon.floors.getLast()) return null;
		return new ArtifactChest(RPG.pick(realm.artifacts));
	}
}