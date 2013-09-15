package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.EquipmentMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;

public class Squad implements WorldActor {
	static public List<Squad> squads = new ArrayList<Squad>();
	/**
	 * See {@link Javelin#act()}.
	 */
	static public Squad active;

	/**
	 * TODO need to make {@link Monster} serializable
	 */
	public ArrayList<Combatant> members = new ArrayList<Combatant>();
	public int gold = 0;
	public EquipmentMap equipment = new EquipmentMap();
	public int x;
	public int y;
	public transient Thing visual;
	/**
	 * Start at morning.
	 */
	public long hourselapsed;
	public Town lasttown = null;

	public Squad(final int xp, final int yp, final long hourselapsedp) {
		super();
		squads.add(this);
		x = xp;
		y = yp;
		hourselapsed = hourselapsedp;
	}

	public void disband() {
		squads.remove(this);
		if (squads.isEmpty()) {
			Javelin.lose();
		}
		if (active == this) {
			active = null;
		}
		visual.remove();
	}

	public float size() {
		float sum = 0;
		for (final Combatant m : members) {
			final int size = m.source.size;
			switch (size) {
			case Monster.FINE:
				sum += 1 / 16.0;
				break;
			case Monster.DIMINUTIVE:
				sum += 1 / 8.0;
				break;
			case Monster.TINY:
				sum += 1 / 4.0;
				break;
			case Monster.SMALL:
				sum += 1 / 2.0;
				break;
			case Monster.MEDIUM:
				sum += 1;
				break;
			case Monster.LARGE:
				sum += 2;
				break;
			case Monster.HUGE:
				sum += 4;
				break;
			case Monster.GARGANTUAN:
				sum += 8;
				break;
			case Monster.COLOSSAL:
				sum += 16;
				break;
			default:
				throw new RuntimeException("Unknown size " + size);
			}
		}
		return sum;
	}

	@Override
	public void place() {
		final Thing avatar = Lib.create("mercenary captain");
		visual = avatar;
		declareleader();
		JavelinApp.overviewmap.addThing(avatar, x, y);
		if (Game.hero() == null) {
			Game.instance().hero = avatar;
		}
	}

	public void declareleader() {
		Combatant leader = members.get(0);
		for (int i = 1; i < members.size(); i++) {
			Combatant m = members.get(i);
			if (ChallengeRatingCalculator.calculateCr(m.source) > ChallengeRatingCalculator
					.calculateCr(leader.source)) {
				leader = m;
			}
		}
		visual.combatant = new Combatant(visual, leader.source, false);
	}

	public void join(final Squad squad) {
		members.addAll(squad.members);
		gold += squad.gold;
		hourselapsed = Math.max(hourselapsed, squad.hourselapsed);
		for (final Combatant m : squad.members) {
			equipment.put(m.toString(), squad.equipment.get(m.toString()));
		}
		squad.disband();
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
		squads.remove(this);
	}

	@Override
	public String describe() {
		return "one of your squads";
	}

	public void displace() {
		int deltax = 0, deltay = 0;
		int[] nudges = new int[] { -1, 0, +1 };
		while (deltax == 0 && deltay == 0) {
			deltax = RPG.pick(nudges);
			deltay = RPG.pick(nudges);
		}
		int tox = x + deltax;
		int toy = y + deltay;
		ArrayList<WorldActor> actors = WorldScreen.getactors();
		actors.remove(Squad.active);
		if (WorldScreen.getactor(tox, toy, actors) == null) {
			x = tox;
			y = toy;
		} else {
			displace();
		}
	}

	public void eat() {
		gold -= size() / 2;
		if (gold < 0) {
			gold = 0;
		}
	}
}
