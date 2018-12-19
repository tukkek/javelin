package javelin.model.world.location.town.labor.ecological;

import javelin.controller.kit.Barbarian;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
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

	@Override
	protected void generate(){
		while(x==-1||!(Terrain.get(x,y).equals(Terrain.PLAIN)
				||Terrain.get(x,y).equals(Terrain.HILL)))
			super.generate();
	}
}
