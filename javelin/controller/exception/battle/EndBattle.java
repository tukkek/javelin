package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.model.BattleMap;
import javelin.model.EquipmentMap;
import javelin.model.item.Item;
import javelin.model.item.scroll.RaiseScroll;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.HaxorScreen;

/**
 * A victory or defeat condition has been achieved.
 * 
 * @author alex
 */
public class EndBattle extends BattleEvent {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HaxorScreen#RESSURECT
	 */
	public static Combatant lastkilled = null;

	public static void updateoriginal(List<Combatant> originalteam) {
		for (final Combatant inbattle : BattleMap.blueTeam) {
			for (final Combatant original : originalteam) {
				if (original.source.customName == inbattle.source.customName) {
					updatehp(inbattle, original);
					original.xp = inbattle.xp;
					copyspells(inbattle, original);
					break;
				}
			}
		}
		for (final Combatant dead : BattleMap.dead) {
			for (final Combatant original : originalteam) {
				if (dead.id == original.id) {
					updatehp(dead, original);
					break;
				}
			}
		}
	}

	/**
	 * TODO move to {@link Combatant}
	 */
	public static void updatehp(final Combatant from, final Combatant to) {
		to.hp = from.hp;
		if (to.hp > to.maxhp) {
			to.hp = to.maxhp;
		} else if (to.hp < 1) {
			to.hp = 1;
		}
	}

	static public void copyspells(final Combatant from, final Combatant to) {
		for (int i = 0; i < from.spells.size(); i++) {
			to.spells.get(i).used = from.spells.get(i).used;
		}
	}

	static public void bury(List<Combatant> originalteam) {
		for (Combatant active : BattleMap.dead) {
			for (final Combatant original : new ArrayList<Combatant>(
					originalteam)) {
				if (active.toString().equals(original.toString())) {
					if (Combatant.DEADATHP < active.hp && active.hp <= 0) {
						original.hp = 1;
					} else if (!revive(original)) {
						lastkilled = original;
						originalteam.remove(original);
						Squad.active.equipment.remove(original.toString());
					}
					break;
				}
			}
		}
		BattleMap.dead.clear();
	}

	static public boolean revive(Combatant original) {
		Item revive = EquipmentMap.pop(RaiseScroll.class, Squad.active);
		if (revive != null) {
			revive.use(new Combatant(null, original.source, false));
			return true;
		}
		return false;
	}
}
