package javelin.controller.quality;

import javelin.model.feat.ImprovedGrapple;
import javelin.model.unit.Monster;

/**
 * @see ImprovedGrab
 * @author alex
 */
public class ImprovedGrab extends Quality {

	/** Constructor. */
	public ImprovedGrab() {
		super("improved grab");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (!has(m)) {
			m.feats.add(ImprovedGrapple.singleton);
		}
	}

	@Override
	public boolean has(Monster m) {
		return m.feats.contains(ImprovedGrapple.singleton);
	}

	@Override
	public float rate(Monster m) {
		return 0;
	}

	@Override
	public String describe(Monster m) {
		return null;
	}
}
