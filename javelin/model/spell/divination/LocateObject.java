package javelin.model.spell.divination;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Chest;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;

/**
 * Allows player to find nearest treasure chest in a {@link Dungeon}.
 */
public class LocateObject extends Spell {
	/** Constructor. */
	public LocateObject() {
		super("Locate object", 2, SpellsFactor.ratespelllikeability(2),
				Realm.MAGICAL);
		castinbattle = false;
		castoutofbattle = true;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		return Dungeon.active != null;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant) {
		Feature closest = findtreasure();
		if (closest == null) {
			return "No treasure left.";
		}
		Dungeon.active.visible[closest.x][closest.y] = true;
		return null;
	}

	/**
	 * @return Closest treasure chest.
	 */
	public static Chest findtreasure() {
		Chest closest = null;
		for (Feature f : Dungeon.active.features) {
			if (f instanceof Chest) {
				Chest t = (Chest) f;
				if (closest == null) {
					closest = t;
				} else if (Walker.distance(Dungeon.active.hero.x,
						Dungeon.active.hero.y, t.x,
						t.y) < Walker.distance(Dungeon.active.hero.x,
								Dungeon.active.hero.y, closest.x, closest.y)) {
				}

			}
		}
		return closest;
	}
}
