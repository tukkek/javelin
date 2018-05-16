package javelin.controller.fight.minigame.arena.building;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.RPG;

public class ArenaGateway extends ArenaBuilding {
	public ArenaGateway() {
		super("Gateway", "locationportal", "Gateway");
		repairing = true;
		materials = 9000;
		setlevel(getlevel());
		hp = maxhp / 2;
		ap = Fight.state.next.ap;
	}

	BuildingLevel getlevel() {
		float levels = 0;
		for (Combatant c : ArenaFight.get().getgladiators()) {
			levels += c.source.cr;
		}
		float averagepartylevel = levels / 4;
		int tier = Math.min(Math.round(averagepartylevel / 5), 3);
		return LEVELS[tier];
	}

	@Override
	public void act(BattleState s) {
		super.act(s);
		if (hp < maxhp) {
			repairing = true;
			materials = 9000;
		}
	}

	@Override
	protected void upgradebuilding() {
		// fixed level
	}

	@Override
	protected boolean click(Combatant current) {
		if (Fight.state.redTeam.contains(this)) {
			String red = "Convert the game to your team first!";
			Game.message(red, Delay.WAIT);
			return false;
		}
		if (getnumericstatus() < STATUSSCRATCHED) {
			String damaged = "The gate is too damaged to pass through!";
			Game.message(damaged, Delay.WAIT);
			return false;
		}
		Fight.state.blueTeam.remove(current);
		ArenaFight.get().victors.add(current);
		return false;
	}

	public void place() {
		Point p = null;
		BattleState s = Fight.state;
		while (p == null || s.map[p.x][p.y].blocked
				|| s.getcombatant(p.x, p.y) != null) {
			p = new Point(RPG.r(0, s.map.length), RPG.r(0, s.map[0].length));
		}
		setlocation(p);
		s.blueTeam.add(this);
	}

	@Override
	public void damage(int damagep, BattleState s, int reduce) {
		super.damage(damagep, s, reduce);
		if (hp < maxhp) {
			repairing = true;
			materials = 9000;
			if (hp <= 0) {
				s.swapteam(this);
				hp = 1;
			}
		}
	}
}
