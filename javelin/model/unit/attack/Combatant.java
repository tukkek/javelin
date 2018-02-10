package javelin.model.unit.attack;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.Point;
import javelin.controller.SpellbookGenerator;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.upgrade.BreathUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.walker.Walker;
import javelin.model.Cloneable;
import javelin.model.Realm;
import javelin.model.TeamContainer;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Conditions;
import javelin.model.unit.CurrentAttack;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Disciplines;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.serpent.TearingFang.Bleeding;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Spells;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Condition.Effect;
import javelin.model.unit.condition.Melding;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.GreatCleave;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * A Combatant is an in-game unit, like the enemies in the battlefield or the
 * named player characters. It contains the data that changes frequently during
 * the course of battle, speeding up the cloning process for BattleNode
 * replication by sharing the same reference to {@link Monster} among cloned
 * instances.
 *
 * @author alex
 */
public class Combatant implements Serializable, Cloneable {
	/** TODO turn into {@link Enum}. */
	public static final int STATUSDEAD = -2;
	public static final int STATUSUNCONSCIOUS = -1;
	public static final int STATUSDYING = 0;
	public static final int STATUSINJURED = 1;
	public static final int STATUSWOUNDED = 2;
	public static final int STATUSHURT = 3;
	public static final int STATUSSCRATCHED = 4;
	public static final int STATUSUNHARMED = 5;
	/** TODO proper dying process + healing phase at the end of combat */
	public static final int DEADATHP = -8;
	/** The statistics for this monster. */
	public Monster source;
	/**
	 * Action points. 1 represent a full-round action, .5 a move-equivalent or
	 * attack action and so on.
	 */
	public float ap = 0;
	/**
	 * XY coordenates. Better used as an array since this enables more
	 * programmatic way of handling directions, like deltas.
	 */
	public int[] location = new int[2];
	static private int ids = 1;
	/** Last time {@link #refresh()} was invoked. */
	public float lastrefresh = -Float.MAX_VALUE;
	/** TODO implement this as a {@link Condition} instead. */
	public float initialap;
	/** Current hit points. */
	public int hp;
	/** Maxium hitpoints. */
	public int maxhp;
	/**
	 * Unique identifier of unit. Several different units (with unique ids) can
	 * share a same {@link #source} with a same id.
	 */
	public int id = STATUSUNCONSCIOUS;
	/**
	 * Which of the current melee attack sequences is being used and how far it
	 * is in this sequence.
	 */
	public CurrentAttack currentmelee = new CurrentAttack();
	/** See {@link #currentmelee}. */
	public CurrentAttack currentranged = new CurrentAttack();
	/** Temporary modifier to {@link Monster#ac}. */
	public int acmodifier = 0;
	/** List of current active {@link Condition}s on this unit. */
	private Conditions conditions = new Conditions();
	/** See {@link Discipline}. */
	public Disciplines disciplines = new Disciplines();
	/**
	 * Canonical representation of the spells this unit has.
	 *
	 * @see Monster#spells
	 * @see Monster#spellcr
	 */
	public Spells spells = new Spells();
	/**
	 * XP in CR, you'll want to multiply by 100 and round to show the player.
	 * 
	 * @see #learn(float)
	 */
	public BigDecimal xp = new BigDecimal(0);
	public boolean summoned = false;
	public ArrayList<Artifact> equipped = new ArrayList<Artifact>(0);
	/** See {@link MercenariesGuild} */
	public boolean mercenary = false;
	/** See {@link Monster#burrow}. */
	public boolean burrowed = false;
	/** Is a player unit that should be controlled by {@link BattleAi}. */
	public boolean automatic = false;

