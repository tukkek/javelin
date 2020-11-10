package javelin.model.world.location.town.labor.basic;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.Images;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * TODO the constants are a mess, push {@link #TITLES} and {@link #IMAGES} and
 * {@link #LABOR} to {@link Lodging}
 *
 * @author alex
 */
public class Lodge extends Fortification{
	/** Smaller-tier lodge. */
	public static final Lodging LODGE=new Lodging("lodge",1,0);
	/** Medium-tier lodge. */
	public static final Lodging HOTEL=new Lodging("hotel",2,.5f);
	/** Highest-tier lodge. */
	public static final Lodging HOSPITAL=new Lodging("hospital",4,2);
	/** All tiers from lowest to biggest. */
	public static final Lodging[] LODGING=new Lodging[]{LODGE,HOTEL,HOSPITAL};
	/** More descriptive titles for {@link #LODGING} entries. */
	public static final String[] TITLES=new String[]{"Traveller's lodge","Hotel",
			"Hospital"};
	/** Avatar filenames for {@link #LODGING} tiers (without extension). */
	public static final String[] IMAGES=new String[]{"inn","innhotel",
			"innhospital"};
	/** Labor cost for each {@link #LODGING} tier. */
	public static final int[] LABOR=new int[]{5,10,15};
	/** Standard amount of time for a rest. */
	public static final int RESTPERIOD=8;

	static final int WEEKLONGREST=24*7/RESTPERIOD;
	static final int MAXLEVEL=TITLES.length-1;

	static class Lodging{
		String name;
		private float fee;
		int quality;

		Lodging(String name,int quality,float fee){
			this.name=name;
			this.fee=fee;
			this.quality=quality;
		}

		public int getfee(){
			return fee==0?0:Math.round(Math.max(1,Squad.active.eat()*fee));
		}
	}

	/**
	 * {@link Town} project.
	 *
	 * @author alex
	 */
	public static class BuildLodge extends Build{
		/** Constructor. */
		public BuildLodge(){
			super("Build "+Lodge.TITLES[0].toLowerCase(),Lodge.LABOR[0],Rank.HAMLET,
					null);
		}

		@Override
		public Location getgoal(){
			return new Lodge();
		}

		@Override
		public boolean validate(District d){
			if(site==null
					&&(d.getlocation(Lodge.class)!=null||d.isbuilding(Lodge.class)))
				return false;
			return super.validate(d);
		}
	}

	class UpgradeLodge extends BuildingUpgrade{
		public UpgradeLodge(Lodge i){
			super(TITLES[i.level+1],LABOR[i.level+1],5,i,Rank.RANKS[i.level+1]);
		}

		@Override
		public Location getgoal(){
			return previous;
		}

		@Override
		public void done(Location l){
			Lodge i=(Lodge)l;
			if(i.level<MAXLEVEL){
				i.level+=1;
				i.rename(TITLES[i.level]);
			}
			super.done(l);
		}

		@Override
		public boolean validate(District d){
			Lodge i=(Lodge)previous;
			return d!=null&&i.level<MAXLEVEL&&super.validate(d);
		}
	}

	int level=0;

	/** Consstructor. */
	public Lodge(){
		super(TITLES[0],TITLES[0],1,5);
		gossip=true;
		neutral=true;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		int price=LODGING[level].getfee();
		int weekprice=WEEKLONGREST*price;
		String s="Do you want to rest at the "+TITLES[level].toLowerCase()+"?\n";
		s+="\nENTER or s to stay ($"+price+"), w to stay for a week ($"+weekprice
				+")";
		s+="\nany other key to leave";
		Character input=Javelin.prompt(s);
		if(input=='\n'||input=='s') return rest(price,level+1);
		if(input=='w') return rest(weekprice,WEEKLONGREST*(level+1));
		return false;
	}

	boolean rest(long price,int periods){
		if(Squad.active.gold<price){
			Javelin.message("You can't pay the $"+price+" fee!",false);
			return false;
		}
		Squad.active.gold-=price;
		rest(periods,RESTPERIOD*periods,true,LODGING[level]);
		return true;
	}

	@Override
	protected void generate(){
		x=-1;
		while(x==-1||getdistrict()!=null||checkproximity())
			generateawayfromtown();
	}

	/** @return <code>true</code> if there is another lodge nearby. */
	public boolean checkproximity(){
		Actor nearest=findnearest(Lodge.class);
		return nearest!=null&&nearest.distance(x,y)<=District.RADIUSMAX;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public ArrayList<Labor> getupgrades(District d){
		ArrayList<Labor> upgrades=super.getupgrades(d);
		if(level<MAXLEVEL) upgrades.add(new UpgradeLodge(this));
		return upgrades;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("world",IMAGES[level]));
	}

	/**
	 * @param restperiods Normally 1 rest period equals to 8 hours of rest in
	 *          normal conditions.
	 * @param hours Number of hours elapsed.
	 * @param accomodation Level of the resting environment.
	 */
	public static void rest(int restperiods,int hours,boolean advancetime,
			Lodging a){
		var s=Squad.active;
		for(int i=0;i<restperiods;i++){
			s.quickheal();
			for(final Combatant c:s){
				int heal=c.source.hd.count();
				if(!a.equals(HOSPITAL)&&s.heal()>=15) heal*=2;
				if(heal<1) heal=1;
				c.heal(heal,false);
				for(Spell p:c.spells)
					p.used=0;
				if(c.source.poison>0){
					final int detox=restperiods==1?RPG.r(0,1):i%2;
					c.detox(Math.min(c.source.poison,detox));
				}
				c.terminateconditions(hours);
			}
		}
		if(advancetime) s.delay(hours);
		identifyitems(s);
	}

	static void identifyitems(Squad s){
		var identified=new ArrayList<Item>(0);
		for(var i:s.equipment.getall())
			if(!i.identified&&s.identify(i)){
				i.identified=true;
				identified.add(i);
			}
		if(!identified.isEmpty()){
			String message="The following items have been identified: %s!";
			message=String.format(message,Javelin.group(identified).toLowerCase());
			Javelin.message(message,true);
		}
	}
}
