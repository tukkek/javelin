package javelin.controller.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import javelin.controller.challenge.factor.TouchAttackFactor;
import javelin.controller.challenge.factor.quality.BreathFactor;
import javelin.controller.challenge.factor.quality.QualitiesFactor;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.exception.UnbalancedTeams;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.RPG;

/**
 * Determines a {@link Monster#cr} according to the rules of Upper Krust's work,
 * which is repackaged with permission on the 'doc' directory.
 *
 * His reference is used to the best of my abilities but has been adapted in a
 * few cases due to programming complexity and artificial intelligence
 * efficiency. Such cases should be documented in Javadoc.
 *
 * @see CrFactor
 * @see Difficulty
 * @see RewardCalculator
 */
public class ChallengeCalculator{
	static final float PCEQUIPMENTCRPERLEVEL=.2f;

	static final int MINIMUM_EL=-7;
	/**
	 * This isn't part of the CCR document but it's obvious that the system was
	 * "rewarding" very large groups of creatures with low CRs so this is used to
	 * modifiy the group-size table by a factor. Since this affects all parties
	 * equally, it should be more of an adjustment than a game changer but it
	 * might need to be more deeply rethought if the problem persists despite
	 * adjustments.
	 */
	static final int GROUPFACTOR=3;

	public static final float[] CR_FRACTIONS=new float[]{3.5f,3f,2.5f,2f,1.75f,
			1.5f,1.25f,1f,.5f,0f,-.5f,-1f,-1.5f,-2f,-2.5f,-3};
	public static final HdFactor HIT_DICE_FACTOR=new HdFactor();
	private static final CrFactor CLASS_LEVEL_FACTOR=new ClassLevelFactor();
	public static final CrFactor[] CR_FACTORS=new CrFactor[]{new SkillsFactor(),
			new AbilitiesFactor(),new ArmorClassFactor(),new FeatsFactor(),
			new FullAttackFactor(),HIT_DICE_FACTOR,new SizeFactor(),new SpeedFactor(),
			new SpellsFactor(),CLASS_LEVEL_FACTOR,new QualitiesFactor(),
			new BreathFactor(),new TouchAttackFactor(),};
	static final int[] XPPERLEVEL=new int[]{0,1000,3000,6000,10000,15000,21000,
			28000,36000,45000,55000,66000,78000,91000,105000,12000,136000,153000,
			171000,190000,};
	static final int[] GOLDPERLEVEL=new int[]{0,900,2700,5400,9000,13000,19000,
			27000,36000,49000,66000,88000,110000,150000,200000,260000,340000,440000,
			580000,760000,};
	static final HashMap<Integer,List<Float>> CRSBYEL=new HashMap<>();

	static{
		log("Some monsters may have their CRs calculated in more tha one pass (necessary for summons spells, etc)\n");
		ArrayList<Float> crs=new ArrayList<>(8+30);
		for(float fraction:new float[]{1/16f,1/12f,1/8f,1/6f,1/4f,1/3f,1/2f,2/3f})
			crs.add(fraction);
		for(int cr=1;cr<=30;cr++)
			crs.add(new Float(cr));
		for(float cr:crs){
			int el=crtoel(cr);
			List<Float> list=CRSBYEL.get(el);
			if(list==null){
				list=new ArrayList<>(1);
				CRSBYEL.put(el,list);
			}
			list.add(cr);
		}

	}

	/**
	 * See "challenging challenge ratings" source document (@ 'doc' folder), page
	 * 1: "HOW DO FACTORS WORK?". The silver rule isn't computed for at this stage
	 * the plan is not to use the PHB classes.
	 *
	 * This is intended more for human-readable CR, if you need to make
	 * calculation you might prefer {@link #calculaterawcr(Monster)}.
	 *
	 * Will also update {@link Monster#cr}.
	 *
	 * @param m Unit to rate.
	 * @return The calculated CR.
	 */
	static public float calculatecr(final Monster m){
		if(m.passive) return m.cr;
		float[] r=calculaterawcr(m);
		float goldenrule=r[1];
		float base=goldenrule>=4?Math.round(goldenrule):roundfraction(goldenrule);
		float cr=translatecr(base);
		m.cr=cr;
		log(" total: "+r[0]+" golden rule: "+goldenrule+" final: "+cr+"\n");
		return cr;
	}

