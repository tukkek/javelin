package javelin.model.world.location.town.labor.ecological;

import javelin.controller.content.kit.Barbarian;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.academy.Guild;
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
