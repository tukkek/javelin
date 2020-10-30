package javelin.model.unit.condition.abilitydamage;

import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Poisoned;

/**
 * TODO convert abilities (and skills) to Enum-based {@link Combatant} fields.
 *
 * TODO {@link Poisoned} can be handled like this?
 *
 * TODO handle abilities getting to 0 and below
 *
 * @author alex
 */
public abstract class AbilityDamage extends Condition{
	int damagepool=0;
	int timepool;
	int damage;

	public AbilityDamage(int damage,Combatant c,String description){
		super(description,null,Float.MAX_VALUE,Integer.MAX_VALUE,Effect.NEGATIVE);
		this.damage=damage;
	}

	@Override
	public void start(Combatant c){
		damage(damage,c);
	}

	void damage(int damage,Combatant c){
		c.source=c.source.clone();
		modifyability(c,-damage);
		damagepool+=damage;
	}

	abstract void modifyability(Combatant c,int change);

	@Override
	public void end(Combatant c){
	}

	@Override
	public boolean validate(Combatant c){
		return c.source.wisdom>2;
	}

	@Override
	protected boolean expire(int time,Combatant c){
		timepool+=time;
		while(timepool>=48&&damagepool>0){
			timepool-=48;
			damage(-Math.min(2,damagepool),c);
		}
		return damagepool==0;
	}

	@Override
	public void merge(Combatant c,Condition condition){
		AbilityDamage ad=(AbilityDamage)condition;
		damage(ad.damage,c);
	}
}