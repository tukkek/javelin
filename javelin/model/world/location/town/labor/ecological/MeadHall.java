package javelin.model.world.location.town.labor.ecological;

import javelin.controller.kit.Barbarian;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * {@link Barbarian} {@link Academy}.
 *
 * @author alex
 */
public class MeadHall extends Guild{
	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildMeadHall extends BuildAcademy{
		/** Constructor. */
		public BuildMeadHall(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return new MeadHall();
		}
	}

	/** Constructor. */
	public MeadHall(){
		super("Mead hall",Barbarian.INSTANCE);
	}
}
