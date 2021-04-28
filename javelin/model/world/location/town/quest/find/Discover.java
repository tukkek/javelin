package javelin.model.world.location.town.quest.find;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.view.screen.WorldScreen;

/**
 * Find an undiscovered {@link Town}, {@link Temple} or {@link Haunt}.
 *
 * @see Trait#EXPANSIVE
 * @see WorldScreen#discover(int, int)
 * @author alex
 */
public abstract class Discover extends FindQuest{
	public static class DiscoverTown extends Discover{

	}

	public static class DiscoverTemple extends Discover{

	}

	public static class DiscoverHaunt extends Discover{

	}

	List<Town> undiscovered;

	/** Reflection constructor. */
	public Discover(Town t){
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
	protected boolean checkcomplete(){
		for(var t:undiscovered)
			if(WorldScreen.see(t.getlocation())&&t.exists()) return true;
		return false;
	}
}
