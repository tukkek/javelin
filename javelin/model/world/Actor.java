package javelin.model.world;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.ActorByDistance;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.scenario.dungeonworld.DungeonWorld;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.WorldScreen;

/**
 * An independent overworld feature.
 *
 * If you're creating a new actor type don't forget to update
 * {@link Actor#getallmapactors()}!
 *
 * @author alex
 */
public abstract class Actor implements Serializable{
	static final int[] NUDGES=new int[]{-1,0,+1};
	/** x coordinate (-1 when uninitialized). */
	public int x=-1;
	/** y coordinate (-1 when uninitialized). */
	public int y=-1;
	/**
	 * Which team this actor belongs to or <code>null</code> if neutral. If
	 * <code>null</code> during construction, in which case it won't be updated by
	 * {@link #place()}.
	 */
	public Realm realm=null;
	/**
	 * If <code>true</code> this actor will be ignored by {@link Incursion}s.
	 */
	public boolean impermeable=false;
	/**
	 * TODO probably better to handle this the way {@link DungeonWorld} does.
	 */
	public boolean allowedinscenario=true;
	/**
	 * Unique actors are those whose names are very unlikely to appear twice in
	 * the same {@link World}.
	 */
	public boolean unique=false;

	/** Constructor. */
	public Actor(){
		// registerinstance();
	}

	/** Permanently removes this actor from the game. */
	public void remove(){
		deregisterinstance();
	}

	/** Adds this actor to the game. Should only be used once in theory. */
	public void place(){
		if(World.scenario.allowallactors||allowedinscenario) registerinstance();
	}

	/** Move actor to the given coordinates. */
	public void move(int tox,int toy){
		x=tox;
		y=toy;
	}

	/**
	 * Called when an incursion reaches this actor's location.
	 *
	 * @see Incursion#ignore(Incursion)
	 * @see Incursion#fight(int, int)
	 *
	 * @param incursion Attacking incursion.
	 * @return <code>true</code> if this place gets destroyed, <code>false</code>
	 *         if the Incursion is destroyed or <code>null</code> if neither.
	 */
	abstract public Boolean destroy(Incursion attacker);

	/** Called during construction to setup {@link World#actors}. */
	protected void registerinstance(){
		final World w=World.getseed();
		ArrayList<Actor> list=w.actors.get(getClass());
		if(list==null){
			list=new ArrayList<>(1);
			w.actors.put(getClass(),list);
		}
		if(!list.contains(this)) list.add(this);
		if(Location.class.isInstance(this))
			w.locations.put(new Point(x,y),(Location)this);
	}

	/** Removes this instance from {@link World#actors}. */
	protected void deregisterinstance(){
		final World w=World.getseed();
		List<Actor> list=w.actors.get(getClass());
		if(list!=null) list.remove(this);
		w.locations.remove(new Point(x,y));
	}

	public void displace(int depth){
		if(depth==50){
			if(Javelin.DEBUG) System.err.println("Too many calls to displace!");
			return;
		}
		int deltax=0,deltay=0;
		while(deltax==0&&deltay==0){
			deltax=NUDGES[RPG.r(NUDGES.length)];
			deltay=NUDGES[RPG.r(NUDGES.length)];
		}
		int tox=x+deltax;
		int toy=y+deltay;
		if(!World.validatecoordinate(tox,toy)||!cancross(tox,toy)){
			displace(depth+1);
			return;
		}
		ArrayList<Actor> actors=World.getactors();
		actors.remove(this);
		if(tox>=0&&toy>=0&&tox<World.scenario.size&&toy<World.scenario.size
				&&World.get(tox,toy,actors)==null)
			move(tox,toy);
		else
			displace(depth+1);
	}

	protected boolean cancross(int tox,int toy){
		final boolean to=Terrain.WATER.equals(Terrain.get(tox,toy));
		if(!to) return true;
		final boolean from=Terrain.WATER.equals(Terrain.get(x,y));
		return from&&to;
	}

	/**
	 * Moves actor to nearby square until a free square is found.
	 */
	public void displace(){
		displace(0);
	}

	/**
	 * Called on each instance once per day.
	 *
	 * @param time Current hour, starting from hour zero at the beggining of the
	 *          game.
	 */
	public void turn(long time,WorldScreen world){
		// nothing by default
	}

