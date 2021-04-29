package javelin.model.world.location.town.diplomacy.quest.find;

import java.util.ArrayList;

import javelin.model.world.location.ResourceSite;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * Connect a {@link Resource} type to a {@link Town}.
 *
 * @see Trait#MERCANTILE
 * @author alex
 */
public class Connect extends FindQuest{
	Resource target=null;

	/** Reflection-friendly constructor. */
	public Connect(Town t){
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
		return super.validate()&&target!=null;
	}

	@Override
	protected String getname(){
		return "Connect "+target.name.toLowerCase();
	}

	@Override
	protected boolean complete(){
		return town.resources.contains(target);
	}
}
