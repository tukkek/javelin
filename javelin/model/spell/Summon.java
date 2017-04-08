package javelin.model.spell;

import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import tyrant.mikera.engine.RPG;

/**
 * Brings an ally to fight with your team.
 *
 * Upper Krust's method is not followed here since it makes no sense for a
 * Gelugon (CR21) that could summon another Gelugon to be CR21.4. More
 * discussion on {@link #ratechallenge(int, String, float)}.
 *
 * TODO This should not be a {@link Spell}. See
 * {@link #cast(Combatant, Combatant, BattleState, boolean)}
 *
 * @author alex
 */
public class Summon extends Spell {
	public String monstername;
	private float chance;

	public Summon(String monstername, float chance) {
		super("Summon " + monstername, 0, 0, Realm.MAGIC);
		assert chance == 1;// TODO cannot be a Spell
		this.monstername = monstername;
		this.chance = chance;
		castinbattle = true;
		if (!Javelin.MONSTERSBYCR.isEmpty()) {
			postloadmonsters();
		}
	}

	public Summon(String name) {
		this(name, 1);
	}

	/**
	 * Rationale behind CR calculation: {@link Monster} counts as it's own CR
	 * but divided by 5. 5 is the number of fights a {@link Squad} is supposed
	 * to be able to survive before resting according to the Dungeon Master's
	 * Guide. So a summonable counts as an ally of his CR that will participate
	 * in only 1 battle.
	 *
	 * Chance is applied as a normal %.
	 *
	 * TODO isn't taking into account summoning a group.
	 */
	static float ratechallenge(String monstername2, float chance2) {
		Monster monster = Javelin.getmonster(monstername2);
		return chance2 * monster.challengerating / 5f;
	}

	@Override
	public String cast(Combatant caster2, Combatant target, BattleState s,
			boolean saved) {
		List<Combatant> team = target.getteam(s);
		Monster m = Javelin.getmonster(monstername);
		m.name = "Summoned " + m.name.toLowerCase();
		Combatant summoned = new Combatant(m, true);
		team.add(summoned);
		summoned.summoned = true;
		/*
		 * TODO test if this is called after the CastSpell AP has already been
		 * spent
		 */
		summoned.ap = target.ap;
		int x = target.location[0];
		int y = target.location[1];
		while (s.getcombatant(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || y < 0 || x >= s.map.length || y >= s.map.length) {
				x = target.location[0];
				y = target.location[1];
			}
		}
		summoned.location[0] = x;
		summoned.location[1] = y;
		return "";// default message is enough
	}

	@Override
	public int hit(Combatant active, Combatant target, BattleState state) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public boolean apply(Combatant c) {
		Monster m = Javelin.getmonster(monstername);
		if (m == null) {
			throw new RuntimeException("Unknown summon: " + monstername);
		}
		return ChallengeRatingCalculator
				.calculatecr(c.source) >= m.challengerating && super.apply(c);
	}

	@Override
	public boolean canbecast(Combatant c) {
		return !c.summoned && super.canbecast(c);
	}

	@Override
	public void postloadmonsters() {
		cr = ratechallenge(monstername, chance);
		level = Math.round(Javelin.getmonster(monstername).challengerating / 2);
		casterlevel = calculatecasterlevel(level);
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				&& monstername.equals(((Summon) obj).monstername);
	}
}
