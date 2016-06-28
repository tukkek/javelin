package javelin.model.spell.conjuration.teleportation;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Brings you back to last visited town. See the d20 SRD for more info.
 */
public class WordOfRecall extends Spell {
	/** Constructor. */
	public WordOfRecall() {
		super("Word of recall", 6, SpellsFactor.ratespelllikeability(6),
				Realm.MAGICAL);
		castoutofbattle = true;
		isritual = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		return Squad.active.lasttown != null;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant) {
		if (Dungeon.active != null) {
			Dungeon.active.leave();
		}
		Squad.active.visual.remove();
		Squad.active.transport = null;
		Squad.active.x = Squad.active.lasttown.x;
		Squad.active.y = Squad.active.lasttown.y;
		Squad.active.displace();
		Squad.active.place();
		return "";
	}
}
