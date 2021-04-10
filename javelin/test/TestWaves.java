package javelin.test;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.fight.mutator.Waves;
import javelin.controller.db.EncounterIndex;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.world.location.haunt.Haunt;

/**
 * Tests every {@link Haunt} for all possible {@link Waves}.
 *
 * TODO https://github.com/tukkek/javelin/issues/293#issuecomment-817077101
 *
 * @author alex
 */
public class TestWaves{
	/** Text-based test case. */
	public static void test(){
		for(var el=1;el<=20;el++){
			for(var h:LocationGenerator.HAUNTS)
				try{
					var s=h.getConstructor().newInstance();
					if(el<s.getminimumel()) continue;
					System.out.println(s.getClass().getSimpleName()+" EL"+el);
					for(var w:Waves.ELMODIFIER.entrySet())
						try{
							var target=el+w.getValue();
							var wave=Waves.generate(target,new EncounterIndex(s.pool));
							var actual=ChallengeCalculator.calculateel(wave);
							if(target!=actual) System.out
									.println("    wanted EL"+target+", generated EL"+actual);
						}catch(GaveUp e){
							System.out.println("Failed at "+w.getKey()+" wave(s)");
							return;
						}
				}catch(ReflectiveOperationException e){
					throw new RuntimeException(e);
				}
			System.out.println();
		}
	}
}
