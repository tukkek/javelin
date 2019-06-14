package javelin.model.world.location.town.labor.basic;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * An {@link Academy} that any {@link Town} can build regardless of
 * {@link Trait}s. Will offer basic upgrades from non-prestige {@link Kit}s
 * only.
 *
 * This is a solution to the problem of players needing to have {@link Upgrade}
 * options as soon as possible in their games. The initial solution was Realm
 * academies, which are now unfeasible without {@link Realm}s being linked to
 * {@link Item}s or {@link Upgrade}s. The next solution was to just put a random
 * {@link Guild} in each {@link Town} but that would mean only being able to
 * level up a single play style for the player's whole {@link Squad}. This
 * approach allows us to mix-and-match and offer more variety but still keeping
 * options simple and limited, incentivizing the player to build a proper
 * variety of {@link Guild}s accross {@link Town}s.
 *
 * Another benefit of a {@link BasicAcademy} over Relam Academies or a random
 * Guild is that it prevents, to a reasonable extent, players from feeling like
 * they need to start a new game repeatedly until they get the most optimal set
 * of circunstances. While the {@link Upgrade} of a basic academy is still
 * random, it has been designed to offer a decent variety and quantity of
 * choices in the vast majority of cases.
 *
 * @see Kit#basic #see {@link Kit#prestige}
 * @author alex
 */
public class BasicAcademy extends Academy{
	/**
	 * {@link Town} project for {@link BasicAcademy}.
	 *
	 * TODO this is currenlty not bieng used, in the hopes that having a single
	 * {@link BasicAcademy} in the starting area will be enough to provide a
	 * player with an initial selection of {@link Upgrade}. Ideally, the player
	 * would go on to {@link Build} and find a wider selection of {@link Guild}s
	 * in other {@link Town}s as the game progresses.
	 *
	 * Since the {@link BasicAcademy} content is largely random, lacking theme and
	 * identity (even visually), it should be used to the minimal extent possible.
	 *
	 * TODO if playtesting shows that not needing a {@link Labor} is a decent
	 * solution, remove it and {@link BuildBasicAcademy} altogether.
	 */
	static final Labor BUILD=new BuildBasicAcademy();

	static class BuildBasicAcademy extends Build{
		BuildBasicAcademy(){
			super("Build academy",5,Rank.HAMLET,null);
		}

		@Override
		public Location getgoal(){
			return new BasicAcademy();
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)&&d.getlocation(BasicAcademy.class)==null;
		}
	}

	static final String DESCRIPTION="Academy";

	/** Constructor. */
	public BasicAcademy(){
		super(DESCRIPTION,DESCRIPTION,getbasicupgrades());
	}

	static HashSet<Upgrade> getbasicupgrades(){
		HashSet<Upgrade> upgrades=new HashSet<>();
		RPG.shuffle(new ArrayList<>(Kit.KITS)).stream().filter(k->!k.prestige)
				.limit(3).forEach(k->upgrades.addAll(k.basic));
		return upgrades;
	}
}
