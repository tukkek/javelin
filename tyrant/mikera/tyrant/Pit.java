package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class Pit {
	public int type;

	public static BattleMap create(int level) {
		int size=9+RPG.d(level);
		BattleMap m=new BattleMap(size,size);
		m.set("Level",level);
		build(m,0,level);
		m.set("Description","A Dark Pit");
		return m;
	}
	
	public static BattleMap createArtifactVault(String monsterType, int level) {
		BattleMap m=new BattleMap(21,21);
		
		
		// maze pit
		m.setTheme(RPG.pick(new String[]{"standard", "caves",
				"labyrinthe", "sewer"}));
		Maze.buildMaze(m,0,0,m.width-1,m.height-1);
		
		m.setEntrance(Portal.create("stairs up"));
		m.addThing(m.getEntrance());
		m.set("Level",level);
		m.set("MonsterType",monsterType);
		m.set("WanderingType",monsterType);
		m.set("WanderingRate",300);
		m.set("Description","Dark Vault");
		m.set("EnterMessage","You feel a surge of excitement!");

		
		//TODO: big nasty with artifact
		Thing art=Lib.createArtifact(level);
		Thing nasty=Lib.createType(monsterType,level+6);
		AI.name(nasty,"The Boss");
		nasty.addThing(art);
		m.addThing(nasty);
		
		for (int x=0; x<m.width; x++) {
			for (int y=0; y<m.height; y++) {
				if (m.isClear(x,y)) {
					m.addThing(Lib.createType(monsterType,level),x,y);
				}
			}			
		}
		
		return m;
	}

	public static void build(BattleMap m,int t, int level) {
		int w=m.width;
		int h=m.height;

		
		if (t == 0) {
			t = RPG.d(8);
		}

		switch (t) {
			// regular pit
			case 1 : {
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				buildPitComplex(m,0,0,m.width-1,m.height-1);

				m.addEntrance("ladder up");
				m.addThing(Lib.create("secret item"));

				m.addThing(Lib.createCreature(level));
				break;
			}

			// room pit
			case 2 : {
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				m.fillArea(0, 0, m.width - 1, m.height - 1, m.floor());
				m.fillBorder(0, 0, m.width - 1, m.height - 1, m.wall());

				m.addEntrance("stairs up");

				m.addThing(Lib.createCreature(level));
				if (RPG.d(3) == 1)
					m.addThing(Lib.createCreature(level + 1));
				if (RPG.d(3) == 1)
					m.addThing(Lib.create("secret item"));
				if (RPG.d(3) == 1)
					m.addThing(Trap.create(level));
				break;
			}

			// trapped pit
			case 3 : {
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				buildPitComplex(m,0,0,m.width-1,m.height-1);

				m.setEntrance(Portal.create("ladder up"));
				m.addThing(Secret.hide(m.getEntrance()));

				m.addThing(Trap.create(level));
				m.addThing(Trap.create(level));
				m.addThing(Trap.create(level));
				m.addThing(Trap.create(level));
				m.addThing(Trap.create(level));
				m.addThing(Trap.create(level));
				if (RPG.d(3) == 1)
					m.addThing(Lib.createCreature(level + 1));
				break;
			}

			// critter pit
			case 4 : {
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				buildPitComplex(m,0,0,m.width-1,m.height-1);
				
				Thing c = Lib.createMonster(level - 3);
				for (int x = 0; x < w; x += 1) {
					for (int y = 0; y < h; y += 1) {
						if ((RPG.d(3) == 1) && (!m.isBlocked(x, y)))
							m.addThing(Lib.create(c.getstring("Name")), x, y);
					}
				}

				m.addEntrance("stairs up");
				break;
			}
			
			case 5: {
				// maze pit
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				Maze.buildMaze(m,0,0,m.width-1,m.height-1);
				
				m.addEntrance("stairs up");
				break;				
			}
			
			case 6: {
				// hellish maze pit
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				Maze.buildMaze(m,0,0,m.width-1,m.height-1);
				
				for (int x = 0; x < w; x += 1) {
					for (int y = 0; y < h; y += 1) {
						if ((RPG.d(3) == 1) && (!m.isBlocked(x, y)))
							m.addThing(Lib.createType("IsMonster",m.getLevel()-1), x, y);
					}
				}
				
				m.addEntrance("stairs up");
				break;				
			}
			
			case 7: {
				// undead pit
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				buildPitComplex(m,0,0,w-1,h-1);
				
				for (int x = 1; x <= level; x ++) {
					m.addThing(Lib.createType("IsUndead",level));
					m.addThing(Lib.createType("IsGravestone",level));
				}

				m.addEntrance("stairs up");
				break;
			}

			default: {
				m.setTheme(RPG.pick(new String[]{"standard", "caves",
						"labyrinthe", "sewer"}));
				buildPitComplex(m,0,0,m.width-1,m.height-1);
				
				m.addThing(Lib.createType("IsMonster",level));
				m.addThing(Lib.createItem(level));

				m.addEntrance("stairs up");
				break;
			}
				
		}
	}
	
	public static void buildPitComplex(BattleMap m, int x1, int y1, int x2, int y2) {
		m.fillArea(x1, y1, x2, y2, m.floor());
		for (int x = x1; x <= x2; x += 8) {
			for (int y = y1; y <= y2; y += 8) {
				m.setTile(x, y, m.wall());
			}
		}
		m.fractalize(x1, y1, x2, y2, 4);
		m.fillBorder(x1, y1, x2, y2, m.wall());
		
	}

}