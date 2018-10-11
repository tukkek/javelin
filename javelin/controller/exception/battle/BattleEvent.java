package javelin.controller.exception.battle;

import javelin.JavelinApp;

/**
 * @see {@link StartBattle}
 * @see EndBattle
 * @see JavelinApp
 * @author alex
 */
public class BattleEvent extends RuntimeException{
	@Override
	public synchronized Throwable fillInStackTrace(){
		/* default implemntation is very inneficient */
		return this;
	}
}
