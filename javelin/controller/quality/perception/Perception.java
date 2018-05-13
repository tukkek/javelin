package javelin.controller.quality.perception;

import javelin.controller.quality.Quality;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

/**
 * Since {@link Perception} allows you to "see" far away enemies makes sense
 * that this would function like 20 ranks of listen.
 *
 * @author alex
 */
public class Perception extends Quality {
	/** Constructor. */
	public Perception() {
		super("Tremorsense");
	}

	@Override
	public void add(String declaration, Monster m) {
		Skill.PERCEPTION.raise(10, m);
	}

	@Override
	public boolean has(Monster m) {
		return false;
	}

	@Override
	public float rate(Monster m) {
		/* rated as a skill */
		return 0;
	}

	@Override
	public boolean apply(String text, Monster m) {
		return super.apply(text, m) || text.contains("scent");
	}
}
