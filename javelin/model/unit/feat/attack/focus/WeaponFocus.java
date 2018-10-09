package javelin.model.unit.feat.attack.focus;

import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.db.reader.fields.Feats;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class WeaponFocus extends Feat{
	/**
	 * Map of base attack bonus increment per level by lower-case monster type.
	 */
	public static final TreeMap<MonsterType,Double> BAB=new TreeMap<>();
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON=new WeaponFocus();

	static{
		BAB.put(MonsterType.ABERRATION,3.0/4.0);
		BAB.put(MonsterType.ANIMAL,3.0/4.0);
		BAB.put(MonsterType.CONSTRUCT,3.0/4.0);
		BAB.put(MonsterType.DRAGON,1.0);
		BAB.put(MonsterType.ELEMENTAL,3.0/4.0);
		BAB.put(MonsterType.FEY,1.0/2.0);
		BAB.put(MonsterType.GIANT,3.0/4.0);
		BAB.put(MonsterType.HUMANOID,3.0/4.0);
		BAB.put(MonsterType.MAGICAL_BEAST,1.0);
		BAB.put(MonsterType.SHAPECHANGER,1.0);
		BAB.put(MonsterType.MONSTROUS_HUMANOID,1.0);
		BAB.put(MonsterType.OOZE,3.0/4.0);
		BAB.put(MonsterType.OUTSIDER,1.0);
		BAB.put(MonsterType.PLANT,3.0/4.0);
		BAB.put(MonsterType.UNDEAD,1.0/2.0);
		BAB.put(MonsterType.VERMIN,3.0/4.0);
	}

	WeaponFocus(String name){
		super(name);
	}

	/** Constructor. */
	private WeaponFocus(){
		super("Weapon focus");
	}

	@Override
	public String inform(final Combatant m){
		return "Base attack bonus: "+m.source.getbab();
	}

	int countattacks(final Monster m){
		final HashSet<String> attacks=new HashSet<>();
		for(final List<Attack> as:getattacks(m))
			for(final Attack a:as)
				attacks.add(a.name);
		return attacks.size();
	}

	@Override
	public boolean upgrade(final Combatant c){
		Monster m=c.source;
		List<AttackSequence> attacks=getattacks(m);
		if(m.hasfeat(this)||attacks==null||attacks.isEmpty()||m.getbab()<1)
			return false;
		for(int i=0;i<countattacks(m);i++)
			super.upgrade(c);
		for(final List<Attack> as:attacks)
			for(final Attack a:as)
				a.bonus+=1;
		return true;
	}

	/**
	 * @return {@link Monster#ranged} or {@link Monster#melee}. The default
	 *         implementation returns <code>null</code> so {@link Feats} can read
	 *         monster with generic Weapon Focus feats.
	 */
	protected List<AttackSequence> getattacks(final Monster m){
		return null;
	}
}
