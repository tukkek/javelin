package javelin.model.feat.attack;

import javelin.controller.action.ai.AbstractAttack;
import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 * 
 * @see AbstractAttack#cleave()
 */
public class Cleave extends Feat {
	public static final Cleave SINGLETON = new Cleave();

	public Cleave() {
		super("Cleave");
	}

}
