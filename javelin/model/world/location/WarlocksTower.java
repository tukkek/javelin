/**
 *
 */
package javelin.model.world.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.fight.setup.BattleSetup;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.Item;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * The Mage Tower is meant as power-leveling tool: extremely
 * {@link Difficulty#DEADLY} {@link Fight}s that offer a 50-50% chance of
 * winning. Each round adds a summoned ally in the player's control and an
 * opportunity to leave. Should the player win, the rewards are handsome and one
 * of the summoned alllies can be kept and the location of the next one is
 * {@link WorldScreen#discover(int, int)}.
 *
 * The Tower is a static location relatively near the starting {@link Town} and
 * it scales and can be done multiple times - making it a good challenge-mode
 * experience. Upon winning, the player is allowed to upgrade any units with a
 * given number of {@link Kit}s.
 *
 * {@link Fight}s {@link Map}s are based on a random {@link Terrain}, while
 * {@link Branch}es are used to make each Fight more interesting and unique.
 *
 * TODO {@link Branch} stuff
 *
 * @author alex
 */
public class WarlocksTower extends Location{
	static final String DESCRIPTION="A crucible";
	static final String PROMPT="Are you sure you wish to submit yourself to the Warlock's challenge? You are unlikely to survive...\n\n"
			+"Press s to submit yourself to the trial or any other key to continue...";
	static final String CONCEDE="\"Do you concede, mortal? I will let you leave but any fallen allies will be mine!\"\n\n"
			+"Press c to concede or s to stay and fight...";
	static final String NEWWAVE="\"I will grant you recovery and an ally, puny mortals - so you have a chance to win!\"";
	static final String RECRUIT="\"I will allow you to retain one of my minions. Which one would you like?\"";
	static final String[] PROGRESS=new String[]{null,null,
			"\"I didn't expect you to survive...\"","\"Hmmm, not bad...\"",
			"\"You are doing quite well...\"",
			"\"You are doing exceptionally well!\""};
	static final String UPGRADE="The Warlock offers to magically train %s. What set of skills should they learn?";
	static final int WAVES=5;
	//	static final int CRUCIBLES=4;
	static final int MAXKITS=2;

	/**
	 * Gold and XP multiplier. As of now, doing crucibles only, the initial
	 * campaign units end up around half the level of the awarded ones. TODO might
	 * require some fine-tuning but right now the desired EL after each crucible
	 * is on-point.
	 */
	static final int BOOST=1;

	class Waves extends Mutator{
		List<Monster> enemies=new ArrayList<>();
		List<Combatant> allies=new ArrayList<>();
		int wave=1;

		@Override
		public void ready(Fight f){
			super.ready(f);
			for(var r:Fight.state.redteam)
				enemies.add(r.source);
		}

		Combatant recruit(float crp,TowerFight f){
			if(crp<1) crp=1;
			Combatant r=null;
			var candidates=new ArrayList<Monster>();
			for(var cr=crp;candidates.size()<3;cr--){
				var finalcr=cr;
				var recruits=f.monsters.stream().filter(m->m.cr==finalcr)
						.collect(Collectors.toList());
				if(recruits!=null) candidates.addAll(recruits);
			}
			r=new Combatant(RPG.pick(candidates),true);
			r.summoned=true;
			while(ChallengeCalculator.calculatecr(r.source)<crp)
				if(!Commoner.SINGLETON.upgrade(r)) break;
			return r;
		}

		@Override
		public void checkend(Fight f){
			super.checkend(f);
			var s=Fight.state;
			if(!s.redteam.isEmpty()) return;
			wave+=1;
			if(wave>WAVES) return;
			if(Javelin.prompt(PROGRESS[wave]+'\n'+CONCEDE,Set.of('c','s'))=='c'){
				s.redteam.addAll(Combatants.from(enemies));
				f.flee(false);
				return;
			}
			var r=recruit(Squad.active.getel()+wave-1,(TowerFight)f);
			var blue=s.blueteam;
			f.add(new Combatants(List.of(r)),blue);
			allies.add(r);
			for(var b:blue)
				Fountain.heal(b);
			Javelin.redraw();
			Javelin.message(NEWWAVE,true);
			for(var foe:f.getfoes(Math.round(r.source.cr)))
				enemies.add(foe.source);
			f.add(Combatants.from(enemies),s.redteam);
		}
	}

	class Setup extends BattleSetup{
		void wall(Square a){
			a.clear();
			a.blocked=true;
		}

		@Override
		public Map generatemap(Fight f,Map m){
			m=super.generatemap(f,m);
			m.floor=Images.get(List.of("terrain","warlockfloor"));
			m.wall=Images.get(List.of("terrain","warlockwall"));
			int width=m.map.length;
			int height=m.map[0].length;
			for(var x=0;x<width;x++){
				var walls=Math.max(1,Math.abs(width/2-x)*3/5);
				for(var y=0;y<walls;y++){
					wall(m.map[x][y]);
					wall(m.map[x][height-1-y]);
				}
			}
			for(var y=0;y<height;y++){
				wall(m.map[0][y]);
				wall(m.map[width-1][y]);
			}
			return m;
		}

	}

	class TowerFight extends Fight{
		Waves mutator=new Waves();
		List<Monster> monsters=new ArrayList<>();
		Terrain terrain;

		TowerFight(){
			bribe=false;
			hide=false;
			rewardgold=false;
			period=Period.EVENING;
			mutators.add(mutator);
			terrain=RPG.pick(Terrain.NONWATER);
			map=terrain.getmaps().pick();
			monsters.addAll(terrain.getmonsters());
			setup=new Setup();
		}

		@Override
		public Integer getel(int teamel){
			return Squad.active.getel();
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer el){
			return super.getfoes(el);
		}

		void loot(){
			var gold=0;
			var dead=new Combatants(Fight.state.dead);
			dead.retainAll(Fight.originalredteam);
			for(var d:dead)
				gold+=RewardCalculator.getgold(d.source.cr)*BOOST;
			var nitems=Squad.active.members.size()*2;
			nitems=RPG.randomize(nitems,1,Integer.MAX_VALUE);
			for(var i:RewardCalculator.generateloot(gold,nitems,Item.NONPRECIOUS)){
				i.identified=true;
				i.grab();
			}
		}

		@Override
		public boolean onend(){
			super.onend();
			if(!Fight.victory) return true;
			Javelin.message("\"You... have won?\"",true);
			var a=mutator.allies
					.get(Javelin.choose(RECRUIT,mutator.allies,true,true));
			Fight.state.clone(a).summoned=false;
			//			a.summoned=false;
			Squad.active.add(a);
			Fight.originalblueteam.add(a);
			for(var c:Squad.active)
				Fountain.heal(c);
			loot();
			for(var m:Squad.active){
				if(m.xp.floatValue()<=0) continue;
				var kits=RPG.shuffle(Kit.getpreferred(m.source,true));
				if(kits.size()>MAXKITS) kits=kits.subList(0,MAXKITS);
				var i=Javelin.choose(String.format(UPGRADE,m),kits,true,false);
				if(i>=0)
					AdventurersGuild.train(m,kits.get(i).getupgrades(),m.xp.floatValue());
			}
			return true;
		}

		@Override
		public int flood(){
			return RPG.pick(Arrays.asList(Weather.DISTRIBUTION));
		}
	}

	/** Constructor. */
	public WarlocksTower(){
		super(DESCRIPTION);
		impermeable=true;
		allowentry=false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean ishostile(){
		return false;
	}

	@Override
	public Integer getel(Integer attackerel){
		return attackerel;
	}

	@Override
	public boolean interact(){
		if(Javelin.prompt(PROMPT)!='s') return false;
		throw new StartBattle(new TowerFight());
	}
}
