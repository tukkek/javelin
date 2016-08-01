package javelin.model.unit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.SpellbookGenerator;
import javelin.controller.action.Action;
import javelin.controller.action.Defend;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.action.ai.RangedAttack;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.upgrade.BreathUpgrade;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Cloneable;
import javelin.model.Realm;
import javelin.model.TeamContainer;
import javelin.model.condition.Charging;
import javelin.model.condition.Condition;
import javelin.model.condition.Condition.Effect;
import javelin.model.condition.Defending;
import javelin.model.feat.Cleave;
import javelin.model.feat.GreatCleave;
import javelin.model.item.artifact.Artifact;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Meld;
import javelin.model.unit.abilities.Spells;
import javelin.model.world.WorldActor;
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
	private CloneableList<Condition> conditions =
			new CloneableList<Condition>();
	/**
	 * Canonical representation of the spells this unit has.
	 * 
	 * @see Monster#spells
	 * @see Monster#spellcr
	 */
	public Spells spells = new Spells();
	/**
	 * XP in CR, you'll want to multiply by 100 and round to show the player.
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
		source = sourcep;
		newid();
		ap = 0;
		hp = source.hd.roll(source);
		maxhp = hp;
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
	 * @see WorldActor#getcombatants()
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
		for (WorldActor a : Squad.getall()) {
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
	public void meleeAttacks(Combatant targetCombatant, BattleState state) {
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
				current.chooseattack(current.source.melee, targetCombatant),
				0));
	}

	public List<AttackSequence> getAttacks(final boolean melee) {
		return melee ? source.melee : source.ranged;
	}

	public int getHp() {
		return hp;
	}

	public boolean isAlly(final Combatant c, final TeamContainer tc) {
		return getTeam(tc) == c.getTeam(tc);
	}

	public List<Combatant> getTeam(final TeamContainer tc) {
		final List<Combatant> redTeam = tc.getRedTeam();
		return redTeam.contains(this) ? redTeam : tc.getBlueTeam();
	}

	@Override
	public String toString() {
		return source.toString();
	}

	public boolean hasAttackType(final boolean meleeOnly) {
		return !getAttacks(meleeOnly).isEmpty();
	}

	public void checkAttackType(final boolean meleeOnly) {
		if (!hasAttackType(meleeOnly)) {
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
				hp += source.fasthealing * turns;
				if (hp > maxhp) {
					hp = maxhp;
				}
			}
			for (Condition c : (List<Condition>) conditions.clone()) {
				c.expire(this);
			}
			lastrefresh = ap;
		}
	}

	public Condition hascondition(Class<? extends Condition> clazz) {
		for (Condition c : conditions) {
			if (c.getClass().equals(clazz)) {
				return c;
			}
		}
		return null;
	}

	public void charge() {
		conditions
				.add(new Charging(ap + 1f + BattleScreen.active.spentap, this));
		acmodifier -= 2;
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
			die(s);
		}
	}

	public void die(BattleState s) {
		s.remove(this);
		s.dead.add(this);
		if ((hp <= DEADATHP && Javelin.app.fight.meld) || Meld.DEBUG) {
			s.addmeld(location[0], location[1]);
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

	public void rollinitiative() {
		ap = -(RPG.r(1, 20) + source.initiative) / 20f;
		initialap = ap;
	}

	public int getNumericStatus() {
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
		return this.maxhp + source.poison * source.hd.count();
	}

	public String getStatus() {
		switch (getNumericStatus()) {
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
					"Unknown possibility: " + getNumericStatus());
		}
	}

	/**
	 * @param period
	 *            objective period of the day
	 * @return subjective period of the day
	 */
	public String perceive(String period) {
		switch (source.vision) {
		case 0:
			return period;
		case 2:
			return Javelin.PERIODNOON;
		case 1:
			if (period == Javelin.PERIODNIGHT) {
				return Javelin.PERIODEVENING;
			} else if (period == Javelin.PERIODEVENING) {
				return Javelin.PERIODNOON;
			}
		}
		return period;
	}

	/**
	 * @param period
	 *            Objective period
	 * @return monster's vision in squares (5 feet)
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

	public void await() {
		ap += Defend.APCOST;
		if (!burrowed) {
			conditions
					.add(new Defending(ap + BattleScreen.active.spentap, this));
		}
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
				if (state.isflanked(state.clone(this), state.clone(c))) {
					statuslist.add("flanked");
					break;
				}
			}
		}
		if (surprise() != 0) {
			statuslist.add("flat-footed");
		}
		Vision v = state.hasLineOfSight(state.next, this);
		if (v == Vision.COVERED) {
			statuslist.add("covered");
		} else if (v == Vision.BLOCKED) {
			statuslist.add("blocked");
		}
		if (source.fly == 0 && state.map[location[0]][location[1]].flooded) {
			statuslist.add("knee-deep");
		}
		for (Condition c : conditions) {
			statuslist.add(c.description);
		}
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
	 * 1/5 HD to up one {@link #getStatus()}
	 */
	public void meld() {
		hp += Math.ceil(maxhp / 5f);
		if (hp > maxhp) {
			hp = maxhp;
		}
		// ap -= .5;
		source = source.clone();
		source.ac += 2;
		for (AttackSequence s : source.melee) {
			for (Attack a : s) {
				a.bonus += 2;
			}
		}
		for (AttackSequence s : source.ranged) {
			for (Attack a : s) {
				a.bonus += 2;
			}
		}
	}

	/**
	 * @return a roll of {@link Skills#movesilently}.
	 */
	public int movesilently() {
		return Skills.take10(source.skills.stealth, source.dexterity);
	}

	public void escape(BattleScreen screen) {
		screen.fleeing.add(this);
		Fight.state.blueTeam.remove(this);
	}

	public void addcondition(Condition c) {
		if (c.stacks || hascondition(c.getClass()) == null) {
			c.start(this);
			conditions.add(c);
		}
	}

	public void terminateconditions(int timecost) {
		for (Condition co : new ArrayList<Condition>(conditions)) {
			co.terminate(timecost, this);
			if (hp <= DEADATHP) {
				Javelin.message(
						this + " dies from being " + co.description + "!",
						true);
				Squad.active.remove(this);
				return;
			}
		}
		if (hp <= 0) {
			hp = 1;
		}
	}

	public void removecondition(Condition c) {
		c.end(this);
		conditions.remove(c);
	}

	public void finishconditions(BattleState s, BattleScreen screen) {
		for (Condition co : conditions) {
			co.finish(s);
			screen.checkblock();
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
			} else {
				to.addcondition(c);
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
	 *            Remove this many points of {@link #poison} damage.
	 */
	public void detox(int detox) {
		if (detox > 0) {
			source.poison -= detox;
			source.raiseconstitution(this, detox);
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
	 * Updates {@link Monster#challengeRating} internally.
	 * 
	 * @param r
	 *            Applies one {@link Upgrade} from this set to the given
	 *            {@link Combatant}.
	 * @return
	 * @return <code>true</code> if an upgrade has been successfully applied.
	 * @see Upgrade#upgrade(Combatant)
	 */
	public boolean upgrade(Realm r) {
		Upgrade upgrade = RPG.pick(new ArrayList<Upgrade>(
				UpgradeHandler.singleton.getfullupgrades(r)));
		if (upgrade instanceof BreathUpgrade) {
			/* TODO Breaths are pretty CPU intensive right now so avoid them */
			return false;
		}
		if (!upgrade.upgrade(this)) {
			return false;
		}
		if (upgrade.purchaseskills) {
			source.purchaseskills(upgrade).upgradeautomatically();
		}
		ChallengeRatingCalculator.calculateCr(source);
		return true;
	}

	/**
	 * @param garrison
	 *            Upgrades the weakest member of this group.
	 * @see #upgrade(Realm)
	 */
	public static void upgradeweakest(List<Combatant> garrison, Realm r) {
		Combatant weakest = null;
		for (Combatant sensei : garrison) {
			ChallengeRatingCalculator.calculateCr(sensei.source);
			if (weakest == null
					|| sensei.source.challengeRating < weakest.source.challengeRating) {
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
				if (forcevision || s.hasLineOfSight(here, new Point(x, y),
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
}
