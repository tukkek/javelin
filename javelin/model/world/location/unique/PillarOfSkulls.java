package javelin.model.world.location.unique;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Heroic;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Sacrifice allies, experience and items for benefits.
 *
 * @author alex
 */
public class PillarOfSkulls extends UniqueLocation{
	static String[] QUOTES=new String[]{"hisses at you!",
			"is blabbering nonsense...","is talking to its neighbor.",
			"submerges deep into the pillar!",
			"emerges from the pillar and looks at you with an intense stare...",
			"asks \"Have you seen Morte?\"","asks \"Who are you?\"",
			"sings \"... it must be a dream from below!\"","bobs playfully."};

	static Option SHOWLOCATION=new Option("Show location (50XP)",0,'l');
	static Option OFFERITEM=new Option("Offer item (1 item)",0,'o');
	static Option SACRIFICE=new Option("Sacrifice (1 non-mercenary unit)",0,'s');
	static Option UNHOLYBLESSING=new Option(
			"Unholy blessing (99% of a non-mercenary's hit points)",0,'u');

	class Screen extends SelectScreen{
		public Screen(){
			super(
					"You approach The Pillar of Skulls, a towering abomination made of living skulls!",
					null);
			stayopen=false;
		}

		@Override
		public String getCurrency(){
			return "";
		}

		@Override
		public String printinfo(){
			return "One of the skulls "+getdailyquote(Arrays.asList(QUOTES))+" ";
		}

		@Override
		public String printpriceinfo(Option o){
			return "";
		}

		@Override
		public List<Option> getoptions(){
			ArrayList<Option> list=new ArrayList<>();
			list.add(SHOWLOCATION);
			list.add(OFFERITEM);
			list.add(SACRIFICE);
			list.add(UNHOLYBLESSING);
			return list;
		}

		@Override
		public boolean select(Option o){
			if(o==SACRIFICE) return sacrifice();
			if(o==SHOWLOCATION) return showlocation();
			if(o==UNHOLYBLESSING) return bless();
			if(o==OFFERITEM) return offeritem();
			return false;
		}

		boolean offeritem(){
			ArrayList<Item> items=new ArrayList<>();
			ArrayList<Combatant> owners=new ArrayList<>();
			ArrayList<String> choices=new ArrayList<>();
			gatheritems(items,owners,choices);
			if(items.isEmpty()){
				print(text+"\nCome back with an item to offer!");
				return false;
			}
			int choice=Javelin.choose("Sacrifice which item?",choices,true,false);
			if(choice==-1) return false;
			Item i=items.get(choice);
			Combatant owner=owners.get(choice);
			if(i instanceof Artifact){
				Artifact a=(Artifact)i;
				if(owner.equipped.contains(a)) a.remove(owner);
			}
			Squad.active.equipment.get(owner).remove(i);
			ArrayList<Combatant> mock=new ArrayList<>();
			float targetcr=ChallengeCalculator.goldtocr(i.price);
			while(mock.isEmpty()){
				List<Monster> tier=Monster.BYCR.get(targetcr);
				if(tier==null){
					targetcr-=1;
					if(targetcr<Monster.BYCR.firstKey()) return false;
					continue;
				}
				mock.add(new Combatant(RPG.pick(tier),false));
			}
			RewardCalculator.rewardxp(Squad.active.members,mock,1);
			return true;
		}

		void gatheritems(ArrayList<Item> items,ArrayList<Combatant> owners,
				ArrayList<String> choices){
			for(Combatant c:Squad.active.members)
				for(Item i:Squad.active.equipment.get(c)){
					items.add(i);
					owners.add(c);
					choices.add(i+" (with "+c+")");
				}
		}

		boolean showlocation(){
			if(!PillarOfSkulls.canrecruit(50)){
				print(text+"\nReturn when you have more experience!");
				return false;
			}
			PillarOfSkulls.spend(.5f);
			Actor closest=find(UniqueLocation.class);
			if(closest==null) closest=find(Town.class);
			if(closest==null) closest=find(Location.class);
			if(closest==null)
				print(text+"\nWe have nothing more to show you!");
			else
				WorldScreen.discover(closest.x,closest.y);
			return true;
		}

