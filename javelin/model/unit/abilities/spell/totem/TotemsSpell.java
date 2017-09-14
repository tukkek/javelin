package javelin.model.unit.abilities.spell.totem;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Touch;

/**
 * Common implementation of this type of spell.
 * 
 * @author alex
 */
public abstract class TotemsSpell extends Touch {

	public TotemsSpell(String name, Realm realmp) {
		super(name, 2, CrCalculator.ratespelllikeability(2), realmp);
		castonallies = true;
		castinbattle = true;
		ispotion = true;
	}
}