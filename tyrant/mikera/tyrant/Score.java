/*
 * Created on 27-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * Implements Tyrant point-scoring
 * 
 * Most notable feature is an exponential decay in the rate of 
 * point scoring. This is to encourage risk taking and skilled
 * play rather than slow, tedious level-building and hoarding
 */
public class Score {
	private static final double SCORE_HALFLIFE=3000000.0;
	public static void scoreIdentify(Thing hero, Thing thing) {
		// score for non-obvious items
		if (!Item.isDisguisedName(thing)) {
			addScore(hero, Lib.getDefaultStat(thing,"Level")*2);
		}
	}
	
	public static void scoreExplore(Thing t) {
		int score=t.getStat("ExploreScore");
		if (score>0) {
			addScore(Game.hero(), score);
			t.incStat("ExploreScore",-score);
		}
	}
	
	public static void scoreFirstKill(Thing t) {
		addScore(Game.hero(), Lib.getDefaultStat(t,"Level")*5);
	}
	
	public static void scoreSecretDoor(int lev) {
		addScore(Game.hero(), lev*10);
	}

    /**
     * Adds to the players score. The actual score added is modified
     * by a factor that decay exponentially over game time.
     *
     * @param rawscore
     */
    private static void addScore(Thing hero, int rawscore) {
        int score=RPG.round(rawscore*Math.pow(0.5,hero.getStat("GameTime")/SCORE_HALFLIFE));
        
        hero.incStat("Score",score);
    }
}
