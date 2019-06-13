package javelin.model.unit.skill;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * Represents a particular {@link Monster} skill, each offering a different
 * gameplay advantage (not necessarily combat-oriented). Monsters often come
 * with a few ranks in a set of skill by default but they can be upgraded like
 * most other game statistics.
 *
 * Progression is traditionally tied to a character class or monster type, with
 * bonuses for high Inteligence - but since Javelin is classless, they are
 * instead trained or untrained, with trained skills always being maximized
 * whenever a new class level is added.
 *
 * @see Monster#ranks
 * @see Monster#trained
 * @see SkillUpgrade
 *
 * @author alex
 */
public class Skill implements Serializable{
	/**
	 * TODO abilites should be handled like this skill redesign
	 */
	enum Ability{
		STRENGTH,DEXTERITY,CONSTITUTION,INTELLIGENCE,WISDOM,CHARISMA
	}

	/**
	 * Guideline DC values, from the SRD.
	 *
	 * @author alex
	 */
	public static class DifficultyClass{
		/** Diffculty class guideline. */
		public static final int DCVERYEASY=0;
		/** Diffculty class guideline. */
		public static final int DCEASY=5;
		/** Diffculty class guideline. */
		public static final int DCAVERAGE=10;
		/** Diffculty class guideline. */
		public static final int DCTOUGH=15;
		/** Diffculty class guideline. */
		public static final int DCCHALLENGING=20;
		/** Diffculty class guideline. */
		public static final int DCFORMIDABLE=25;
		/** Diffculty class guideline. */
		public static final int DCHEROIC=30;
		/** Diffculty class guideline. */
		public static final int DCNEARLYIMPOSSIBLE=40;
	}

	/**
	 * Prefer using {@link Skill#getupgrade()}.
	 */
	public static final HashMap<Skill,SkillUpgrade> UPGRADES=new HashMap<>();
	/**
	 * All skills by lowercase {@link Skill#name}.
	 */
	public static final HashMap<String,Skill> BYNAME=new HashMap<>();
	/** All skills available in Javelin. */
	public static final HashSet<Skill> ALL=new HashSet<>();

	/** Singleton. */
	public static final Skill ACROBATICS=new Acrobatics();
	/** Singleton. */
	public static final Skill BLUFF=new Bluff();
	/** Singleton. */
	public static final Skill CONCENTRATION=new Concentration();
	/** Singleton. */
	public static final Skill DIPLOMACY=new Diplomacy();
	/** Singleton. */
	public static final Skill DISABLEDEVICE=new DisableDevice();
	/** Singleton. */
	public static final Skill DISGUISE=new Disguise();
	/** Singleton. */
	public static final Skill HEAL=new Heal();
	/** Singleton. */
	public static final Skill KNOWLEDGE=new Knowledge();
	/** Singleton. */
	public static final Skill PERCEPTION=new Perception();
	/** Singleton. */
	public static final Skill SENSEMOTIVE=new SenseMotive();
	/** Singleton. */
	public static final Skill SPELLCRAFT=new Spellcraft();
	/** Singleton. */
	public static final Skill STEALTH=new Stealth();
	/** Singleton. */
	public static final Skill SURVIVAL=new Survival();
	/** Singleton. */
	public static final Skill USEMAGICDEVICE=new UseMagicDevice();

	/**
	 * Makes a skill trained, up to 3+int bonus trained skill per unit.
	 *
	 * @see Skill
	 * @author alex
	 */
	public class SkillUpgrade extends Upgrade{
		SkillUpgrade(){
			super("Skill: "+Skill.this.name.toLowerCase());
			usedincombat=Skill.this.usedincombat;
		}

		@Override
		public String inform(Combatant c){
			return "Currently: "+Skill.this.name+" "+getsignedbonus(c)+" (untrained)";
		}

		@Override
		protected boolean apply(Combatant c){
			Monster m=c.source;
			if(m.trained.contains(Skill.this.name)) return false;
			if(m.trained.size()>=3+Monster.getbonus(m.intelligence)) return false;
			if(!canuse(c)) return false;
			m.trained.add(Skill.this.name);
			maximize(m);
			return true;
		}

		@Override
		public int hashCode(){
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj){
			return obj instanceof SkillUpgrade&&((SkillUpgrade)obj).name.equals(name);
		}
	}

