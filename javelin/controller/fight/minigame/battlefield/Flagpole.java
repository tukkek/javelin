package javelin.controller.fight.minigame.battlefield;

import java.math.BigDecimal;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;

public class Flagpole extends Building {
	BattlefieldFight fight;
	boolean blueteam;
	float rank;

	public Flagpole(BattlefieldFight battlefieldFight, float rank,
			boolean blueteam) {
		super(Javelin.getmonster("building"), false);
		fight = battlefieldFight;
		this.rank = rank;
		maxhp = Math.round(rank * 25);
		hp = maxhp;
		source.cr = 2 * rank;
		source.customName = "Flagpole";
		source.passive = true;
		setteam(blueteam);
	}

	void setteam(boolean blueteam) {
		this.blueteam = blueteam;
		source.avatarfile = blueteam ? "flagpoleblue" : "flagpolered";
	}

	@Override
	public void damage(int damagep, BattleState s, int reduce) {
		super.damage(damagep, s, reduce);
		if (hp <= 0) {
			Flagpole clone = (Flagpole) s.clone(this).clonesource();
			if (blueteam) {
				s.blueTeam.remove(clone);
				s.redTeam.add(clone);
			} else {
				s.redTeam.remove(clone);
				s.blueTeam.add(clone);
			}
			clone.setteam(!blueteam);
			clone.hp = 1;
		}
	}

	@Override
	public void act(BattleState s) {
		super.act(s);
		hp += rank;
		if (hp > maxhp) {
			hp = maxhp;
		}
		ap += 1;
	}

	@Override
	public BattleMouseAction getmouseaction() {
		return new BattleMouseAction() {
			@Override
			public void onenter(Combatant current, Combatant target, Tile t,
					BattleState s) {
				BigDecimal points = new BigDecimal(
						rank * BattlefieldFight.POINTSPERTURN);
				points.setScale(2);
				Flagpole.this.fight.updateflagpoles();
				int upkeep = Math.round(100 * Flagpole.this.fight.getupkeep(
						BattlefieldFight.state.blueTeam,
						Flagpole.this.fight.blueflagpoles));
				int currentpoints = Math.round(Flagpole.this.fight.bluepoints);
				String message = "This is your flagpole. It generates " + points
						+ " army point(s) per turn, reduced by your current army upkeep ("
						+ upkeep
						+ "%).\nIf it is captured by the enemy, attack it to recapture it for your team!\nYou currently have "
						+ (currentpoints < 1 ? 0 : currentpoints)
						+ " army points, click to recruit new units.";
				Game.message(message, Delay.NONE);
			}

			@Override
			public boolean validate(Combatant current, Combatant target,
					BattleState s) {
				return blueteam;
			}

			@Override
			public void act(Combatant current, Combatant target,
					BattleState s) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						Flagpole.this.fight.recruitbluearmy();
					}
				});
			}
		};
	}
}