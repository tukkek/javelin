package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.item.scroll.RaiseScroll;
import javelin.model.spell.ScrollSpell;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.haxor.HaxorScreen;

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
				if (original.equals(inbattle)) {
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
					} else if (BattleMap.victory && !revive(original)) {
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

	static boolean revive(Combatant original) {
		RaiseScroll scroll = (RaiseScroll) Squad.active.equipment
				.contains(RaiseScroll.class);
		if (scroll != null
				&& scroll.revive(new Combatant(null, original.source, false))) {
			Squad.active.equipment.pop(RaiseScroll.class);
			return true;
		}
		Spell spell = null;
		search: for (Combatant c : Squad.active.members) {
			for (Spell s : c.spells) {
				ScrollSpell ss =
						s instanceof ScrollSpell ? (ScrollSpell) s : null;
				if (ss != null && ss.s instanceof RaiseScroll
						&& !ss.exhausted()) {
					spell = s;
					break search;
				}
			}
		}
		if (spell != null && ((ScrollSpell) spell).s
				.use(new Combatant(null, original.source, false))) {
			spell.used += 1;
			return true;
		}
		return false;
	}

	public static void end(BattleScreen battleScreen,
			ArrayList<Combatant> originalTeam) {
		int nsquads = Squad.getall(Squad.class).size();
		battleScreen.onEnd();
		BattleMap.combatants.clear();
		AiCache.reset();
		/* TODO probably size comparison is enough */
		if (Squad.active != null
				&& nsquads == Squad.getall(Squad.class).size()) {
			while (WorldScreen.getactor(Squad.active.x, Squad.active.y,
					Incursion.class) != null) {
				Squad.active.visual.remove();
				Squad.active.displace();
				Squad.active.visual.remove();
				Squad.active.place();
			}
			end(originalTeam);
			if (Dungeon.active != null) {
				Dungeon.active.activate();
			}
		}
	}

	static void end(ArrayList<Combatant> originalteam) {
		for (Combatant c : new ArrayList<Combatant>(BattleMap.combatants)) {
			if (c.summoned) {
				BattleMap.combatants.remove(c);
				BattleMap.blueTeam.remove(c);
				BattleMap.redTeam.remove(c);
			}
		}
		EndBattle.updateoriginal(originalteam);
		EndBattle.bury(originalteam);
		if (Javelin.captured != null) {
			originalteam.add(Javelin.captured);
			Javelin.captured = null;
		}
		Squad.active.members = originalteam;
		for (Combatant member : Squad.active.members) {
			member.currentmelee.sequenceindex = -1;
			member.currentranged.sequenceindex = -1;
		}
		ThreadManager.printbattlerecord();
	}
}