	/** Name as per d20 SRD. */
	public String name;
	/**
	 * Relevant ability.
	 *
	 * @see #getabilitybonus(Monster)
	 */
	public Ability ability;
	/** @see Upgrade#usedincombat */
	public boolean usedincombat=false;
	/**
	 * If <code>true</code>, will not allow units with low intelligence to train
	 * this.
	 */
	public boolean intelligent=false;

	Skill(String[] names,Ability a,Realm realm){
		name=names[0].toLowerCase();
		ability=a;
		SkillUpgrade upgrade=new SkillUpgrade();
		realm.getupgrades(UpgradeHandler.singleton).add(upgrade);
		ALL.add(this);
		UPGRADES.put(this,upgrade);
		for(String name:names)
			BYNAME.put(name.toLowerCase(),this);
	}

	Skill(String name,Ability a,Realm realm){
		this(new String[]{name},a,realm);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o){
		return o instanceof Skill&&name.equals(((Skill)o).name);
	}

	/**
	 * Subclasses often use this to look up relevant {@link Feat} bonuses or
	 * modify this result.
	 *
	 * @return Total bonus, including rank, ability modifier and condition
	 *         modifiers.
	 *
	 * @see Combatant#skillmodifier
	 */
	public int getbonus(Combatant c){
		return getranks(c)+getabilitybonus(c.source)+c.skillmodifier;
	}

	/**
	 * @return Relevant {@link #ability} modifier.
	 * @see Monster#getbonus(int)
	 */
	public int getabilitybonus(Monster m){
		return Monster.getbonus(getabilityvalue(m));
	}

	/** @see Monster#ranks */
	public int getranks(Combatant c){
		return getranks(c.source);
	}

	/** @see Monster#ranks */
	public int getranks(Monster m){
		Integer ranks=m.ranks.get(name);
		return ranks==null?0:ranks;
	}

	int getabilityvalue(Monster m){
		if(ability==Ability.STRENGTH) return m.strength;
		if(ability==Ability.DEXTERITY) return m.dexterity;
		if(ability==Ability.CONSTITUTION) return m.constitution;
		if(ability==Ability.INTELLIGENCE) return m.intelligence;
		if(ability==Ability.WISDOM) return m.wisdom;
		if(ability==Ability.CHARISMA) return m.charisma;
		throw new RuntimeException("#unknownability "+ability);
	}

	/**
	 * @param ranks Raises the number or tanks in this skill by this amount.
	 * @param m Target unit.
	 * @see #setoriginal(int, Monster)
	 * @see #maximize(Monster)
	 */
	public void raise(int ranks,Monster m){
		setranks(getranks(m)+ranks,m);
	}

	/**
	 * Used to set from {@link MonsterReader}. Will not overwrite previous lesser
	 * values.
	 *
	 * @param ranks Final bonus as shown in stat block, with ability modifier.
	 * @param m Target unit.
	 * @see #raise(int, Monster)
	 */
	public void setoriginal(int ranks,Monster m){
		ranks-=getabilitybonus(m);
		if(ranks>getranks(m)) setranks(ranks,m);
	}

	void setranks(int ranks,Monster m){
		m.ranks.put(name,Math.max(0,ranks));
	}

	/**
	 * @param m Raises the skill's ranks to its maximum value possible given
	 *          {@link Monster#hd}. Will not overwrite previously-higher values.
	 */
	public void maximize(Monster m){
		int max=m.hd.count()+3;
		if(max>getranks(m)) setranks(max,m);
	}

	/**
	 * Signed version of {@link #getbonus(Combatant)}.
	 *
	 * @see #getsigned(int)
	 */
	public String getsignedbonus(Combatant c){
		return getsigned(getbonus(c));
	}

	/**
	 * @param c Needs to be intelligent...
	 * @return if this is an {@link #intelligent} skill.
	 *
	 * @see Monster#think(int)
	 */
	public boolean canuse(Combatant c){
		return !intelligent||c.source.think(-2);
	}

	/**
	 * @return An upgrade to make this skill trained.
	 * @see Monster#trained
	 */
	public SkillUpgrade getupgrade(){
		return UPGRADES.get(this);
	}

	/**
	 * @return Given input in a signed format (+1, -3, +0...).
	 */
	public static String getsigned(int bonus){
		return bonus>=0?"+"+bonus:Integer.toString(bonus);
	}

	/**
	 * Loads all skills and register their upgrades.
	 *
	 * @see #getupgrade()
	 */
	public static void setup(){
		// does nothing, just ensures class is fully loaded
	}
}
