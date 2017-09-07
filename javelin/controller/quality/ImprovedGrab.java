package javelin.controller.quality;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;

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
			m.feats.add(ImprovedGrapple.SINGLETON);
		}
	}

	@Override
	public boolean has(Monster m) {
		return m.feats.contains(ImprovedGrapple.SINGLETON);
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
