package javelin.model.world.location.town.labor.basic;

import java.util.ArrayList;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;

public class Cancel extends Labor{

	public Cancel(){
		super("Cancel projects",0,Rank.HAMLET);
		automatic=false;
	}

	@Override
	protected void define(){
		// nothing
	}

	@Override
	public boolean validate(District d){
		return super.validate(d)&&!d.town.getgovernor().getprojects().isEmpty();
	}

	@Override
	public void done(){
		for(Labor l:new ArrayList<>(town.getgovernor().getprojects()))
			l.cancel();
	}
}
