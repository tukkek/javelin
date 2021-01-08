package javelin.test;

import java.security.InvalidParameterException;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.fight.mutator.Boss;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.GaveUp;

/** @see Boss */
public class TestBoss{
	/** Console-output helper to check valid EL range. */
	public static void test(){
		for(var el=3;el<=20+Difficulty.DEADLY;el++)
			for(var t:Terrain.NONWATER)
				try{
					var f=new Boss(el,List.of(t));
					f.setup(null);
					var foes=f.generate(null);
					System.out.println("Success: "+t+" "+el+" ("+Javelin.group(foes)
							+") el "+ChallengeCalculator.calculateel(foes));
				}catch(InvalidParameterException|GaveUp e){
					System.out.println("Failure: "+t+" "+el);
					return;
				}
	}
}
