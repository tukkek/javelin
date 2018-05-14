package javelin.model.unit;

import java.awt.Image;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.CombatantHealthComparator;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.comparator.SpellLevelComparator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.IncursionFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.model.EquipmentMap;
import javelin.model.item.Item;
import javelin.model.transport.Transport;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.Resource;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.BribingScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * A group of units that the player controls as a overworld game unit. If a
 * player loses all his squads the game ends.
 *
 * TODO when this breaks the 1000 line limit an easy fix is to turn
 * {@link #members} into a SquadMemmbers class. See {@link #sort()}.
 *
 * @author alex
 */
public class Squad extends Actor implements Cloneable {
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

	/**
	 * <code>false</code> will never prompt to skip battles.
	 */
	public Boolean strategic = false;
	/** Terrain type this squad is coming from after movement. */
	public Terrain lastterrain = null;

	transient private Image image = null;

	/** @see Resource */
	public int resources = 0;

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
		ArrayList<Actor> squads = World.getall(Squad.class);
		squads.remove(this);
		if (squads.isEmpty()) {
			Javelin.lose();
		}
		if (Squad.active == this) {
			Squad.active = Javelin.nexttoact();
		}
		if (Dungeon.active != null) {
			Dungeon.active.leave();
		}
	}

	@Override
	public void place() {
		super.place();
		updateavatar();
	}

	/**
	 * Updates {@link Actor#visual}, taking {@link #transport} into account.
	 *
	 * @return
	 */
	public void updateavatar() {
		if (members.isEmpty()) {
			return;
		}
		if (transport != null && Dungeon.active == null) {
			image = Images
					.getImage(transport.name.replaceAll(" ", "").toLowerCase());
			return;
		}
		Combatant leader = null;
		for (Combatant c : members) {
			if (leader == null || c.source.cr > leader.source.cr) {
				leader = c;
			}
		}
		image = Images.getImage(leader.source.avatarfile);
	}

	@Override
	public Image getimage() {
		updateavatar();
		return image;
	}

	/**
	 * Sorts alphabetically, with all mercenaries to the final of the
	 * {@link #members} list, making it easier to overcome some current UI
	 * limitations.
	 *
	 * TODO this shouldn't be public but ensure by the architecure. A solution
	 * would be to make {@link #members} a class of its own (not a List) which
	 * exposes a singgle add methods like {@link #add(Combatant)} and
	 * {@link #add(Combatant, List)}. This needs some rewriting in the code and
	 * also making sure Cloning works as intended both for shallow and deep
	 * oprateions.
	 */
	public void sort() {
		members.sort(CombatantsByNameAndMercenary.SINGLETON);
	}

	/**
	 * @param s
	 *            Takes this squad, {@link #disband()} it and join with the
	 *            current instance.
	 */
	public void join(final Squad s) {
		members.addAll(s.members);
		sort();
		gold += s.gold;
		resources += s.resources;
		hourselapsed = Math.max(hourselapsed, s.hourselapsed);
		for (final Combatant m : s.members) {
			equipment.put(m.id, s.equipment.get(m.id));
		}
		if (transport == null
				|| s.transport != null && s.transport.speed > transport.speed) {
			transport = s.transport;
		}
		strategic = strategic && s.strategic;
		s.disband();
	}

	/**
	 * @return The sum of {@link Monster#size()} for this squad.
	 */
	public float eat() {
		float sum = 0;
		for (final Combatant c : members) {
			sum += c.source.eat();
		}
		return sum;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		paymercenaries();
		int surival = Skill.SURVIVAL.getbonus(getbest(Skill.SURVIVAL));
		float foodfound = surival / (4f * members.size());
		if (foodfound > 1) {
			foodfound = 1;
		} else if (foodfound < 0) {
			foodfound = 0;
		}
		gold -= Math.round(Math.ceil(eat() * (1 - foodfound)));
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
						Delay.NONE);
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
		for (MercenariesGuild g : sortbydistance(
				MercenariesGuild.getguilds())) {
			if (g.all.contains(c)) {
				g.receive(c);
				return;
			}
		}
	}

	/**
	 * Will automatically {@link #sort()} after inclusion..
	 *
	 * @param member
	 *            Adds this unit to {@link #members}.
	 * @param equipmentp
	 *            Unit's equipment to be added to {@link #equipment}.
	 */
	public void add(Combatant member, List<Item> equipmentp) {
		members.add(member);
		sort();
		equipment.put(member.id, new ArrayList<Item>(equipmentp));
	}

	/**
	 * Like {@link #add(Combatant, List)} but adds an empty item list.
	 */
	public void add(Combatant c) {
		add(c, new ArrayList<Item>(0));
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
		equipment.additem(key, this);
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
	 * @param periodbonus
	 * @return a {@link Skills#perception} roll.
	 */
	public int perceive(boolean flyingbonus, boolean weatherpenalty,
			boolean periodbonus) {
		return perceive(flyingbonus, weatherpenalty, periodbonus, members);
	}

	public static int perceive(boolean flyingbonus, boolean weatherpenalty,
			boolean periodbonus, List<Combatant> squad) {
		int best = Integer.MIN_VALUE;
		for (Combatant c : squad) {
			int roll = 10
					+ c.perceive(flyingbonus, weatherpenalty, periodbonus);
			if (roll > best) {
				best = roll;
			}
		}
		return best;
	}

	/**
	 * Spot a group of enemies.
	 *
	 * @param target
	 *            If not <code>null</code>, will only shown information if
	 *            adjacent in the {@link WorldScreen}.
	 * @return A list with the name of the given {@link Combatant}s, replaced
	 *         with "?" when failed to {@link #perceive()} properly.
	 */
	public String spotenemies(List<Combatant> opponents, Actor target) {
		if (target != null && distanceinsteps(target.x, target.y) > 1) {
			return "?";
		}
		String garrison = "";
		int spot = perceive(true, true, true);
		for (int i = 0; i < opponents.size(); i++) {
			Combatant c = opponents.get(i);
			boolean spotted = spot >= c.roll(Skill.STEALTH);
			garrison += (spotted ? c : "?") + ", ";
		}
		garrison = garrison.substring(0, garrison.length() - 2);
		return garrison;
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
		boolean outside = Dungeon.active == null;
		boolean flying = outside && !Terrain.current().equals(Terrain.FOREST);
		int listenroll = Squad.active.perceive(flying, outside, outside);
		boolean listen = false;
		for (Combatant foe : foes) {
			if (listenroll >= foe.taketen(Skill.STEALTH)) {
				listen = true;
				break;
			}
		}
		if (!listen) {
			return false; // doesn't hear them coming
		}
		int hideroll = getworst(Skill.STEALTH).roll(Skill.STEALTH);
		for (Combatant foe : foes) {
			if (10 + foe.perceive(flying, outside, outside) >= hideroll) {
				return false; // spotted!
			}
		}
		// hidden
		char input = ' ';
		final String prompt = "You have hidden from a "
				+ Difficulty.describe(foes)
				+ " group of enemies!\n"
				+ "Press s to storm them or w to wait for them to go away...\n\n"
				+ "Enemies: " + Squad.active.spotenemies(foes, null);
		while (input != 'w' && input != 's') {
			input = Javelin.prompt(prompt);
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
			if (c.source.think(-1)) {
				intelligent = true;
				break;
			}
		}
		if (!intelligent) {
			return false;
		}
		int diplomac = getbest(Skill.DIPLOMACY).roll(Skill.DIPLOMACY);
		int highest = Integer.MIN_VALUE;
		int dailyfee = 0;
		for (Combatant foe : foes) {
			int will = foe.source.will();
			if (RPG.r(1, 20) + will >= diplomac) {
				return false;// no deal!
			}
			if (will > highest) {
				highest = will;
			}
			dailyfee += MercenariesGuild.getfee(foe);
		}
		final int bribe = Math.max(1, RewardCalculator.receivegold(foes) / 2);
		final boolean canhire = diplomac >= highest + 5;
		boolean b = new BribingScreen().bribe(foes, dailyfee, bribe, canhire);
		Javelin.app.switchScreen(BattleScreen.active);
		return b;
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
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @return Like {@link #speed()} but return time in hours.
	 */
	public float move(boolean ellapse, Terrain t, int x, int y) {
		float hours = WorldMove.TIMECOST * (30f * WorldMove.NORMALMARCH)
				/ speed(t, x, y);
		if (hours < 1) {
			hours = 1;
		}
		if (ellapse) {
			ellapse(Math.round(hours));
		}
		return hours;
	}

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @return The land speed movement overland in miles per hour. This is the
	 *         amount covered in an hour but the correspoinding movement per day
	 *         is less since it has to account for sleep, resting, etc.
	 */
	public int speed(Terrain t, int x, int y) {
		int snow = t.getweather() == Terrain.SNOWING ? 2 : 1;
		if (transport != null) {
			int transportspeed = transport.getspeed(members) / snow;
			return transport.flies ? transportspeed
					: t.speed(transportspeed, x, y);
		}
		int speed = Integer.MAX_VALUE;
		boolean allfly = true;
		for (Combatant c : members) {
			Monster m = c.source;
			speed = Math.min(speed, Terrain.WATER.equals(t)
					? Math.max(m.fly, m.swim) : c.gettopspeed(null));
			if (m.fly == 0) {
				allfly = false;
			}
		}
		return Math.round(WorldMove.NORMALMARCH
				* (allfly ? speed : t.speed(speed, x, y)) / snow);
	}

	/**
	 * A squad cannot move on water if any of the combatants can't swim.
	 * Alternatively a boat or airship will let non-swimming squads move on
	 * water.
	 *
	 * @return <code>true</code> if this squad can move on water.
	 * @see #MISSING()
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
	 * @see WorldScreen#discovered
	 * @see Squad#perceive(boolean)
	 */
	public void seesurroundings(int vision) {
		Outpost.discover(Squad.active.x, Squad.active.y,
				Math.max(1, vision / 5));
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

	/**
	 * @return All squads in the {@link World}.
	 */
	public static ArrayList<Squad> getsquads() {
		ArrayList<Actor> actors = World.getall(Squad.class);
		ArrayList<Squad> squads = new ArrayList<Squad>(actors.size());
		for (Actor a : actors) {
			squads.add((Squad) a);
		}
		return squads;
	}

	public boolean skipcombat(int diffifculty) {
		if (!strategic) {
			return false;
		}
		Character input = ' ';
		while (input != '\n' && input != 's') {
			Javelin.app.switchScreen(BattleScreen.active);
			final String difficulty = Difficulty
					.describe(diffifculty);
			final String prompt = "Do you want to skip this " + difficulty
					+ " battle?\n\n" //
					+ "Press ENTER to open the battle screen.\n"
					+ "Press s to skip it and calculate results automatically.";
			input = Javelin.prompt(prompt);
		}
		return input == 's';
	}

	public void seesurroudings() {
		seesurroundings(Squad.active.perceive(true, true, true)
				+ (Squad.active.transport == Transport.AIRSHIP ? +4
						: Terrain.current().visionbonus));
	}

	public static void updatevision() {
		for (Squad s : Squad.getsquads()) {
			s.seesurroudings();
		}
	}

	/**
	 * prevents players from cheating the strategic combat system by never
	 * buying items, which would have no effect in the outcome of battle.
	 *
	 * @see StartBattle
	 */
	public String wastegold(float resourcesused) {
		int spent = Math.round(Squad.active.gold * resourcesused);
		if (spent <= 0) {
			return "";
		}
		Squad.active.gold -= spent;
		return "$" + SelectScreen.formatcost(spent) + " in resources lost.\n\n";
	}

	@Override
	protected boolean cancross(int tox, int toy) {
		return swim() || super.cancross(tox, toy);
	}

	@Override
	public Squad clone() {
		try {
			/* shallow clone */
			return (Squad) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Daily mercenary cost in gold.
	 * @see Combatant#mercenary
	 */
	public int getupkeep() {
		int upkeep = 0;
		for (Combatant c : members) {
			if (c.mercenary) {
				upkeep += MercenariesGuild.getfee(c);
			}
		}
		return upkeep;
	}

	/**
	 * 100XP = 1CR.
	 *
	 * @return Total of XP between all active {@link Squad} members.
	 */
	public int sumxp() {
		BigDecimal sum = new BigDecimal(0);
		for (Combatant c : Squad.active.members) {
			sum = sum.add(c.xp);
		}
		return Math.round(sum.floatValue() * 100);
	}

	/**
	 * @return <code>true</code> if can use any available spell to heal
	 *         {@link #members}.
	 * @see #quickheal()
	 */
	public boolean canheal() {
		for (Spell s : getavailablespells()) {
			for (Combatant c : members) {
				if (s.canheal(c)) {
					return true;
				}
			}
		}
		return false;

	}

	ArrayList<Spell> getavailablespells() {
		ArrayList<Spell> available = new ArrayList<Spell>();
		for (Combatant c : members) {
			for (Spell s : c.spells) {
				if (!s.exhausted()) {
					available.add(s);
				}
			}
		}
		return available;
	}

	/**
	 * Uses available spells to heal your party.
	 *
	 * @see #canheal()
	 */
	public void quickheal() {
		ArrayList<Combatant> members = new ArrayList<Combatant>(this.members);
		members.sort(CombatantHealthComparator.SINGLETON);
		ArrayList<Spell> spells = getavailablespells();
		spells.sort(SpellLevelComparator.SINGLETON);
		for (Spell s : spells) {
			for (Combatant c : members) {
				while (s.canheal(c) && !s.exhausted()) {
					s.castpeacefully(null, c);
					s.used += 1;
				}
			}
		}
	}

	@Override
	public Integer getel(int attackerel) {
		return ChallengeCalculator.calculateel(members);
	}

	/**
	 * @return A new list contianing all mercenaries in this squad.
	 * @see Combatant#mercenary
	 */
	public ArrayList<Combatant> getmercenaries() {
		ArrayList<Combatant> mercenaries = new ArrayList<Combatant>();
		for (Combatant c : members) {
			if (c.mercenary) {
				mercenaries.add(c);
			}
		}
		return mercenaries;
	}

	public Combatant getbest(Skill s) {
		Combatant best = null;
		for (Combatant c : Squad.active.members) {
			if (best == null || s.getbonus(c) > s.getbonus(best)) {
				best = c;
			}
		}
		return best;
	}

	public Combatant getworst(Skill s) {
		Combatant worst = null;
		for (Combatant c : Squad.active.members) {
			if (worst == null || s.getbonus(c) < s.getbonus(worst)) {
				worst = c;
			}
		}
		return worst;
	}

	/**
	 * @return The highest take 10 roll of the current {@link Squad}. Creatures
	 *         that are injured or worse won't be able to heal others. If nobody
	 *         can heal, returns {@link Integer#MIN_VALUE}.
	 *
	 * @see Combatant#getstatus()
	 */
	public int heal() {
		int heal = Integer.MIN_VALUE;
		for (Combatant c : Squad.active.members) {
			if (c.getnumericstatus() > Combatant.STATUSINJURED) {
				heal = Math.max(heal, c.taketen(Skill.HEAL));
			}
		}
		return heal;
	}
}
