package javelin.controller.challenge;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.challenge.factor.AbilitiesFactor;
import javelin.controller.challenge.factor.ArmorClassFactor;
import javelin.controller.challenge.factor.ClassLevelFactor;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.challenge.factor.FeatsFactor;
import javelin.controller.challenge.factor.FullAttackFactor;
import javelin.controller.challenge.factor.HdFactor;
import javelin.controller.challenge.factor.SizeFactor;
import javelin.controller.challenge.factor.SpeedFactor;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.challenge.factor.quality.BreathWeapons;
import javelin.controller.challenge.factor.quality.DamageReduction;
import javelin.controller.challenge.factor.quality.EnergyResistance;
import javelin.controller.challenge.factor.quality.HealingFactor;
import javelin.controller.challenge.factor.quality.QualitiesFactor;
import javelin.controller.challenge.factor.quality.SpellResistance;
import javelin.controller.exception.UnbalancedTeamsException;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Some of the factors, as "2. Templates", "4. Traits (Type/Subtype/Race)" and
 * "3. Size", are packages of individual factors, they are not implemented.
 * 
 * TODO Implement whole "Challenging challenge rating*. What hasn't been done
 * yet:
 * 
 * 1. Character Levels (Prestige Classes and NPC Classes)
 * 
 * 6. Speed
 * 
 * 7. Armor Class
 * 
 * 8. Full Attack
 * 
 * 9. Special Abilities/Qualities
 * 
 * 9.01 Ability Score Loss
 * 
 * 9.02 Breath Weapons
 * 
 * 9.03 Create Spawn
 * 
 * 9.04 Damage Reduction
 * 
 * 9.05 Disease
 * 
 * 9.06 Energy Drain
 * 
 * 9.07 Energy Resistance
 * 
 * 9.08 Fast Healing
 * 
 * 9.09 Gaze Weapons
 * 
 * 9.10 Generic Abilities
 * 
 * 9.11 Immunities
 * 
 * 9.12 Insight/Luck/Profane/Sacred Bonuses
 * 
 * 9.13 Poison
 * 
 * 9.14 Ray Attacks
 * 
 * 9.15 Regeneration
 * 
 * 9.16 Spell-like Abilities
 * 
 * 9.17 Spell Resistance
 * 
 * 9.18 Spells (Integrated Spell Levels)
 * 
 * 9.19 Summon
 * 
 * 9.20 Touch Attacks
 * 
 * 9.21 Turn Resistance
 * 
 * 9.22 Unusual Abilities
 */
