package javelin.model.world.location.town.labor.ecological;

import javelin.controller.content.kit.Ranger;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.academy.Guild;
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
}
