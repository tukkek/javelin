package javelin.model.unit;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

public class Building extends Combatant {

	public Building(Monster sourcep, boolean generatespells) {
		super(sourcep, generatespells);
		source.passive = true;
		source.immunitytocritical = true;
		source.immunitytomind = true;
		source.immunitytoparalysis = true;
		source.immunitytopoison = true;
	}

	@Override
	public String getstatus() {
		switch (getnumericstatus()) {
		case STATUSUNHARMED:
			return "pristine";
		case STATUSSCRATCHED:
			return "scathed";
		case STATUSHURT:
			return "worn";
		case STATUSWOUNDED:
			return "broken";
		case STATUSINJURED:
			return "torn";
		case STATUSDYING:
			return "demolished";
		case STATUSUNCONSCIOUS:
		case STATUSDEAD:
			return "destroyed";
		default:
			throw new RuntimeException(
					"Unknown possibility: " + getnumericstatus());
		}
	}

}