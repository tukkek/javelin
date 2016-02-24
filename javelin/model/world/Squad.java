package javelin.model.world;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.EquipmentMap;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.view.screen.town.TransportScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;

/**
 * A group of units that the player controls as a overworld game unit. If a
 * player loses all his squads the game ends.
 * 
 * @author alex
 */
public class Squad implements WorldActor {
	public enum Transport {
		NONE, CARRIAGE, AIRSHIP
	}

	static public ArrayList<Squad> squads = new ArrayList<Squad>();
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
	public Transport transport = Transport.NONE;

	public Squad(final int xp, final int yp, final long hourselapsedp) {
		super();
		Squad.squads.add(this);
		x = xp;
		y = yp;
		hourselapsed = hourselapsedp;
		if (Squad.active == null) {
			Squad.active = this;
		}
	}

	public void disband() {
		Squad.squads.remove(this);
		if (Squad.squads.isEmpty() && Town.train() == null) {
			Javelin.lose();
		}
		if (Squad.active == this) {
			Squad.active = Javelin.nexttoact();
		}
		visual.remove();
		if (Dungeon.active != null) {
			Dungeon.active.leave();
		}
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
		final Thing avatar = createThing();
		JavelinApp.overviewmap.addThing(avatar, x, y);
		visual = avatar;
		updateavatar();
		if (Game.hero() == null) {
			Game.instance().hero = avatar;
		}
	}

	public Thing createThing() {
		return Lib.create("mercenary captain");
	}

	public void updateavatar() {
		Combatant leader = members.get(0);
		for (int i = 1; i < members.size(); i++) {
			Combatant m = members.get(i);
			if (ChallengeRatingCalculator
					.calculateCr(m.source) > ChallengeRatingCalculator
							.calculateCr(leader.source)) {
				leader = m;
			}
		}
		Monster dummy = leader.source;
		if (transport != Transport.NONE && Dungeon.active == null) {
			dummy = dummy.clone();
			dummy.avatarfile =
					transport == Transport.CARRIAGE ? "carriage" : "airship";
		}
		visual.combatant = new Combatant(visual, dummy, false);
	}

	public void join(final Squad squad) {
		members.addAll(squad.members);
		gold += squad.gold;
		hourselapsed = Math.max(hourselapsed, squad.hourselapsed);
		for (final Combatant m : squad.members) {
			equipment.put(m.id, squad.equipment.get(m.id));
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
		Squad.squads.remove(this);
	}

	@Override
	public String describe() {
		return "one of your squads";
	}

	public void displace() {
		WorldScreen.displace(this);
	}

	public void eat() {
		gold -= size() / 2;
		if (transport == Transport.CARRIAGE) {
			gold -= TransportScreen.CARRIAGEMAINTENANCE;
		} else if (transport == Transport.AIRSHIP) {
			gold -= TransportScreen.AIRSHIPMAINTENANCE;
		}
		if (gold < 0) {
			gold = 0;
			transport = Transport.NONE;
		}
	}

	public void add(Combatant member, ArrayList<Item> equipmentp) {
		members.add(member);
		equipment.put(member.id, equipmentp);
	}

	public void remove(Combatant c) {
		members.remove(c);
		if (members.isEmpty()) {
			disband();
		}
	}

	@Override
	public void move(int tox, int toy) {
		x = tox;
		y = toy;
		if (visual != null) {
			visual.x = tox;
			visual.y = tox;
		}
	}

	@Override
	public String toString() {
		return members.toString();
	}

	public void receiveitem(Item key) {
		equipment.add(key, this);
	}
}
