package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * {@link Temple} fights are different from normal {@link Dungeon} encounters
 * because the creatures are upgraded with {@link Upgrade}s from the respective
 * {@link Realm}.
 * 
 * @author alex
 */
public class TempleEncounter extends RandomDungeonEncounter {
	Temple temple;

	/** Constructor. */
	public TempleEncounter(Temple temple) {
		this.temple = temple;
	}

	@Override
	public int getel(int teamel) {
		return Math.max(1, temple.el - RPG.r(3, 5));
	}

	@Override
	public void enhance(List<Combatant> foes) {
		while (ChallengeRatingCalculator.calculateel(foes) < temple.el) {
			Combatant.upgradeweakest(foes, temple.realm);
		}
	}

	@Override
	public boolean onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		super.onEnd(screen, originalTeam, s);
		Temple.leavingfight = true;
		return true;
	}

	@Override
	public boolean validate(ArrayList<Combatant> foes) {
		return temple.validate(foes);
	}

	@Override
	public ArrayList<Terrain> getterrains() {
		return temple.getterrains();
	}
}
