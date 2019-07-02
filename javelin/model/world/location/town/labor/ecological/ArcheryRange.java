package javelin.model.world.location.town.labor.ecological;

import javelin.controller.kit.Ranger;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * {@link Ranger} {@link Academy}.
 *
 * @author alex
 */
public class ArcheryRange extends Guild{
	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildArcheryRange extends BuildAcademy{
		/** Constructor. */
		public BuildArcheryRange(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return new ArcheryRange();
		}
	}

	/** Constructor. */
	public ArcheryRange(){
		super("Archery range",Ranger.INSTANCE);
	}

	@Override
	protected void generate(){
		while(x==-1||!Terrain.get(x,y).equals(Terrain.FOREST))
			super.generate();
	}
}
