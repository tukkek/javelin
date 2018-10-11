package javelin.model.unit.skill;

import javelin.controller.action.ActionCost;
import javelin.controller.ai.BattleAi;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.skill.Deceitful;

/**
 * As long as melee attack misses a target, reduce a small amount of its AC (up
 * to Dex modifier). Reset whenever the unit is hit.
 *
 * This is important because Javelin is a huge miss-fest early on, when so many
 * monsters are small or tiny and with good Dexterity values, resulting in very
 * high AC against BABs as low as literally zero. Even when the skill is
 * untrained, this should help alleviate miss chances a little bit as modifiers
 * pile up.
 *
 * @author alex
 */
public class Bluff extends Skill{
	public static class Feigned extends Condition{
		static final float DURATION=ActionCost.FULL;
		int acpenalty=1;

		public Feigned(Combatant attacker,Combatant target){
			super(attacker.ap+DURATION,target,Effect.NEUTRAL,"feigned",null);
		}

		@Override
		public void start(Combatant c){
			c.acmodifier-=1;
		}

		@Override
		public void end(Combatant c){
			c.acmodifier+=acpenalty;
		}

		@Override
		public void merge(Combatant c,Condition condition){
			super.merge(c,condition);
			if(acpenalty<Monster.getbonus(c.source.dexterity)){
				acpenalty+=1;
				c.acmodifier-=1;
			}
		}

		@Override
		public String toString(){
			String times=acpenalty==1?"":" x"+acpenalty;
			return description+times;
		}
	}

	public Bluff(){
		super("Bluff",Ability.CHARISMA,Realm.EVIL);
		usedincombat=true;
	}

	@Override
	public int getbonus(Combatant c){
		int bonus=super.getbonus(c);
		if(c.source.hasfeat(Deceitful.SINGLETON)) bonus+=Deceitful.BONUS;
		return bonus;
	}

	/**
	 * Applies a cumulative -1 to AC, for one turn, on a succesful bluff check.
	 * Does nothing if the target doesn't have a dexterity bonus.
	 *
	 * Really shouldn't use {@link Combatant#roll(Skill)} here since this is
	 * called from {@link BattleAi} but it's such a minor effect which at the same
	 * time helps so much with the early levels miss-fest, that we might as well
	 * just keep it.
	 *
	 * @param target Uses {@link Bluff} against this target.
	 * @author alex
	 */
	public static void feign(Combatant attacker,Combatant target){
		if(target.source.dexterity<12) return;
		int wisdom=Monster.getbonus(target.source.wisdom);
		int sensemotive=Math.max(target.taketen(Skill.SENSEMOTIVE),
				10+target.source.getbab()+wisdom);
		if(attacker.roll(Skill.BLUFF)>=sensemotive)
			target.addcondition(new Feigned(attacker,target));
	}
}
