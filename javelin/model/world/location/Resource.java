package javelin.model.world.location;

import java.awt.Image;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.Work;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Improvement;
import javelin.model.world.Season;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.view.Images;

/**
 * A resource can be extracted by Workers and acts as "mobile labor" which can
 * be returned to {@link Town}s or used to speed {@link Improvement}s.
 * 
 * TODO instead of using the traditional {@link FeatureGenerator} maybe have a
 * system for {@link Terrain}s spawning resources based on {@link Season}? This
 * would promote more resoruces without the expense of other {@link Location}s.
 * 
 * @see Squad#resources
 * @author alex
 */
public class Resource extends Location {
	static class ResourceType {
		String name;
		int amount;
		String action;

		public ResourceType(String name, int amount, String action) {
			this.name = name;
			this.amount = amount;
			this.action = action;
		}
	}

	static final HashMap<Terrain, ResourceType> RESOURCES =
			new HashMap<Terrain, ResourceType>();

	static {
		RESOURCES.put(Terrain.WATER, new ResourceType("Fish", 5, "fishing"));
		RESOURCES.put(Terrain.FOREST, new ResourceType("Fruits", 5, "picking"));
		RESOURCES.put(Terrain.PLAIN,
				new ResourceType("Grains", 15, "harvesting"));
		RESOURCES.put(Terrain.MARSH,
				new ResourceType("Mercury", 15, "extracting"));
		RESOURCES.put(Terrain.HILL, new ResourceType("Stone", 15, "quarrying"));
		RESOURCES.put(Terrain.DESERT,
				new ResourceType("Gems", 30, "collecting"));
		RESOURCES.put(Terrain.MOUNTAINS,
				new ResourceType("Crystal", 30, "mining"));
	}

	/** Constructor. */
	public Resource() {
		super(null);
	}

	@Override
	protected void generate() {
		generate(this, true);
		description = gettype().name + " (resource)";
		sacrificeable = true;
		allowentry = false;
	}

	ResourceType gettype() {
		return RESOURCES.get(Terrain.get(x, y));
	}

	@Override
	protected Integer getel(int attackerel) {
		return Integer.MIN_VALUE;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public Image getimage() {
		return Images
				.getImage("locationresource" + gettype().name.toLowerCase());
	}

	@Override
	public boolean interact() {
		int nworkers = Work.countworkers();
		int gold = getspoils();
		if (nworkers == 0) {
			Character input = Javelin
					.prompt("To gather this resource you must bring at least one worker from a town.\n"
							+ "Do you want to plunder it instead for $" + gold
							+ "?\n\n"
							+ "Press p to plunder or any other key to cancel...");
			if (input == 'p') {
				plunder(gold);
			}
			return true;
		}
		ResourceType type = gettype();
		int time = Math.max(1, type.amount / nworkers);
		Character input = Javelin.prompt("With your current " + nworkers
				+ " workers, gathering this resource will take " + time
				+ " days.\n\n" //
				+ "Press s to start " + type.action + "\n"
				+ "Press p to immediately plunder resource for $" + gold + "\n"
				+ "Press any other key to leave...");
		if (input == 'p') {
			plunder(gold);
		} else if (input == 's') {
			String result = "Your team begins " + type.action + ".\n"//
					+ "Once done you may bring the resources back to a Town\n"
					+ "or use it to accelerate Work actions on the world map.";
			Javelin.message(result, false);
			Squad.active.hourselapsed += type.amount * 24 / nworkers;
			remove();
			Squad.active.resources +=
					Math.round(type.amount * 2 * Town.DAILYLABOR);
		}
		return true;
	}

	void plunder(int gold) {
		remove();
		Squad.active.gold += gold;
	}

	int getspoils() {
		ResourceType type = gettype();
		int el;
		if (type.amount == 5) {
			el = 1;
		} else if (type.amount == 15) {
			el = 2;
		} else if (type.amount == 30) {
			el = 3;
		} else {
			throw new RuntimeException("#unkwnonresourceamount " + type.amount);
		}
		return Fortification.getspoils(Fortification.leveltoel(el));
	}
}
