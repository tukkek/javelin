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
import javelin.Javelin.Delay;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ActorByDistance;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * The crucibles are meant as power-leveling tools, extremely
 * {@link Difficulty#DEADLY} {@link Fight}s that offer a 50-50% chance of
 * winning. Each round adds a summoned ally in the player's control and an
 * opportunity to leave. Should the player win, the rewards are handsome and one
 * of the summoned alllies can be kept and the location of the next one is
 * {@link WorldScreen#discover(int, int)}.
 *
 * Each crucible has 5 rounds and there are 5 of them in the world (each in a
 * different {@link Terrain}), meant to take a {@link Squad} from EL4 (at
 * campaign start) to EL24 (late end-game).
 *
 * After being conquered, the crucible can be {@link #interact()} only once to
 * fully upgrade any members on a
 * {@link Kit#getpreferred(javelin.model.unit.Monster, boolean)}.
 *
 * @author alex
 */
public class Crucible extends Location{
	static final String DESCRIPTION="A crucible";
	static final String PROMPT="Are you sure you wish to submit yourself to the crucible? You are unlikely to surive...\n\n"
			+"Press s to submit yourself to the trial or any other key to continue...";
	static final String CONCEDE="\"Do you concede, mortal? I will let you leave but any fallen allies will be mine!\"\n\n"
			+"Press c to concede or s to stay and fight...";
	static final String NEWWAVE="\"I will grant you recovery and an ally, puny mortals - so you have a chance to win!\"";
	static final String RECRUIT="\"I will allow you to retain one of my minions. Which one would you like?\"";
	static final String UPGRADE="You have conquered this crucible!\n"
			+"It offers a one-time upgrade of your units, randomly assigning a kit to each.\n\n"
			+"Press u to accept the upgrade or any other key to cancel...";
	static final String[] PROGRESS=new String[]{null,null,
			"\"I didn't expect you to survive...\"","\"Hmmm, not bad...\"",
			"\"You are doing quite well...\"",
			"\"You are doing exceptionally well!\""};
	static final int WAVES=5;
	static final int CRUCIBLES=4;
	/**
	 * Gold and XP multiplier. As of now, doing crucibles only, the initial
	 * campaign units end up around half the level of the awarded ones. TODO might
	 * require some fine-tuning but right now the desired EL after each crucible
	 * is on-point.
	 */
	static final int BOOST=1;

	class CrucibleWaves extends Mutator{
		List<Monster> enemies=new ArrayList<>();
		List<Combatant> allies=new ArrayList<>();
		int wave=1;

		@Override
		public void ready(Fight f){
			super.ready(f);
			for(var r:Fight.state.redteam)
				enemies.add(r.source);
		}

		Combatant recruit(float crp){
			if(crp<1) crp=1;
			Combatant r=null;
			var candidates=new ArrayList<Monster>();
			for(var cr=crp;candidates.size()<3;cr--){
				var finalcr=cr;
				var recruits=Terrain.get(x,y).getmonsters().stream()
						.filter(m->m.cr==finalcr).collect(Collectors.toList());
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
			var r=recruit(Squad.active.getel()+wave-1);
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

		@Override
		public void after(Fight f){
			super.after(f);
			var members=Squad.active.members;
			if(!Fight.victory){
				if(wave>1){
					var xp=RewardCalculator.rewardxp(members,Fight.originalredteam,BOOST);
					Javelin.message(xp,true);
				}
				return;
			}
			Javelin.message("\"You... have won?\"",true);
			var a=allies.get(Javelin.choose(RECRUIT,allies,true,true));
			a.summoned=false;
			Squad.active.add(a);
			Fight.originalblueteam.add(a);
			for(var c:Squad.active)
				Fountain.heal(c);
			var gold=0;
			var dead=new Combatants(Fight.state.dead);
			dead.retainAll(Fight.originalredteam);
			for(var d:dead)
				gold+=RewardCalculator.getgold(d.source.cr)*BOOST;
			var nitems=members.size()*2;
			nitems=RPG.randomize(nitems,1,Integer.MAX_VALUE);
			for(var i:RewardCalculator.generateloot(gold,nitems,Item.NONPRECIOUS)){
				i.identified=true;
				i.grab();
			}
			conquered=true;
		}
	}

	class CrucibleFight extends Fight{
		CrucibleFight(){
			bribe=false;
			hide=false;
			rewardgold=false;
			map=Terrain.get(x,y).getmaps().pick();
			mutators.add(new CrucibleWaves());
		}

		@Override
		public Integer getel(int teamel){
			return Squad.active.getel();
		}
	}

	boolean conquered=false;

	/** Constructor. */
	public Crucible(){
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
		return !conquered;
	}

	@Override
	public Integer getel(Integer attackerel){
		return attackerel;
	}

	@Override
	public boolean interact(){
		if(conquered){
			var next=World.getall(Crucible.class).stream().map(c->(Crucible)c)
					.filter(c->c!=Crucible.this&&!c.conquered)
					.sorted(new ActorByDistance(Crucible.this)).findFirst().orElse(null);
			if(next!=null&&!next.cansee()){
				next.reveal();
				Javelin.redraw();
				WorldScreen.current.center(next.x,next.y);
				Javelin.message("The next nearest crucible is revealed!",Delay.NONE);
				Javelin.input();
			}
			if(Javelin.prompt(UPGRADE)!='u') return false;
			for(var m:Squad.active){
				var k=RPG.pick(Kit.getpreferred(m.source,true));
				AdventurersGuild.train(m,k.getupgrades(),m.xp.floatValue());
			}
			remove();
			return true;
		}
		if(Javelin.prompt(PROMPT)!='s') return false;
		throw new StartBattle(new CrucibleFight());
	}

	/**
	 * TODO might be faster to scan the whole map for terrain rather than the
	 * scattershot approach. TODO same for finding occupied tiles
	 */
	public static List<Crucible> generate(){
		var terrains=RPG.shuffle(new ArrayList<>(Arrays.asList(Terrain.STANDARD)));
		var w=World.getseed();
		var m=w.map;
		var generated=new ArrayList<Crucible>(5);
		for(var t:terrains.subList(0,CRUCIBLES)){
			var c=new Crucible();
			var a=World.getactors();
			while(c.x==-1||!Terrain.get(c.x,c.y).equals(t)
					||!c.validateplacement(false,w,a))
				c.setlocation(new Point(RPG.r(0,m.length-1),RPG.r(0,m[0].length-1)));
			c.place();
			generated.add(c);
		}
		return generated;
	}
}
