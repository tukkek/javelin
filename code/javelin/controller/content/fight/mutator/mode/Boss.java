package javelin.controller.content.fight.mutator.mode;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Tier;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.old.RPG;
import javelin.test.TestHaunt;

/**
 * A {@link Fight} mode with one major enemy and a large group of minor
 * nuisances. This is only recommended for EL>=5, as it can be complicated to
 * generate lesser ELs with currently available {@link Monster}s.
 *
 * TODO expand this to work with at least EL 1 - 20, may require (many) more
 * monsters with low {@link Monster#cr}
 *
 * @see TestHaunt
 * @author alex
 */
public class Boss extends FightMode{
	static final ArrayList<Encounter> EMPTY=new ArrayList<>(0);

	List<Encounter> minions=new ArrayList<>();
	Combatants bosses=new Combatants(4);
	List<Terrain> terrains;
	int el;

	/**
	 * @param targetel Intended Encounter Level.
	 * @param terrains Indicates which Monsters can be used.
	 */
	public Boss(int elp,List<Terrain> terrains){
		el=elp;
		this.terrains=terrains;
	}

	/** @return Any units that can be bosses. */
	protected List<Monster> listbosses(List<Terrain> terrains){
		var terrainnames=terrains.stream().map(t->t.name)
				.collect(Collectors.toSet());
		return Monster.ALL.stream()
				.filter(
						m->m.getterrains().stream().anyMatch(t->terrainnames.contains(t)))
				.collect(Collectors.toList());
	}

	/** @return Any units that can be fodder. */
	protected List<Encounter> listminions(List<Terrain> terrains){
		return terrains.stream().flatMap(t->t.getencounters().values().stream())
				.flatMap(i->i.stream()).collect(Collectors.toList());
	}

	@Override
	public void setup(Fight f){
		super.setup(f);
		var max=Tier.get(el).getordinal();
		var nbosses=1;
		for(;nbosses<max&&RPG.chancein(2);nbosses++)
			continue;
		int bossel=el-2*nbosses;
		var bosses=RPG.shuffle(listbosses(terrains)).stream()
				.filter(m->m.cr<=bossel).collect(Collectors.toList());
		if(Javelin.DEBUG&&bosses.isEmpty()) throw new InvalidParameterException();
		while(this.bosses.size()<nbosses){
			var b=NpcGenerator.generate(RPG.pick(bosses),bossel);
			if(b!=null) this.bosses.add(b);
		}
		var a=RPG.pick(this.bosses).source.alignment;
		minions.addAll(RPG.shuffle(listminions(terrains)).stream()
				.filter(e->e.el<=bossel&&e.group.size()>1
						&&e.group.stream().noneMatch(m->m.source.elite)&&e.iscompatible(a))
				.collect(Collectors.toList()));
		if(minions.isEmpty())
			while(ChallengeCalculator.calculateel(this.bosses)<el){
				var b=NpcGenerator.generate(RPG.pick(bosses),bossel);
				if(b!=null) this.bosses.add(b);
			}
	}

	@Override
	public Combatants generate(Fight f) throws GaveUp{
		var foes=new Combatants(2);
		if(bosses.isEmpty()) try{
			setup(null);
		}catch(InvalidParameterException e){
			throw new GaveUp();
		}
		foes.addAll(bosses);
		if(!minions.isEmpty()) while(ChallengeCalculator.calculateel(foes)<el){
			foes.addAll(RPG.pick(minions).generate());
			if(foes.size()>Encounter.BIG){
				var w=foes.getweakest();
				foes.remove(w);
				w=NpcGenerator.generatenpc(w.source,el*4/5);
				if(w!=null) foes.add(bosses.size(),w);
			}
		}
		if(Javelin.DEBUG&&foes.isEmpty()) throw new InvalidParameterException();
		return foes;
	}
}
