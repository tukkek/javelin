package javelin.controller.fight.minigame.battlefield;

import java.math.BigDecimal;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;

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
			s.swapteam(this);
			setteam(!blueteam);
			hp = 1;
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
				fight.updateflagpoles();
				int upkeep = Math.round(100 * fight
						.getupkeep(Fight.state.blueTeam, fight.blueflagpoles));
				int currentpoints = Math.round(fight.bluepoints);
				String message = "This is your flagpole. It generates " + points
						+ " army point(s) per turn, reduced by your current army upkeep ("
						+ upkeep
						+ "%).\nIf it is captured by the enemy, attack it to recapture it for your team!\nYou currently have "
						+ (currentpoints < 1 ? 0 : currentpoints)
						+ " army points, click to recruit new units.";
				Javelin.message(message, Javelin.Delay.NONE);
			}

			@Override
			public boolean validate(Combatant current, Combatant target,
					BattleState s) {
				return blueteam;
			}

			@Override
			public Runnable act(Combatant current, Combatant target,
					BattleState s) {
				return () -> fight.recruitbluearmy();
			}
		};
	}
}