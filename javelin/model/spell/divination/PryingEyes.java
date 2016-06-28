package javelin.model.spell.divination;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Reveals {@link Dungeon} map or nearby area on the WorldScreen.
 * 
 * @author alex
 */
public class PryingEyes extends Spell {
	/** Constructor. */
	public PryingEyes() {
		super("Prying eyes", 5, SpellsFactor.ratespelllikeability(5),
				Realm.MAGICAL);
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant) {
		if (Dungeon.active == null) {
			Outpost.discover(Squad.active.x, Squad.active.y,
					Outpost.VISIONRANGE);
			return null;
		}
		for (int x = 0; x < Dungeon.active.visible.length; x++) {
			for (int y = 0; y < Dungeon.active.visible[x].length; y++) {
				Dungeon.active.visible[x][y] = true;
			}
		}
		return null;
	}
}
