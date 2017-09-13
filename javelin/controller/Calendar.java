package javelin.controller;

/**
 * Holds data period values.
 * 
 * @author alex
 */
public class Calendar {
	public static final int DAY = 1;
	public static final int WEEK = 7;
	/**
	 * Months are never actually shown to the player but are still a useful
	 * period of time when designing game features.
	 */
	public static final int MONTH = 30;
	/**
	 * A 100 days because it's easier to predict and understand as a player.
	 */
	public static final int SEASON = 100;
	/**
	 * In order to have 4 {@link #SEASON}s. This is better than having player
	 * doing algebra in their heads in order to understand what proper
	 * season/year it should be 10 days from now.
	 * 
	 * Plus it also makes perfectly normal sense because magic.
	 */
	public static final int YEAR = 400;
}
