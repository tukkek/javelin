package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.IncursionFight;
import javelin.model.EquipmentMap;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.Transport;
import javelin.model.world.place.unique.MercenariesGuild;
import javelin.view.screen.BribingScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.TransportScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * A group of units that the player controls as a overworld game unit. If a
 * player loses all his squads the game ends.
 * 
 * @author alex
 */
public class Squad extends WorldActor {
	/**
	 * See {@link Javelin#act()}.
	 */
	static public Squad active;

	/**
	 * TODO need to make {@link Monster} serializable
	 */
	public ArrayList<Combatant> members = new ArrayList<Combatant>();
	/** Gold pieces (currency) */
	public int gold = 0;
	/**
	 * {@link Item}s carried by each of the {@link #members}. Since the
	 * {@link BattleAi} doesn't use items having this as a Squad filed is the
	 * best choice performance-wise.
	 */
	public EquipmentMap equipment = new EquipmentMap();
	/**
	 * Start at morning.
	 */
	public long hourselapsed;
	/** See {@link Transport}. */
	public Transport transport = Transport.NONE;
	/**
	 * Last visited town by a squad. Ideally this should always be a
	 * {@link Town} but in practice should never be <code>null</code>.
	 */
	public WorldPlace lasttown = null;

	/**
	 * @param xp
	 *            Starting location (x).
	 * @param yp
	 *            Starting location (y).
	 * @param hourselapsedp
	 *            See {@link #hourselapsed}.
	 * @param lasttownp
	 *            See {@link #lasttown}.
	 */
	public Squad(final int xp, final int yp, final long hourselapsedp,
			WorldPlace lasttownp) {
		x = xp;
		y = yp;
		hourselapsed = hourselapsedp;
		if (Squad.active == null) {
			Squad.active = this;
		}
		lasttown = lasttownp;
	}

	public void disband() {
		ArrayList<WorldActor> squads = getall(Squad.class);
		squads.remove(this);
		if (squads.isEmpty() && Town.train() == null) {
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
			sum += m.source.eat();
		}
		return sum;
	}

	@Override
	public void place() {
		super.place();
		updateavatar();
		if (Game.hero() == null) {
			Game.instance().hero = visual;
		}
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
		transport = Transport.values()[Math.max(transport.ordinal(),
				squad.transport.ordinal())];
		squad.disband();
	}

	@Override
	public void turn(long time, WorldScreen world) {
		paymercenaries();
		eat();
		if (transport == Transport.CARRIAGE) {
			gold -= TransportScreen.MAINTENANCECARRIAGE;
		} else if (transport == Transport.AIRSHIP) {
			gold -= TransportScreen.MAINTENANCEAIRSHIP;
		}
		if (gold < 0) {
			gold = 0;
			transport = Transport.NONE;
		}
	}

	void eat() {
		float foodfound = (survive() - 10) / (4f * members.size());
		if (foodfound > 1) {
			foodfound = 1;
		} else if (foodfound < 0) {
			foodfound = 0;
		}
		float gold = size() / 2f;
		this.gold -= Math.round(Math.ceil(gold * (1 - foodfound)));
	}

	void paymercenaries() {
		for (Combatant c : new ArrayList<Combatant>(members)) {
			if (!c.mercenary) {
				continue;
			}
			int fee = MercenariesGuild.getfee(c);
			if (gold >= fee) {
				gold -= fee;
			} else {
				Game.messagepanel.clear();
				Game.message(
						c + " is not paid, abandons your ranks!\n\nPress ENTER to coninue...",
						null, Delay.NONE);
				while (Game.getInput().getKeyChar() != '\n') {
					// wait for enter
				}
				Game.messagepanel.clear();
				dismiss(c);
			}
		}
	}

