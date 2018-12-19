package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.World;
import javelin.model.world.location.ConstructionSite;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;

/**
 * A labor that places a {@link ConstructionSite} site and replaces it with the
 * actual structure once it's {@link #done()}.
 *
 * @author alex
 */
public abstract class Build extends Labor{
	/** The in-progress construction site. */
	protected ConstructionSite site;
	/**
	 * Previous building, in case of an upgrade. <code>null</code> if this is the
	 * first contruction project.
	 */
	protected Location previous;

	/**
	 * @param previous See {@link #previous}.
	 *
	 * @see Labor#Labor(String, int, Rank)
	 */
	public Build(String name,int cost,Rank minimumrank,Location previous){
		super(name,cost,minimumrank);
		this.previous=previous;
	}

	/**
	 * @return Actual structure to be placed once {@link #work(float)} is
	 *         {@link #done()}.
	 */
	public abstract Location getgoal();

	@Override
	protected void define(){
		// nothing
	}

	@Override
	public void done(){
		site.remove();
		site.goal.setlocation(site.getlocation());
		site.goal.place();
		if(!town.ishostile()) site.goal.capture();
		done(site.goal);
	}

	/** Called after {@link #done()}. */
	protected void done(Location goal){

	}

	/**
	 * @return {@link World} coordinate where to place the
	 *         {@link ConstructionSite}.
	 */
	protected Point getsitelocation(){
		return town.getdistrict().getfreespaces().get(0);
	}

	@Override
	public void start(){
		super.start();
		site=new ConstructionSite(getgoal(),previous,this);
		if(town.ishostile()) site.realm=town.realm;
		site.setlocation(getsitelocation());
		site.place();
	}

	@Override
	public boolean validate(District d){
		return super.validate(d)&&site==null?!d.getfreespaces().isEmpty()
				:d.getlocations().contains(site);
	}

	@Override
	public void cancel(){
		super.cancel();
		site.remove(false);
	}
}