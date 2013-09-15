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
import javelin.model.BattleMap;
import javelin.model.TeamContainer;
import javelin.model.condition.Charging;
import javelin.model.condition.Condition;
import javelin.model.condition.Defending;
import javelin.model.state.BattleState;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * A Combatant is an in-game unit, like the enemies in the battlefield or the
 * named player characters. It contains the data that changes constantly during
 * the course of battle, speeding up the cloning process for BattleNode
 * replication by sharing the same reference to Mosnter among cloned instances.
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
	public ArrayList<Condition> conditions = new ArrayList<Condition>();
	public Spells spells = new Spells();
	public BigDecimal xp = new BigDecimal(0);

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
		ids += 1;
		while (checkidcollision()) {
			ids += 1;
		}
		id = ids;
		ap = 0;
		hp = source.hd.roll(source);
		maxhp = hp;
		if (generatespells && source.spellcr > 0) {
			SpellbookGenerator.generate(this);
		}
	}

	private boolean checkidcollision() {
		for (Squad s : Squad.squads) {
			for (Combatant c : s.members) {
				if (ids == c.id) {
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
			c.conditions = (ArrayList<Condition>) conditions.clone();
			c.spells = spells.clone();
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
	 * @param attack
	 */
	public void meleeAttacks(Combatant combatant, Combatant targetCombatant) {
		final BattleMap map = visual.getMap();
		BattleState state = map.getState();
		combatant = state.translatecombatant(combatant);
		targetCombatant = state.translatecombatant(targetCombatant);
		Action.outcome(MeleeAttack.SINGLETON.attack(state, this,
				targetCombatant,
				combatant.chooseattack(combatant.source.melee), 0));
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
		return source.toString();
	}

	public boolean hasAttackType(final boolean meleeOnly) {
		return !getAttacks(meleeOnly).isEmpty();
	}

	public void checkAttackType(final boolean meleeOnly) {
		if (!hasAttackType(meleeOnly)) {
			Game.message(
					"No " + (meleeOnly ? "mẽlée" : "ranged") + " attacks.",
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
		if (lastrefresh == Float.MIN_VALUE) {
			lastrefresh = ap;
		} else if (ap != lastrefresh) {
			ap -= .01;
			final float turns = ap - lastrefresh;
			lastrefresh = ap;
			hp += source.fasthealing * turns;
			if (hp > maxhp) {
				hp = maxhp;
			}
			for (Condition c : (List<Condition>) conditions.clone()) {
				if (c.expire(this)) {
					conditions.remove(c);
				}
			}
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
		conditions.add(new Charging(ap + 1f, this));
		acmodifier -= 2;
	}

	/**
	 * Theoretically total reduction should be allowed but since we're making
	 * all energy resistance universal and all damage reduction impenetrable
	 * this is a measure to avoid monsters from becoming invincible.
	 */
	public int damage(final int damagep, BattleState s, final int reduce) {
		if (reduce == Integer.MAX_VALUE) {
			return 0;
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
		}
		return damage;
	}

	public CurrentAttack getcurrentattack(List<AttackSequence> attacktype) {
		return attacktype == source.melee ? currentmelee : currentranged;
	}

	public CurrentAttack chooseattack(List<AttackSequence> attacktype) {
		CurrentAttack currentattack = getcurrentattack(attacktype);
		if (currentattack.continueattack()) {
			return currentattack;
		}
		int attackindex = 0;
		if (attacktype.size() > 1) {
			ArrayList<String> attacks = new ArrayList<String>();
			for (AttackSequence sequence : attacktype) {
				attacks.add(sequence.toString());
			}
			attackindex = CastSpells.choose("Start which attack sequence?",
					attacks);
			if (attackindex == -1) {
				Game.messagepanel.clear();
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
		ap -= (RPG.r(1, 20) + source.initiative) / 20.0;
		initialap = ap;
	}

	public int getNumericStatus() {
		if (hp == maxhp) {
			return 5;
		}
		if (hp <= 1) {
			if (hp == 1) {
				return 0;
			}
			if (hp <= 0) {
				return hp > Combatant.DEADATHP ? -1 : -2;
			}
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
			throw new RuntimeException("Unknown possibility: "
					+ getNumericStatus());
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
		if (source.vision == 2 || source.vision == 1
				&& period == Javelin.PERIOD_EVENING) {
			return 16;
		}
		if (period == Javelin.PERIOD_EVENING || source.vision == 1) {
			return 8;
		}
		if (period == Javelin.PERIOD_NIGHT) {
			return 4;
		}
		return Integer.MAX_VALUE;
	}

	public Combatant deepclone() {
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
		conditions.add(new Defending(ap, this));
	}
}
