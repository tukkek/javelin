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
import javelin.controller.challenge.factor.SkillsFactor;
import javelin.controller.challenge.factor.SpeedFactor;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.challenge.factor.quality.BreathFactor;
import javelin.controller.challenge.factor.quality.QualitiesFactor;
import javelin.controller.exception.UnbalancedTeams;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;

/**
 * Determines a {@link Monster#challengerating} according to the rules of Upper
 * Krust's work, which is repackaged with permission on the 'doc' directory.
 * 
 * His reference is used to the best of my abilities but has been adapted in a
 * few cases due to programming complexity and artificial intelligence
 * efficiency. Such cases should be documented in Javadoc.
 * 
 * #see CrFactor
 */
public class CrCalculator {
	public static final int DIFFICULTYVERYEASY = -9;
	public static final int DIFFICULTYMODERATE = -4;

	private static final float PCEQUIPMENTCRPERLEVEL = .2f;

	private static final int MAXIMUM_EL = 50;
	private static final int MINIMUM_EL = -7;

	public static final float[] CR_FRACTIONS = new float[] { 3.5f, 3f, 2.5f, 2f,
			1.75f, 1.5f, 1.25f, 1f, .5f, 0f, -.5f, -1f, -1.5f, -2f, -2.5f, -3 };
	public static final HdFactor HIT_DICE_FACTOR = new HdFactor();
	private static final CrFactor CLASS_LEVEL_FACTOR = new ClassLevelFactor();
	public static final CrFactor[] CR_FACTORS = new CrFactor[] {
			new AbilitiesFactor(), new ArmorClassFactor(), new FeatsFactor(),
			new FullAttackFactor(), HIT_DICE_FACTOR, new SizeFactor(),
			new SpeedFactor(), new SpellsFactor(), CLASS_LEVEL_FACTOR,
			new QualitiesFactor(), new BreathFactor(), new TouchAttackFactor(),
			new SkillsFactor() };
	private static final FileWriter LOGFILE;
	static final int[] XPPERLEVEL = new int[] { 0, 1000, 3000, 6000, 10000,
			15000, 21000, 28000, 36000, 45000, 55000, 66000, 78000, 91000,
			105000, 12000, 136000, 153000, 171000, 190000, };
	static final int[] GOLDPERLEVEL = new int[] { 0, 900, 2700, 5400, 9000,
			13000, 19000, 27000, 36000, 49000, 66000, 88000, 110000, 150000,
			200000, 260000, 340000, 440000, 580000, 760000, };

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

	/**
	 * See "challenging challenge ratings" source document (@ 'doc' folder),
	 * page 1: "HOW DO FACTORS WORK?". The silver rule isn't computed for at
	 * this stage the plan is not to use the PHB classes.
	 * 
	 * This is intended more for human-readable CR, if you need to make
	 * calculation you might prefer {@link #calculaterawcr(Monster)}.
	 * 
	 * Will also update {@link Monster#challengerating}.
	 * 
	 * @param monster
	 *            Unit to rate.
	 * @return The calculated CR.
	 */
	static public float calculatecr(final Monster monster) {
		float[] r = calculaterawcr(monster);
		float goldenrule = r[1];
		float base = goldenrule >= 4 ? Math.round(goldenrule)
				: roundfraction(goldenrule);
		float cr = translatecr(base);
		monster.challengerating = cr;
		log(" total: " + r[0] + " golden rule: " + goldenrule + " final: " + cr
				+ "\n");
		return cr;
	}

	/**
	 * Unlike {@link #calculatecr(Monster)} this method doesn't
	 * {@link #roundfraction(float)} or {@link #translatecr(float)}, making it
	 * more suitable for more precise calculations.
	 * 
	 * @param monster
	 *            Unit whose CR is to be calculated.
	 * @return An array where index 0 is the sum of all {@link #CR_FACTORS} and
	 *         1 is the same after the golden rule has been applied.
	 */
	public static float[] calculaterawcr(final Monster monster) {
		log(monster.toString());
		final TreeMap<CrFactor, Float> factorHistory = new TreeMap<CrFactor, Float>();
		float cr = 0;
		for (final CrFactor f : CR_FACTORS) {
			final float result = f.calculate(monster);
			log(" " + f + ": " + result);
			factorHistory.put(f, result);
			cr += result;
		}
		return new float[] { cr, goldenrule(factorHistory, cr) };
	}

	/**
	 * @param cr
	 *            Decimal CR.
	 * @return Similar result from allowed {@link #CR_FRACTIONS}.
	 */
	static float roundfraction(float cr) {
		for (final float limit : CR_FRACTIONS) {
			if (cr >= limit) {
				return limit;
			}
		}
		throw new RuntimeException("CR not correctly rounded: " + cr);
	}