		boolean sacrifice(){
			ArrayList<Combatant> sacrifices=getsacrifices(Combatant.STATUSDYING);
			if(Squad.active.members.size()==1||sacrifices.isEmpty()){
				print(text+"\nBring me a good sacrifice first!");
				return false;
			}
			int choice=Javelin.choose("Sacrifice who?",sacrifices,true,false);
			if(choice<0) return false;
			Combatant sacrifice=sacrifices.get(choice);
			Squad.active.remove(sacrifice);
			ArrayList<Combatant> mock=new ArrayList<>();
			mock.add(sacrifice);
			RewardCalculator.rewardxp(Squad.active.members,mock,2);
			RewardCalculator.distributexp(Squad.active.members,
					sacrifice.xp.floatValue());
			return true;
		}

		boolean bless(){
			ArrayList<Combatant> sacrifices=getsacrifices(Combatant.STATUSSCRATCHED);
			if(sacrifices.isEmpty()){
				print(text+"\nCome back in better health!");
				return false;
			}
			int choice=Javelin.choose("Who shall we drain?",sacrifices,true,false);
			if(choice==-1) return false;
			sacrifices.get(choice).hp=1;
			for(Combatant c:Squad.active.members)
				c.addcondition(new Heroic(c,20,24));
			return true;
		}
	}

	private static final String DESCRIPTION="The Pillar of Skulls";

	/** Constructor. */
	public PillarOfSkulls(){
		super(DESCRIPTION,DESCRIPTION,1,1);
	}

	Actor find(Class<? extends Location> class1){
		Actor closest=null;
		for(Actor a:World.getactors())
			if(class1.isInstance(a)&&!WorldScreen.see(new Point(a.x,a.y)))
				if(closest==null||a.distance(x,y)<closest.distance(x,y)) closest=a;
		return closest;
	}

	ArrayList<Combatant> getsacrifices(int status){
		ArrayList<Combatant> sacrifices=new ArrayList<>();
		for(Combatant c:Squad.active.members)
			if(!c.mercenary&&c.getnumericstatus()>=status) sacrifices.add(c);
		return sacrifices;
	}

	@Override
	protected void generategarrison(int minlevel,int maxlevel){
		garrison.clear();
	}

	@Override
	protected void generate(){
		while(x==-1||!Terrain.get(x,y).equals(Terrain.MARSH))
			super.generate();
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		new Screen().show();
		return true;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	/**
	 * @param cr Spend this much CR in recruiting a rookie (1CR = 100XP).
	 */
	static public void spend(double cr){
		double nmembers=Squad.active.members.size();
		double percapita=cr/nmembers;
		boolean buyfromall=true;
		for(Combatant c:Squad.active.members)
			if(percapita>c.xp.doubleValue()){
				buyfromall=false;
				break;
			}
		if(buyfromall)
			for(Combatant c:Squad.active.members)
				c.xp=c.xp.subtract(new BigDecimal(percapita));
		else{
			ArrayList<Combatant> squad=new ArrayList<>(Squad.active.members);
			ChallengeCalculator.calculateel(squad);
			Collections.sort(squad,(o1,o2)->{
				final Float cr1=o2.xp.floatValue()+o2.source.cr;
				final Float cr2=o1.xp.floatValue()+o1.source.cr;
				return cr1.compareTo(cr2);
			});
			for(Combatant c:squad){
				if(c.xp.doubleValue()>=cr){
					c.xp=c.xp.subtract(new BigDecimal(cr));
					return;
				}
				cr-=c.xp.doubleValue();
				c.xp=new BigDecimal(0);
			}
		}
	}

	/**
	 * @param m Given a monster...
	 * @return recruits into {@link Squad#active} and {@link PillarOfSkulls#spend(double)} XP if
	 *         {@link canrecruit}.
	 */
	public static boolean recruit(Monster m){
		if(!canrecruit(m.cr*100)) return false;
		PillarOfSkulls.spend(m.cr);
		Squad.active.recruit(m);
		return true;
	}

	/**
	 * @param price Price in XP (100XP = 1CR).
	 * @return <code>true</code> if currently active {@link Squad} can afford this
	 *         much.
	 */
	static public boolean canrecruit(double price){
		return price<=Squad.active.sumxp();
	}

	/**
	 * @return A random element from the list, guaranteed to be the same for a 24
	 *         hour period.
	 */
	public static String getdailyquote(List<String> list){
		Calendar now=Calendar.getInstance();
		String seed="";
		seed+=now.get(Calendar.DAY_OF_MONTH);
		seed+=now.get(Calendar.MONTH)+1;
		seed+=now.get(Calendar.YEAR);
		return list.get(new Random(seed.hashCode()).nextInt(list.size()-1));
	}
}