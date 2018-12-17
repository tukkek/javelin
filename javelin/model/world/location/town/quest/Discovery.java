package javelin.model.world.location.town.quest;

import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Player has to discover an unrevealed {@link Location}.
 *
 * @author alex
 */
public class Discovery extends Quest{
	Location target=null;

	/** Reflection-friendly constructor. */
	public Discovery(Town t){
		super(t);
		var source=t.getlocation();
		target=World.getactors().stream().filter(a->a instanceof Location)
				.map(a->(Location)a).filter(f->f.unique)
				.filter(f->!WorldScreen.see(f.getlocation()))
				.sorted((a,b)->Integer.compare(source.distanceinsteps(a.getlocation()),
						source.distanceinsteps(b.getlocation())))
				.findFirst().orElse(null);
	}

	@Override
	public boolean validate(){
		return super.validate()&&target!=null;
	}

	@Override
	protected String getname(){
		return "Find: "+target.toString();
	}

	@Override
	public boolean complete(){
		return WorldScreen.see(target.getlocation());
	}
}
