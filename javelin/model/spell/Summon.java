package javelin.model.spell;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
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
	private static final boolean SHOWSPELLLLEVEL = false;
	public String monstername;
	private float chance;

	public Summon(String monstername, float chance) {
		super("Summon " + monstername, 0, false, 0, true);
		assert chance == 1;// TODO cannot be a Spell
		this.monstername = monstername;
		this.chance = chance;
		if (!Javelin.MONSTERSBYCR.isEmpty()) {
			postloadmonsters();
		}
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
		Monster monster = findmonster(monstername2);
		return chance2 * monster.challengeRating / 5f;
	}

	public static Monster findmonster(String monstername2) {
		Monster monster = null;
		for (Monster m : Javelin.ALLMONSTERS) {
			if (m.name.equalsIgnoreCase(monstername2)) {
				monster = m.clone();
				break;
			}
		}
		if (monster == null) {
			return null;
		}
		ChallengeRatingCalculator.calculateCr(monster);
		return monster;
	}

	@Override
	public int calculatetouchdc(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		ArrayList<Combatant> team =
				s.blueTeam.contains(caster) ? s.blueTeam : s.redTeam;
		Monster m = findmonster(monstername);
		m.name = "Summoned " + m.name.toLowerCase();
		Combatant summoned = new Combatant(null, m, true);
		team.add(summoned);
		summoned.summoned = true;
		/* TODO check if this is indeed after the CastSpell AP has been spent */
		summoned.ap = caster.ap;
		int x = caster.location[0];
		int y = caster.location[1];
		while (s.getCombatant(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || y < 0 || x >= s.map.length || y >= s.map.length) {
				x = caster.location[0];
				y = caster.location[1];
			}
		}
		summoned.location[0] = x;
		summoned.location[1] = y;
		assert chance == 1;// TODO cannot be a Spell
		return "";// default message is enough
	}

	@Override
	public int calculatehitdc(Combatant active, Combatant target,
			BattleState state) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public int calculatesavetarget(Combatant caster, Combatant target) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful {
		throw new NotPeaceful();
	}

	@Override
	public boolean apply(Combatant m) {
		return ChallengeRatingCalculator.calculateCr(
				m.source) >= findmonster(monstername).challengeRating
				&& super.apply(m);
	}

	@Override
	public boolean canbecast(Combatant c) {
		return !c.summoned && super.canbecast(c);
	}

	@Override
	public void postloadmonsters() {
		cr = ratechallenge(monstername, chance);
		casterlevel = calculatecasterlevel(
				Math.round(findmonster(monstername).challengeRating / 2));
		if (SHOWSPELLLLEVEL && Javelin.DEBUG) {
			System.out.println(
					name + " CL" + casterlevel + " #summoncasterlevel");
		}
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
