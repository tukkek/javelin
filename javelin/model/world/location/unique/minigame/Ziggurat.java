package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.fight.minigame.Run;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.unique.UniqueLocation;

/**
 * A mini-game vaguely inspired by Z by the Bitmap Brothers in which a map is
 * segmented in several sections. The player starts in the southwesternmost and
 * also controls the segment north and east. Each segment represents a
 * {@link Monster} type, so the player has 3 types at his disposal when the
 * match starts.
 *
 * Every time a new, unexplored segment in entered an enemy group is formed from
 * the not-captured segments north, east, south and west of the current segment
 * (and the current one too, of course). Monsters are positioned in the given
 * passage between segments. Once a battle finishes the player can rebuild his
 * team with units from any of the segments he has captured before, up to the
 * Encounter Level of the current segment (next fight). The objective is to
 * reach and conquer the northeastmost segment.
 *
 * Every time a {@link Run} is completed a {@link TempleKey} to a locked
 * {@link Temple} spawns to be taken via {@link #interact()}. A player may also
 * choose to sacrifice this key to be teleported to the location of any Temple.
 *
 * @author alex
 */
public class Ziggurat extends UniqueLocation {

	private static final String DESCRIPTION = "Ziggurat";
	/** Treasure for conquering the Ziggurat. */
	public Item key = null;

	/** Constructor. */
	public Ziggurat() {
		super(DESCRIPTION, DESCRIPTION, 0, 0);
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String intro = "You climb to the top of the huge pyramid...\n\n";
		if (key == null) {
			intro += "It is empty... Try winning a Ziggurat match.\n";
			Javelin.message(intro, false);
		} else {
			intro += "There is a " + key.toString().toLowerCase()
					+ " upon the altar. Do you want to:";
			ArrayList<String> choices = new ArrayList<String>();
			choices.add("Take the key");
			choices.add("Sacrifice the key and teleport to a location");
			choices.add("Leave");
			int choice = Javelin.choose(intro, choices, true, true);
			if (choice == 0) {
				key.grab();
				key = null;
			} else if (choice == 1) {
				key = null;
				new GreaterTeleport().castpeacefully(null, null);
			}
		}
		return true;
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// empty
	}

	@Override
	protected void generate() {
		while (x < 0 || !Terrain.get(x, y).equals(Terrain.FOREST)) {
			super.generate();
		}
	}
}
