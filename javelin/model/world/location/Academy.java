package javelin.model.world.location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.OptionsByPriority;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.upgrading.AcademyScreen;

/**
 * A place where units can go to learn about a general topic - be it physical
 * feats or intellectual or magical prowess.
 *
 * @author alex
 */
public abstract class Academy extends Fortification{
	/**
	 * Builds one academy of this type. Since cannot have only 1 instance,
	 * {@link #generateacademy()} needs to be defined by subclasses.
	 *
	 * @see BuildAcademies
	 * @author alex
	 */
	public abstract static class BuildAcademy extends Build{
		public Academy goal;

		public BuildAcademy(Rank minimumrank){
			super("",0,minimumrank,null);
		}

		@Override
		protected void define(){
			goal=generateacademy();
			super.define();
			cost=goal.getlabor();
			name="Build "+goal.descriptionknown.toLowerCase();
		}

		protected abstract Academy generateacademy();

		@Override
		public Location getgoal(){
			return goal;
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)
					&&validatecount(d.getlocationtype(goal.getClass()),d);
		}

		protected boolean validatecount(ArrayList<Location> count,District d){
			return count.isEmpty();
		}
	}

	/**
	 * Like {@link BuildAcademy} except allows for 1 academy of the given type per
	 * town rank.
	 *
	 * @author alex
	 */
	public abstract static class BuildAcademies extends BuildAcademy{
		public BuildAcademies(Rank minimumrank){
			super(minimumrank);
		}

		@Override
		protected boolean validatecount(ArrayList<Location> count,District d){
			if(count.size()>=d.town.getrank().rank) return false;
			for(Location l:count){
				Academy a=(Academy)l;
				if(a.descriptionknown.equals(goal.descriptionknown)) return false;
			}
			return true;
		}
	}

	/** Currently training unit. */
	public OrderQueue training=new OrderQueue();
	/** Money {@link #training} unit had before entering here (if alone). */
	public int stash;
	/** Upgrades that can be learned here. */
	public HashSet<Upgrade> upgrades;
	/** If a single unit parks with a vehicle here it is stored. */
	public Transport parking=null;
	public int level=0;

	/**
	 * See {@link Fortification#Fortification(String, String, int, int)}.
	 *
	 * @param upgradesp
	 * @param classadvancement
	 * @param raiseability
	 */
	public Academy(String descriptionknown,String descriptionunknown,int minlevel,
			int maxlevel,Set<Upgrade> upgradesp,RaiseAbility raiseability,
			ClassLevelUpgrade classadvancement){
		super(descriptionknown,descriptionunknown,minlevel,maxlevel);
		upgrades=new HashSet<>(upgradesp);
		if(raiseability!=null) upgrades.add(raiseability);
		if(classadvancement!=null) upgrades.add(classadvancement);
		sacrificeable=false;
		level=upgrades.size();
		clear=true;
		gossip=true;
	}

	/** Constructor. */
	public Academy(String descriptionknown,String descriptionunknown,
			HashSet<Upgrade> upgrades){
		this(descriptionknown,descriptionunknown,Math.max(1,upgrades.size()-1),
				upgrades.size()+1,upgrades,null,null);
	}

	public int getlabor(){
		return upgrades.size();
	}

	/**
	 * @param options {@link #upgrades}, to be sorted.
	 */
	public void sort(List<Option> options){
		options.sort(OptionsByPriority.INSTANCE);
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		completetraining(false);
		getscreen().show();
		return true;
	}

	void completetraining(boolean force){
		var complete=force?training.queue:training.reclaim(Javelin.gettime());
		for(Order o:complete)
			completetraining((TrainingOrder)o);
	}

	protected AcademyScreen getscreen(){
		return new AcademyScreen(this,null);
	}

	Squad completetraining(TrainingOrder next){
		Squad s=moveout(next,next.trained);
		s.gold+=stash;
		stash=0;
		if(parking!=null) if(s.transport==null){
			s.transport=parking;
			parking=null;
			s.updateavatar();
		}else if(parking.price>s.transport.price){
			Transport swap=parking;
			parking=s.transport;
			s.transport=swap;
			s.updateavatar();
		}
		return s;
	}

	@Override
	public boolean hasupgraded(){
		return training.reportanydone();
	}

	@Override
	public List<Combatant> getcombatants(){
		ArrayList<Combatant> combatants=new ArrayList<>(garrison);
		for(Order o:training.queue){
			TrainingOrder next=(TrainingOrder)o;
			combatants.add(next.untrained);
		}
		return combatants;
	}

	/**
	 * @return <code>true</code> if already has the maximum number of upgrades.
	 */
	public boolean full(){
		return upgrades.size()>=9;
	}

	/**
	 * Applies the upgrade and adjustments. Currently never creates a new squad
	 * because this isn't being called from {@link WorldActor#turn(long,
	 * javelin.view.screen.WorldScreen).}
	 *
	 * @param o Training information.
	 * @param member Joins a nearby {@link Squad} or becomes a new one.
	 * @param p Place the training was realized.
	 * @param member Member to be returned (upgraded or not, in case of cancel).
	 * @return The Squad the trainee is now into.
	 */
	public Squad moveout(TrainingOrder o,Combatant member){
		var district=getdistrict()==null?Set.of():getdistrict().getarea();
		var free=new ArrayList<Point>();
		var actors=World.getactors();
		for(var p:RPG.shuffle(getlocation().getadjacent())){
			if(!World.validatecoordinate(p.x,p.y)
					||Terrain.get(p.x,p.y).equals(Terrain.WATER))
				continue;
			var existing=(Squad)World.get(p.x,p.y,Squad.class);
			if(existing!=null){
				existing.add(member,o.equipment);
				if(existing.gettime()<o.completionat) existing.settime(o.completionat);
				return existing;
			}
			if(World.get(p.x,p.y,actors)==null&&district.contains(p))
				free.add(new Point(p.x,p.y));
		}
		var nodistrict=free.isEmpty();
		var destination=nodistrict?getlocation():RPG.pick(free);
		var s=new Squad(destination.x,destination.y,o.completionat,null);
		s.add(member,o.equipment);
		s.place();
		if(nodistrict) s.displace();
		return s;
	}

	@Override
	public Integer getel(Integer attackerel){
		return ChallengeCalculator.calculateel(getcombatants());
	}

	@Override
	protected void captureforai(Incursion attacker){
		completetraining(true);
		super.captureforai(attacker);
		training.clear();
		stash=0;
		parking=null;
	}

	@Override
	public boolean isworking(){
		return !training.queue.isEmpty()&&!training.reportalldone();
	}

	@Override
	public void accessremotely(){
		if(ishostile()){
			super.accessremotely();
			return;
		}
		if(training.queue.isEmpty()){
			Javelin.message("No one is training here right now...",false);
			return;
		}
		String s="Training period left:\n\n";
		boolean anydone=false;
		for(Order o:training.queue){
			s+=o+"\n";
			anydone=anydone||o.completed(Javelin.gettime());
		}
		s+="\n";
		if(anydone)
			s+="To move units who have completed their trainings into a new squad press m (or any other key to continue)...";
		else
			s+="Clicking this location again once any training period is over will allow you to have units exit into the world map. ";
		if(Javelin.promptscreen(s)=='m'&&anydone) completetraining(false);
	}

	@Override
	public boolean canupgrade(){
		return super.canupgrade()&&training.isempty();
	}

	@Override
	public boolean hold(){
		return !training.isempty();
	}

	@Override
	public void turn(long time,WorldScreen world){
		super.turn(time,world);
		if(training.reportalldone()&&Squad.getsquads().isEmpty())
			completetraining(false);
	}
}