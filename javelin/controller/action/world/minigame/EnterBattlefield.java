package javelin.controller.action.world.minigame;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Battle;
import javelin.controller.fight.minigame.BattlefieldFight;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.view.screen.WorldScreen;

/**
 * @see Battlefield
 * @see Battle
 * @author alex
 */
public class EnterBattlefield extends EnterMinigame {
	public EnterBattlefield() {
		super("Battlefield (mini-game)", new int[] {}, new String[] { "B" });
	}

	@Override
	public void perform(WorldScreen screen) {
		super.perform(screen);
		BattlefieldFight f = new BattlefieldFight();
		if (f.choosearmy()) {
			throw new StartBattle(f);
		}
	}
}
