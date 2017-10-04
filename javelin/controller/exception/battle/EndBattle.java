package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.item.Scroll;
import javelin.model.state.BattleState;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.RaiseDead;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.BattleScreen;
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

	/**
	 * Handles all post-battle updates.
	 *
	 * @param screen
	 *            Open battle screen.
	 * @param originalTeam
	 *            Player team.
	 */
	public static void end() {
		int nsquads = World.getall(Squad.class).size();
		Fight.victory = Javelin.app.fight.win();
		terminateconditions(Fight.state, BattleScreen.active);
		if (!Javelin.app.fight.onend()) {
			return;
		}
		AiCache.reset();
		if (Squad.active != null
				&& nsquads == World.getall(Squad.class).size()) {
			while (World.get(Squad.active.x, Squad.active.y,
					Incursion.class) != null) {
				Squad.active.displace();
				Squad.active.place();
			}
			end(Fight.originalblueteam);
			if (Dungeon.active != null) {
				Temple.climbing = false;
				Dungeon.active.activate(false);
			}
		}
	}

	static void terminateconditions(BattleState s, BattleScreen screen) {
		screen.block();
		for (Combatant c : Fight.state.getcombatants()) {
			c.finishconditions(s, screen);
		}
	}

	/**
	 * Prints combat info (rewards, etc).
	 *
	 * @param prefix
	 */
	public static void showcombatresult() {
		Game.messagepanel.clear();
		String combatresult;
		if (Fight.victory) {
			combatresult = Javelin.app.fight.reward();
		} else if (Fight.state.getfleeing(Fight.originalblueteam).isEmpty()) {
			Squad.active.disband();
			combatresult = "You lost!";
		} else if (Javelin.app.fight.friendly) {
			combatresult = "You lost!";
		} else {
			combatresult = "Fled from combat. No awards received.";
			if (!Fight.victory && Fight.state.fleeing
					.size() != Fight.originalblueteam.size()) {
				combatresult += "\nFallen allies left behind are lost!";
				for (Combatant abandoned : Fight.state.dead) {
					abandoned.hp = Combatant.DEADATHP;
				}
			}
			if (Squad.active.transport != null) {
				combatresult += " Vehicle lost!";
				Squad.active.transport = null;
				Squad.active.updateavatar();
			}
		}
		Game.message(combatresult + "\nPress any key to continue...",
				Delay.BLOCK);
		BattleScreen.active.getUserInput();
	}

	static void updateoriginal(List<Combatant> originalteam) {
		ArrayList<Combatant> update = new ArrayList<Combatant>(
				Fight.state.blueTeam);
		update.addAll(Fight.state.dead);
		for (final Combatant inbattle : update) {
			int originali = originalteam.indexOf(inbattle);
			if (originali >= 0) {
				Combatant original = originalteam.get(originali);
				update(inbattle, original);
				inbattle.transferconditions(original);
			}
		}
	}

	static void update(final Combatant from, final Combatant to) {
		to.hp = from.hp;
		if (to.hp > to.maxhp) {
			to.hp = to.maxhp;
		} else if (to.hp < 1) {
			to.hp = 1;
		}
		copyspells(from, to);
	}

	static void copyspells(final Combatant from, final Combatant to) {
		for (int i = 0; i < from.spells.size(); i++) {
			to.spells.get(i).used = from.spells.get(i).used;
		}
	}

	/**
	 * Tries to {@link #revive(Combatant)} the combatant. If can't, remove him
	 * from the game.
	 *
	 * TODO isn't updating {@link #lastkilled} when the entire Squad dies! this
	 * probably isn't being called
	 */
	static void bury(List<Combatant> originalteam) {
		for (Combatant active : Fight.state.dead) {
			for (final Combatant original : new ArrayList<Combatant>(
					originalteam)) {
				if (active.equals(original)) {
					if (active.hp > Combatant.DEADATHP && active.hp <= 0
							&& active.source.constitution > 0) {
						original.hp = 1;
					} else if (!Fight.victory
							|| !revive(original, originalteam)) {
						lastkilled = original;
						originalteam.remove(original);
						Squad.active.members.remove(original); // remove member
						MercenariesGuild.die(original);
						if (Fight.victory) {
							final ArrayList<Item> bag = Squad.active.equipment
									.get(original.id);
							for (Item i : bag) {
								i.grab();
							}
						}
						Squad.active.remove(original); // remove equipment
					}
					break;
				}
			}
		}
		Fight.state.dead.clear();
	}

	static boolean revive(Combatant dead, List<Combatant> originalteam) {
		Spell spell = null;
		Scroll scroll = null;
		search: for (Combatant c : Squad.active.members) {
			for (Spell s : c.spells) {
				if (s instanceof RaiseDead && !s.exhausted()) {
					spell = s;
					break search;
				}
			}
		}
		if (spell == null) {
			for (List<Item> bag : Squad.active.equipment.values()) {
				for (Item i : bag) {
					Scroll s = i instanceof Scroll ? (Scroll) i : null;
					if (s != null && s.spell instanceof RaiseDead
							&& canuse(s, originalteam)) {
						spell = s.spell;
						scroll = s;
					}
				}
			}
		}
		if (spell == null) {
			return false;
		}
		if (!spell.validate(null, dead)) {
			return false;
		}
		spell.castpeacefully(null, dead);
		spell.used += 1;
		if (scroll != null) {
			Squad.active.equipment.popitem(scroll.getClass(), Squad.active);
		}
		return true;
	}

	private static boolean canuse(Item s, List<Combatant> alive) {
		for (Combatant c : alive) {
			if (!Fight.state.dead.contains(c) && s.canuse(c) == null) {
				return true;
			}
		}
		return false;
	}

	static void end(ArrayList<Combatant> originalteam) {
		for (Combatant c : Fight.state.getcombatants()) {
			if (c.summoned) {
				Fight.state.blueTeam.remove(c);
				Fight.state.redTeam.remove(c);
			}
		}
		updateoriginal(originalteam);
		bury(originalteam);
		Squad.active.members = originalteam;
		for (Combatant member : Squad.active.members) {
			member.currentmelee.sequenceindex = -1;
			member.currentranged.sequenceindex = -1;
		}
		ThreadManager.printbattlerecord();
	}
}
