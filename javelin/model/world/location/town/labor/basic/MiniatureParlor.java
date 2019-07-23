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
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.PurchaseScreen;

public class MiniatureParlor extends Location{
	static final Option PLAY=new Option("Play a match",0);
	static final int MINIMUMSTOCK=5;

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
				var opponent=new ArrayList<Monster>();
				var target=RPG.rolldice(miniatures.size(),4);
				var external=RPG.r(1,4);
				while(opponent.size()<target){
					var mini=RPG.pick(miniatures);
					add(mini,opponent);
					while(RPG.r(1,4)<external&&opponent.size()<target){
						var cr=mini.cr+RPG.randomize(4);
						var tier=Monster.MONSTERS.stream().filter(m->m.cr==cr)
								.collect(Collectors.toList());
						if(!tier.isEmpty()) add(RPG.pick(tier),opponent);
					}
				}
				Miniatures.play(opponent);
				return true;
			}
			return super.select(o);
		}

		void add(Monster mini,ArrayList<Monster> collection){
			var t=Tier.get(mini.cr);
			int quantity;
			if(t==Tier.LOW)
				quantity=RPG.r(1,8);
			else if(t==Tier.MID)
				quantity=RPG.r(1,6);
			else if(t==Tier.HIGH)
				quantity=RPG.r(1,4);
			else
				quantity=1;
			for(int i=0;i<quantity;i++)
				collection.add(mini);
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
		if(d==null){
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