public class ChallengeRatingCalculator {
	private static final int MAXIMUM_EL = 50;
	private static final int MINIMUM_EL = -7;
	public static final float[] CR_FRACTIONS = new float[] { 3.5f, 3f, 2.5f,
			2f, 1.75f, 1.5f, 1.25f, 1f, .5f, 0f, -.5f, -1f, -1.5f, -2f, -2.5f,
			-3 };
	public static final HdFactor HIT_DICE_FACTOR = new HdFactor();
	private static final CrFactor CLASS_LEVEL_FACTOR = new ClassLevelFactor();
	/**
	 * TODO reinclude AbilitiesFactor. right now it's making a huge difference
	 * at very low el (game very start)
	 */
	public static final CrFactor[] CR_FACTORS = new CrFactor[] {
			new AbilitiesFactor(), new ArmorClassFactor(), new FeatsFactor(),
			new FullAttackFactor(), HIT_DICE_FACTOR, new HealingFactor(),
			new SizeFactor(), new SpeedFactor(), new SpellsFactor(),
			CLASS_LEVEL_FACTOR, new QualitiesFactor(), new BreathWeapons(),
			new DamageReduction(), new EnergyResistance(),
			new SpellResistance() };
	private static final FileWriter LOGFILE;
	static {
		if (Javelin.DEBUG) {
			try {
				LOGFILE = new FileWriter("crs.log", false);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			LOGFILE = null;
		}
	}
	static public boolean appliedgoldrenrule = false;

	/**
	 * See "challenging challenge ratings" source document (@ 'doc' folder), pg
	 * 1: "HOW DO FACTORS WORK?". The silver rule isn't computed for at this
	 * stage the plan is not to use the PHB classes - may change though TODO
	 * 
	 * @param monster
	 * @return The calculated CR.
	 */
	static public float calculateCr(final Monster monster) {
		float[] r = calculaterawcr(monster);
		float goldenrule = r[1];
		float cr = translatecr(goldenrule >= 4 ? Math.round(goldenrule)
				: roundfraction(goldenrule));
		monster.challengeRating = cr;
		log(" total: " + r[0] + " golden rule: " + goldenrule + " final: " + cr
				+ "\n");
		return cr;
	}

	public static float[] calculaterawcr(final Monster monster) {
		log(monster.toString());
		final TreeMap<CrFactor, Float> factorHistory = new TreeMap<CrFactor, Float>();
		float crp = 0;
		for (final CrFactor f : CR_FACTORS) {
			final float result = f.calculate(monster);
			log(" " + f + ": " + result);
			factorHistory.put(f, result);
			crp += result;
		}
		return new float[] { crp, goldenRule(factorHistory, crp) };
	}

	public static float roundfraction(float cr) {
		for (final float limit : CR_FRACTIONS) {
			if (cr >= limit) {
				return limit;
			}
		}
		throw new RuntimeException("CR not correctly rounded: " + cr);
	}

	public static float translatecr(float cr) {
		if (cr == .5) {
			cr = 2 / 3f;
		} else if (cr == 0) {
			cr = 1 / 2f;
		} else if (cr == -.5) {
			cr = 1 / 3f;
		} else if (cr == -1) {
			cr = 1 / 4f;
		} else if (cr == -1.5) {
			cr = 1 / 6f;
		} else if (cr == -2) {
			cr = 1 / 8f;
		} else if (cr == -2.5) {
			cr = 1 / 12f;
		} else if (cr == -3) {
			cr = 1 / 16f;
		}
		return cr;
	}

	private static void log(final String s) {
		if (!Javelin.DEBUG) {
			return;
		}
		try {
			LOGFILE.write(s + "\n");
			LOGFILE.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static private float goldenRule(
			final TreeMap<CrFactor, Float> factorHistory, final float cr) {
		final float hpCheck = factorHistory.get(HIT_DICE_FACTOR)
				+ factorHistory.get(CLASS_LEVEL_FACTOR);
		if (hpCheck < cr / 2) {
			final float twicehp = hpCheck * 2;
			// appliedgoldrenrule = true;
			return twicehp + (cr - twicehp) / 2;
		}
		appliedgoldrenrule = false;
		return cr;
	}

	public static int calculateEl(final List<Monster> team)
			throws UnbalancedTeamsException {
		return calculateEl(team, true);
	}

	public static int calculateSafe(final List<Monster> team) {
		try {
			return calculateEl(team, false);
		} catch (final UnbalancedTeamsException e) {
			throw new RuntimeException("shouldn't happen!");
		}
	}

	private static int calculateEl(final List<Monster> team, final boolean check)
			throws UnbalancedTeamsException {
		float highestCr = Float.MIN_VALUE;
		float sum = 0;
		for (final Monster mg : team) {
			sum += mg.challengeRating;
			if (mg.challengeRating > highestCr) {
				highestCr = mg.challengeRating;
			}
		}
		if (check) {
			for (final Monster mg : team) {
				if (highestCr - mg.challengeRating > 18) {
					throw new UnbalancedTeamsException();
				}
			}
		}
		final int groupCr = elFromCr(sum)
				+ multipleOpponentsElModifier(team.size());
		final int highestCrEl = elFromCr(highestCr);
		return groupCr <= highestCrEl ? highestCrEl : groupCr;
	}

	public static int multipleOpponentsElModifier(final int teamSize) {
		if (teamSize <= 1) {
			return 0;
		}
		if (teamSize <= 2) {
			return -2;
		}
		if (teamSize <= 3) {
			return -3;
		}
		if (teamSize <= 5) {
			return -4;
		}
		if (teamSize <= 7) {
			return -5;
		}
		if (teamSize <= 11) {
			return -6;
		}
		if (teamSize <= 15) {
			return MINIMUM_EL;
		}
		if (teamSize <= 23) {
			return -8;
		}
		if (teamSize <= 31) {
			return -9;
		}
		if (teamSize <= 47) {
			return -10;
		}
		if (teamSize <= 63) {
			return -11;
		}
		if (teamSize <= 95) {
			return -12;
		}
		if (teamSize <= 127) {
			return -13;
		}
		if (teamSize <= 191) {
			return -14;
		}
		if (teamSize <= 255) {
			return -15;
		}
		if (teamSize <= 383) {
			return -16;
		}
		if (teamSize <= 511) {
			return -17;
		}
		throw new RuntimeException("Expand multiple opponents EL table: "
				+ teamSize);
	}

	public static int elFromCr(final float cr) {
		if (cr <= 1 / 16f) {
			return MINIMUM_EL;
		}
		if (cr <= 1 / 12f) {
			return -6;
		}
		if (cr <= 1 / 8f) {
			return -5;
		}
		if (cr <= 1 / 6f) {
			return -4;
		}
		if (cr <= 1 / 4f) {
			return -3;
		}
		if (cr <= 1 / 3f) {
			return -2;
		}
		if (cr <= 1 / 2f) {
			return -1;
		}
		if (cr <= 2 / 3f) {
			return 0;
		}
		if (cr <= 1) {
			return 1;
		}
		if (cr <= 1.25) {
			return 2;
		}
		if (cr <= 1.5) {
			return 3;
		}
		if (cr <= 1.75) {
			return 4;
		}
		if (cr <= 2) {
			return 5;
		}
		if (cr <= 2.5) {
			return 6;
		}
		if (cr <= 3) {
			return 7;
		}
		if (cr <= 3.5) {
			return 8;
		}
		if (cr <= 4) {
			return 9;
		}
		if (cr <= 5) {
			return 10;
		}
		if (cr <= 6) {
			return 11;
		}
		if (cr <= 7) {
			return 12;
		}
		if (cr <= 9) {
			return 13;
		}
		if (cr <= 11) {
			return 14;
		}
		if (cr <= 13) {
			return 15;
		}
		if (cr <= 15) {
			return 16;
		}
		if (cr <= 19) {
			return 17;
		}
		if (cr <= 23) {
			return 18;
		}
		if (cr <= 27) {
			return 19;
		}
		if (cr <= 31) {
			return 20;
		}
		if (cr <= 39) {
			return 21;
		}
		if (cr <= 47) {
			return 22;
		}
		if (cr <= 55) {
			return 23;
		}
		if (cr <= 63) {
			return 24;
		}
		if (cr <= 79) {
			return 25;
		}
		if (cr <= 95) {
			return 26;
		}
		if (cr <= 111) {
			return 27;
		}
		if (cr <= 127) {
			return 28;
		}
		if (cr <= 159) {
			return 29;
		}
		if (cr <= 191) {
			return 30;
		}
		if (cr <= 223) {
			return 31;
		}
		if (cr <= 255) {
			return 32;
		}
		if (cr <= 319) {
			return 33;
		}
		if (cr <= 383) {
			return 34;
		}
		if (cr <= 447) {
			return 35;
		}
		if (cr <= 511) {
			return 36;
		}
		if (cr <= 639) {
			return 37;
		}
		if (cr <= 767) {
			return 38;
		}
		if (cr <= 895) {
			return 39;
		}
		if (cr <= 1023) {
			return 40;
		}
		if (cr <= 1279) {
			return 41;
		}
		if (cr <= 1535) {
			return 42;
		}
		if (cr <= 1791) {
			return 43;
		}
		if (cr <= 2047) {
			return 44;
		}
		if (cr <= 2559) {
			return 45;
		}
		if (cr <= 3071) {
			return 46;
		}
		if (cr <= 3583) {
			return 47;
		}
		if (cr <= 4095) {
			return 48;
		}
		if (cr <= 5119) {
			return 49;
		}
		if (cr <= 6143) {
			return MAXIMUM_EL;
		}
		throw new RuntimeException("Expand EL conversion: " + cr);
	}

	public static float calculatepositive(final List<Monster> group) {
		return calculateSafe(group) + Math.abs(MINIMUM_EL) + 1;
	}

	public static List<Monster> convertlist(List<Combatant> team) {
		final ArrayList<Monster> monsterlist = new ArrayList<Monster>(
				team.size());
		for (Combatant c : team) {
			monsterlist.add(c.source);
		}
		return monsterlist;
	}

	public static float[] eltocr(int teamel) {
		switch (teamel) {
		case -9:
			return new float[] { 1 / 32f };
		case -8:
			return new float[] { 1 / 24f };
		case -7:
			return new float[] { 1 / 16f };
		case -6:
			return new float[] { 1 / 12f };
		case -5:
			return new float[] { 1 / 8f };
		case -4:
			return new float[] { 1 / 6f };
		case -3:
			return new float[] { 1 / 4f };
		case -2:
			return new float[] { 1 / 3f };
		case -1:
			return new float[] { 1 / 2f };
		case 0:
			return new float[] { 2 / 3f };
		case 1:
			return new float[] { 1 };
		case 2:
			return new float[] { 1.25f };
		case 3:
			return new float[] { 1.5f };
		case 4:
			return new float[] { 1.75f };
		case 5:
			return new float[] { 2 };
		case 6:
			return new float[] { 2.5f };
		case 7:
			return new float[] { 3 };
		case 8:
			return new float[] { 3.5f };
		case 9:
			return new float[] { 4 };
		case 10:
			return new float[] { 5 };
		case 11:
			return new float[] { 6 };
		case 12:
			return new float[] { 7 };
		case 13:
			return new float[] { 8, 9 };
		case 14:
			return new float[] { 10, 11 };
		case 15:
			return new float[] { 12, 13 };
		case 16:
			return new float[] { 14, 15 };
		case 17:
			return new float[] { 16, 17, 18, 19 };
		case 18:
			return new float[] { 20, 21, 22, 23 };
		default:
			throw new RuntimeException("Unknown EL " + teamel);
		}
	}
}
