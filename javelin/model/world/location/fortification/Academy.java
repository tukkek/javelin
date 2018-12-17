package javelin.model.world.location.fortification;

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
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.Realm;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.view.screen.Option;
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
			super("",0,null,minimumrank);
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
	/** If <code>true</code> will allow the academy to be pillaged for money. */
	public boolean pillage=true;
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

	public Academy(String descriptionknown,String descriptionunknown,
			HashSet<Upgrade> upgrades){
		this(descriptionknown,descriptionunknown,Math.max(1,upgrades.size()-1),
				upgrades.size()+1,upgrades,null,null);
	}

	/**
	 * Normally {@link #training} units don't get out of the academy by themselves
	 * since this would mean being alone in the wild but if the game is about to
	 * be lost due to the absence of {@link Squad}s then the unit gets out to
	 * prevent the game from ending.
	 *
	 * @return <code>false</code> if there was no unit in {@link #training}.
	 */
	public static boolean train(){
		boolean trained=false;
		for(Actor actor:World.getactors())
			if(actor instanceof Academy){
				Academy a=(Academy)actor;
				/* don't inline */
				for(Order order:a.training.queue){
					TrainingOrder o=(TrainingOrder)order;
					a.completetraining(o).hourselapsed=Math.max(o.completionat,
							Squad.active.hourselapsed);
					trained=true;
				}
				a.training.clear();
			}
		return trained;
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
		completetraining();
		getscreen().show();
		return true;
	}

	void completetraining(){
		for(Order o:training.reclaim(Squad.active.hourselapsed))
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
		ArrayList<Point> free=new ArrayList<>();
		ArrayList<Actor> actors=World.getactors();
		HashSet<Point> district=getdistrict()==null?null:getdistrict().getarea();
		for(Point p:Point.getadjacent()){
			p.x+=x;
			p.y+=y;
			if(!World.validatecoordinate(p.x,p.y)
					||Terrain.get(p.x,p.y).equals(Terrain.WATER))
				continue;
			Squad stationed=(Squad)World.get(p.x,p.y,Squad.class);
			if(stationed==null){
				if(World.get(p.x,p.y,actors)==null
						&&(district==null||district.contains(p)))
					free.add(new Point(p.x,p.y));
			}else{
				stationed.add(member,o.equipment);
				return stationed;
			}
		}

		Point destination=free.isEmpty()?getlocation():RPG.pick(free);
		Squad s=new Squad(destination.x,destination.y,
				Math.round(Math.ceil(o.completionat/24f)*24),null);
		s.add(member,o.equipment);
		s.place();
		if(free.isEmpty()) s.displace();
		return s;
	}

	@Override
	public Integer getel(Integer attackerel){
		return ChallengeCalculator.calculateel(getcombatants());
	}

	@Override
	protected void captureforai(Incursion attacker){
		super.captureforai(attacker);
		training.clear();
		stash=0;
		parking=null;
	}

	protected static HashSet<Upgrade> getupgrades(Realm r){
		return UpgradeHandler.singleton.getupgrades(r);
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
		String s="Training period left::\n\n";
		boolean anydone=false;
		for(Order o:training.queue){
			s+=o+"\n";
			anydone=anydone||o.completed(Squad.active.hourselapsed);
		}
		s+="\n";
		if(anydone)
			s+="To move units who have completed their trainings into a new squad press m (or any other key to continue)...";
		else
			s+="Clicking this location again once any training period is over will allow you to have units exit into the world map. ";
		if(Javelin.promptscreen(s)=='m'&&anydone) completetraining();
	}

	@Override
	public boolean canupgrade(){
		return super.canupgrade()&&training.isempty();
	}
}