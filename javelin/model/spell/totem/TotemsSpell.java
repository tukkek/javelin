package javelin.model.spell.totem;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.spell.Touch;

/**
 * Common implementation of this type of spell.
 * 
 * @author alex
 */
public abstract class TotemsSpell extends Touch {

	public TotemsSpell(String name, Realm realmp) {
		super(name, 2, SpellsFactor.ratespelllikeability(2), realmp);
		castonallies = true;
		castinbattle = true;
		ispotion = true;
	}
}