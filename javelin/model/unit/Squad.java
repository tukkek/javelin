package javelin.model.unit;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.Work;
import javelin.controller.action.world.WorldMove;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.IncursionFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.model.EquipmentMap;
import javelin.model.item.Item;
import javelin.model.unit.transport.Transport;
import javelin.model.world.Improvement;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.Images;
import javelin.view.screen.BribingScreen;
import javelin.view.screen.WorldScreen;

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

	/** Which units are in this squad. */
	public ArrayList<Combatant> members = new ArrayList<Combatant>();
	/** Gold pieces (currency). 1GP = 10 silver pieces. 1SP=10 copper pieces. */
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
	public Transport transport = null;
	/**
	 * Last visited town by a squad. Ideally this should always be a
	 * {@link Town} but in practice should never be <code>null</code>.
	 */
	public Town lasttown = null;

	/** See {@link Work}. */
	public Improvement work = null;

	/**
	 * If <code>true</code> will skip all possible combats and resolve them
	 * automatically.
	 * 
	 * TODO make it and {@link Combatant#automatic} super easy to change in the
	 * UI.
	 */
	public boolean strategic = false;
	/** Terrain type this squad is coming from after movement. */
	public Terrain lastterrain = null;

	transient private Image image = null;

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
			Town lasttownp) {
		x = xp;
		y = yp;
		hourselapsed = hourselapsedp;
		if (Squad.active == null) {
			Squad.active = this;
		}
		lasttown = lasttownp;
	}

	/**
	 * Removes this squad from the game, likely triggering
	 * {@link Javelin#lose()}.
	 */
	public void disband() {
		ArrayList<WorldActor> squads = getall(Squad.class);
		squads.remove(this);
		if (squads.isEmpty() && Town.train() == null && !Academy.train()) {
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

	/**
	 * @return The sum of {@link Monster#eat()} for this squad.
	 */
	public float eat() {
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

	/**
	 * Updates {@link WorldActor#visual}, taking {@link #transport} into
	 * account.
	 * 
	 * @return
	 */
	public void updateavatar() {
		if (members.isEmpty()) {
			return;
		}
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
		if (transport != null && Dungeon.active == null) {
			dummy = dummy.clone();
			dummy.avatarfile = transport.name.replaceAll(" ", "").toLowerCase();
		}
		visual.combatant = new Combatant(visual, dummy, false);
	}

	@Override
	public Image getimage() {
		try {
			image = Images.getImage(visual.combatant.source.avatarfile);
		} catch (NullPointerException e) {
			// TODO fix ^
		}
		return image;
	}

	/**
	 * @param s
	 *            Takes this squad, {@link #disband()} it and join with the
	 *            current instance.
	 */
	public void join(final Squad s) {
		members.addAll(s.members);
		gold += s.gold;
		hourselapsed = Math.max(hourselapsed, s.hourselapsed);
		for (final Combatant m : s.members) {
			equipment.put(m.id, s.equipment.get(m.id));
		}
		if (transport == null || (s.transport != null
				&& s.transport.speed > transport.speed)) {
			transport = s.transport;
		}
		strategic = strategic && s.strategic;
		s.disband();
	}

	@Override
	public void turn(long time, WorldScreen world) {
		paymercenaries();
		float foodfound = (survive() - 10) / (4f * members.size());
		if (foodfound > 1) {
			foodfound = 1;
		} else if (foodfound < 0) {
			foodfound = 0;
		}
		gold -= Math.round(Math.ceil(eat() / 2f * (1 - foodfound)));
		if (gold < 0) {
			gold = 0;
		}
		if (transport != null) {
			transport.keep(this);
		}
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
		members.remove(c);
		for (Item i : equipment.get(c.id)) {
			i.grab();
		}
		remove(c);
		MercenariesGuild guild = (MercenariesGuild) Location
				.getall(MercenariesGuild.class).get(0);
		guild.receive(c);
	}

	/**
	 * @param member
	 *            Adds this unit to {@link #members}.
	 * @param equipmentp
	 *            Unit's equipment to be added to {@link #equipment}.
	 */
	public void add(Combatant member, ArrayList<Item> equipmentp) {
		members.add(member);
		equipment.put(member.id, equipmentp);
	}

	/**
	 * @param c
	 *            Removes this unit and {@link #disband()} if necessary.
	 */
	public void remove(Combatant c) {
		members.remove(c);
		equipment.clean(this);
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

	/**
	 * Use {@link Item#grab()} instead
	 * 
	 * @param key
	 *            Adds this item to a random {@link #equipment} bag.
	 */
	@Deprecated
	public void receiveitem(Item key) {
		equipment.add(key, this);
	}

	@Override
	public Boolean destroy(Incursion incursion) {
		Javelin.message("An incursion reaches one of your squads!", true);
		Squad.active = this;
		throw new StartBattle(new IncursionFight(incursion));
	}

	/**
	 * @return <code>true</code> if all the squad can fly, <code>false</code> if
	 *         at least one can fly or <code>null</code> otherwise.
	 */
	public boolean fly() {
		if (Squad.active.transport != null && Squad.active.transport.flies) {
			return true;
		}
		int fliers = 0;
		for (Combatant c : members) {
			if (c.source.fly > 0) {
				fliers += 1;
			}
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
		return best;
	}

	/**
	 * @return a {@link Skills#perception} roll.
	 */
	public int perceive(boolean flyingbonus) {
		int best = Integer.MIN_VALUE;
		for (int i = 0; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.perceive(flyingbonus), m.wisdom);
			if (roll > best) {
				best = roll;
			}
		}
		return best;
	}

	/**
	 * Note that this will use the lowest Hide rating found.
	 * 
	 * @return A {@link Skills#hide} roll.
	 * @see #hide(List)
	 */
	public int hide() {
		int worst = Integer.MAX_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = Skills.take10(m.skills.stealth, m.dexterity);
			if (roll < worst) {
				worst = roll;
			}
		}
		return worst;
	}

	/**
	 * @return A list with the name of the given {@link Combatant}s, replaced
	 *         with "?" when failed to {@link #perceive()} properly.
	 */
	public String spot(List<Combatant> opponent) {
		String garrison = "";
		int spot = perceive(false);
		for (int i = 0; i < opponent.size(); i++) {
			Combatant c = opponent.get(i);
			garrison += (Skills.take10(c.source.skills.stealth,
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
		int listenroll = Squad.active.perceive(Dungeon.active == null
				&& !Terrain.current().equals(Terrain.FOREST));
		boolean listen = false;
		for (Combatant foe : foes) {
			if (listenroll >= Skills.take10(foe.source.skills.stealth,
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
			if (Skills.take10(foe.source.perceive(false),
					foe.source.wisdom) >= hideroll) {
				return false; // spotted!
			}
		}
		// hidden
		char input = ' ';
		while (input != 'w' && input != 's') {
			input = Javelin
					.prompt("You have hidden from a group of enemies!\nPress s to storm them or w to wait for them to go away...\n\nEnemies: "
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
			if (c.source.think(null)) {
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
		return best;
	}

	/**
	 * @return roll of {@link Skills#disabledevice}
	 */
	public int disarm() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = m.disarm();
			if (roll > best) {
				best = roll;
			}
		}
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
		return best;
	}

	/**
	 * Takes 10.
	 * 
	 * @return a roll of {@link Skills#survival}.
	 */
	public int survive() {
		int best = Integer.MIN_VALUE;
		for (int i = 1; i < members.size(); i++) {
			Monster m = members.get(i).source;
			int roll = m.survive();
			if (roll > best) {
				best = roll;
			}
		}
		return best;
	}

	/**
	 * @param timecost
	 *            Updates {@link #hourselapsed} and any side-effects.
	 */
	public void ellapse(int timecost) {
		if (timecost == 0) {
			return;
		}
		hourselapsed += timecost;
		for (Combatant c : new ArrayList<Combatant>(members)) {
			c.terminateconditions(timecost);
		}
	}

	/**
	 * @return Like {@link #speed()} but return time in hours.
	 */
	public float move(boolean ellapse, Terrain t) {
		float hours =
				WorldMove.TIMECOST * (30f * WorldMove.NORMALMARCH) / speed(t);
		if (hours < 1) {
			hours = 1;
		}
		if (ellapse) {
			ellapse(Math.round(hours));
		}
		return hours;
	}

	/**
	 * @return The land speed movement overland in miles per hour. This is the
	 *         amount covered in an hour but the correspoinding movement per day
	 *         is less since it has to account for sleep, resting, etc.
	 */
	public int speed(Terrain t) {
		int snow = t.getweather() == Terrain.SNOWING ? 2 : 1;
		if (transport != null) {
			int transportspeed = transport.getspeed(members) / snow;
			return transport.flies ? transportspeed : t.speed(transportspeed);
		}
		int speed = Integer.MAX_VALUE;
		boolean allfly = true;
		for (Combatant c : members) {
			Monster m = c.source;
			speed = Math.min(speed, Terrain.WATER.equals(t)
					? Math.max(m.fly, m.swim) : m.gettopspeed());
			if (m.fly == 0) {
				allfly = false;
			}
		}
		return Math.round(WorldMove.NORMALMARCH
				* ((allfly ? speed : t.speed(speed))) / snow);
	}

	/**
	 * Finishes the current {@link #work}.
	 */
	public void build() {
		Location built = work.done(x, y);
		if (built != null) {
			built.place();
			built.garrison.clear();
			built.realm = null;
			if (built instanceof Fortification) {
				Fortification f = (Fortification) built;
				f.targetel = 1;
			}
		}
		if (members.size() > 0 && WorldActor.get(x, y) != this) {
			visual.remove();
			displace();
			place();
		}
		String extra = work.inform();
		extra = extra == null ? "" : "\n" + extra;
		Javelin.message(
				"Work finished: " + work.name.toLowerCase() + "!" + extra,
				true);
		Game.messagepanel.clear();
		work = null;
	}

	/**
	 * A squad cannot move on water if any of the combatants can't swim.
	 * Alternatively a boat or airship will let non-swimming squads move on
	 * water.
	 * 
	 * @return <code>true</code> if this squad can move on water.
	 * @see Monster#swim()
	 */
	public boolean swim() {
		if (transport != null && (transport.sails || transport.flies)) {
			return true;
		}
		for (Combatant c : members) {
			if (c.source.swim() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Discover a {@link World} area in a radius around current position.
	 * 
	 * @param vision
	 *            Perceive roll with circumstance bonuses.
	 * @see WorldScreen#DISCOVERED
	 * @see Squad#perceive(boolean)
	 */
	public void view(int vision) {
		Outpost.discover(Squad.active.x, Squad.active.y,
				Math.round(Math.round(Math.floor(vision / 5f))));
	}

	@Override
	public List<Combatant> getcombatants() {
		return members;
	}

	@Override
	public String describe() {
		String members = "";
		for (Combatant c : this.members) {
			members += c + ", ";
		}
		return "Squad (" + members.substring(0, members.length() - 2) + ")";
	}
}
