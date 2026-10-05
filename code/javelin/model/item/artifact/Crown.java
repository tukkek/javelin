package javelin.model.item.artifact;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * Gives +5 to each skill for a week (all-knowing condition)
 *
 * @author alex
 */
public class Crown extends Artifact{
	class Knowledgeable extends Condition{
		/** Constructor. */
		public Knowledgeable(Combatant c){
			super("all-knowing",null,null,Float.MAX_VALUE,24*7,Effect.NEUTRAL);
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
	public Crown(){
		super("Crown of knowlege");
		usedinbattle=false;
		usedoutofbattle=true;
	}

	@Override
	protected boolean activate(Combatant user){
		user.addcondition(new Knowledgeable(user));
		Javelin.message(user+" becomes knowledgeable!",false);
		return true;
	}
}
