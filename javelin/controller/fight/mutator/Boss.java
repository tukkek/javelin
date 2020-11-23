package javelin.controller.fight.mutator;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.fight.Fight;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * A Fight with one major enemy and a group of ever-replenishing nuisances. This
 * is only recommended for EL>=5, as it can be complicated to generate lesser
 * ELs with currently available {@link Monster}s.
 *
 * TODO expand this to work with at least EL 1 - 20
 *
 * @author alex
 */
public class Boss extends Mutator{
	static final ArrayList<Encounter> EMPTY=new ArrayList<>(0);

	List<Encounter> minions=new ArrayList<>();
	Combatants bosses=new Combatants(4);
	int el;
	List<Terrain> terrains;

	/**
	 * @param targetel Intended Encounter Level.
	 * @param terrains Indicates which Monsters can be used.
	 */
	public Boss(int elp,List<Terrain> terrains){
		el=elp;
		this.terrains=terrains;
	}

	@Override
	public void setup(Fight f){
		super.setup(f);
		var nbosses=Tier.get(el).getordinal();
		if(nbosses<1) nbosses=1;
		int bossel=el-2*nbosses;
		var bosses=RPG.shuffle(getbosses(terrains)).stream().filter(m->m.cr<=bossel)
				.collect(Collectors.toList());
		if(bosses.isEmpty()) throw new InvalidParameterException();
		while(this.bosses.size()<nbosses){
			var b=NpcGenerator.generate(RPG.pick(bosses),bossel);
			if(b!=null) this.bosses.add(b);
		}
		minions.addAll(RPG.shuffle(getminions(terrains)).stream()
				.filter(e->e.el<=bossel&&e.group.stream().noneMatch(m->m.source.elite)
						&&e.iscompatible(this.bosses.get(0).source.alignment))
				.collect(Collectors.toList()));
		if(minions.isEmpty())
			while(ChallengeCalculator.calculateel(this.bosses)<el){
				var b=NpcGenerator.generate(RPG.pick(bosses),bossel);
				if(b!=null) this.bosses.add(b);
			}
	}

	/** @return Any units that can be bosses. */
	protected List<Monster> getbosses(List<Terrain> terrains){
		var terrainnames=terrains.stream().map(t->t.name)
				.collect(Collectors.toSet());
		return Monster.ALL.stream()
				.filter(
						m->m.getterrains().stream().anyMatch(t->terrainnames.contains(t)))
				.collect(Collectors.toList());
	}

	/** @return Any units that can be fodder. */
	protected List<Encounter> getminions(List<Terrain> terrains){
		return terrains.stream().flatMap(t->t.getencounters().values().stream())
				.flatMap(i->i.stream()).collect(Collectors.toList());
	}

	/** @return The initial enemy team. */
	public Combatants generate(){
		var foes=new Combatants(2);
		if(bosses.isEmpty()) setup(null);
		foes.addAll(bosses);
		if(!minions.isEmpty()) while(ChallengeCalculator.calculateel(foes)<el){
			foes.addAll(RPG.pick(minions).generate());
			if(foes.size()>10){
				var w=foes.getweakest();
				foes.remove(w);
				w=NpcGenerator.generatenpc(w.source,el*4/5);
				if(w!=null) foes.add(bosses.size(),w);
			}
		}
		if(foes.isEmpty()) throw new InvalidParameterException();
		return foes;
	}

	@Override
	public void prepare(Fight f){
		super.prepare(f);
		Fight.state.redTeam=generate();
	}

	/** Console-output helper to check valid EL range. */
	public static void test(){
		for(var el=3;el<=20+Difficulty.DEADLY;el++)
			for(var t:Terrain.NONWATER)
				try{
					var f=new Boss(el,List.of(t));
					f.setup(null);
					var foes=f.generate();
					System.out.println("Success: "+t+" "+el+" ("+Javelin.group(foes)
							+") el "+ChallengeCalculator.calculateel(foes));
				}catch(InvalidParameterException e){
					System.out.println("Failure: "+t+" "+el);
					return;
				}
	}

	/**
	 * @return Uses the Boss Fight mechanic to just generate an encounter instead.
	 */
	public Combatants generateencounter(int el,List<Terrain> terrains){
		return new Boss(el,terrains).generate();
	}
}
