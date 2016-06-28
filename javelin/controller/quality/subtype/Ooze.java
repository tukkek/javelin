package javelin.controller.quality.subtype;

import javelin.controller.quality.Quality;
import javelin.model.unit.Monster;

/**
 * TODO immune to mind-influencing effects, poison, sleep, paralysis, stunning,
 * and polymorphing. Not subject to critical hits.
 * 
 * @author alex
 */
public class Ooze extends Quality {

	public Ooze() {
		super("ooze");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunitytocritical = true;
		m.immunitytomind = true;
		m.immunitytopoison = true;
		m.immunitytoparalysis = true;
	}

	@Override
	public boolean has(Monster m) {
		return false;
	}

	@Override
	public float rate(Monster m) {
		return 0;
	}
}
