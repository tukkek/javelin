package javelin.model.world.location.dungeon;

public class DungeonTier {
	public static final DungeonTier[] TIERS = new DungeonTier[] {
			new DungeonTier("Cave", 5, 5, 7),
			new DungeonTier("Dungeon", 10, 5, 10),
			new DungeonTier("Ruins", 15, 10, 15),
			new DungeonTier("Keep", 20, 10, 20), };

	public String name;
	public int level;
	public int minrooms;
	public int maxrooms;

	public DungeonTier(String name, int level, int minrooms, int maxrooms) {
		this.name = name;
		this.minrooms = minrooms;
		this.maxrooms = maxrooms;
		this.level = level;
	}
}