	/**
	 * @param generatespells
	 *            if true will create spells for this monster based on his
	 *            {@link Monster#spellcr}. This is essentially a temporary
	 *            measure to allow 1.0 monster who would have spell powers to
	 *            use the currently implemented spells TODO
	 */
	public Combatant(final Monster sourcep, boolean generatespells) {
		super();
		source = sourcep.clone();
		newid();
		ap = 0;
		hp = source.hd.roll(source);
		maxhp = hp;
		for (Feat f : source.feats) {
			f.update(this);
		}
		for (Spell s : source.spells) {
			spells.add(s.clone());
		}
		if (generatespells && source.spellcr > 0) {
			SpellbookGenerator.generate(this);
		}
	}

	/**
	 * Generates an unique identity number for this Combatant.
	 *
	 * @see Actor#getcombatants()
	 */
	public void newid() {
		ids += 1;
		while (checkidcollision()) {
			ids += 1;
		}
		id = ids;
	}

	/**
	 * TODO add WorldActor#getcombatants to do this
	 */
	private boolean checkidcollision() {
		if (World.seed == null) {
			return false;
		}
		for (Actor a : World.getactors()) {
			List<Combatant> combatants = a.getcombatants();
			if (combatants != null) {
				for (Combatant c : combatants) {
					if (c.id == ids) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Combatant clone() {
		try {
			final Combatant c = (Combatant) super.clone();
			c.currentmelee = currentmelee.clone();
			c.currentranged = currentranged.clone();
			c.location = location.clone();
			c.conditions = conditions.clone();
			c.spells = (Spells) spells.clone();
			c.disciplines = disciplines.clone();
			c.xp = c.xp.add(new BigDecimal(0));
			if (c.equipped != null) {
				c.equipped = (ArrayList<Artifact>) c.equipped.clone();
			}
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Rolls for hit and applies damage.
	 *
	 * @param targetCombatant
	 *            The monster being attacked.
	 */
	public void meleeattacks(Combatant targetCombatant, BattleState state) {
		Combatant current = state.clone(this);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(MeleeAttack.SINGLETON.attack(state, this,
				targetCombatant,
				current.chooseattack(current.source.melee, targetCombatant),
				0));
	}

	public void rangedattacks(Combatant targetCombatant, BattleState state) {
		Combatant current = state.clone(this);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(RangedAttack.SINGLETON.attack(state, this,
				targetCombatant,
				current.chooseattack(current.source.ranged, targetCombatant),
				0));
	}

	public List<AttackSequence> getattacks(final boolean melee) {
		return melee ? source.melee : source.ranged;
	}

	public boolean isally(final Combatant c, final TeamContainer tc) {
		return getteam(tc) == c.getteam(tc);
	}

	public List<Combatant> getteam(final TeamContainer tc) {
		final List<Combatant> redTeam = tc.getredTeam();
		return redTeam.contains(this) ? redTeam : tc.getblueTeam();
	}

	@Override
	public String toString() {
		return source.toString();
	}

	public boolean hasattacktype(final boolean meleeOnly) {
		return !getattacks(meleeOnly).isEmpty();
	}

	public void checkAttackType(final boolean meleeOnly) {
		if (!hasattacktype(meleeOnly)) {
			Game.message("No " + (meleeOnly ? "mẽlée" : "ranged") + " attacks.",
					Delay.WAIT);
			throw new RepeatTurn();
		}
	}

	public void refresh() {
		if (lastrefresh == -Float.MAX_VALUE) {
			lastrefresh = ap;
		} else if (ap != lastrefresh) {
			ap -= .01;
			final float turns = ap - lastrefresh;
			if (source.fasthealing > 0) {
				heal(Math.round(source.fasthealing * turns), false);
			}
			/*
			 * don't clone the list here or you'll be acting on different
			 * Conditions than the ones on this Combatant instance!
			 */
			for (Condition c : new ArrayList<Condition>(conditions)) {
				/*
				 * this second check is needed because some conditions like
				 * Bleeding may remove other conditions during this loop
				 */
				if (conditions.contains(c)) {
					c.expireinbattle(this);
				}
			}
			lastrefresh = ap;
		}
	}

	/**
	 * @return If the unit is being affected by the given condition type,
	 *         returns its instance - otherwise <code>null</code>.
	 */
	public Condition hascondition(Class<? extends Condition> clazz) {
		for (Condition c : conditions) {
			if (c.getClass().equals(clazz)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Theoretically total reduction should be allowed but since we're making
	 * all energy resistance universal and all damage reduction impenetrable
	 * this is a measure to avoid monsters from becoming invincible.
	 */
	public void damage(final int damagep, BattleState s, final int reduce) {
		if (reduce == Integer.MAX_VALUE) {
			return;
		}
		int damage = damagep - reduce;
		if (damage < 1) {
			damage = 1;
		}
		final int tenth = damagep / 10;
		if (damage < tenth) {
			damage = tenth;
		}
		hp -= damage;
		if (hp <= 0) {
			Javelin.app.fight.die(this, s);
		}
	}

	public CurrentAttack getcurrentattack(List<AttackSequence> attacktype) {
		return attacktype == source.melee ? currentmelee : currentranged;
	}

	/**
	 * % are against AC only, no bonuses included except attack bonus
	 */
	public CurrentAttack chooseattack(List<AttackSequence> attacktype,
			Combatant target) {
		CurrentAttack currentattack = getcurrentattack(attacktype);
		if (currentattack.continueattack()) {
			return currentattack;
		}
		int attackindex = 0;
		if (attacktype.size() > 1) {
			ArrayList<String> attacks = new ArrayList<String>();
			for (AttackSequence sequence : attacktype) {
				attacks.add(sequence.toString(target));
			}
			attackindex = Javelin.choose("Start which attack sequence?",
					attacks, false, false);
			if (attackindex == STATUSUNCONSCIOUS) {
				Game.messagepanel.clear();
				throw new RepeatTurn();
			}
		}
		currentattack.setcurrent(attackindex, attacktype);
		return currentattack;
	}

	/**
	 * @return 0 if not surprised or the corresponding AC penalty if
	 *         flat-footed.
	 */
	public int surprise() {
		if (ap > initialap) {
			return 0;
		}
		final int dexbonus = Monster.getbonus(source.dexterity);
		return dexbonus > 0 ? -dexbonus : 0;
	}

	/**
	 * Rolls a d20, sums {@link Monster#initiative} and initializes {@link #ap}
	 * according to the result.
	 * 
	 * This also adds a random positive or negative value (below 1% of an action
	 * point) to help keep the initiative order stable by preventing collisions
	 * (such as 2 units having exactly 0 {@link #ap}). Given that AP values are
	 * padded to the user and that normal initiative works in 5% steps and
	 * normal actions don't usually go finer than 10% AP cost, this is entirely
	 * harmless when it comes to game balance.
	 * 
	 * If you want an unit to enter battle mid-way, you can set a starting value
	 * to {@link #ap} representing the current point in time and then call this
	 * method.
	 */
	public void rollinitiative() {
		ap = -(RPG.r(1, 20) + source.initiative) / 20f;
		ap += RPG.r(-444, +444) / 100000f;
		initialap = ap;
		lastrefresh = -Float.MAX_VALUE;
	}

	public int getnumericstatus() {
		int maxhp = getmaxhp();
		if (hp >= maxhp) {
			return 5;
		}
		if (hp == 1) {
			return 0;
		}
		if (hp <= 0) {
			return hp > Combatant.DEADATHP ? STATUSUNCONSCIOUS : STATUSDEAD;
		}
		return Math.round(4.0f * hp / maxhp);
	}

	/**
	 * @return {@link #maxhp}, taking into consideration {@link Monster#poison}.
	 */
	public int getmaxhp() {
		return maxhp + source.poison / 2 * source.hd.count();
	}

	public String getstatus() {
		switch (getnumericstatus()) {
		case STATUSUNHARMED:
			return "unharmed";
		case STATUSSCRATCHED:
			return "scratched";
		case STATUSHURT:
			return "hurt";
		case STATUSWOUNDED:
			return "wounded";
		case STATUSINJURED:
			return "injured";
		case STATUSDYING:
			return "dying";
		case STATUSUNCONSCIOUS:
			return "unconscious";
		case STATUSDEAD:
			return "killed";
		default:
			throw new RuntimeException(
					"Unknown possibility: " + getnumericstatus());
		}
	}

	/**
	 * Not to be confused with {@link Skills#perceive(Monster, boolean)}.
	 *
	 * @param period
	 *            objective period of the day
	 * @return subjective period of the day
	 */
	public String perceive(String period) {
		switch (source.vision) {
		case 0:
			return period;
		case 2:
			return Javelin.app.fight.denydarkvision ? Javelin.PERIODEVENING
					: Javelin.PERIODNOON;
		case 1:
			if (period == Javelin.PERIODNIGHT) {
				return Javelin.PERIODEVENING;
			}
			if (period == Javelin.PERIODEVENING) {
				return Javelin.app.fight.denydarkvision ? Javelin.PERIODEVENING
						: Javelin.PERIODNOON;
			}
		}
		return period;
	}

	/**
	 * Not to be confused with {@link Skills#perceive(Monster, boolean)}.
	 *
	 * @param period
	 *            Objective period.
	 * @return monster's vision in squares (5 feet)
	 * @see #perceive(String)
	 */
	public int view(String period) {
		if (source.vision == 2
				|| source.vision == 1 && period == Javelin.PERIODEVENING) {
			return 12;
		}
		if (period == Javelin.PERIODEVENING || source.vision == 1) {
			return 8;
		}
		if (period == Javelin.PERIODNIGHT) {
			return 4;
		}
		return Integer.MAX_VALUE;
	}

	public int ac() {
		return source.ac + acmodifier;
	}

	@Override
	public boolean equals(Object obj) {
		return id == ((Combatant) obj).id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public void cleave(float ap) {
		if (source.hasfeat(GreatCleave.SINGLETON)) {
			this.ap -= ap;
		} else if (source.hasfeat(Cleave.SINGLETON)) {
			this.ap -= ap / 2f;
		}
	}

	public ArrayList<String> liststatus(final BattleState state) {
		final ArrayList<String> statuslist = new ArrayList<String>();
		if (state.isengaged(this)) {
			statuslist.add("engaged");
			for (Combatant c : Fight.state.blueTeam.contains(this)
					? Fight.state.redTeam : Fight.state.blueTeam) {
				// TODO clone probably unnecessary?
				if (state.isflanked(state.clone(this), state.clone(c))) {
					statuslist.add("flanked");
					break;
				}
			}
		}
		if (surprise() != 0) {
			statuslist.add("flat-footed");
		}
		Vision v = state.haslineofsight(state.next, this);
		if (v == Vision.COVERED) {
			statuslist.add("covered");
		} else if (v == Vision.BLOCKED) {
			statuslist.add("blocked");
		}
		if (source.fly == 0 && state.map[location[0]][location[1]].flooded) {
			statuslist.add("knee-deep");
		}
		for (Condition c : conditions) {
			statuslist.add(c.description.toLowerCase());
		}
		statuslist.sort(null);
		return statuslist;
	}

	public boolean ispenalized(final BattleState s) {
		if (surprise() != 0 || s.map[location[0]][location[1]].flooded
				&& source.swim() == 0) {
			return true;
		}
		for (Condition c : conditions) {
			if (c.effect == Effect.NEGATIVE) {
				return true;
			}
		}
		return false;
	}

	public boolean isbuffed() {
		for (Condition c : conditions) {
			if (c.effect == Effect.POSITIVE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 1/5 HD to up one {@link #getstatus()}
	 */
	public void meld() {
		addcondition(new Melding(this));
	}

	public void escape(BattleState s) {
		s.flee(this);
	}

	/**
	 * Validates, merge (if necessary) and if everything checks add
	 * {@link Condition}.
	 */
	public void addcondition(Condition c) {
		if (!c.validate(this)) {
			return;
		}
		Condition previous = hascondition(c.getClass());
		if (previous == null || previous.stack) {
			c.start(this);
			conditions.add(c);
		} else {
			previous.merge(this, c);
		}
	}

	public void terminateconditions(int timecost) {
		for (Condition co : new ArrayList<Condition>(conditions)) {
			co.terminate(timecost, this);
			if (hp <= DEADATHP) {
				String s = this + " dies from being " + co.description + "!";
				Javelin.message(s, true);
				Squad.active.remove(this);
				return;
			}
		}
		if (hp <= 0) {
			hp = 1;
		}
	}

	/**
	 * @param c
	 *            Tries to remove this exact instance from {@link #conditions}
	 *            first. If fails, resorts to {@link List#remove(Object)}, which
	 *            will look for an equal object.
	 */
	public void removecondition(Condition c) {
		c.end(this);
		for (int i = 0; i < conditions.size(); i++) {
			if (c == conditions.get(i)) {
				conditions.remove(i);
				return;
			}
		}
		conditions.remove(c);
	}

	public void finishconditions(BattleState s, BattleScreen screen) {
		for (Condition co : conditions) {
			co.finish(s);
			screen.block();
		}
	}

	public void transferconditions(Combatant to) {
		for (Condition c : conditions) {
			if (c.longterm == null) {
				continue;
			}
			c.transfer(this, to);
			if (c.longterm == 0) {
				c.end(to);
			} else if (!to.conditions.contains(c)) {
				to.conditions.add(c);
			}
		}
	}

	/**
	 * @return The highest take 10 roll of the current {@link Squad} that isn't
	 *         this combatant. If this is the only member of the squad will
	 *         return {@link Integer#MIN_VALUE}.
	 */
	public int heal() {
		int heal = Integer.MIN_VALUE;
		for (Combatant medic : Squad.active.members) {
			if (!equals(medic)) {
				heal = Math.max(heal, Skills.take10(medic.source.skills.heal,
						medic.source.wisdom));
			}
		}
		return heal;
	}

	/**
	 * @return a copy of the current conditions in effect.
	 */
	public ArrayList<Condition> getconditions() {
		return new ArrayList<Condition>(conditions);
	}

	/**
	 * @param detox
	 *            Remove this many points of {@link #poison} damage. Note that
	 *            this receives a modifier (1 modifier = 2 ability points).
	 */
	public void detox(int detox) {
		if (detox > 0 && source.poison > 0) {
			detox = Math.min(detox * 2, source.poison);
			source.poison -= detox;
			source.changeconstitutionscore(this, +detox);
		}
	}

	/**
	 * Internally clones {@link #source}.
	 *
	 * @return this instance. To allow the following syntax:
	 *         combatant.clone().clonesource()
	 *
	 * @see #clone()
	 */
	public Combatant clonesource() {
		source = source.clone();
		return this;
	}

	/**
	 * Updates {@link Monster#challengerating} internally.
	 *
	 * @param r
	 *            Applies one {@link Upgrade} from this set to the given
	 *            {@link Combatant}.
	 * @return
	 * @return <code>true</code> if an upgrade has been successfully applied.
	 * @see Upgrade#upgrade(Combatant)
	 */
	public boolean upgrade(Realm r) {
		return upgrade(UpgradeHandler.singleton.getfullupgrades(r));
	}

	public boolean upgrade(Collection<? extends Upgrade> upgrades) {
		Upgrade upgrade = RPG.pick(new ArrayList<Upgrade>(upgrades));
		if (upgrade instanceof BreathUpgrade) {
			/* TODO Breaths are pretty CPU intensive right now so avoid them */
			return false;
		}
		if (!upgrade.upgrade(this)) {
			return false;
		}
		postupgradeautomatic(upgrade instanceof ClassLevelUpgrade
				? (ClassLevelUpgrade) upgrade : null);
		CrCalculator.calculatecr(source);
		return true;
	}

	/**
	 * @param garrison
	 *            Upgrades the weakest member of this group.
	 * @see #upgrade(Realm)
	 */
	public static void upgradeweakest(List<Combatant> garrison,
			Collection<Upgrade> r) {
		Combatant weakest = null;
		for (Combatant sensei : garrison) {
			CrCalculator.calculatecr(sensei.source);
			if (weakest == null
					|| sensei.source.challengerating < weakest.source.challengerating) {
				weakest = sensei;
			}
		}
		weakest.upgrade(r);
	}

	/**
	 * @return All squares that are clearly visible by this unit.
	 */
	public HashSet<Point> calculatevision(BattleState s) {
		final HashSet<Point> seen = new HashSet<Point>();
		final Fight f = Javelin.app.fight;
		final String perception = perceive(f.period);
		final int range = view(f.period);
		final boolean forcevision = perception == Javelin.PERIODNOON
				|| perception == Javelin.PERIODMORNING;
		final Point here = new Point(location[0], location[1]);
		for (int x = Math.max(0, here.x - range); x <= here.x + range
				&& x < f.map.map.length; x++) {
			for (int y = Math.max(0, here.y - range); y <= here.y + range
					&& y < f.map.map[0].length; y++) {
				if (forcevision || s.haslineofsight(here, new Point(x, y),
						range, perception) != Vision.BLOCKED) {
					seen.add(new Point(x, y));
				}
			}
		}
		return seen;
	}

	/**
	 * TODO at some point update with reach attacks
	 *
	 * @return <code>true</code> if can reach the target with a mêlée attack.
	 * @see Monster#melee
	 */
	public boolean isadjacent(Combatant target) {
		return Math.abs(location[0] - target.location[0]) <= 1
				&& Math.abs(location[1] - target.location[1]) <= 1;
	}

	/**
	 * @return XP in human readeable format (ex: 150XP).
	 */
	public String gethumanxp() {
		return xp.multiply(new BigDecimal(100)).setScale(0,
				RoundingMode.HALF_UP) + "XP";
	}

	/**
	 * Locates an enemy by sound during battle.
	 *
	 * @see Skills#perceive(Monster, boolean)
	 */
	public void detect() {
		if (Fight.state.redTeam.contains(this)) {
			return;
		}
		int listen = source.skills.perceive(false, true, source);
		for (Combatant c : Fight.state.redTeam) {
			if (listen >= c.source.skills.movesilently(source)
					+ (Walker.distance(this, c) - 1)) {
				BattleScreen.active.mappanel.tiles[c.location[0]][c.location[1]].discovered = true;
			}
		}
	}

	public void setlocation(Point p) {
		location[0] = p.x;
		location[1] = p.y;
	}

	public String wastespells(float resourcesused) {
		String cast = "";
		for (Spell s : spells) {
			int ncast = 0;
			for (int i = s.used; i < s.perday; i++) {
				if (RPG.random() < resourcesused) {
					s.used += 1;
					ncast += 1;
				}
			}
			if (ncast > 0) {
				cast += s.name + " (" + ncast + "x), ";
			}
		}
		if (!cast.isEmpty()) {
			cast = " Cast: " + cast.substring(0, cast.length() - 2) + ".";
		}
		return cast;
	}

	public void unequip(Item i) {
		if (equipped.remove(i)) {
			((Artifact) i).remove(this);
		}
	}

	public boolean equip(Artifact a) {
		return a.equip(this);
	}

	public static void upgradeweakest(List<Combatant> garrison, Realm random) {
		upgradeweakest(garrison, random.getupgrades(UpgradeHandler.singleton));
	}

	/**
	 * @param ismercenary
	 *            Sets {@link #mercenary} and {@link #automatic} to this value.
	 */
	public void setmercenary(boolean ismercenary) {
		mercenary = ismercenary;
		automatic = ismercenary;
	}

	/**
	 * @param xpgained
	 *            Adds this amount to {@link #xp}.
	 */
	public void learn(double xpgained) {
		if (!mercenary) {
			xp = xp.add(new BigDecimal(xpgained));
		}
	}

	public void ready(Maneuver m) {
		ap += ActionCost.FULL;
		m.spent = false;
	}

	/**
	 * Adds this {@link Discipline}'s {@link Maneuver} to the given
	 * {@link Combatant}.
	 * 
	 * @param d
	 *            TODO
	 * @param m
	 *            TODO
	 * @param disciplines
	 *            TODO
	 * @return <code>false</code> if {@link Maneuver#validate(Combatant)} fails
	 *         or <code>true</code> otherwise.
	 */
	public boolean addmaneuver(Discipline d, Maneuver m) {
		if (!m.validate(this)) {
			return false;
		}
		disciplines.add(d, m.clone());
		return true;
	}

	public String printstatus(BattleState s) {
		final ArrayList<String> statuslist = liststatus(s);
		if (statuslist.isEmpty()) {
			return "";
		}
		String description = "";
		CountingSet cs = new CountingSet();
		for (String c : statuslist) {
			cs.add(c);
		}
		for (String c : cs.getelements()) {
			int n = cs.getcount(c);
			String amount = n == 1 ? "" : " (x" + n + ")";
			description += c + amount + ", ";
		}
		return description.substring(0, description.length() - 2) + "";
	}

	/**
	 * @param c
	 *            Removes all {@link Condition} instances of this type.
	 */
	public void clearcondition(Class<? extends Condition> c) {
		for (Condition co = hascondition(c); co != null; co = hascondition(c)) {
			removecondition(co);
		}
	}

	public void heal(int amount, boolean magical) {
		hp += amount;
		if (hp > maxhp) {
			hp = maxhp;
		}
		if (magical) {
			clearcondition(Bleeding.class);
		}
	}

	/**
	 * Simpler version of {@link #damage(int, BattleState, int)}. Just takes the
	 * given amount from {@link #hp} while making sure it stays positive.
	 */
	public void damage(int damage) {
		hp -= damage;
		if (hp < 1) {
			hp = 1;
		}
	}

	/**
	 * Some {@link Upgrade}s need further interaction from a human after they
	 * are applied. This method deals with all of these cleanups.
	 * 
	 * @see #postupgradeautomatic(boolean, Upgrade)
	 */
	public void postupgrade(ClassLevelUpgrade classlevel) {
		if (source.skillpool > 0) {
			source.purchaseskills(classlevel).show();
		}
		for (Feat f : source.feats) {
			f.postupgrade(this);
		}
	}

	/**
	 * Like {@link #postupgrade(boolean, Upgrade)} but this handles all of these
	 * scenarios automatically - either because the player might not care enoug
	 * to hand-pick the the outcome or because we are upgrading an NPC like with
	 * {@link #upgrade(Collection)}.
	 */
	public void postupgradeautomatic(ClassLevelUpgrade upgrade) {
		if (source.skillpool > 0) {
			source.purchaseskills(upgrade).upgradeautomatically();
		}
		for (Feat f : source.feats) {
			f.postupgradeautomatic(this);
		}
	}

	/**
	 * Passive {@link Monster}s don't rely on BattleAi, and use this instead.
	 * 
	 * @see Monster#passive.
	 * 
	 * @return Outcomes.
	 */
	public void act(BattleState s) {
		// do nothing by default
	}

	public Point getlocation() {
		return new Point(location[0], location[1]);
	}

}
