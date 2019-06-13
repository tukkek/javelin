package javelin.model.world.location.unique;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.Rank;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.upgrading.UpgradingScreen.UpgradeOption;

/**
 * Lets you learn summoning spells. All monsters are theoretically possible but
 * offer just a few for higher randomization.
 *
 * @author alex
 */
public class SummoningCircle extends Academy{
	static final String DESCRIPTION="Summoning circle";
	static final int MAXSPELLS=9;

	/**
	 * Builds a {@link SummoningCircle}.
	 *
	 * @author alex
	 */
	public static class BuildSummoningCircle extends BuildAcademy{
		public BuildSummoningCircle(){
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy generateacademy(){
			return new SummoningCircle(cost-1,cost+1);
		}
	}

	/** Constructor. */
	public SummoningCircle(int minlevelp,int maxlevelp){
		super(DESCRIPTION,DESCRIPTION,new HashSet<Upgrade>());
		minlevel=minlevelp;
		maxlevel=maxlevelp;
		pillage=false;
		populate();
	}

	void populate(){
		List<Monster> summons=Monster.get(MonsterType.OUTSIDER);
		while(upgrades.size()<MAXSPELLS&&!summons.isEmpty()){
			Monster m=RPG.pick(summons);
			summons.remove(m);
			upgrades.add(new Summon(m.name,1f));
		}
	}

	Monster pickmonster(float cr){
		List<Monster> monsters=Javelin.MONSTERSBYCR.get(cr);
		Collections.shuffle(monsters);
		for(Monster m:monsters)
			if(m.type.equals(MonsterType.OUTSIDER)) return m;
		return null;
	}

	@Override
	public void sort(List<Option> upgrades){
		for(Option o:upgrades){
			UpgradeOption uo=o instanceof UpgradeOption?(UpgradeOption)o:null;
			Summon s=uo!=null&&uo.u instanceof Summon?(Summon)uo.u:null;
			if(s!=null) o.priority+=s.cr/21f;
		}
	}

	@Override
	protected void generate(){
		while(x==-1||Terrain.get(x,y).equals(Terrain.PLAIN)
				||Terrain.get(x,y).equals(Terrain.HILL))
			super.generate();
	}
}
