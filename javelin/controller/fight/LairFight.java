package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Lair;
import javelin.view.screen.BattleScreen;
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
	public ArrayList<Combatant> getmonsters(int teamel) {
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
		Javelin.captured.maxhp = capture.maxhp;
		return "You have captured the " + capture + "!";
	}

	@Override
	public void checkEndBattle(BattleScreen screen) {
		super.checkEndBattle(screen);
		if (checkend()) {
			throw new EndBattle();
		}
	}

	boolean checkend() {
		if (Fight.state.redTeam.size() >= 2) {
			return false;
		}
		Combatant target = findmonster();
		return target == null || target.hp <= target.maxhp / 2;
	}

	static Combatant findmonster() {
		int nfoes = Fight.state.redTeam.size();
		if (nfoes == 1) {
			return Fight.state.redTeam.get(0);
		}
		if (nfoes == 0) {
			for (Combatant c : Fight.state.dead) {
				if (BattleScreen.originalredteam.contains(c)
						&& c.getNumericStatus() != Combatant.STATUSDEAD) {
					return c;
				}
			}
		}
		return null;
	}

	@Override
	public ArrayList<Combatant> generate(int teamel, Terrain terrain) {
		ArrayList<Combatant> foes = super.generate(teamel - 2, terrain);
		foes.addAll(super.generate(teamel - 2, terrain));
		// while (foes.size() == 1 && Preferences.DEBUGFOE == null) {
		// foes = super.generate(teamel, terrain);
		// }
		return foes;
	}

	@Override
	public Boolean win(BattleScreen screen) {
		return super.win(screen) || checkend();
	}
}