package javelin.view.screen;

import java.awt.Image;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.exception.EndBattle;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

public class DungeonScreen extends BattleScreen {
	public static final Image DUNGEONTEXTURE = QuestApp
			.getImage("/images/texture1.png");

	// public Monster capture;

	public DungeonScreen(final QuestApp q, final BattleMap mapp) {
		super(q, mapp);
		Javelin.settexture(DUNGEONTEXTURE);
	}

	/*
	 * TODO this type of thing should be on DungeonFight
	 */
	@Override
	public String dealreward() {
		Combatant capture = findmonster();
		if (capture == null) {
			return "You have killed the monster, cannot capture it!";
		}
		Javelin.captured = Javelin.recruit(capture.source.clone());
		return "You have captured the " + capture + "!";
	}

	private Combatant findmonster() {
		return BattleMap.redTeam.size() == 1 ? BattleMap.redTeam.get(0) : null;
	}

	// public Combatant findmonster() {
	// ArrayList<Combatant> all = new ArrayList<Combatant>(BattleMap.redTeam);
	// all.addAll(BattleMap.dead);
	// for (Combatant c : all) {
	// if (c.toString().equals(capture.toString())) {
	// return c;
	// }
	// }
	// throw new RuntimeException("Missing capture!");
	// }

	@Override
	public void afterend() {
		WorldMove.isleavingdungeon = true;
	}

	@Override
	public void checkEndBattle() {
		super.checkEndBattle();
		if (BattleMap.redTeam.size() >= 2) {
			return;
		}
		Combatant target = findmonster();
		if (target == null || target.hp <= target.maxhp / 2) {
			throw new EndBattle();
		}
	}

	@Override
	public void onEnd() {
		if (!BattleMap.blueTeam.isEmpty()) {
			messagepanel.clear();
			singleMessage(dealreward(), Delay.BLOCK);
			getUserInput();
			return;
		}
		super.onEnd();
	}
}
