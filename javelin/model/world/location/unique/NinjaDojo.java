package javelin.model.world.location.unique;

import javelin.controller.content.kit.Ninja;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;

/**
 * An academy dedicated to learning how to Infiltrate.
 *
 * @author alex
 */
public class NinjaDojo extends Guild{
	static final String DESCRITPION="Ninja dojo";

	/**
	 * {@link Labor} project for {@link NinjaDojo}.
	 *
	 * @author alex
	 */
	public static class BuildNinjaDojo extends BuildAcademy{
		/** Constructor. */
		public BuildNinjaDojo(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return new NinjaDojo();
		}
	}

	/** Constructor. */
	public NinjaDojo(){
		super(DESCRITPION,Ninja.INSTANCE);
	}
}
