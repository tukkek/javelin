package javelin.controller.scenario.dungeonworld;

import javelin.Javelin;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.item.key.door.MasterKey;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.temple.Temple;

public class DungeonWorld extends Campaign {
	public DungeonWorld() {
		size = size * 2;
		startingpopulation = 6;
		lockedtemples = false;
		minigames = false;
		record = false;
		respawnlocations = false;
		fogofwar = false;
		expiredungeons = true;
		worldencounters = false;
		helpfile = "Dungeon World";
		spawn = false;
		labormodifier = 0;
		featuregenerator = ZoneGenerator.class;
		worldgenerator = DungeonWorldGenerator.class;
	}

	@Override
	public boolean win() {
		if (Javelin.DEBUG) {
			return false;
		}
		for (Dungeon d : Dungeon.getdungeons()) {
			if (d.gettier() == DungeonTier.HIGHEST) {
				return false;
			}
		}
		String success = "You have cleared all major dungeons! Congratulations!";
		Javelin.message(success, true);
		return true;
	}

	@Override
	public Item openspecialchest(Dungeon d) {
		return new MasterKey();
	}

	@Override
	public Item openaltar(Temple t) {
		return new TempleKey(t.realm);
	}
}
