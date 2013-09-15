// Defines Themes for different dungeons
//
//  Themes can be responsible for any or all of the following:
//   - Tile Types
//   - Random decorations
//   - Random objects
//   - Room decorations
//   - Creature generation
//
//
//  usage:
//       map.setTheme(new Theme("woods"));
//

package tyrant.mikera.tyrant;

import java.util.HashMap;

import tyrant.mikera.engine.BaseObject;


public class Theme extends BaseObject {
	private static final long serialVersionUID = 3258129150488361268L;

	public Theme() {
		// default theme has no properties set
	}
	
	public Theme(String s, String b) {
		super(getTheme(b));
		set("Name",s);
	}

	private static HashMap themes=null;
	
	private static void addTheme(Theme t) {
		themes.put(t.getString("Name"),t);
	}
	
	private static void init() {
		themes=new HashMap();
		
		Theme t=new Theme();
		t.set("Name","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.CAVEFLOOR);
		t.set("DungeonDensity",0.15);
		addTheme(t);
		
		t=new Theme("standard","base theme");
		t.set("WallTile",Tile.WALL);
		t.set("FloorTile",Tile.FLOOR);
		t.set("DungeonDNA","tttkrrroozhs");
		addTheme(t);
		
		t=new Theme("deep halls","base theme");
		t.set("WallTile",Tile.WALL);
		t.set("FloorTile",Tile.FLOOR);
		t.set("DungeonDNA","nrr");
		addTheme(t);
		
		t=new Theme("woods","base theme");
		t.set("WallTile",Tile.TREE);
		t.set("FloorTile",Tile.FORESTFLOOR);
		addTheme(t);
		
		t=new Theme("deepforest","base theme");
		t.set("WallTile",Tile.TREE);
		t.set("FloorTile",Tile.FORESTFLOOR);
		addTheme(t);
		
		t=new Theme("caves","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.CAVEFLOOR);
		t.set("DungeonDNA","conn");
		addTheme(t);
		
		t=new Theme("mines","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.CAVEFLOOR);
		t.set("DungeonDNA","rcot");
		addTheme(t);
		
		t=new Theme("sewer","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.GUNK);
		t.set("DungeonDNA","cttttkkkrrrrozs");
		addTheme(t);
		
		t=new Theme("ice","base theme");
		t.set("WallTile",Tile.ICEWALL);
		t.set("FloorTile",Tile.ICEFLOOR);
		t.set("DungeonDNA","cttttkkkrrrrozs");
		addTheme(t);
		
		t=new Theme("fire","base theme");
		t.set("WallTile",Tile.REDWALL);
		t.set("FloorTile",Tile.REDFLOOR);
		t.set("DungeonDNA","cttttkkkrrrrozs");
		addTheme(t);
		
		t=new Theme("hell","base theme");
		t.set("WallTile",Tile.REDWALL);
		t.set("FloorTile",Tile.REDFLOOR);
		t.set("DungeonDNA","cttttkkkrrrrozs");
		t.set("MonsterType","IsDemonic");
		t.set("WanderingRate",1000);
		addTheme(t);
		
		t=new Theme("labyrinthe","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.CAVEFLOOR);
		t.set("DungeonDNA","cczzzrrrkkkk");
		addTheme(t);
		
		t=new Theme("stone","base theme");
		t.set("WallTile",Tile.STONEWALL);
		t.set("FloorTile",Tile.STONEFLOOR);
		t.set("DungeonDNA","tttkrrroozhs");
		t.set("DungeonDensity",0.15);
		addTheme(t);
		
		t=new Theme("goblins","caves");
		t.set("MonsterType","IsGoblinoid");
		addTheme(t);
		
		t=new Theme("goblin village","goblins");
		t.set("MonsterType","IsGoblinoid");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.FORESTFLOOR);
		addTheme(t);
		
		t=new Theme("dungeon","standard");
		addTheme(t);
		
		t=new Theme("metal","base theme");
		t.set("WallTile",Tile.METALWALL);
		t.set("FloorTile",Tile.METALFLOOR);
		t.set("DungeonDNA","tttkrrroozhs");
		t.set("DungeonDensity",0.2);
		addTheme(t);
		
		t=new Theme("plains","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.GRASS);
		addTheme(t);
		
		t=new Theme("swamp","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.GUNK);
		addTheme(t);
		
		
		t=new Theme("village","base theme");
		t.set("WallTile",Tile.CAVEWALL);
		t.set("FloorTile",Tile.GRASS);
		addTheme(t);
	}
	
	public static Theme getTheme(String s) {
		if (themes==null) {
			init();
		}
		
		Theme t=(Theme)(themes.get(s));
		
		if (t==null) throw new Error("Theme ["+s+"] not found!");
		
		return t;
	}
}