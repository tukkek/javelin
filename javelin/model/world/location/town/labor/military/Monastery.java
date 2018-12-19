package javelin.model.world.location.town.labor.military;

import javelin.controller.kit.Monk;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * {@link Monk} {@link Academy}.
 *
 * @author alex
 */
public class Monastery extends Guild{
	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildMonastery extends BuildAcademy{
		/** Constructor. */
		public BuildMonastery(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return new Monastery();
		}
	}

	/** Constructor. */
	public Monastery(){
		super("Monastery",Monk.INSTANCE);
	}

	@Override
	protected void generate(){
		while(x==-1||!Terrain.get(x,y).equals(Terrain.MOUNTAINS))
			super.generate();
	}
}
