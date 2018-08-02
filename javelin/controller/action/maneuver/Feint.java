package javelin.controller.action.maneuver;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.attack.expertise.ImprovedFeint;

public class Feint extends ExpertiseAction {
	public static final Action INSTANCE = new Feint();

	public class Feigned extends Condition {

		public Feigned(float expireatp, Combatant c) {
			super(expireatp, c, Effect.NEGATIVE, "feigned", null);
		}

		@Override
		public void start(Combatant c) {
			c.acmodifier -= Monster.getbonus(c.source.dexterity);
		}

		@Override
		public void end(Combatant c) {
			c.acmodifier += Monster.getbonus(c.source.dexterity);
		}
	}

	private Feint() {
		super("Feint", "F", ImprovedFeint.SINGLETON, 0);
	}

	@Override
	boolean validatetarget(Combatant target) {
		return Monster.getbonus(target.source.dexterity) >= +1
				&& target.hascondition(Feigned.class) == null;
	}

	@Override
	ChanceNode hit(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, float chance) {
		battleState = battleState.clone();
		targetCombatant = battleState.clone(targetCombatant);
		targetCombatant.addcondition(
				new Feigned(targetCombatant.ap + .1f, targetCombatant));
		return new ChanceNode(battleState, chance, "Feint succesfull!",
				Javelin.Delay.BLOCK);
	}

	@Override
	int getsavebonus(Combatant targetCombatant) {
		return targetCombatant.source.getwill();
	}

	@Override
	int getattackerbonus(Combatant combatant) {
		return Monster.getbonus(combatant.source.intelligence);
	}

	@Override
	ChanceNode miss(Combatant combatant, Combatant target,
			BattleState battleState, float chance) {
		return new ChanceNode(battleState, chance, "Feint attemp fails...",
				Javelin.Delay.WAIT);
	}
}
