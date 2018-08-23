package javelin.controller.action.minigame;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Run;
import javelin.controller.map.Ziggurat;

/**
 * @see Ziggurat
 * @author alex
 */
public class EnterZiggurat implements Runnable{
	@Override
	public void run(){
		new StartBattle(new Run()).battle();
	}
}
