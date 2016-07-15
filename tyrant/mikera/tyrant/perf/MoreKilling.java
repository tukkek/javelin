package tyrant.mikera.tyrant.perf;

import javelin.controller.action.Action;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Hero;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.test.MapHelper;
import tyrant.mikera.tyrant.test.NullHandler;
import tyrant.mikera.tyrant.test.TyrantTestCase;

public class MoreKilling implements IWork {
	private Thing hero;
	private BattleMap map;
	private BattleScreen gameScreen;

	public MoreKilling() {
		// empty constructor
	}

	public void run() {
		final boolean originalGetSet = BaseObject.GET_SET_DEBUG;
		try {
			while (hero.x < map.getWidth() - 2) {
				gameScreen.tryTick(hero, Action.MOVE_E, false);
			}
		} finally {
			BaseObject.GET_SET_DEBUG = originalGetSet;
		}
	}

	public void setUp() {
		RPG.setRandSeed(0);
		Lib.clear();
		hero = Hero.createHero("bob", "human", "fighter");
		TyrantTestCase.setTestHero(hero);
		NullHandler.installNullMessageHandler();
		Game.setUserinterface(null);

		final String mapString = "################################" + "\n"
				+ "#@.............................#" + "\n"
				+ "##.............................#" + "\n"
				+ "#..............................#" + "\n"
				+ "################################";

		map = new MapHelper().createMap(mapString);
		for (int x = hero.x; x < map.getWidth(); x++) {
			if (!map.isBlocked(x, 1)) {
				map.addThing(Lib.create("[IsMonster]"), x, 1);
				map.addThing(Lib.create("[IsItem]"), x, 1);
				map.addThing(Lib.create("menhir"), x, 2);
				map.addThing(Lib.create("[IsMonster]"), x, 3);
				map.addThing(Lib.create("[IsItem]"), x, 3);
			}
		}
		hero.set("IsImmortal", true);
		gameScreen = new BattleScreen(new QuestApp());
		gameScreen.map = map;
	}

	public String getMessage() {
		return "";
	}
}
