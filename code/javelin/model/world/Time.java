package javelin.model.world;

import javelin.model.unit.Combatant;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Growth;

/**
 * Measures a period of time rather than {@link Period}s of the day. Useful
 * for readability and because of some differences with their real-world
 * counterparts.
 *
 * @author alex
 */
public class Time{
  /** One day. */
  public static final int DAY=1;
  /** {@value #WEEK} days. */
  public static final int WEEK=7*DAY;
  /** {@value #MONTH} days. */
  public static final int MONTH=30;
  /**
   * {@value #SEASON} days. This is a rounded value so that it's easier for
   * players to understand when a new season will roll in.
   *
   * @see Season
   */
  public static final int SEASON=100;
  /**
   * {@value #YEAR} days or 4 {@link #SEASON}s. This is a very important value
   * for Javelin, as anything with a level 1-20 progression is expected to
   * take place within a year, more or less - including {@link Town}
   * {@link Growth} and the length of the {@link Campaign} itself.
   */
  public static final int YEAR=4*SEASON;
  /** Counted in {@link Combatant#ap}. */
  public static final float MINUTE=10;
}