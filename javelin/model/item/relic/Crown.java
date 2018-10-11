package javelin.model.item.relic;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * Gives +5 to each skill for a week (all-knowing condition)
 *
 * @author alex
 */
public class Crown extends Relic{
	public class Knowledgeable extends Condition{
		/** Constructor. */
		public Knowledgeable(Combatant c){
			super(Float.MAX_VALUE,c,Effect.NEUTRAL,"all-knowing",20,24*7);
		}

		@Override
		public void start(Combatant c){
			c.skillmodifier+=5;
		}

		@Override
		public void end(Combatant c){
			c.skillmodifier-=5;
		}
	}

	/** Constructor. */
	public Crown(Integer level){
		super("Crown of knowlege",level);
		usedinbattle=false;
		usedoutofbattle=true;
	}

	@Override
	protected boolean activate(Combatant user){
		user.addcondition(new Knowledgeable(user));
		Javelin.message(user+" becomes knowledgeable",false);
		return true;
	}
}
