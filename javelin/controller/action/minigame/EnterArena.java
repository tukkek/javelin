package javelin.controller.action.minigame;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.arena.ArenaFight;

/**
 * Allows access to the Arena at any point in time (unless in battle).
 *
 * @author alex
 */
public class EnterArena implements Runnable{
	@Override
	public void run(){
		throw new StartBattle(new ArenaFight());
	}
}
