package javelin.controller.content.upgrade.damage.effect;

import java.util.HashSet;

import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Adds an effect to all melee attacks. A {@link Kit} should ever only have one
 * of thse.
 *
 * @author alex
 */
public class EffectUpgrade extends Upgrade{
	Spell effect;

	/**
	 * @param name Upgrade name.
	 * @param effect Effect to bestow when a melee attack hits.
	 */
	public EffectUpgrade(String name,Spell effect){
		super("Damage effect: "+name.toLowerCase());
		this.effect=effect;
	}

	/** Uses spell name as upgrade name. */
	public EffectUpgrade(DamageEffect e){
		this(e.name,e.spell.clone());
	}

	@Override
	public String inform(Combatant c){
		var effects=new HashSet<String>();
		for(var a:c.source.getattacks()){
			var e=a.geteffect();
			if(e!=null) effects.add(e.name);
		}
		if(effects.isEmpty()) return "";
		return "Replaces "+String.join(", ",effects);
	}

	@Override
	protected boolean apply(Combatant c){
		var upgraded=false;
		for(var a:c.source.getattacks())
			if(!effect.equals(a.geteffect())){
				a.seteffect(effect);
				upgraded=true;
			}
		return upgraded;
	}
}
