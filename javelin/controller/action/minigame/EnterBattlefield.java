package javelin.controller.action.minigame;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Battle;
import javelin.controller.fight.minigame.battlefield.ArmySelectionScreen;
import javelin.controller.fight.minigame.battlefield.BattlefieldFight;

/**
 * @see Battlefield
 * @see Battle
 * @author alex
 */
public class EnterBattlefield implements Runnable{
	@Override
	public void run(){
		BattlefieldFight f=new BattlefieldFight();
		if(new ArmySelectionScreen().selectarmy(f)) throw new StartBattle(f);
	}
}
