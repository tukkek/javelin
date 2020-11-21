package javelin.controller.fight.mutator;

import java.io.Serializable;

import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.view.screen.BattleScreen;

/**
 * Represents custom {@link Fight} mechanics that can be used in certain
 * {@link Location}s, {@link Dungeon} {@link Branch}es...
 *
 * Having these as their own units allows them to be set up dynamically,
 * mix-and-match... which is much more valuable in general but specially for
 * procedural generation and replayability.
 *
 * @author alex
 */
@SuppressWarnings("unused")
public abstract class Mutator implements Serializable{
	/** Called before {@link Fight#setup()}. */
	public void setup(Fight f){
		return;
	}

	/** Last opportunity for changing this fight. */
	public void ready(Fight f){
		return;
	}

	/**
	 * Called after {@ling BattleState#blueTeam} and {@ling BattleState#redTeam}
	 * team are set but before they are placed, allowing for temporary
	 * {@link Combatant}s to be included.
	 */
	public void prepare(Fight f){
		return;
	}

	/** Called after painting the {@link BattleScreen} for the first time. */
	public void draw(Fight f){
		return;
	}

	/** @throws EndBattle */
	public void checkend(Fight f){
		return;
	}
}
