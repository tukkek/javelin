package javelin.model.world.location.town.quest;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.view.screen.WorldScreen;

/**
 * {@link Trait#EXPANSIVE} quest to find any town.
 *
 * @author alex
 */
public class DiscoverTown extends Quest{
	List<Town> undiscovered;

	/** Reflection constructor. */
	public DiscoverTown(Town t){
		super(t);
		undiscovered=Town.gettowns().stream()
				.filter(town->!WorldScreen.see(town.getlocation()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean validate(){
		return !undiscovered.isEmpty();
	}

	@Override
	protected String getname(){
		return "Discover a new town";
	}

	@Override
	public boolean complete(){
		for(var t:undiscovered)
			if(WorldScreen.see(t.getlocation())&&t.exists()) return true;
		return false;
	}
}
