package javelin.controller.exception.battle;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.item.Scroll;
import javelin.model.spell.conjuration.healing.RaiseDead;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
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
	public static void end(BattleScreen screen,
			ArrayList<Combatant> originalTeam) {
		int nsquads = Squad.getall(Squad.class).size();
		BattleMap.victory = Javelin.app.fight.win(screen);
		BattleState s = screen.map.getState();
		terminateconditions(s, screen);
		Javelin.app.fight.onEnd(screen, originalTeam, s);
		BattleMap.combatants.clear();
		AiCache.reset();
		if (Squad.active != null
				&& nsquads == Squad.getall(Squad.class).size()) {
			while (WorldActor.get(Squad.active.x, Squad.active.y,
					Incursion.class) != null) {
				Squad.active.visual.remove();
				Squad.active.displace();
				Squad.active.visual.remove();
				Squad.active.place();
			}
			end(originalTeam);
			if (Dungeon.active != null) {
				Temple.climbing = false;
				Dungeon.active.activate(false);
			}
		}
	}

	static void terminateconditions(BattleState s, BattleScreen screen) {
		screen.checkblock();
		for (Combatant c : BattleMap.combatants) {
			c.finishconditions(s, screen);
		}
	}

	/**
	 * Prints combat info (rewards, etc).
	 * 
	 * @param prefix
	 */
	public static void showcombatresult(BattleScreen screen,
			List<Combatant> originalteam, String prefix) {
		Game.messagepanel.clear();
		String combatresult;
		if (BattleMap.victory) {
			combatresult = prefix + Javelin.app.fight.dealreward();
		} else if (screen.fleeing.isEmpty()) {
			Squad.active.disband();
			combatresult = "You lost!";
		} else if (Javelin.app.fight.friendly) {
			combatresult = "You lost!";
		} else {
			combatresult = "Fled from combat. No awards received.";
			if (!BattleMap.victory
					&& screen.fleeing.size() != originalteam.size()) {
				combatresult += "\nFallen allies left behind are lost!";
				for (Combatant abandoned : BattleMap.dead) {
					abandoned.hp = Combatant.DEADATHP;
				}
			}
			if (Squad.active.transport != null) {
				combatresult += " Vehicle lost!";
				Squad.active.transport = null;
				Squad.active.updateavatar();
			}
		}
		screen.singleMessage(combatresult + "\nPress any key to continue...",
				Delay.BLOCK);
		screen.getUserInput();
	}

	static void updateoriginal(List<Combatant> originalteam) {
		for (final Combatant inbattle : BattleMap.dead) {
			for (final Combatant original : originalteam) {
				if (original.equals(inbattle)) {
					update(inbattle, original);
				}
			}
		}
		for (final Combatant inbattle : new ArrayList<Combatant>(
				BattleMap.blueTeam)) {
			for (final Combatant original : originalteam) {
				if (original.equals(inbattle)) {
					update(inbattle, original);
					original.xp = inbattle.xp;
					inbattle.transferconditions(original);
					break;
				}
			}
		}
	}

	public static void update(final Combatant from, final Combatant to) {
		to.hp = from.hp;
		if (to.hp > to.maxhp) {
			to.hp = to.maxhp;
		} else if (to.hp < 1) {
			to.hp = 1;
		}
		copyspells(from, to);
		// to.automatic = from.automatic;
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
		for (Combatant active : BattleMap.dead) {
			for (final Combatant original : new ArrayList<Combatant>(
					originalteam)) {
				if (active.equals(original)) {
					if (active.hp > Combatant.DEADATHP && active.hp <= 0
							&& active.source.constitution > 0) {
						original.hp = 1;
					} else if (!BattleMap.victory || !revive(original)) {
						lastkilled = original;
						originalteam.remove(original);
						if (BattleMap.victory) {
							for (Item i : Squad.active.equipment
									.get(original.id)) {
								i.grab();
							}
						}
						Squad.active.equipment.remove(original.id);
					}
					break;
				}
			}
		}
		BattleMap.dead.clear();
	}

	static boolean revive(Combatant original) {
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
					if (s != null && s.spell instanceof RaiseDead) {
						spell = s.spell;
						scroll = s;
					}
				}
			}
		}
		if (spell == null) {
			return false;
		}
		Combatant dead = new Combatant(null, original.source, false);
		if (!spell.validate(null, dead)) {
			return false;
		}
		spell.castpeacefully(null, dead);
		spell.used += 1;
		if (scroll != null) {
			Squad.active.equipment.pop(scroll.getClass());
		}
		return true;
	}

	static void end(ArrayList<Combatant> originalteam) {
		for (Combatant c : new ArrayList<Combatant>(BattleMap.combatants)) {
			if (c.summoned) {
				BattleMap.combatants.remove(c);
				BattleMap.blueTeam.remove(c);
				BattleMap.redTeam.remove(c);
			}
		}
		updateoriginal(originalteam);
		bury(originalteam);
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