	/**
	 * Called when a {@link Squad} enters the same world square as this actor.
	 *
	 * @return <code>true</code> if the {@link World} should react after this
	 *         interaction.
	 * @throws RepeatTurn
	 */
	public abstract boolean interact();

	/**
	 * Note that this doesn't {@link #place()} or update the actor in any way.
	 *
	 * @param x World coordinate.
	 * @param y World coordinate.
	 */
	public void setlocation(int x,int y){
		this.x=x;
		this.y=y;
	}

	/**
	 * @return Distance from this actor to the given coordinates.
	 */
	public double distance(int xp,int yp){
		return Walker.distance(xp,yp,x,y);
	}

	/**
	 * @return The given realm color will be drawn on the {@link WorldScreen}.
	 *         <code>null</code> means no overlay.
	 * @see Realm#getawtcolor()
	 */
	public Realm getrealmoverlay(){
		return realm;
	}

	/**
	 * @return Any combatants that are situated in this actor, may return
	 *         <code>null</code> if this is not a valid request for this type of
	 *         actor. For performance if a {@link List} is returned it should be
	 *         the canonical one, and thus should not be directly modified by the
	 *         receiver.
	 * @see Combatant#newid()
	 */
	abstract public List<Combatant> getcombatants();

	/**
	 * @return Visual representation of this actor.
	 */
	abstract public Image getimage();

	/**
	 * @return Textual representation of this actor.
	 */
	abstract public String describe();

	/**
	 * @return <code>true</code> if both these actors are touching each other in
	 *         the {@link WorldScreen}.
	 */
	public boolean isadjacent(Actor active){
		return Math.abs(x-active.x)<=1&&Math.abs(y-active.y)<=1;
	}

	public Actor findnearest(Class<? extends Location> targets){
		Actor nearest=null;
		for(Actor p:World.getall(targets)){
			if(p==this) continue;
			if(nearest==null||distance(p.x,p.y)<distance(nearest.x,nearest.y))
				nearest=p;
		}
		return nearest;
	}

	/**
	 * TODO ideally would be nice to chance all .x .y to .location (Point)
	 */
	public void setlocation(Point p){
		setlocation(p.x,p.y);
	}

	public Point getlocation(){
		return new Point(x,y);
	}

	public int distanceinsteps(int xp,int yp){
		return Walker.distanceinsteps(x,y,xp,yp);
	}

	/**
	 * Note that this could return a hostile town!
	 *
	 * @return A {@link Town} district this location is part of or
	 *         <code>null</code> if it is not located inside one. If more than one
	 *         district encompasses this location, the one with the highest
	 *         {@link Town#population} will be returned.
	 */
	public District getdistrict(){
		ArrayList<Town> towns=Town.gettowns();
		District main=null;
		for(Town t:towns){
			District d=t.getdistrict();
			if(distanceinsteps(t.x,t.y)<=d.getradius()
					&&(main==null||t.population>d.town.population))
				main=d;
		}
		return main;
	}

	public <K extends Actor> List<K> sortbydistance(List<K> actor){
		actor.sort(new ActorByDistance(this));
		return actor;
	}

	/**
	 * Called when players click on a {@link World} {@link Location} that is not
	 * adjacent to the currently active Squad.
	 */
	public void accessremotely(){
		MessagePanel.active.clear();
		Javelin.message("Too far away...",Javelin.Delay.WAIT);
	}

	/**
	 * Called when an {@link Incursion} reaches its target (and for selecting
	 * targets).
	 *
	 * @param attackerel Given an attacking encounter level... (may pass
	 *          <code>null</code> to locations which should be "absolute" EL)
	 * @return the defending encounter level. A <code>null</code> value is allowed
	 *         for places that should not be conquered. {@link Integer#MIN_VALUE}
	 *         means an automatic victory for the attacker.
	 * @see Actor#impermeable
	 * @see Incursion#fight(int, int)
	 * @see ChallengeCalculator#calculateel(List)
	 */
	public abstract Integer getel(Integer attackerel);

	/**
	 * @return <code>true</code> if this actor actually exists at its
	 *         {@link #x}:{@link #y} coordinate. If not, it has probably been
	 *         {@link #remove()}d from the {@link World} map.
	 */
	public boolean exists(){
		return World.get(x,y,World.getactors())==this;
	}

	/** @return See {@link WorldScreen#see(Point)}. */
	public boolean see(){
		return WorldScreen.see(getlocation());
	}
}
