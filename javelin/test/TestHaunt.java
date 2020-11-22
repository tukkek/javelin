package javelin.test;

import javelin.controller.exception.GaveUp;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.world.location.haunt.Haunt;

public class TestHaunt{
	public static void test(){
		for(var type:LocationGenerator.HAUNTS)
			for(var el=1;el<=Haunt.MAXEL;el++){
				Haunt h=null;
				try{
					h=type.getConstructor().newInstance();
					h.targetel=el;
					h.place();
					var bosses=h.testboss();
					System.out.println(String.format("%s (EL%s): #%s %s",h,h.targetel,
							bosses.size(),bosses));
					var waves=h.testwaves();
					System.out.println(String.format("%s (EL%s): #%s %s",h,h.targetel,
							waves.size(),waves));
				}catch(ReflectiveOperationException|RuntimeException|GaveUp e){
					System.out.println(String.format("Error: %s (EL%s)",h,el));
					return;
				}finally{
					if(h!=null) h.remove();
				}
			}
	}
}
