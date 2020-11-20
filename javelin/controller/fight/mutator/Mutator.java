package javelin.controller.fight.mutator;

import java.io.Serializable;

import javelin.controller.fight.Fight;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;

/**
 * Represents custom {@link Fight} mechanics that can be used in certain
 * {@link Location}s, {@link Dungeon} {@link Branch}es...
 *
 * Having these as their own units allows to set them up dynamically,
 * mix-and-match... which is much more valuable (in general but specially for
 * procedural generation and replayability).
 *
 * @author alex
 */
@SuppressWarnings("unused")
public class Mutator implements Serializable{
	/** Called before {@link Fight#setup()}. */
	public void setup(Fight f){
		return;
	}
}
