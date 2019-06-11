package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.old.RPG;
import javelin.view.screen.GuildScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.upgrading.AcademyScreen;

/**
 * TODO all methods marked final to help with refactoring, may remove it
 *
 * @author alex
 */
public abstract class Guild extends Academy{
	protected Combatant[] hires=new Combatant[4];
	protected Kit kit;

	public Guild(String string,Kit k){
		super(string,string,getselection(k));
		k.validate();
		kit=k;
		while(!hashire())
			generatehires();
	}

	static HashSet<Upgrade> getselection(Kit k){
		HashSet<Upgrade> upgrades=new HashSet<>(k.basic);
		LinkedList<Upgrade> extension=new LinkedList<>(k.extension);
		while(upgrades.size()<9){
			Upgrade u=RPG.pick(extension);
			extension.remove(u);
			upgrades.add(u);
		}
		return upgrades;
	}

	@Override
	public final boolean isworking(){
		return super.isworking()&&!ishostile();
	}

	boolean hashire(){
		for(Combatant c:hires)
			if(c!=null) return true;
		return false;
	}

	protected final Combatant generatehire(int level){
		if(!RPG.chancein(level*20)) return null;
		Monster m=getcandidate(level);
		return m==null?null:NpcGenerator.generatenpc(m,kit,level);
	}

	Monster getcandidate(int level){
		ArrayList<Monster> candidates=new ArrayList<>();
		for(Monster m:Javelin.ALLMONSTERS)
			if(m.cr<=level/2&&m.think(-1)&&Kit.getpreferred(m).contains(kit))
				candidates.add(m);
		return candidates.isEmpty()?null:RPG.pick(candidates);
	}

	@Override
	protected final AcademyScreen getscreen(){
		return new GuildScreen(this);
	}

	public final void clearhire(Combatant hire){
		for(int i=0;i<hires.length;i++)
			if(hires[i]==hire){
				hires[i]=null;
				return;
			}
	}

	@Override
	public final List<Combatant> getcombatants(){
		List<Combatant> combatants=super.getcombatants();
		combatants.addAll(gethires());
		return combatants;
	}

	public List<Combatant> gethires(){
		List<Combatant> hires=new ArrayList<>(4);
		for(Combatant hire:this.hires)
			if(hire!=null) hires.add(hire);
		return hires;
	}

	@Override
	protected final void generategarrison(int minlevel,int maxlevel){
		targetel=RPG.r(minlevel,maxlevel);
		if(World.scenario.clearlocations) return;
		while(ChallengeCalculator.calculateel(garrison)<targetel){
			generatehires();
			for(Combatant hire:hires)
				if(hire!=null) garrison.add(hire);
		}
	}

	protected void generatehires(){
		Integer[] crs=new Integer[]{RPG.r(1,5),RPG.r(6,10),RPG.r(11,15),
				RPG.r(16,20),};
		for(int i=0;i<crs.length;i++){
			Combatant hire=generatehire(crs[i]);
			if(hire!=null) hires[i]=hire;
		}
	}

	@Override
	public final void turn(long time,WorldScreen world){
		if(ishostile()) return;
		generatehires();
		if(RPG.chancein(100)){
			List<Combatant> hires=gethires();
			if(!hires.isEmpty()) clearhire(hires.get(0));
		}
	}
}