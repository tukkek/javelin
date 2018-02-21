package javelin.controller.fight.minigame.arena.building;

import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.Fountain;

public class ArenaFountain extends ArenaBuilding {
	private static final String REFILLING = "This fountain is refilling... be patient!";
	boolean spent = true;

	public ArenaFountain() {
		super("Fountain", "dungeonfountain",
				"Click this fountain to fully restore the active unit!");
		setspent(spent);
	}

	@Override
	protected boolean click(Combatant current) {
		if (spent) {
			return false;
		}
		restore(current);
		setspent(true);
		Game.messagepanel.clear();
		Game.message(current + " is completely restored!", Delay.BLOCK);
		return true;
	}

	void restore(Combatant current) {
		Combatant c = Fight.state.clone(current);
		for (Condition co : c.getconditions()) {
			c.removecondition(co);
		}
		Fountain.heal(c);
	}

	public void setspent(boolean spent) {
		this.spent = spent;
		this.source.avatarfile = spent ? "dungeonfountaindry"
				: "dungeonfountain";
	}

	@Override
	public String getactiondescription(Combatant current) {
		return spent ? REFILLING : super.getactiondescription(current);
	}
}
