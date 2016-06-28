package javelin.controller.quality.subtype;

import javelin.controller.quality.Quality;
import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.model.unit.Monster;

/**
 * @see MindImmunity
 * @see CriticalImmunity
 * @author alex
 */
public class Undead extends Quality {
	/** Constructor. */
	public Undead() {
		super("undead");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunitytomind = true;
		m.immunitytocritical = true;
		m.immunitytopoison = true;
		m.immunitytoparalysis = true;
	}

	@Override
	public boolean has(Monster m) {
		return m.type.equals("undead");
	}

	@Override
	public float rate(Monster m) {
		/* see respective factors */
		return 0;
	}

	@Override
	public boolean apply(String attack, Monster m) {
		return super.apply(attack, m) || has(m);
	}

}
