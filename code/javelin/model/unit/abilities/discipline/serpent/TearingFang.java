package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.ActionCost;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Boost;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.skill.Skill;
import javelin.old.RPG;

/**
 * http://www.d20pfsrd.com/path-of-war/disciplines-and-maneuvers/steel-serpent-maneuvers/#TOC-Tearing-Fang
 *
 * @author alex
 */
public class TearingFang extends Boost{
	static final int EXTRADAMAGE=RPG.average(2,6);
	static final int BLEEDDURATION=RPG.average(1,4);
	static final int HEALDC=15;
	static final int BLEEDDAMAGE=2;

	class Tearing extends Condition{
		public Tearing(Combatant c){
			super("tearing",null,c.ap+1,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			for(var a:c.source.getattacks()){
				a.damage[2]+=EXTRADAMAGE;
				a.temporaryeffect=new Bleed();
			}
		}

		@Override
		public void end(Combatant c){
			for(var a:c.source.getattacks()){
				a.damage[2]-=EXTRADAMAGE;
				a.temporaryeffect=null;
			}
		}
	}

	/**
	 * @see Attack#temporaryeffect
	 * @author alex
	 */
	class Bleed extends Spell{
		public Bleed(){
			super("Bleed",0,0);
		}

		@Override
		public String cast(Combatant caster,Combatant target,boolean saved,
				BattleState s,ChanceNode cn){
			target.addcondition(new Bleeding(target));
			return target+" is bleeding!";
		}
	}

	public class Bleeding extends Condition{
		int ticks;
		float lastbleed;

		Bleeding(Combatant c){
			super("bleeding",null,Float.MAX_VALUE,Effect.NEGATIVE);
			stack=true;
			ticks=BLEEDDURATION;
			lastbleed=c.ap;
		}

		@Override
		public void start(Combatant c){
			/*
			 * don't do anything right away, give a chance of having multiple
			 * attacks done in sequence before trying to heal them, otherwise
			 * the AP cost for the first-aid action could grow into entire turns
			 * worth of healing, as each Bleed is added and immediately patched.
			 */
		}

		boolean tick(Combatant c){
			c.damage(BLEEDDAMAGE,0);
			ticks-=1;
			if(ticks==0){
				c.removecondition(this);
				return false;
			}
			return true;
		}

		/** TODO ideally would also check if enganged. */
		boolean heal(final Combatant c){
			return c.taketen(Skill.HEAL)>=HEALDC;
		}

		@Override
		public boolean expireinbattle(Combatant c){
			while(c.ap-lastbleed>=1){
				if(heal(c)){
					c.ap+=ActionCost.STANDARD;
					c.clearcondition(Bleeding.class);
					return true;
				}
				lastbleed+=1;
				if(!tick(c)) return true;
			}
			return false;
		}

		@Override
		public void end(Combatant c){
			if(ticks>0&&!heal(c)) while(tick(c)){
				// tick until over
			}
		}
	}

	public TearingFang(){
		super("Tearing fang",4);
	}

	@Override
	protected void boost(Combatant c){
		c.addcondition(new Tearing(c));
	}
}
