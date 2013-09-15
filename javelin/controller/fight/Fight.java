package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.model.BattleMap;
import javelin.model.unit.Monster;
import javelin.view.screen.BattleScreen;

public interface Fight {
	public int getel(JavelinApp javelinApp, int teamel);

	public BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap);

	public List<Monster> getmonsters(int teamel);
}