	/**
	 * @param cr
	 *            Decimal CR (ex: -3).
	 * @return D&D-style cr (ex: 1/16).
	 */
	static float translatecr(float cr) {
		if (cr == .5) {
			return 2 / 3f;
		}
		if (cr == 0) {
			return 1 / 2f;
		}
		if (cr == -.5) {
			return 1 / 3f;
		}
		if (cr == -1) {
			return 1 / 4f;
		}
		if (cr == -1.5) {
			return 1 / 6f;
		}
		if (cr == -2) {
			return 1 / 8f;
		}
		if (cr == -2.5) {
			return 1 / 12f;
		}
		if (cr == -3) {
			return 1 / 16f;
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

	static private float goldenrule(
			final TreeMap<CrFactor, Float> factorHistory, final float cr) {
		final float hpCheck = factorHistory.get(HIT_DICE_FACTOR)
				+ factorHistory.get(CLASS_LEVEL_FACTOR);
		if (hpCheck < cr / 2) {
			final float twicehp = hpCheck * 2;
			return twicehp + (cr - twicehp) / 2;
		}
		return cr;
	}

	/**
	 * Same as {@link #calculateel(List)} but throws {@link UnbalancedTeams} as
	 * needed.
	 */
	public static int calculateelsafe(final List<Combatant> team)
			throws UnbalancedTeams {
		return calculateel(team, true);
	}

	/**
	 * @param group
	 *            Given a group of units...
	 * @return the calculated encounter level.
	 */
	public static int calculateel(final List<Combatant> group) {
		try {
			return calculateel(group, false);
		} catch (final UnbalancedTeams e) {
			throw new RuntimeException("shouldn't happen!");
		}
	}

	static int calculateel(final List<Combatant> group, final boolean check)
			throws UnbalancedTeams {
		float highestCr = Float.MIN_VALUE;
		float sum = 0;
		for (final Combatant mgc : group) {
			Monster mg = mgc.source;
			sum += mg.challengerating;
			if (mg.challengerating > highestCr) {
				highestCr = mg.challengerating;
			}
		}
		if (check) {
			for (final Combatant mgc : group) {
				Monster mg = mgc.source;
				if (highestCr - mg.challengerating > 18) {
					throw new UnbalancedTeams();
				}
			}
		}
		return calculatel(sum, highestCr, group.size());
	}

	public static int calculatel(float totalcr, float highestcr, int size) {
		final int groupCr = crtoel(totalcr) + multipleOpponentsElModifier(size);
		final int highestCrEl = crtoel(highestcr);
		return Math.max(highestCrEl, groupCr);
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
			return -7;
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
		throw new RuntimeException(
				"Expand multiple opponents EL table: " + teamSize);
	}

	public static int crtoel(final float cr) {
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

	public static float calculatepositive(final List<Combatant> group) {
		return calculateel(group) + Math.abs(MINIMUM_EL) + 1;
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
			return range(16, 19);
		case 18:
			return range(20, 23);
		case 19:
			return range(24, 27);
		case 20:
			return range(28, 31);
		case 21:
			return range(32, 39);
		case 22:
			return range(40, 47);
		case 23:
			return range(48, 55);
		case 24:
			return range(56, 63);
		case 25:
			return range(64, 79);
		case 26:
			return range(80, 95);
		case 27:
			return range(96, 111);
		case 28:
			return range(112, 127);
		case 29:
			return range(128, 159);
		case 30:
			return range(160, 191);
		default:
			if (Javelin.DEBUG) {
				throw new RuntimeException("Unknown EL " + teamel);
			}
			return eltocr(30);
		}
	}

	private static float[] range(int from, int to) {
		float[] range = new float[to - from + 1];
		int i = 0;
		for (; from <= to; from++) {
			range[i] = from;
			i += 1;
		}
		return range;
	}

	public static String describedifficulty(int delta) {
		if (delta <= -13) {
			return "irrelevant";
		}
		if (delta <= DIFFICULTYVERYEASY) {
			return "very easy";
		}
		if (delta <= -5) {
			return "easy";
		}
		if (delta == DIFFICULTYMODERATE) {
			return "moderate";
		}
		if (delta <= 0) {
			return "difficult";
		}
		if (delta <= +4) {
			return "very difficult";
		}
		return "impossible";
	}

	/**
	 * @param teamel
	 *            Active group encounter level.
	 * @param el
	 *            Opponent encounter level.
	 * @return % of resources used on battle.
	 */
	public static float useresources(int delta) {
		if (delta <= -12) {
			return .015f;
		}
		if (delta <= -11) {
			return .022f;
		}
		if (delta <= -10) {
			return .031f;
		}
		if (delta <= -9) {
			return .045f;
		}
		if (delta <= -8) {
			return .062f;
		}
		if (delta <= -7) {
			return .1f;
		}
		if (delta <= -6) {
			return .125f;
		}
		if (delta <= -5) {
			return .2f;
		}
		if (delta <= -4) {
			return .25f;
		}
		if (delta <= -3) {
			return .34f;
		}
		if (delta <= -2) {
			return .5f;
		}
		if (delta <= -1) {
			return .75f;
		}
		return 1;
	}

	/**
	 * @return Same as {@link #describedifficulty(int)} except compares given
	 *         {@link Combatant}s to {@link Squad#active}.
	 */
	public static String describedifficulty(List<Combatant> opponents) {
		return describedifficulty(calculateel(opponents)
				- CrCalculator.calculateel(Squad.active.members));
	}

	/** To use with the Buy the Numbers system, */
	static float xptocr(int xp) {
		int xptolevel = -1;
		int level = 1;
		for (; level < 20; level++) {
			if (XPPERLEVEL[level] >= xp) {
				xptolevel = XPPERLEVEL[level];
				break;
			}
		}
		if (xptolevel == -1) {
			throw new RuntimeException("Not enough levels #xptocr " + xp);
		}
		return level * (xp / new Float(xptolevel))
				* (1 - PCEQUIPMENTCRPERLEVEL);
	}

	/**
	 * @param gold
	 *            Given a certain amount of gold...
	 * @return the challenge rating that would warrant this treasure.
	 */
	public static int goldtocr(final float gold) {
		return Math.round(Math.round(Math.cbrt(gold / 7.5f)));
	}

	/**
	 * To use with the Buy the Numbers system. Covers passive abilities, for
	 * others see {@link #rateability(int, int, int, int)}.
	 * 
	 * Note than when adding abilities to Javelin this way you should carefully
	 * consider the prerequisite tree, like having Barbarian Rage before
	 * Tireless Rage.
	 * 
	 * @param prerequisite
	 *            Usually 0 but when converting an ability from a prestige class
	 *            this should be the minimum level required to enter that class.
	 * @param level
	 *            Level this ability is being taken from.
	 * @param adjustment
	 *            Usually 50% to 200% depending on the ability's usefulness.
	 * 
	 * @return Challenge rating for this ability.
	 */
	public float ratesimpleability(int prerequisite, int level,
			float adjustment) {
		return xptocr(Math.round(300 * (level + prerequisite) * adjustment));
	}

	/**
	 * For use with the Buy the Numbers system. Encompasses activated,
	 * level-dependent and abilities with a limit on use per day (usually one of
	 * the two, at least).
	 * 
	 * Note than when adding abilities to Javelin this way you should carefully
	 * consider the prerequisite tree, like having Barbarian Rage before
	 * Tireless Rage.
	 * 
	 * @param minimumlevel
	 *            The minimum level in which this ability can be accessed. If
	 *            from a prestige class, add the prestige class level to the
	 *            minimum level needed to enter that class.
	 * @param usesperday
	 *            How many times per day the ability can be used. 0 if the
	 *            ability is not limited in uses per day.
	 * @param startinglevel
	 *            If the ability is enhanced by leveling up this is the starting
	 *            levle it operates on. 0 otherwise. If the class is a prestige
	 *            class this is the pure prestige class level (not added to
	 *            minimumlevel).
	 * @param casterlevel
	 *            The current level this ability is operating on. 0 if not
	 *            dependent upon level. If the class is a prestige class this is
	 *            the pure prestige class level (not added to minimumlevel).
	 * @param adjustment
	 *            Usually 100 to 200% depending on the ability's usefulness.
	 * 
	 * @return Challenge rating.
	 */
	public float rateability(int minimumlevel, int usesperday,
			int startinglevel, int casterlevel, float adjustment) {
		int access = minimumlevel * (startinglevel != 0 ? 100 : 150);
		int basecost = access / 2;
		int xp = access; // access cost
		for (int useperday = 1; useperday <= usesperday; useperday++) {
			xp += useperday * basecost;
		}
		for (int level = startinglevel + 1; level <= casterlevel; level++) {
			xp += 50 * startinglevel;
		}
		return xptocr(Math.round(xp * adjustment));
	}

	/**
	 * @return the given level (typically from 1 to 20) to an encounter level.
	 */
	public static int leveltoel(int level) {
		return crtoel(level * 4) - 4;
	}

	/**
	 * I'm not sure if it's a typo or on purpose but .001 seems to be too low a
	 * factor, I'm using .01
	 */
	public static float ratespelllikeability(int spelllevel, int casterlevel) {
		return casterlevel * spelllevel * .01f;
	}

	/**
	 * @return challenge rating factor for the given spell level cast at the
	 *         minimum possible caster level.
	 */
	public static float ratespelllikeability(int spelllevel) {
		return CrCalculator.ratespelllikeability(spelllevel,
				Spell.calculatecasterlevel(spelllevel));
	}

	/**
	 * Same as {@link CrCalculator#ratespelllikeability(int)} but to be used in
	 * case a touch spell is being used as a ray spell instead.
	 */
	public static float ratetouchspellconvertedtoray(int spelllevel) {
		return .4f * spelllevel;
	}

	/**
	 * @param update
	 *            Updates {@link Monster#challengerating} for each unit in this
	 *            list.
	 */
	public static void updatecr(ArrayList<Combatant> update) {
		for (Combatant c : update) {
			calculatecr(c.source);
		}
	}
}