	/**
	 * Unlike {@link #calculatecr(Monster)} this method doesn't
	 * {@link #roundfraction(float)} or {@link #translatecr(float)}, making it
	 * more suitable for more precise calculations.
	 *
	 * @param m Unit whose CR is to be calculated.
	 * @return An array where index 0 is the sum of all {@link #CR_FACTORS} and 1
	 *         is the same after the golden rule has been applied.
	 */
	public static float[] calculaterawcr(final Monster m){
		if(m.passive) return new float[]{m.cr,m.cr};
		log(m.toString());
		final TreeMap<CrFactor,Float> factorHistory=new TreeMap<>();
		float cr=0;
		for(final CrFactor f:CR_FACTORS){
			final float result=f.calculate(m);
			if(Javelin.DEBUG&&result!=0) log(" "+f+": "+result+" "+f.log(m));
			factorHistory.put(f,result);
			cr+=result;
		}
		return new float[]{cr,goldenrule(factorHistory,cr)};
	}

	/**
	 * @param cr Decimal CR.
	 * @return Similar result from allowed {@link #CR_FRACTIONS}.
	 */
	static float roundfraction(float cr){
		for(final float limit:CR_FRACTIONS)
			if(cr>=limit) return limit;
		throw new RuntimeException("CR not correctly rounded: "+cr);
	}

	/**
	 * @param cr Decimal CR (ex: -3).
	 * @return D&D-style cr (ex: 1/16).
	 */
	static float translatecr(float cr){
		if(cr==.5) return 2/3f;
		if(cr==0) return 1/2f;
		if(cr==-.5) return 1/3f;
		if(cr==-1) return 1/4f;
		if(cr==-1.5) return 1/6f;
		if(cr==-2) return 1/8f;
		if(cr==-2.5) return 1/12f;
		if(cr==-3) return 1/16f;
		return cr;
	}

	static void log(final String s){
		if(MonsterReader.logs!=null) MonsterReader.log(s,"crs.log");
	}

	static private float goldenrule(final TreeMap<CrFactor,Float> factorHistory,
			final float cr){
		final float hpCheck=factorHistory.get(HIT_DICE_FACTOR)
				+factorHistory.get(CLASS_LEVEL_FACTOR);
		if(hpCheck<cr/2){
			final float twicehp=hpCheck*2;
			return twicehp+(cr-twicehp)/2;
		}
		return cr;
	}

	/**
	 * Same as {@link #calculateel(List)} but throws {@link UnbalancedTeams} as
	 * needed.
	 */
	public static int calculateelsafe(final List<Combatant> team)
			throws UnbalancedTeams{
		return calculateel(team,true);
	}

	/**
	 * @param group Given a group of units...
	 * @return the calculated encounter level.
	 */
	public static int calculateel(final List<Combatant> group){
		try{
			return calculateel(group,false);
		}catch(final UnbalancedTeams e){
			throw new RuntimeException("Unbalanced teams #crcalculator");
		}
	}

	static int calculateel(List<Combatant> group,boolean check)
			throws UnbalancedTeams{
		List<Float> crs=group.stream().map(c->c.source.cr)
				.collect(Collectors.toList());
		return calculateelfromcrs(crs,check);
	}

