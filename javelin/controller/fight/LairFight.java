package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Lair;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
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
	/** Texture for {@link Dungeon} fights. */
	public static final Image DUNGEONTEXTURE =
			QuestApp.getImage("/images/texture1.png");

	/** Constructor. */
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
		try {
			Combatant capture = findmonster();
			if (capture == null) {
				return "You have killed the monster, cannot capture it!";
			}
			Javelin.captured = Javelin.recruit(capture.source.clone());
			Javelin.captured.hp = capture.hp;
			Javelin.captured.maxhp = capture.maxhp;
			return "You have captured the " + capture + "!";
		} finally {
			Javelin.app.switchScreen(BattleScreen.active);
		}
	}

	static Combatant findmonster() {
		if (Fight.state.redTeam.isEmpty()) {
			for (Combatant c : Fight.state.dead) {
				if (BattleScreen.originalredteam.contains(c)
						&& c.getnumericstatus() != Combatant.STATUSDEAD) {
					return c;
				}
			}
			return null;
		}
		return RPG.pick(Fight.state.redTeam);
	}

	@Override
	public ArrayList<Combatant> generate(int teamel) {
		ArrayList<Combatant> foes = super.generate(teamel - 2);
		foes.addAll(super.generate(teamel - 2));
		return foes;
	}

	@Override
	public Boolean win() {
		if (super.win()) {
			return true;
		}
		if (Fight.state.redTeam.size() > 1) {
			return false;
		}
		Combatant target = findmonster();
		return target == null || target.hp <= target.maxhp / 2;
	}
}