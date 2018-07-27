package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Makes a movement on the overworld or {@link Dungeon}.
 *
 * TODO {@link WorldScreen} hierarchy should be refactored into proper Battle /
 * Dungeon / World screens.
 *
 * TODO {@link #perform(WorldScreen)} needs refactoring after 2.0
 *
 * @see Javelin#getDayPeriod()
 * @author alex
 */
public class WorldMove extends WorldAction {
    /**
     * Represents time spent on resting, eating, sleeping, etc.
     *
     * TODO forced march
     */
    public static final float NORMALMARCH = 4f / 5f;
    /**
     * Ideally a move should always be 6 hours in a worst-case scenario (the time of
     * a period), so as to avoid a move taking longer than a period, which could
     * confuse {@link Hazard}s.
     *
     * @see #TIMECOST
     */
    public static final float MOVETARGET = 6f;
    /**
     * How much time it takes to walk a single square with speed 30 (~30mph, normal
     * human speed).
     *
     * Calculation of worst case scenario (which should take 6 hours): 6 * 15/30ft
     * (slow races) * .5 (bad terrain)
     *
     * @see Monster#gettopspeed()
     * @see #MOVETARGET
     * @see #NORMALMARCH
     */
    public static final float TIMECOST = NORMALMARCH * (MOVETARGET * 15f / 30f) / 2f;
    /** TODO remove, hack */
    private final int deltax;
    private final int deltay;
    /**
     * Used when {@link WorldScreen#react(int, int)} returns <code>true</code> but
     * we still want to stop moving.
     */
    public static boolean abort;

    /**
     * Constructor
     *
     * @param keycodes Integer/char keys.
     * @param deltax   Direction of x movement.
     * @param deltay   Direction of y movement.
     * @param keys     Text keys.
     */
    public WorldMove(final int[] keycodes, final int deltax, final int deltay, final String[] keys) {
	super("Move (" + keys[1].charAt(0) + ")", keycodes, keys);
	this.deltax = deltax;
	this.deltay = deltay;
    }

    @Override
    public void perform(final WorldScreen s) {
	final Point p = JavelinApp.context.getherolocation();
	int x = p.x + deltax;
	int y = p.y + deltay;
	if (move(x, y)) {
	    BattleScreen.active.mappanel.center(x, y, true);
	}
    }

    /**
     * @return <code>true</code> to continue moving, if relevant.
     * @see BattleScreen
     */
    public static boolean move(int tox, int toy) {
	if (!JavelinApp.context.validatepoint(tox, toy)) {
	    throw new RepeatTurn();
	}
	abort = false;
	if (JavelinApp.context.react(tox, toy) || abort || !place(tox, toy)) {
	    return false;
	}
	boolean stop = false;
	if (walk(JavelinApp.context.getherolocation())) {
	    stop = JavelinApp.context.explore(tox, toy);
	}
	heal();
	return stop;
    }

    /**
     * @return <code>true</code> if moved current actor to the given location.
     */
    public static boolean place(final int tox, final int toy) {
	if (!JavelinApp.context.allowmove(tox, toy) || !JavelinApp.context.validatepoint(tox, toy)) {
	    return false;
	}
	JavelinApp.context.updatelocation(tox, toy);
	JavelinApp.context.view(tox, toy);
	return true;
    }

    static void heal() {
	for (final Combatant c : Squad.active.members) {
	    if (c.source.fasthealing != 0) {
		c.heal(c.maxhp, false);
	    }
	}
    }

    static boolean walk(final Point t) {
	if (Dungeon.active != null) {
	    return true;
	}
	final List<Squad> here = new ArrayList<>();
	for (final Actor p : World.getall(Squad.class)) {
	    Squad s = (Squad) p;
	    if (s.x == t.x && s.y == t.y) {
		here.add(s);
	    }
	}
	if (here.size() <= 1) {
	    return true;
	}
	here.get(0).join(here.get(1));
	return false;
    }
}