	public static int calculateelfromcrs(List<Float> crs,boolean check)
			throws UnbalancedTeams{
		double highestcr=Float.MIN_VALUE;
		double sum=0;
		for(final float cr:crs){
			if(cr>highestcr) highestcr=cr;
			sum+=cr>=2?Math.pow(2,cr/2.0):cr;
		}
		if(check) for(final float cr:crs)
			if(Math.abs(highestcr-cr)>18) throw new UnbalancedTeams();
		if(sum>=2) sum=2*Math.log(sum)/Math.log(2);
		return translateelfraction(sum);
	}

	public static int translateelfraction(double el){
		if(el<=1/16f) return MINIMUM_EL;
		if(el<=1/12f) return -6;
		if(el<=1/8f) return -5;
		if(el<=1/6f) return -4;
		if(el<=1/4f) return -3;
		if(el<=1/3f) return -2;
		if(el<=1/2f) return -1;
		if(el<=2/3f) return 0;
		return Math.round(Math.round(el));
	}

	public static int crtoel(final float cr){
		try{
			return calculateelfromcrs(List.of(cr),false);
		}catch(UnbalancedTeams e){
			throw new RuntimeException("Shouldn't happen #crtoel",e);
		}
	}

	public static float calculatepositiveel(final List<Combatant> group){
		return calculateel(group)+Math.abs(MINIMUM_EL)+1;
	}

	public static List<Float> eltocrs(int el){
		List<Float> crs=CRSBYEL.get(el);
		if(crs!=null) return crs;
		if(Javelin.DEBUG) throw new RuntimeException("Unknown EL "+el);
		return eltocrs(30);
	}

	public static Float eltocr(int el){
		return RPG.pick(eltocrs(el));
	}

	/**
	 * @param delta EL of enemies minus EL of {@link Squad}.
	 * @return % of resources used on battle.
	 */
	public static float useresources(int delta){
		if(delta<=-12) return .015f;
		if(delta<=-11) return .022f;
		if(delta<=-10) return .031f;
		if(delta<=-9) return .045f;
		if(delta<=-8) return .062f;
		if(delta<=-7) return .1f;
		if(delta<=-6) return .125f;
		if(delta<=-5) return .2f;
		if(delta<=-4) return .25f;
		if(delta<=-3) return .34f;
		if(delta<=-2) return .5f;
		if(delta<=-1) return .75f;
		return 1;
	}

	/** To use with the Buy the Numbers system, */
	static float xptocr(int xp){
		int xptolevel=-1;
		int level=1;
		for(;level<20;level++)
			if(XPPERLEVEL[level]>=xp){
				xptolevel=XPPERLEVEL[level];
				break;
			}
		if(xptolevel==-1)
			throw new RuntimeException("Not enough levels #xptocr "+xp);
		return level*(xp/new Float(xptolevel))*(1-PCEQUIPMENTCRPERLEVEL);
	}

	/**
	 * @param gold Given a certain amount of gold...
	 * @return the challenge rating that would warrant this treasure.
	 */
	public static int goldtocr(final float gold){
		return Math.round(Math.round(Math.cbrt(gold/7.5f)));
	}

	/**
	 * To use with the Buy the Numbers system. Covers passive abilities, for
	 * others see {@link #rateability(int, int, int, int)}.
	 *
	 * Note than when adding abilities to Javelin this way you should carefully
	 * consider the prerequisite tree, like having Barbarian Rage before Tireless
	 * Rage.
	 *
	 * @param prerequisite Usually 0 but when converting an ability from a
	 *          prestige class this should be the minimum level required to enter
	 *          that class.
	 * @param level Level this ability is being taken from.
	 * @param adjustment Usually 50% to 200% depending on the ability's
	 *          usefulness.
	 *
	 * @return Challenge rating for this ability.
	 */
	public float ratesimpleability(int prerequisite,int level,float adjustment){
		return xptocr(Math.round(300*(level+prerequisite)*adjustment));
	}

