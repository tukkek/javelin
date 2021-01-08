package javelin.test;

import javelin.Debug;
import javelin.controller.content.fight.mutator.Boss;
import javelin.controller.content.fight.mutator.Waves;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.item.Tier;
import javelin.model.world.location.haunt.Haunt;

/**
 * A {@link System#out}-based utility to check how well current {@link Haunt}s
 * are being generated, in particular in regards to {@link Waves} and
 * {@link Boss} {@link Encounter}s.
 *
 * @see Debug#onworldhelp()
 * @author alex
 */
public class TestHaunt{
	static final int BIG=20;

	/** Test case. */
	public static void test(){
		for(var type:LocationGenerator.HAUNTS)
			for(var el=1;el<=Haunt.MAXEL;el++){
				Haunt h=null;
				try{
					h=type.getConstructor().newInstance();
					var m=h.getminimumel();
					if(el<m) el=m;
					h.targetel=el;
					h.place();
					var boss=h.testboss();
					System.out.println(String.format("%s boss (EL%s): #%s %s",h,
							h.targetel,boss.size(),boss));
					var waves=h.testwaves();
					System.out.println(String.format("%s waves (EL%s): #%s %s",h,
							h.targetel,waves.size(),waves));
					if(Tier.get(el).equals(Tier.EPIC)) continue;
					if(boss.size()>BIG)
						throw new RuntimeException("Large boss encounter: "+boss.size());
					if(waves.size()>BIG*4)
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
