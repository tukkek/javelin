package javelin.test;

import javelin.Debug;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.fight.mutator.Boss;
import javelin.controller.fight.mutator.Mutator;
import javelin.controller.fight.mutator.Waves;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.item.Tier;
import javelin.model.unit.Monster;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.haunt.Haunt;

/**
 * A {@link System#out}-based utility to check how well current {@link Haunt}s
 * are being generated, in particular in regards to {@link Waves} and
 * {@link Boss} {@link Encounter}s.
 *
 * TODO sadly, this is only barely holding together as-of right now. The
 * aforementioned {@link Mutator}s are barely doing what they need to,
 * thematically as there are a large number of variables ot juggle - quantity of
 * enemies, ELs from all {@link Tier}s, {@link Haunt}s with bigger or smalelr
 * {@link Monster} pools, etc. If {@link Haunt}s are ever generated from
 * {@link Branch}es, a more stable, long-term solution might need to be cooked
 * but for now this seem to be as good as it'll get as far as a
 * one-size-fits-all solution can be. It's sad that we can have {@link Waves}
 * and {@link Boss} {@link Fight}s as originally intended but this is a good
 * compromise between {@link Haunt} Fight variety, overall code complexity right
 * now and attending all haunts, all ELs and both Mutaror cases.
 *
 * @see Debug#onworldhelp()
 * @author alex
 */
public class TestHaunt{
	static final int LARGE=20;

	/** Test case. */
	public static void test(){
		for(var type:LocationGenerator.HAUNTS)
			for(var el=1;el<=Haunt.MAXEL;el++){
				Haunt h=null;
				try{
					h=type.getConstructor().newInstance();
					h.targetel=el;
					h.place();
					var boss=h.testboss();
					System.out.println(String.format("%s boss (EL%s): #%s %s",h,
							h.targetel,boss.size(),boss));
					var waves=h.testwaves();
					System.out.println(String.format("%s waves (EL%s): #%s %s",h,
							h.targetel,waves.size(),waves));
					if(Tier.get(el).equals(Tier.EPIC)) continue;
					if(boss.size()>LARGE)
						throw new RuntimeException("Large boss encounter: "+boss.size());
					if(waves.size()>LARGE*4)
						throw new RuntimeException("Large waves encounter: "+waves.size());
				}catch(ReflectiveOperationException|RuntimeException|GaveUp e){
					System.out.println(String.format("Error: %s (EL%s)",h,el));
					e.printStackTrace();
					return;
				}finally{
					if(h!=null) h.remove();
				}
			}
	}
}
