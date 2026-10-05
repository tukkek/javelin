package javelin.controller.content.upgrade.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.factor.ClassLevelFactor;
import javelin.controller.challenge.factor.SkillsFactor;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.skill.Skill;
import javelin.old.RPG;

/**
 * Levels up in an NPC class.
 *
 * @author alex
 */
public abstract class ClassLevelUpgrade extends Upgrade{
	/**
	 * Contains all full-fledged (20 level) classes found in the game. Needs to be
	 * initialized with {@link #setup()} to prevent racy initialization.
	 */
	public static ClassLevelUpgrade[] classes=null;

	public static void setup(){
		if(classes==null) classes=new ClassLevelUpgrade[]{Commoner.SINGLETON,
				Aristocrat.SINGLETON,Warrior.SINGLETON,Expert.SINGLETON};
	}

	/** Table of improvements per level as per the SRD. */
	public final Level[] table;
	/**
	 * How many skills points are gained at each level.
	 *
	 * @see SkillsFactor
	 */
	public final int skillrate;
	/** Name of the skill. */
	public final String descriptivename;
	final int hd;
	/** @see ClassLevelFactor */
	public float crperlevel;
	/**
	 * A linear expression of a class' Base Attack Bonus advancement per level.
	 * It's OK not to be exact as it's prefereable for allowing a gradual
	 * improvement to BAB over time than requiring a player to advance in only a
	 * single class, which may require revisiting academies, bookkeeping of which
	 * character is which class, etc.
	 *
	 * TODO saves should probably be done like this too...
	 *
	 * @see Monster#babpartial
	 */
	float babprogression;

	public ClassLevelUpgrade(String name,float bab,Level[] tablep,int hdp,
			int skillratep,float crperlevelp){
		super("Class: "+name.toLowerCase());
		descriptivename=name;
		babprogression=bab;
		table=tablep;
		if(Javelin.DEBUG&&table.length!=21)
			throw new RuntimeException("#>20levels");
		hd=hdp;
		skillrate=skillratep;
		crperlevel=crperlevelp;
	}

	@Override
	public String inform(Combatant m){
		return "Current level: "+getlevel(m.source);
	}

	public abstract int getlevel(Monster m);

	abstract void setlevel(int level,Monster m);

	@Override
	public boolean apply(Combatant c){
		Monster m=c.source;
		int level=getlevel(m)+1;
		if(level>=table.length) return false;
		setlevel(level,m);
		int bonus=m.constitution>0?Monster.getbonus(m.constitution):0;
		m.hd.add(1.0f,hd,bonus);
		int hp=RPG.r(1,hd)+bonus;
		if(hp<1) hp=1;
		c.hp+=hp;
		c.maxhp+=hp;
		Level next=table[level];
		Level last=table[level-1];
		c.source.babpartial+=advancebab(level);
		if(c.source.babpartial>=1) levelbabup(c,m);
		m.fort+=next.fort-last.fort;
		m.ref+=next.ref-last.ref;
		m.addwill(next.will-last.will);
		for(String skill:m.trained)
			Skill.BYNAME.get(skill).maximize(m);
		return true;
	}

	public void levelbabup(Combatant c,Monster m){
		c.source.babpartial-=1;
		advanceattack(1,m.melee);
		advanceattack(1,m.ranged);
		int newattackbonusdelta=checkfornewattack(m,1);
		if(newattackbonusdelta!=0){
			upgradeattack(m.melee,newattackbonusdelta);
			upgradeattack(m.ranged,newattackbonusdelta);
		}
	}

	public float advancebab(int level){
		return babprogression;
	}

	public void advanceattack(int bab,ArrayList<AttackSequence> melee){
		for(List<Attack> attacks:melee)
			for(Attack a:attacks)
				a.bonus+=bab;
	}

	private void upgradeattack(List<AttackSequence> sequences,
			int newattackbonusdelta){
		for(AttackSequence sequence:sequences){
			Attack a=sequence.get(0).clone();
			a.bonus-=newattackbonusdelta;
			sequence.add(a);
		}
	}

	/**
	 * Prevents an upgrade to reach a level where the character will receive a new
	 * attack per turn, which would need to modify the {@link Monster} to the
	 * point of creating new attacks, calculating 2-handed weapon bonuses...
	 *
	 * TODO can be converted to a {@link HashMap}
	 */
	public int checkfornewattack(Monster m,int babdelta){
		switch(m.getbab()+babdelta){
			case 6:
				return 5;
			case 11:
				return 10;
			case 16:
				return 15;
			default:
				return 0;
		}
	}

	public static ClassLevelUpgrade getpreferred(Combatant c){
		setup();
		ArrayList<ClassLevelUpgrade> preferred=new ArrayList<>(1);
		for(ClassLevelUpgrade u:classes)
			if(u.prefer(c)) preferred.add(u);
		return RPG.pick(preferred);
	}

	protected static int gethighestability(Monster m){
		int highest=0;
		for(int ability:new int[]{m.strength,m.dexterity,m.constitution,
				m.intelligence,m.wisdom,m.charisma})
			highest=Math.max(highest,ability);
		return highest;
	}

	abstract protected boolean prefer(Combatant c);
}