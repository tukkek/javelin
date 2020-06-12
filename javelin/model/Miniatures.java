package javelin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.MonstersByCr;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.map.Map;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;

public class Miniatures{
	public static ArrayList<Monster> miniatures=new ArrayList<>(0);

	public static class MiniatureFight extends Fight{
		protected List<Monster> bluearmy;
		protected List<Monster> redarmy;
		protected List<Monster> bluecollection;
		protected List<Monster> redcollection;
		/** Extra EL difficulty for {@link #balance()}. */
		protected int difficulty=0;

		public MiniatureFight(List<Monster> bluearmy,List<Monster> redarmy,
				List<Monster> bluecollection,List<Monster> redcollection){
			this.bluearmy=bluearmy;
			this.redarmy=redarmy;
			this.bluecollection=bluecollection;
			this.redcollection=redcollection;
			map=RPG.pick(Map.getall().stream().filter(m->m.standard)
					.collect(Collectors.toList()));
			map.floor=Images.get("terrainboardfloor");
			map.wall=Images.get("terrainboardwall");
			bribe=false;
			hide=false;
			meld=true;
			period=Javelin.PERIODNOON;
			rewardgold=false;
			rewardgold=false;
			rewardxp=false;
			weather=Weather.CLEAR;
			onready.add(()->{
				for(var tiles:map.map)
					for(var tile:tiles)
						tile.obstructed=false;
				while(balance())
					continue;
			});
		}

		boolean balance(){
			var blue=ChallengeCalculator.calculateel(state.blueTeam);
			var red=ChallengeCalculator.calculateel(state.redTeam);
			if(blue+difficulty==red) return false;
			var weak=blue<red?state.blueTeam:state.redTeam;
			var weakest=new Combatants(weak).getweakest();
			if(!Commoner.SINGLETON.upgrade(weakest)) return false;
			ChallengeCalculator.calculatecr(weakest.source);
			weakest.source.elite=true;
			return true;
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer teamel){
			return new Combatants(redarmy.stream().map(m->new Combatant(m,true))
					.collect(Collectors.toList()));
		}

		@Override
		public ArrayList<Combatant> getblueteam(){
			return new Combatants(bluearmy.stream().map(m->new Combatant(m,true))
					.collect(Collectors.toList()));
		}

		@Override
		public void withdraw(Combatant combatant,BattleScreen screen){
			if(Javelin.prompt("Do you want to concede this match?\n"
					+"Press ENTER to confirm or any other key to cancel...")!='\n'){
				BattleScreen.active.messagepanel.clear();
				return;
			}
			Fight.state.blueTeam.clear();
			throw new EndBattle();
		}

		@Override
		public boolean onend(){
			if(victory){
				var unique=new TreeSet<>(MonstersByCr.SINGLETON.reversed());
				unique.addAll(redarmy);
				var choices=new ArrayList<>(unique);
				var i=Javelin.choose("You've won! Choose a miniature to take as prize:",
						choices,true,false);
				if(i>=0){
					var chosen=choices.get(i);
					redcollection.remove(chosen);
					bluecollection.add(chosen);
				}
			}else{
				RPG.shuffle(bluearmy).sort(MonstersByCr.SINGLETON);
				var lost=bluearmy.remove(bluearmy.size()-1);
				bluecollection.remove(lost);
				redcollection.add(lost);
				Javelin.message("You have lost this match!\n"+"Your opponent takes a "
						+lost+" miniature as reward...",true);
			}
			return false;
		}
	}

	static boolean choose(List<Monster> bluearmy,List<Monster> redarmy,
			List<Monster> blueset,List<Monster> redset){
		var setup="Your army: "+Javelin.group(bluearmy)+".\n\n";
		setup+="Opponent's army: "+Javelin.group(redarmy)+".\n\n";
		var blueel=ChallengeCalculator.calculateelfromcrs(
				bluearmy.stream().map(m->m.cr).collect(Collectors.toList()));
		var redel=ChallengeCalculator.calculateelfromcrs(
				redarmy.stream().map(m->m.cr).collect(Collectors.toList()));
		if(blueel==redel?!RPG.chancein(2):blueel<redel){
			var uniques=new TreeSet<>(MonstersByCr.SINGLETON.reversed());
			uniques.addAll(blueset);
			var left=new ArrayList<>(uniques);
			var choseni=Javelin.choose(setup+"Choose a miniature:",left,true,false);
			if(choseni<0) return false;
			var chosen=left.get(choseni);
			blueset.remove(chosen);
			bluearmy.add(chosen);
		}else{
			var pick=RPG.pick(redset);
			redset.remove(pick);
			redarmy.add(pick);
			setup+="Your opponent adds: "+pick+".";
			Javelin.promptscreen(setup);
		}
		return !redset.isEmpty()&&!blueset.isEmpty();
	}

	public static void play(List<Monster> opponent){
		if(miniatures.isEmpty()){
			Javelin.message("You don't own any miniatures...",false);
			return;
		}
		if(opponent.isEmpty()){
			Javelin.message("Your opponent doenn't own any miniatures...",false);
			return;
		}
		var bluearmy=new ArrayList<Monster>(miniatures.size());
		var redarmy=new ArrayList<Monster>(opponent.size());
		var blueset=new ArrayList<>(miniatures);
		var redset=new ArrayList<>(opponent);
		while(choose(bluearmy,redarmy,blueset,redset))
			continue;
		if(!bluearmy.isEmpty()&&!redarmy.isEmpty()) throw new StartBattle(
				new MiniatureFight(bluearmy,redarmy,miniatures,opponent));
	}

	/** @return Collection of given size and rough average {@link Monster#cr}. */
	public static List<Monster> buildcollection(int size,int averagecr){
		var collection=new ArrayList<Monster>(size);
		while(collection.size()<size){
			var cr=averagecr+RPG.randomize(4);
			var tier=Monster.MONSTERS.stream().filter(m->m.cr==cr&&!m.passive)
					.collect(Collectors.toList());
			if(!tier.isEmpty()) add(RPG.pick(tier),collection);
		}
		return collection;
	}

	/** @param mini Adds appropriate number of these to collection. */
	public static void add(Monster mini,ArrayList<Monster> collection){
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
