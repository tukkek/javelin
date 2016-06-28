package javelin.controller.upgrade.classes;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

/**
 * Levels up in an NPC class.
 * 
 * @author alex
 */
public abstract class ClassAdvancement extends Upgrade {
	public static final Warrior WARRIOR = new Warrior();
	public static final Expert EXPERT = new Expert();
	public static final Aristocrat ARISTOCRAT = new Aristocrat();
	public static final Commoner COMMONER = new Commoner();
	public static final ClassAdvancement[] CLASSES =
			new ClassAdvancement[] { COMMONER, ARISTOCRAT, WARRIOR, EXPERT };
	public final String descriptivename;

	public ClassAdvancement(String name) {
		super("Class: " + name.toLowerCase());
		descriptivename = name;
	}

	@Override
	public String info(Combatant m) {
		return "Current level: " + getlevel(m.source);
	}

	public abstract int getlevel(Monster m);

	abstract int gethd();

	public abstract Level[] gettable();

	abstract void setlevel(int level, Monster m);

	@Override
	public boolean apply(Combatant c) {
		Monster m = c.source;
		int level = getlevel(m) + 1;
		Level[] table = gettable();
		if (level >= table.length) {
			return false;
		}
		setlevel(level, m);
		int hd = gethd();
		int bonus = m.constitution > 0 ? Monster.getbonus(m.constitution) : 0;
		m.hd.add(1.0f, hd, bonus);
		int hp = RPG.r(1, hd) + bonus;
		if (hp < 1) {
			hp = 1;
		}
		c.hp += hp;
		c.maxhp += hp;
		Level next = table[level];
		Level last = table[level - 1];
		int bab = next.bab - last.bab;
		advanceattack(bab, m.melee);
		advanceattack(bab, m.ranged);
		int newattackbonusdelta = checkfornewattack(m, bab);
		if (newattackbonusdelta != 0) {
			upgradeattack(m.melee, newattackbonusdelta);
			upgradeattack(m.ranged, newattackbonusdelta);
		}
		m.fort += next.fort - last.fort;
		m.ref += next.ref - last.ref;
		m.addwill(next.will - last.will);
		return true;
	}

	public void advanceattack(int bab, ArrayList<AttackSequence> melee) {
		for (List<Attack> attacks : melee) {
			for (Attack a : attacks) {
				a.bonus += bab;
			}
		}
	}

	private void upgradeattack(List<AttackSequence> sequences,
			int newattackbonusdelta) {
		for (AttackSequence sequence : sequences) {
			Attack a = sequence.get(0).clone();
			a.bonus -= newattackbonusdelta;
			sequence.add(a);
		}
	}

	/**
	 * Prevents an upgrade to reach a level where the character will receive a
	 * new attack per turn, which would need to modify the {@link Monster} to
	 * the point of creating new attacks, calculating 2-handed weapon bonuses...
	 */
	public int checkfornewattack(Monster m, int babdelta) {
		switch (m.getbaseattackbonus() + babdelta) {
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
}