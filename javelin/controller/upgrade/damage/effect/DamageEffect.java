package javelin.controller.upgrade.damage.effect;

import java.util.ArrayList;
import java.util.List;

import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.attack.Attack;
import javelin.model.world.location.town.Town;

/**
 * A spell-like effect to be applied after damage.
 *
 * @see Attack
 * @author alex
 */
public class DamageEffect{
	/** @see Doom */
	public static final DamageEffect FEAR=new DamageEffect("fear",new Doom(),
			Realm.EVIL);
	/** @see HoldMonster */
	public static final DamageEffect PARALYSIS=new DamageEffect("paralysis",
			new HoldMonster(),Realm.EARTH);
	/** @see Poison */
	public static final DamageEffect POISON=new DamageEffect("poison",
			new Poison(),Realm.EVIL);
	/** All supported damage effects */
	public static final ArrayList<DamageEffect> EFFECTS=new ArrayList<>();

	/**
	 * Call before using {@link #EFFECTS}.
	 */
	static public void setup(){
		if(EFFECTS.isEmpty()) EFFECTS.addAll(List.of(PARALYSIS,FEAR,POISON));
	}

	/** Name of the {@link Spell}-like effect. */
	public String name;
	/**
	 * @see Spell#cast(javelin.model.unit.Combatant, javelin.model.unit.Combatant,
	 *      javelin.model.state.BattleState, boolean).
	 */
	public Spell spell;
	/**
	 * In which {@link Town} this will be featured as an {@link EffectUpgrade}.
	 */
	public Realm realm;

	/** Constructor. */
	DamageEffect(String name,Spell s,Realm r){
		this.name=name;
		spell=s;
		realm=r;
	}

	@Override
	public String toString(){
		return name;
	}
}
