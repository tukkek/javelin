package javelin.model.world.location;

import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.view.screen.WorldScreen;

/**
 * An outpost maintains vision of an area around it.
 *
 * @see World#discovered
 * @author alex
 */
public class Outpost extends Fortification{
	/** How many squares away to help vision with. */
	public static final int VISIONRANGE=3;
	private static final String DESCRIPTION="Outpost";

	// public static class BuildOutpost extends Build {
	// public BuildOutpost() {
	// super("Build outpost", 5, null, Rank.HAMLET);
	// }
	//
	// @Override
	// public Location getgoal() {
	// return new Outpost();
	// }
	//
	// @Override
	// public boolean validate(District d) {
	// if (!super.validate(d)) {
	// return false;
	// }
	// if (site != null) {
	// return true;
	// }
	// if (site == null && d.getlocationtype(Outpost.class)
	// .size() >= d.town.getrank().rank) {
	// return false;
	// }
	// return super.validate(d) && getsitelocation() != null;
	// }
	//
	// @Override
	// protected Point getsitelocation() {
	// District d = town.getdistrict();
	// ArrayList<Point> free = d.getfreespaces();
	// for (Point p : free) {
	// if (town.distance(p.x, p.y) == d.getradius()) {
	// return p;
	// }
	// }
	// return null;
	// }
	//
	// @Override
	// protected void done(Location goal) {
	// super.done(goal);
	// if (!town.ishostile()) {
	// Outpost.discover(goal.x, goal.y, Outpost.VISIONRANGE);
	// }
	// }
	// }

	/** Constructor. */
	public Outpost(){
		super(DESCRIPTION,DESCRIPTION,1,5);
		gossip=true;
		vision=VISIONRANGE;
		allowedinscenario=false;
	}

	/** Puts a new instance in the {@link World} map. */
	public static void build(){
		new Outpost().place();
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		return false;
	}

	/**
	 * Given a coordinate shows a big amount of land around that.
	 *
	 * @param range How far squares away will become visible.
	 * @see WorldScreen#discovered
	 */
	static public void discover(int xp,int yp,int range){
		for(int x=xp-range;x<=xp+range;x++)
			for(int y=yp-range;y<=yp+range;y++)
				WorldScreen.discover(x,y);
	}

	@Override
	protected boolean validateplacement(boolean water,World w,List<Actor> actors){
		var o=findnearest(Outpost.class);
		if(o!=null&&o.distance(x,y)<=VISIONRANGE*2) return false;
		return super.validateplacement(water,w,actors);
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}
}
