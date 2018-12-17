package javelin.model.world.location.town.quest;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.kit.Cleric;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * Visit holy sites and bring the faithful back to safety.
 *
 * @author alex
 */
public class Pilgrimage extends Quest{
	class Pilgrim extends Combatant{
		Pilgrim(Monster m){
			super(m,true);
			setmercenary(true);
			m.customName="Pilgrim";
		}

		@Override
		public int pay(){
			return 0;
		}

		@Override
		public void bury(){
			followers.remove(this);
		}
	}

	class HolySite extends Actor{
		static final String ESCORT="Do you want to escort the pilgrims from this holy site?\n"
				+"Press ENTER to confirm or any other key to cancel...";

		public HolySite(){
			x=-1;
			while(x==-1||!World.validatecoordinate(x,y)
					||Terrain.get(x,y).equals(Terrain.WATER)){
				x=RPG.r(town.x-distance,town.x+distance);
				y=RPG.r(town.y-distance,town.y+distance);
			}
		}

		@Override
		public Boolean destroy(Incursion attacker){
			return null;
		}

		@Override
		public List<Combatant> getcombatants(){
			return null;
		}

		@Override
		public Image getimage(){
			return Images.get("locationquestmarker");
		}

		@Override
		public String describe(){
			return "A holy site (quest marker)";
		}

		@Override
		public Integer getel(Integer attackerel){
			return null;
		}

		@Override
		public void place(){
			super.place();
			WorldScreen.current.mappanel.tiles[x][y].discovered=true;
		}

		@Override
		public boolean interact(){
			if(!super.interact()) return false;
			if(Javelin.prompt(ESCORT)!='\n') return false;
			var pilgrims=generatepilgrims();
			Squad.active.members.addAll(pilgrims);
			followers.addAll(pilgrims);
			var result="A group of pilgrims joins you for protection.\n";
			remove();
			sites-=1;
			if(sites>1){
				new HolySite().place();
				result+="They indicate the way to their next holy site.";
			}else
				result+="Bring them back safely to "+town+".";
			Javelin.message(result,true);
			return true;
		}
	}

	List<Combatant> followers=new ArrayList<>(0);
	int sites;

	/** Reflection constructor. */
	public Pilgrimage(Town t){
		super(t);
		sites=t.getrank().rank;
		sites+=RPG.randomize(sites);
		new HolySite().place();
		sites-=1;
	}

	List<Combatant> generatepilgrims(){
		var targetel=Math.max(1,town.population+4+Difficulty.VERYEASY);
		var candidates=Terrain.get(town.x,town.y).getmonsters().stream()
				.filter(m->m.cr<targetel&&!Boolean.FALSE.equals(m.good)&&m.think(-1))
				.collect(Collectors.toList());
		var npilgrims=RPG.rolldice(2,4);
		var group=new ArrayList<Combatant>(npilgrims);
		while(group.isEmpty()||ChallengeCalculator.calculateel(group)<targetel){
			group.add(new Pilgrim(RPG.pick(candidates)));
			npilgrims-=1;
			if(npilgrims==0) break;
		}
		var upgrades=Cleric.INSTANCE.getupgrades();
		while(ChallengeCalculator.calculateel(group)<targetel)
			Combatant.upgradeweakest(group,upgrades);
		return group;
	}

	@Override
	public boolean validate(){
		return true;
	}

	@Override
	protected String getname(){
		return "Pilgrimage";
	}

	@Override
	public boolean complete(){
		if(sites>0) return false;
		var returning=false;
		for(var c:new ArrayList<>(Squad.active.members))
			if(followers.contains(c)){
				returning=true;
				break;
			}
		if(!returning) return false;
		removepilgrims();
		return true;
	}

	@Override
	public boolean cancel(){
		var cancelled=super.cancel()||sites==0&&followers.isEmpty();
		if(cancelled&&removepilgrims()) Javelin
				.message("Pilgrimage deadline expired, all pilgrims disband...",true);
		return cancelled;
	}

	boolean removepilgrims(){
		var inprogress=false;
		for(var s:Squad.getsquads()){
			var items=new ArrayList<Item>(0);
			for(var pilgrim:new ArrayList<>(s.members)){
				if(!followers.contains(pilgrim)) continue;
				inprogress=true;
				for(var i:s.equipment.get(pilgrim)){
					if(i instanceof Artifact) ((Artifact)i).remove(pilgrim);
					items.add(i);
				}
				s.remove(pilgrim);
				followers.remove(pilgrim);
			}
			for(var i:items)
				s.equipment.get(RPG.pick(s.members)).add(i);
		}
		return inprogress;
	}
}
