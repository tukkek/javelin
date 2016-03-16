package javelin.model.unit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.SpellbookGenerator;
import javelin.controller.action.Action;
import javelin.controller.action.Wait;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.action.world.CastSpells;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.Cloneable;
import javelin.model.TeamContainer;
import javelin.model.condition.Charging;
import javelin.model.condition.Condition;
import javelin.model.condition.Condition.Effect;
import javelin.model.condition.Defending;
import javelin.model.feat.Cleave;
import javelin.model.feat.GreatCleave;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.abilities.Spells;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

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
	/**
	 * TODO
	 */
	public static final int DEADATHP = -8;
	/**
	 * Should probably be external, like a mapping in {@link BattleScreen}.
	 */
	public Thing visual;
	public Monster source;
	public float ap = 0;
	public int[] location = new int[2];
	static private int ids = 1;
	public float lastrefresh = -Float.MAX_VALUE;
	public float initialap;
	public int hp;
	public int maxhp;
	public int id = -1;
	public CurrentAttack currentmelee = new CurrentAttack();
	public CurrentAttack currentranged = new CurrentAttack();
	public int acmodifier = 0;
	public CloneableList<Condition> conditions = new CloneableList<Condition>();
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

	/**
	 * @param generatespells
	 *            if true will create spells for this monster based on his
	 *            {@link Monster#spellcr}. This is essentially a temporary
	 *            measure to allow 1.0 monster who would have spell powers to
	 *            use the currently implemented spells TODO
	 */
	public Combatant(final Thing visual, final Monster sourcep,
			boolean generatespells) {
		super();
		this.visual = visual;
		source = sourcep;
		if (visual != null) {
			visual.combatant = this;
		}
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

	public void newid() {
		Combatant.ids += 1;
		while (checkidcollision()) {
			Combatant.ids += 1;
		}
		id = Combatant.ids;
	}

	private boolean checkidcollision() {
		for (Squad s : Squad.squads) {
			for (Combatant c : s.members) {
				if (Combatant.ids == c.id) {
					return true;
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
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Rolls for hit and applies damage.
	 * 
	 * TODO :
	 * 
	 * implement ranged and 'melee and ranged' attack sequences
	 * 
	 * describe missed attacks better based on AC segments?
	 * 
	 * implement criticals?
	 * 
	 * @param targetCombatant
	 *            The monster being attacked.
	 * @param combatant
	 * @param state2
	 * @param attack
	 */
	public void meleeAttacks(Combatant combatant, Combatant targetCombatant,
			BattleState state) {
		final BattleMap map = visual.getMap();
		combatant = state.clone(combatant);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(MeleeAttack.SINGLETON.attack(state, this,
				targetCombatant,
				combatant.chooseattack(combatant.source.melee, targetCombatant),
				0));
	}

	public List<AttackSequence> getAttacks(final boolean melee) {
		return melee ? source.melee : source.ranged;
	}

	public int getHp() {
		return hp;
	}

	// public boolean isAlly(final Combatant c) {
	// return isAlly(c, TeamContainer.DEFAULT);
	// }

	public boolean isAlly(final Combatant c, final TeamContainer tc) {
		return getTeam(tc) == c.getTeam(tc);
	}

	public List<Combatant> getTeam(final TeamContainer tc) {
		final List<Combatant> redTeam = tc.getRedTeam();
		return redTeam.contains(this) ? redTeam : tc.getBlueTeam();
	}

	@Override
	public String toString() {
		// TODO location:
		return source.toString()
		// + Arrays.toString(location)
		;
	}

	public boolean hasAttackType(final boolean meleeOnly) {
		return !getAttacks(meleeOnly).isEmpty();
	}

	public void checkAttackType(final boolean meleeOnly) {
		if (!hasAttackType(meleeOnly)) {
			Game.message("No " + (meleeOnly ? "mẽlée" : "ranged") + " attacks.",
					null, Delay.WAIT);
			throw new RepeatTurnException();
		}
	}

	public int[] determineLocation() {
		location[0] = visual.x;
		location[1] = visual.y;
		return location;
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
				if (c.expire(this)) {
					conditions.remove(c);
				}
			}
			lastrefresh = ap;
		}
	}

	public boolean ischarging() {
		return hascondition(Charging.class);
	}

	public boolean hascondition(Class<? extends Condition> clazz) {
		for (Condition c : conditions) {
			if (c.getClass().equals(clazz)) {
				return true;
			}
		}
		return false;
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
			s.remove(this);
			s.dead.add(this);
			if (hp <= DEADATHP && Javelin.app.fight.meld()) {
				s.addmeld(location[0], location[1]);
			}
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
			attackindex = CastSpells.choose("Start which attack sequence?",
					attacks, false, false);
			if (attackindex == -1) {
				Game.messagepanel.clear();
				Game.instance().setHero(visual);
				throw new RepeatTurnException();
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
		if (hp == maxhp) {
			return 5;
		}
		if (hp == 1) {
			return 0;
		}
		if (hp <= 0) {
			return hp > Combatant.DEADATHP ? -1 : -2;
		}
		return Math.round(4.0f * hp / maxhp);
	}

	public String getStatus() {
		switch (getNumericStatus()) {
		case 5:
			return "unharmed";
		case 4:
			return "scratched";
		case 3:
			return "hurt";
		case 2:
			return "wounded";
		case 1:
			return "injured";
		case 0:
			return "dying";
		case -1:
			return "unconscious";
		case -2:
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
			return Javelin.PERIOD_NOON;
		case 1:
			if (period == Javelin.PERIOD_NIGHT) {
				return Javelin.PERIOD_EVENING;
			} else if (period == Javelin.PERIOD_EVENING) {
				return Javelin.PERIOD_NOON;
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
				|| source.vision == 1 && period == Javelin.PERIOD_EVENING) {
			return 12;
		}
		if (period == Javelin.PERIOD_EVENING || source.vision == 1) {
			return 8;
		}
		if (period == Javelin.PERIOD_NIGHT) {
			return 4;
		}
		return Integer.MAX_VALUE;
	}

	public Combatant clonedeeply() {
		final Combatant c = clone();
		c.source = c.source.clone();
		return c;
	}

	public int ac() {
		return source.ac + acmodifier;
	}

	@Override
	public boolean equals(Object obj) {
		return id == ((Combatant) obj).id;
	}

	public void await() {
		ap += Wait.APCOST;
		conditions.add(new Defending(ap + BattleScreen.active.spentap, this));
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
		if (state.isEngaged(this)) {
			statuslist.add("engaged");
			for (Combatant c : BattleMap.blueTeam.contains(this)
					? BattleMap.redTeam : BattleMap.blueTeam) {
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
		if (surprise() != 0
				|| s.map[location[0]][location[1]].flooded && !source.swim()) {
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
}
