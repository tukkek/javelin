package javelin.model.world.location.town.diplomacy.quest.find;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.dungeon.branch.temple.Temple.TempleEntrance;
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
	static final String DISCOVERED="You have found a new %s: %s!\n";

	/** @see Town */
	public static class DiscoverTown extends Discover{
		/** Constructor. */
		public DiscoverTown(){
			super("town",Town.class);
		}
	}

	/** @see TempleEntrance */
	public static class DiscoverTemple extends Discover{
		/** Constructor. */
		public DiscoverTemple(){
			super("temple",TempleEntrance.class);
		}
	}

	/** @see Haunt */
	public static class DiscoverHaunt extends Discover{
		/** Constructor. */
		public DiscoverHaunt(){
			super("haunt",Haunt.class);
		}
	}

	Class<? extends Actor> type;
	String typename;
	List<Actor> undiscovered;
	Actor discovered=null;

	/** Constructor. */
	public Discover(String name,Class<? extends Actor> type){
		typename=name;
		this.type=type;
	}

	@Override
	protected void define(Town t){
		super.define(t);
		name="Discover a new "+typename;
		undiscovered=World.getactors().stream()
				.filter(a->type.isInstance(a)&&!a.cansee())
				.collect(Collectors.toList());
	}

	@Override
	public boolean validate(){
		return super.validate()&&!undiscovered.isEmpty();
	}

	@Override
	protected boolean complete(){
		discovered=undiscovered.stream().filter(u->u.cansee()).findAny()
				.orElse(null);
		return discovered!=null;
	}

	@Override
	protected String message(){
		return String.format(DISCOVERED,typename,discovered)+super.message();
	}
}