	/**
	 * Removes a squad member and returns it to the {@link MercenariesGuild} if
	 * a mercenary.
	 */
	public void dismiss(Combatant c) {
		remove(c);
		MercenariesGuild guild = (MercenariesGuild) WorldPlace
				.getall(MercenariesGuild.class).get(0);
		guild.receive(c);
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
		super.move(tox, toy);
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

	@Override
	public Boolean destroy(Incursion incursion) {
		Game.message("An incursion reaches one of your squads!", null,
				Delay.NONE);
		Incursion.waitforenter();
		Squad.active = this;
		throw new StartBattle(new IncursionFight(incursion));
	}

	/**
	 * @return <code>true</code> if all the squad can fly, <code>false</code> if
	 *         at least one can fly or <code>null</code> otherwise.
	 */
	public Boolean fly() {
		int fliers = 0;
		for (Combatant c : members) {
			if (c.source.fly > 0) {
				fliers += 1;
			}
		}
		if (fliers == 0) {
			return null;
		}
		return fliers == members.size();
	}

	/**
	 * @return a {@link Skills#knowledge} roll.
	 */
	public int know() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.knowledge, m.intelligence);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return a {@link Skills#spot} roll.
	 */
	public int spot() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.spot, m.wisdom);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return a {@link Skills#listen} roll.
	 */
	public int listen() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.listen, m.wisdom);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * Note that this will use the lowest Hide rating found.
	 * 
	 * @return A {@link Skills#hide} roll.
	 */
	public int hide() {
		int worst = Integer.MAX_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.hide, m.dexterity);
			if (roll < worst) {
				worst = roll;
			}
		}
		assert worst != Integer.MAX_VALUE;// TODO remove?
		return worst;
	}

	/**
	 * @return A list with the name of the given {@link Combatant}s, replaced
	 *         with "?" when failed to {@link #spot()} properly.
	 */
	public String spot(List<Combatant> opponent) {
		String garrison = "";
		int spot = spot();
		for (int i = 0; i < opponent.size(); i++) {
			Combatant c = opponent.get(i);
			garrison += (Skills.take10(c.source.skills.hide,
					c.source.dexterity) >= spot ? "?" : c) + ", ";
		}
		return garrison.substring(0, garrison.length() - 2);
	}

	/**
	 * Represents a situation in which a group of hostile {@link Combatant} s is
	 * approaching this group. The foes must first be heard successfully and
	 * then a {@link #hide()} attempt is performed.
	 * 
	 * @return <code>true</code> if hide is successful and player gives input to
	 *         stay hidden (confirmation).
	 */
	public boolean hide(List<Combatant> foes) {
		// needs to pass on a listen check to notice enemy
		int listenroll = Squad.active.listen();
		boolean listen = false;
		for (Combatant foe : foes) {
			if (listenroll >= Skills.take10(foe.source.skills.movesilently,
					foe.source.dexterity)) {
				listen = true;
				break;
			}
		}
		if (!listen) {
			return false; // doesn't hear them coming
		}
		int hideroll = Squad.active.hide();
		for (Combatant foe : foes) {
			if (Skills.take10(foe.source.skills.listen,
					foe.source.wisdom) >= hideroll) {
				return false; // spotted!
			}
		}
		// hidden
		char input = ' ';
		while (input != 'w' && input != 'a') {
			input = InfoScreen
					.prompt("You have hidden from a group of enemies!\nPress w to wait for them to go away or a to attack...\n\nEnemies: "
							+ Squad.active.spot(foes));
		}
		return input == 'w';
	}

	/**
	 * The squad tries to parley with the enemy {@link Combatant}s, possibly
	 * bribing or hirimg them to avoid the fight.
	 * 
	 * @return <code>false</code> if the fight is to proceed.
	 */
	public boolean bribe(List<Combatant> foes) {
		boolean intelligent = false;
		for (Combatant c : foes) {
			if (Monster.getbonus(c.source.intelligence) >= -1) {
				intelligent = true;
				break;
			}
		}
		if (!intelligent) {
			return false;
		}
		int diplomacyroll = negotiate() - 10;
		int highest = Integer.MIN_VALUE;
		int dailyfee = 0;
		for (Combatant foe : foes) {
			int will = foe.source.will();
			if (will > diplomacyroll) {
				return false;// no deal!
			}
			if (will > highest) {
				highest = will;
			}
			dailyfee += MercenariesGuild.getfee(foe);
		}
		final int bribe = Math.max(1, RewardCalculator.receivegold(foes) / 2);
		final boolean canhire = diplomacyroll - highest >= 5;
		return new BribingScreen().bribe(foes, dailyfee, bribe, canhire);
	}

	/**
	 * @return A {@link Skills#diplomacy} roll.
	 */
	public int negotiate() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.diplomacy, m.charisma);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return roll of {@link Skills#search}.
	 */
	public int search() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.search, m.intelligence);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return roll of {@link Skills#disabledevice}
	 */
	public int disarm() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.disabledevice, m.intelligence);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return roll of {@link Skills#gatherinformation}.
	 */
	public int gossip() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.gatherinformation, m.charisma);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}

	/**
	 * @return a roll of {@link Skills#survival}.
	 */
	public int survive() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.survival, m.wisdom);
			if (roll > best) {
				best = roll;
			}
		}
		assert best != Integer.MIN_VALUE;// TODO remove?
		return best;
	}
}
