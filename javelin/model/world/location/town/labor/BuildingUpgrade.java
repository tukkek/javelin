package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;

/**
 * Upgrades a {@link District} {@link Location} into a higher tier.
 *
 * @author alex
 */
public abstract class BuildingUpgrade extends Build{
	/** Tier to upgrade to. */
	protected int upgradelevel;

	/**
	 * @param previous Previous {@link Location}.
	 * @param newname Name of new tier.
	 * @param upgradelevel See {@link #upgradelevel}.
	 * @see Build#Build(String, int, Rank, Location)
	 */
	public BuildingUpgrade(String newname,int cost,int upgradelevel,
			Location previous,Rank minimumrank){
		super("Upgrade "+previous.toString().toLowerCase()+" to "
				+newname.toLowerCase(),cost,minimumrank,previous);
		this.upgradelevel=upgradelevel;
	}

	@Override
	abstract public Location getgoal();

	@Override
	public void start(){
		previous.remove();
		super.start();
	}

	@Override
	public boolean validate(District d){
		if(previous==null||!previous.canupgrade()) return false;
		return super.validate(d)&&(site!=null||d.getlocations().contains(previous));
	}

	@Override
	protected Point getsitelocation(){
		return previous.getlocation();
	}

	@Override
	protected void done(Location goal){
		/* TODO goal should be a Location and #raiselevel a Location method */
		if(goal instanceof Fortification)
			((Fortification)goal).raiselevel(upgradelevel);
	}
}
