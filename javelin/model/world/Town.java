package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import javelin.controller.db.StateManager;
import javelin.model.unit.Combatant;
import javelin.view.screen.town.RecruitScreen;
import javelin.view.screen.town.RestingScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitOption;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

public class Town implements WorldActor {

	public final int x;
	public final int y;
	public List<RecruitOption> recruits = new ArrayList<RecruitOption>(
			RecruitScreen.RECRUITSPERTOWN);
	public List<Option> upgrades = new ArrayList<Option>();
	public static List<Town> towns = new ArrayList<Town>();
	transient private Thing visual;

	public Town(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public void enter(final Squad s) {
		RestingScreen inn = new RestingScreen(this);
		inn.showtitle = false;
		inn.show();
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing > 0) {
				m.hp = m.maxhp;
			}
		}
		s.lasttown = this;
		StateManager.save();
	}

	@Override
	public void place() {
		visual = Lib.create("town");
		JavelinApp.overviewmap.addThing(visual, x, y);
	}

	@Override
	public int getx() {
		return x;
	}

	@Override
	public int gety() {
		return y;
	}

	@Override
	public void remove() {
		visual.remove();
		towns.remove(this);
	}

	@Override
	public String describe() {
		return "a town";
	}

}
