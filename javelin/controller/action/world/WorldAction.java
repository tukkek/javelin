package javelin.controller.action.world;

import javelin.controller.action.ActionDescription;
import javelin.view.screen.world.WorldScreen;

public abstract class WorldAction implements ActionDescription {

	String name;
	public final int[] keys;
	public final String[] morekeys;

	public WorldAction(final String name, final int[] is,
			final String[] morekeysp) {
		this.name = name;
		keys = is;
		morekeys = morekeysp;
	}

	@Deprecated
	public WorldAction(final String name2, final int[] is) {
		this(name2, is, new String[] {});
	}

	abstract public void perform(WorldScreen screen);

	@Override
	public String[] getDescriptiveKeys() {
		return new String[] { morekeys[0] };
	}

	@Override
	public String getDescriptiveName() {
		return name;
	}

}
