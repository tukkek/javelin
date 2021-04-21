package javelin.controller.content.fight.mutator;

import java.io.Serializable;

import javelin.controller.content.action.Action;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.view.screen.BattleScreen;

/**
 * Represents custom {@link Fight} mechanics that can be used in certain
 * {@link Location}s, {@link Dungeon} {@link Branch}es...
 *
 * Having these as their own code units allows them to be set up dynamically,
 * mix-and-match... which is much more valuable in general but especially for
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
	 * teams are set but before they are placed, allowing for temporary
	 * {@link Combatant}s to be included.
	 */
	public void prepare(Fight f){
		return;
	}

	/** Called after painting the {@link BattleScreen} for the first time. */
	public void draw(Fight f){
		return;
	}

	/**
	 * Opportunity to change state before a {@link Fight} ends. Should not throw
	 * {@link EndBattle} by itself.
	 */
	public void checkend(Fight f){
		return;
	}

	/** After any unit ends its {@link Action}. */
	public void endturn(Fight f){
		return;
	}

	/** Called when a fight ends but before clean-ups. */
	public void end(Fight f){
		return;
	}

	/** Called upon an unit's death. */
	public void die(Combatant c,BattleState s,Fight f){
		return;
	}

	/** Called after the battle {@link #end(Fight)}, results are shown, etc. */
	public void after(Fight f){
		return;
	}
}
