package javelin.model.world.location.town.quest.basic;

import java.util.ArrayList;

import javelin.model.world.location.ResourceSite;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.quest.Quest;
import javelin.old.RPG;

/**
 * Fetch a {@link Resource} for a {@link Town}.
 *
 * @author alex
 */
public class Fetch extends Quest{
	Resource target=null;

	/** Reflection-friendly constructor. */
	public Fetch(Town t){
		super(t);
		var all=new ArrayList<>(ResourceSite.RESOURCES.values());
		if(t.resources.size()==all.size()) return;
		for(var r:RPG.shuffle(all))
			if(!t.resources.contains(r)){
				target=r;
				return;
			}
	}

	@Override
	public boolean validate(){
		return target!=null;
	}

	@Override
	protected String getname(){
		return "Connect "+target.name.toLowerCase();
	}

	@Override
	protected boolean checkcomplete(){
		return town.resources.contains(target);
	}
}
