package javelin.controller.fight.minigame.arena.building;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.feature.Fountain;

public class ArenaFountain extends ArenaBuilding {
	static final String REFILLING = "This fountain is refilling... be patient!";

	public boolean spent = true;
	public float refillchance;

	public ArenaFountain() {
		super("Fountain", "dungeonfountain",
				"Click this fountain to fully restore the active unit!");
		setspent(spent);
	}

	@Override
	protected boolean click(Combatant current) {
		if (spent) {
			return promptupgrade();
		}
		restore(current);
		setspent(true);
		Game.messagepanel.clear();
		Game.message(current + " is completely restored!", Delay.BLOCK);
		return true;
	}

	private boolean promptupgrade() {
		Integer cost = getupgradecost();
		if (cost == null) {
			Javelin.prompt(REFILLING + " (already at max level)");
			return false;
		}
		String priceformat = Javelin.format(cost);
		int gold = ArenaFight.get().gold;
		if (gold < cost) {
			Javelin.prompt("You can upgrade this fountain for $" + priceformat
					+ " (you currently have $" + Javelin.format(gold)
					+ ").\n\nPress any key to continue...");
			return false;
		}
		if (Javelin.prompt("Do you want to upgrade this fountain for $"
				+ priceformat
				+ "?\nPress ENTER to proceed, any other key to cancel...") != '\n') {
			Game.messagepanel.clear();
			return false;
		}
		ArenaFight.get().gold -= cost;
		new BuildingUpgradeOption().upgrade();
		return true;
	}

	@Override
	protected void upgradebuilding() {
		// does nothing, just gets stronger
	}

	void restore(Combatant current) {
		heal(Fight.state.clone(current));
	}

	public static void heal(Combatant c) {
		for (Condition co : c.getconditions()) {
			c.removecondition(co);
		}
		Fountain.heal(c);
	}

	public void setspent(boolean spent) {
		this.spent = spent;
		source.avatarfile = spent ? "dungeonfountaindry" : "dungeonfountain";
	}

	@Override
	public String getactiondescription(Combatant current) {
		return spent ? REFILLING + " (or click to upgrade)"
				: super.getactiondescription(current);
	}

	@Override
	public void damage(int damagep, BattleState s, int reduce) {
		super.damage(damagep, s, reduce);
		// ArenaFountain f = (ArenaFountain) s.clone(this);
		// if (f != null && f.isdamaged()) {
		if (isdamaged()) {
			// f.clonesource();
			setspent(true);
		}
	}
}
