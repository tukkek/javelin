package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Lair;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

/**
 * A moderate fight is teamel-4, a difficult fight teamel-2 (double the
 * difficulty). The difficulty here is bumped once more due to the last enemy
 * requiring only 50% hp to capture.
 * 
 * @see Lair
 * 
 * @author alex
 */
public class LairFight extends Fight {
	public static final Image DUNGEONTEXTURE =
			QuestApp.getImage("/images/texture1.png");

	public LairFight() {
		texture = DUNGEONTEXTURE;
		meld = true;
		hide = false;
		bribe = false;
	}

	@Override
	public int getel(final int teamel) {
		return teamel - 1;
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public String dealreward() {
		Combatant capture = findmonster();
		if (capture == null) {
			return "You have killed the monster, cannot capture it!";
		}
		Javelin.captured = Javelin.recruit(capture.source.clone());
		Javelin.captured.hp = capture.hp;
		return "You have captured the " + capture + "!";
	}

	@Override
	public void checkEndBattle(BattleScreen screen) {
		super.checkEndBattle(screen);
		if (BattleMap.redTeam.size() >= 2) {
			return;
		}
		Combatant target = findmonster();
		if (target == null || target.hp <= target.maxhp / 2) {
			throw new EndBattle();
		}
	}

	static Combatant findmonster() {
		return BattleMap.redTeam.size() == 1 ? BattleMap.redTeam.get(0) : null;
	}

	@Override
	public void onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		if (!BattleMap.blueTeam.isEmpty()) {
			screen.messagepanel.clear();
			screen.singleMessage(Javelin.app.fight.dealreward(), Delay.BLOCK);
			screen.getUserInput();
			return;
		}
	}
}