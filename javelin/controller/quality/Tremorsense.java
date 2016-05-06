package javelin.controller.quality;

import javelin.controller.upgrade.skill.Listen;
import javelin.model.unit.Monster;

/**
 * Since {@link Listen} allows you to "see" far away enemies makes sense that
 * this would function like 20 ranks of listen.
 * 
 * @author alex
 */
public class Tremorsense extends Quality {

	public static final int LISTENRANKS = 20;

	/** Constructor. */
	public Tremorsense() {
		super("Tremorsense");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (m.skills.listen < LISTENRANKS) {
			m.skills.listen = LISTENRANKS;
		}
	}

	@Override
	public boolean has(Monster m) {
		return m.skills.listen >= LISTENRANKS;
	}

	@Override
	public float rate(Monster m) {
		/* rated as a skill */
		return 0;
	}

}
