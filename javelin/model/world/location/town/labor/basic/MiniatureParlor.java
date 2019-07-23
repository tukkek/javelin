package javelin.model.world.location.town.labor.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.MonstersByCr;
import javelin.model.Miniatures;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.PurchaseScreen;

public class MiniatureParlor extends Location{
	static final Option PLAY=new Option("Play a match",0);
	static final int MINIMUMSTOCK=5;

	/** {@link Labor} with cost 1 so as not to penalize actual game content. */
	public static class BuildMiniatureParlor extends Build{
		/** Constructor. */
		public BuildMiniatureParlor(){
			super("Build miniature parlor",1,Rank.VILLAGE,null);
		}

		@Override
		public Location getgoal(){
			return new MiniatureParlor();
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)&&d.getlocation(MiniatureParlor.class)==null;
		}
	}

	class MiniaturePurchase extends Option{
		Monster mini;

		public MiniaturePurchase(Monster m){
			super(m+" miniature",Javelin.round(RewardCalculator.getgold(m.cr)));
			if(price<1) price=1;
			mini=m;
		}
	}

	class MiniatureScreen extends PurchaseScreen{
		public MiniatureScreen(){
			super("Welcome to the miniature shop!",null);
		}

		@Override
		public List<Option> getoptions(){
			var options=new ArrayList<Option>(miniatures.size()+1);
			for(var m:miniatures)
				options.add(new MiniaturePurchase(m));
			options.add(PLAY);
			return options;
		}

		@Override
		public String printpriceinfo(Option o){
			return o==PLAY?"":super.printpriceinfo(o);
		}

		@Override
		protected void spend(Option o){
			super.spend(o);
			Miniatures.miniatures.add(((MiniaturePurchase)o).mini);
		}

		@Override
		public boolean select(Option o){
			if(o==PLAY){
				var target=RPG.rolldice(miniatures.size(),4);
				var opponent=new ArrayList<Monster>(target);
				var external=Math.round(target*(4-RPG.r(1,4))/4f);
				while(opponent.size()<target-external)
					Miniatures.add(RPG.pick(miniatures),opponent);
				if(external>0){
					var d=getdistrict();
					var cr=d==null||d.town==null?RPG.r(1,20):d.town.population;
					opponent.addAll(Miniatures.buildcollection(external,cr));
				}
				Miniatures.play(opponent);
				return true;
			}
			return super.select(o);
		}

		@Override
		public String printinfo(){
			var collection=Miniatures.miniatures.isEmpty()?"empty..."
					:Javelin.group(Miniatures.miniatures)+".";
			return super.printinfo()+"\n\nYour collection: "+collection;
		}
	}

	Set<Monster> miniatures=new TreeSet<>(MonstersByCr.SINGLETON);

	/** Constructor. */
	public MiniatureParlor(){
		super("Miniature parlor");
		allowentry=false;
		discard=false;
		gossip=true;
		restock();
	}

	void restock(){
		int maxlevel;
		int stock;
		var d=getdistrict();
		if(d==null||d.town==null){
			maxlevel=1;
			stock=MINIMUMSTOCK;
		}else{
			maxlevel=d.town.population;
			stock=Math.max(maxlevel,MINIMUMSTOCK);
		}
		var valid=Monster.MONSTERS.stream().filter(m->m.cr<=maxlevel)
				.collect(Collectors.toList());
		while(miniatures.size()<stock)
			miniatures.add(RPG.pick(valid));
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public void turn(long time,WorldScreen world){
		super.turn(time,world);
		if(!RPG.chancein(7)) return;
		if(!miniatures.isEmpty()&&RPG.chancein(2))
			miniatures.remove(RPG.pick(miniatures));
		if(RPG.chancein(2)) restock();
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		new MiniatureScreen().show();
		return true;
	}
}
