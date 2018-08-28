package javelin.controller.fight.minigame.arena.building;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.model.unit.Combatant;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;

public class ArenaTown extends ArenaBuilding{
	protected class BuildingUpgradeOption extends Option{
		ArenaBuilding building;

		protected BuildingUpgradeOption(ArenaBuilding b){
			super("Upgrade "+b.toString().toLowerCase(),
					RewardCalculator.getgold(level+1),'u');
			building=b;
			priority=2;
		}

		protected void upgrade(){
			building.setlevel(BuildingLevel.LEVELS[building.level+1]);
			building.upgradebuilding();
		}

		public boolean buy(InfoScreen s){
			ArenaFight f=ArenaFight.get();
			if(f.gold>=price){
				f.gold-=price;
				upgrade();
				return true;
			}
			String gold=Javelin.format(f.gold);
			s.print("Not enough gold (you currently have $"+gold+").\n\n"
					+"Press any key to continue....");
			return false;
		}
	}

	public int level=1;

	public ArenaTown(){
		super("Town","locationtownhamlet","Manage your buildings.");
	}

	@Override
	protected void upgradebuilding(){
		// just bump stats
	}

	@Override
	protected boolean click(Combatant current){
		return false;
	}

	public static ArenaTown get(){
		for(Combatant c:Fight.state.blueTeam)
			if(c instanceof ArenaTown) return (ArenaTown)c;
		return null;
	}
}
