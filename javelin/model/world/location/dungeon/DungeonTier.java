package javelin.model.world.location.dungeon;

public class DungeonTier {
	public static final DungeonTier CAVE = new DungeonTier("Cave", 0, 5, 5, 7,
			"cave");
	public static final DungeonTier DUNGEON = new DungeonTier("Dungeon", 1, 10,
			5, 10, "");
	public static final DungeonTier RUINS = new DungeonTier("Ruins", 2, 15, 10,
			15, "ruins");
	public static final DungeonTier KEEP = new DungeonTier("Keep", 3, 20, 10,
			20, "keep");

	public static final DungeonTier HIGHEST = KEEP;
	public static final DungeonTier[] TIERS = new DungeonTier[] { CAVE, DUNGEON,
			RUINS, KEEP, };

	public String name;
	public int level;
	public int minrooms;
	public int maxrooms;
	public String floor;
	public String wall;
	public int tier;

	public DungeonTier(String name, int tier, int level, int minrooms,
			int maxrooms, String tilesuffix) {
		this.name = name;
		this.tier = tier;
		this.level = level;
		this.minrooms = minrooms;
		this.maxrooms = maxrooms;
		floor = "dungeonfloor" + tilesuffix;
		wall = "dungeonwall" + tilesuffix;
	}
}