	/**
	 * For use with the Buy the Numbers system. Encompasses activated,
	 * level-dependent and abilities with a limit on use per day (usually one of
	 * the two, at least).
	 *
	 * Note than when adding abilities to Javelin this way you should carefully
	 * consider the prerequisite tree, like having Barbarian Rage before Tireless
	 * Rage.
	 *
	 * @param minimumlevel The minimum level in which this ability can be
	 *          accessed. If from a prestige class, add the prestige class level
	 *          to the minimum level needed to enter that class.
	 * @param usesperday How many times per day the ability can be used. 0 if the
	 *          ability is not limited in uses per day.
	 * @param startinglevel If the ability is enhanced by leveling up this is the
	 *          starting levle it operates on. 0 otherwise. If the class is a
	 *          prestige class this is the pure prestige class level (not added to
	 *          minimumlevel).
	 * @param casterlevel The current level this ability is operating on. 0 if not
	 *          dependent upon level. If the class is a prestige class this is the
	 *          pure prestige class level (not added to minimumlevel).
	 * @param adjustment Usually 100 to 200% depending on the ability's
	 *          usefulness.
	 *
	 * @return Challenge rating.
	 */
	public float rateability(int minimumlevel,int usesperday,int startinglevel,
			int casterlevel,float adjustment){
		int access=minimumlevel*(startinglevel!=0?100:150);
		int basecost=access/2;
		int xp=access; // access cost
		for(int useperday=1;useperday<=usesperday;useperday++)
			xp+=useperday*basecost;
		for(int level=startinglevel+1;level<=casterlevel;level++)
			xp+=50*startinglevel;
		return xptocr(Math.round(xp*adjustment));
	}

	/**
	 * Rates a {@link Spell} or Spell-like ability. Multiple uses
	 * {@link Spell#perday} should be multiplied externally.
	 *
	 * I'm not sure if it's a typo or on purpose but .001 seems to be too low a
	 * factor, I'm using .01 instead.
	 *
	 * @return challenge rating factor for the given spell level cast at the given
	 *         caster level.
	 */
	public static float ratespell(int spelllevel,int casterlevel){
		return casterlevel*spelllevel*.01f;
	}

	/**
	 * @return As {@link #ratespell(int, int)} but with the minimum possible
	 *         caster level.
	 */
	public static float ratespell(int spelllevel){
		var casterlevel=Spell.getcasterlevel(spelllevel);
		return ChallengeCalculator.ratespell(spelllevel,casterlevel);
	}

	/**
	 * Same as {@link ChallengeCalculator#ratespell(int)} but to be used in case a
	 * touch spell is being used as a ray spell instead.
	 */
	public static float ratetouchspellconvertedtoray(int spelllevel){
		return .4f*spelllevel;
	}

	/**
	 * @param update Updates {@link Monster#cr} for each unit in this list.
	 */
	public static void updatecr(ArrayList<Combatant> update){
		for(Combatant c:update)
			calculatecr(c.source);
	}

	public static int calculateelfromcrs(List<Float> crs){
		try{
			return calculateelfromcrs(crs,false);
		}catch(UnbalancedTeams e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sadly throughout Javelin's code not all variables and fields are properly
	 * named between "level" (average character level in a {@link Squad}) and "el"
	 * (Encounter Level, which is a measure of a group's party). "Level" is the
	 * most common way to describe the overall intended audience for a piece of
	 * content ("this adventure is for characters of level 1-5") while EL is by
	 * definition what should be used for most internal calculations, since it
	 * takes the exact number and challenge rating of each {@link Combatant} into
	 * consideration to get an exact power description.
	 *
	 * "Party level" and "encounter level" can easily be translated from one to
	 * another but they are not equivalent, since passing a party level to a
	 * parameter expecting an ecounter level will produce unintended results 100%
	 * of the time. As such, variables and fields should be named "level" and "el"
	 * to clearly distinguish between each.
	 *
	 * @return Given an average party level, the corresponding Encounter Level for
	 *         a standard party of 3-5 characters.
	 */
	public static int getel(int level){
		return level+4;
	}
}
