package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.controller.fight.minigame.arena.ArenaSetup;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

public class ArenaTown extends ArenaBuilding{
	static final List<Class<? extends ArenaBuilding>> BUILDINGTYPES=List.of(
			ArenaAcademy.class,ArenaFountain.class,ArenaLair.class,ArenaShop.class,
			ArenaShrine.class,ArenaMine.class);

	public static int kingdomlevel=2;

	abstract class TownOption extends Option{
		public TownOption(String name,int price){
			super(name,price);
		}

		abstract void buy();
	}

	class Upgrade extends TownOption{
		ArenaBuilding building;

		Upgrade(ArenaBuilding b,int price){
			super("Upgrade "+b.toString().toLowerCase()+" (level "+(b.level+1)+")",
					price);
			building=b;
			priority=1;
		}

		@Override
		public void buy(){
			building.setlevel(Building.LEVELS[building.level+1]);
			building.upgradebuilding();
		}
	}

	class Build extends TownOption{
		Building building;

		Build(Building b,int price){
			super("Build "+b.toString().toLowerCase(),price);
			building=b;
			priority=2;
		}

		@Override
		void buy(){
			ArenaSetup.place(building,quadrant);
			Fight.state.blueTeam.add(building);
		}
	}

	public class ArenaTownScreen extends PurchaseScreen{
		public ArenaTownScreen(){
			super("What to build?",null);
		}

		@Override
		public List<Option> getoptions(){
			ArrayList<Option> options=new ArrayList<>();
			int price=getprojectprice();
			for(Combatant c:Fight.state.blueTeam){
				ArenaBuilding b=c instanceof ArenaBuilding?(ArenaBuilding)c:null;
				if(b!=null&&b.level!=LEVELS.length-1) options.add(new Upgrade(b,price));
			}
			try{
				for(Class<? extends ArenaBuilding> type:BUILDINGTYPES){
					Building b=type.getDeclaredConstructor().newInstance();
					options.add(new Build(b,price));
				}
			}catch(ReflectiveOperationException e){
				throw new RuntimeException(e);
			}
			return options;
		}

		@Override
		protected int getgold(){
			return Arena.get().gold;
		}

		@Override
		protected void spend(Option o){
			Arena.get().gold-=o.price;
			((TownOption)o).buy();
			kingdomlevel+=1;
		}

		@Override
		protected Comparator<Option> sort(){
			return (a,b)->a.priority==b.priority?a.name.compareTo(b.name)
					:Float.compare(a.priority,b.priority);
		}
	}

	int quadrant;

	public ArenaTown(int quadrant){
		super("Keep","locationtowncity","Manage your buildings.");
		this.quadrant=quadrant;
	}

	@Override
	protected void upgradebuilding(){
		// just bump stats
	}

	@Override
	protected boolean click(Combatant current){
		new ArenaTownScreen().show();
		return true;
	}

	/**
	 * @return A town instance for the current {@link Arena} or
	 *         <code>null</code> if it's destroyed.
	 */
	public static ArenaTown get(){
		return (ArenaTown)Fight.state.blueTeam.stream()
				.filter(c->c instanceof ArenaTown).findAny().orElse(null);
	}

	@Override
	public String getactiondescription(Combatant current){
		int price=getprojectprice();
		String gold=Javelin.format(Arena.get().gold);
		return super.getactiondescription(current)+"\n\n"+"Next project: $"+price
				+". You have $"+gold+".";
	}

	public int getprojectprice(){
		return Javelin.round(RewardCalculator.getgold(kingdomlevel));
	}
}
