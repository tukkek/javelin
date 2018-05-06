package javelin.model.unit;

import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.quality.resistance.PoisonImmunity;

public class Building extends Combatant {
	public static final float CRADJUSTMENT = CriticalImmunity.CR
			+ MindImmunity.CR + ParalysisImmunity.CR + PoisonImmunity.CR;

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