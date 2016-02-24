package javelin.model.feat;

import javelin.controller.action.ai.AbstractAttack;

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
