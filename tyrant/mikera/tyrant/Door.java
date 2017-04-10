package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Door {

	public static class DoorCreation extends Script {
		private static final long serialVersionUID = 3904958655442006583L;

        public boolean handle(Thing t, Event e) {
			String keyName=t.getstring("KeyName");
			if (keyName==null) {
				t.set("KeyName",chooseKey(t.getLevel()));
			}
			return false;
		}
	}
	
    public static class DoorBump extends Script {
        private static final long serialVersionUID = 3904958655442006583L;

        public boolean handle(Thing t, Event e) {
			Thing user=e.getThing("Target");
			e.set("ActionTaken",useDoor(user,t));
			return false;
		}
	}
	
    /**
     * A RiddleDoor can only be opened if our intrepid hero correctly answers the riddle.
     * These doors are associated with the riddle giver by concatenating his name
     * with the String "RiddleSolved" (e.g. "DeanOfAdmissionsRiddleSolved") thus making
     * riddle doors available to more than one riddle giver.
    */
    public static class RiddleDoorBump extends Script {
        private static final long serialVersionUID = -3945891430842518588L;

        public boolean handle(Thing t, Event e) {
            Thing user = e.getThing("Target");
            e.set("ActionTaken",useRiddleDoor(user,t));
            return false;
        }
    }

    public static boolean useRiddleDoor(Thing user, Thing door) {
        if (!door.getFlag("IsOpen")) {
            if (user.isHero()) {
                if (user.getFlag(door.getstring("RiddleStatName"))) {
                    setOpen(door,true);
                } else {
                    if(door.getFlag("IsInvisible")) {
                        Game.messageTyrant("Something is preventing you from moving forward");
                    } else {
                        Game.messageTyrant("This door appears to be magically locked.");
                    }
                }
            }
        }
        return false;
    }

    public static class QuestDoorBump extends Script {
        private static final long serialVersionUID = 8212975466499234213L;

        public boolean handle(Thing t, Event e) {
            Thing user = e.getThing("Target");
            e.set("ActionTaken",useQuestDoor(user,t));
            return false;
        }
    }

    public static boolean useQuestDoor(Thing user, Thing door) {
        if (!door.getFlag("IsOpen")) {
            if (user.isHero()) {
                // TODO integrate with quest system to associate a door with a specific quest
                if (user.getFlag("HasQuest")) {
                    setOpen(door,true);
                } else {
                    Game.messageTyrant("It appears to be impossible to open this door.");
                }
            }
        }
        return false;
    }

    public static class DoorDamage extends Script {
		private static final long serialVersionUID = 3904677184760132917L;

        public boolean handle(Thing t, Event e) {
			Thing[] ts=t.getFlaggedContents("IsTrap");
			BattleMap m=t.getMap();
			
			Thing a=e.getThing("Actor");
			
			for (int i=0; i<ts.length; i++) {
				Game.warn(ts[i].name()+" triggered on door");
				if (a==null) a=Game.hero();
				int nx=t.x+RPG.sign(a.x-t.x);
				int ny=t.y+RPG.sign(a.y-t.y);
					
				if ((nx!=t.x)&&(!m.isTileBlocked(nx,t.y))) {
					ny=t.y;
				} else {
					nx=t.x;
				} 
				
				m.addThing(ts[i],nx,ny);
				Trap.trigger(ts[i]);
			}
			
			int dam=e.getStat("Damage");
			String dt=e.getString("DamageType");
			
			if (dt.equals(RPG.DT_IMPACT)&&!t.getFlag("IsOpen")) {
				int openResistance=t.getStat("OpenResistance");
				if ((openResistance>0)&&RPG.test(dam,openResistance)) {
					t.visibleMessage(t.getTheName()+" is smashed open");
					Door.setOpen(t,true);
				}
				
			}
			
			return false;
		}
		
	}
	
	/* TODO: some kind of door level upgrades
	 * 
	 * public Door(String n, int level) {
		type = Text.index(n, names);
		hits = basehits[type];
		blocking = true;
		locked = (RPG.r(100) < lockedchance[type]);
		//upgrade hits for high-level door
		for (int i = level; i > baselevel[type]; i--) {
			hits = (hits * (10 + RPG.r(7))) / 10;
		}

		if (RPG.test(level, 50))
			makeTrapped(level);

	}*/

	public static Thing create() {
		return create(Game.hero().getLevel());
	}
	
	public static Thing create(int level) {
		return Lib.createType("IsDoor",level);
	}
	
	public static Thing createDoor(int level) {
		return Lib.createType("IsDoor",level);
	}

	public static Thing createToughDoor(int level) {
		Thing t=Lib.createType("IsDoor",level+6);
		t.set("IsLocked",1);
		return t;
	}
	
	protected static String chooseKey(int level) {
		Thing key=Lib.createType("IsKey",level);
		return key.name();
	}
 
	protected static void setOpen(Thing door, boolean open) {
		Event e=new Event(open?"Open":"Close");
		if (door.handle(e)) {
			// Game.warn("Event break from Door.setOpen(...)");
			return;
		}
		
		Score.scoreExplore(door);
		// Game.warn("Trying Door: "+(open?"Open":"Close"));
		door.set("IsBlocking", open?0:1);
		door.set("IsOpen", open?1:0);
		door.set("IsViewBlocking", open?0:Lib.getDefaultStat(door,"IsViewBlocking"));
		int newImage=Lib.getDefaultStat(door,"Image") + (open?Lib.getDefaultStat(door,"ImageOpen"):0);
		door.set("Image", newImage);
		// Game.warn("newImage="+newImage);
		return;
	}
	
	public static boolean canLock(Thing key, Thing door) {
		if (!key.getFlag("IsKey")) return false;
		if (!door.getFlag("IsDoor")) return false;
		if (door.getFlag("IsOpen")) return false;
		if (door.getFlag("IsLocked")) return false;
		return keyFits(key, door);
	}
	
	public static boolean keyFits(Thing key, Thing door) {
		String keyName=door.getstring("KeyName");
		return key.name().equals(keyName);
	}
	
	public static boolean unlock(Thing key, Thing door) {
		if (!door.getFlag("IsLocked")) return false;
		if (!keyFits(key,door)) return false;
		
		door.set("IsLocked",0);
		return true;
	}

	public static void lockedOptions(Thing user, Thing door) {
		Game.messageTyrant(door.getTheName() + " is locked");
		
		String keyName=door.getstring("KeyName");
		if ((keyName!=null)&&user.hasItem(keyName)) {
			Thing key=user.getItem(keyName);
			Game.messageTyrant(key.getYourName()+" fits the lock.");
			//Game.message(key.getYourName()+" seems to fit. Do you want to unlock the door? (y/n)");
			//char c=Game.getOption("yn");
			//if (c=='y') {
			//	Game.message("You unlock "+door.getTheName());
			//	door.set("IsLocked",0);
			//	return;
			//}
			//Game.message("");
			Game.messageTyrant("You unlock "+door.getTheName());
			door.set("IsLocked",0);
            return;
		}
		
		int difficulty = door.getStat("LockDifficulty");
		if (difficulty==0) difficulty=door.getStat("Level")*3;
		
		int skill = user.getStat(RPG.ST_CR) * user.getStat(Skill.PICKLOCK);
		Thing pick=user.getItem("lock pick");
		if ((pick!=null)&&user.getStat(Skill.PICKLOCK) > 0) {
			Game.messageTyrant("Attempt to pick lock? (y/n)");
			char c = Game.getOption("yn");
			if (c == 'y') {
				if ((difficulty>=0)&&RPG.test(skill, difficulty)) {
					Game.messageTyrant("You succeed in picking the lock!");
					door.set("IsLocked",0);
				} else {
					pick=pick.unequip(1);
					Game.messageTyrant(pick.getYourName()+" breaks.");
				}
			} else {
				Game.messageTyrant("");
			}
		}
	}

	public static boolean useDoor(Thing user, Thing door) {
		BattleMap m=door.getMap();
		
		Item.touch(user,door);

		if (!door.getFlag("IsOpen")) {
			if (door.getFlag("IsLocked") || ((!user.isHero()) && (user.getStat(RPG.ST_CR) < 7))) {
				if (user.isHero()) {
					lockedOptions(user,door);
					return true;
				}
			} else {
				if (user.isHero()){
					Game.messageTyrant("You open " + door.getTheName());
				}
				setOpen(door,true);

				user.incStat("APS", -200);
				return true;
			}
		} else if (m.isBlocked(door.x,door.y)){
			if (user.isHero()) Game.messageTyrant("You can't close "+door.getTheName());
		} else {
			if (user.isHero()) {
				Game.messageTyrant("You close " + door.getTheName());
			}
			setOpen(door,false);
			
			user.incStat("APS", -200);
		}
		return false;
	}

	public static void init() {
		initDoors();
	}
	
	private static void initDoors() {
	    Thing t=Lib.extend("base door", "base scenery");
	    t.set("UName","door");
	    t.set("IsScenery",0); // don't want as random decoration
	    t.set("IsDoor",1);
	    t.set("IsOpenable",1);
	    t.set("Image",144);
	    t.set("ImageOpen",1);
	    t.set("Z",Thing.Z_ITEM+1);
	    t.set("Frequency",50);
	    t.set("IsOpen",0);
	    t.set("IsViewBlocking",1);
	    t.set("LevelMin",1);
	    t.set("IsBlocking",1);
	    t.set("IsJumpable",0);
	    t.set("IsTownDoor",0);
	    t.set("OpenResistance",5);
	    t.set("HPS",30);
	    t.set("OnBump",new DoorBump());
	    t.set("ScoreExplore",1);
	    t.set("ASCII","+");
	    t.set("MapColour",0x00704020);
	    t.set("OnCreate",new DoorCreation());
	    t.set("DefaultThings","10% [IsTrap]");
	    t.addHandler("OnDamage",new DoorDamage());
	    Lib.add(t);
	    
	    t=Lib.extend("door", "base door");
	    t.set("LevelMin",1);
	    t.set("IsTownDoor",1);
	    Lib.add(t);
	    
	    t=Lib.extend("locked door", "base door");
	    t.set("LevelMin",6);
	    t.set("IsTownDoor",1);
	    t.set("IsLocked",1);
	    t.multiplyStat("Frequency",0.1);
	    Lib.add(t);
	    
	    t=Lib.extend("weak locked door", "base door");
	    t.set("LevelMin",2);
	    t.set("IsLocked",1);
	    t.set("HPS",2);
	    t.multiplyStat("Frequency",0.1);
	    Lib.add(t);
	    
	    
	    t=Lib.extend("rotten door", "base door");
	    t.set("IsTownDoor",0);
	    t.set("IsOwned",0);
	    t.set("LevelMin",3);
	    t.multiplyStat("Frequency",0.3);
	    t.addHandler("OnOpen",new Script() {
	    	private static final long serialVersionUID = 4120849971320142133L;

            public boolean handle(Thing t, Event e) {
	    		if (t.isVisible(Game.hero())) {
	    			Game.messageTyrant(t.getTheName()+" crumbles into dust");
	    		}
	    		t.die();
	    		return true;
	    	}
	    });
	    Lib.add(t);
	    
	    t=Lib.extend("strong door", "door");
	    t.set("UName","strong door");
	    t.set("HPS",100);
	    t.set("OpenResistance",20);
	    t.set("LevelMin",5);
	    Lib.add(t);
	    
	    t=Lib.extend("stone door", "door");
	    t.set("UName","stone door");
	    t.set("Image",146);
	    t.set("HPS",300);
	    t.set("OpenResistance",30);
	    t.set("LevelMin",10);
	    Lib.add(t);
	    
	    t=Lib.extend("shop door", "door");
	    t.set("UName","shop door");
	    t.set("Image",148);
	    t.set("Frequency",0); // do not generate randomly
		t.set("IsWarning",1);
	    t.set("HPS",60);
	    Lib.add(t);
	    
	    t=Lib.extend("ornate door", "door");
	    t.set("UName","ornate door");
	    t.set("Image",148);
	    t.set("HPS",60);
	    t.set("LevelMin",7);
	    t.set("IsTownDoor",1);
	    Lib.add(t);
	    
	    t=Lib.extend("enchanted door", "door");
	    t.set("Image",148);
	    t.set("HPS",1300);
	    t.set("LevelMin",9);
	    t.set("OpenResistance",0);
	    t.set("IsLocked",1);
	    t.set("LockDifficulty",-1);
	    t.multiplyStat("Frequency",0.1);
	    t.addHandler("OnAction",new Script() {
	    	private static final long serialVersionUID = 3761692294637435190L;

            public boolean handle(Thing t, Event e) {
	    		int time=e.getStat("Time");
	    		BattleMap map=t.getMap();
	    		if (map==null) return false;
	    		if ((RPG.po(time,2000)>0)&&(!map.isBlocked(t.x,t.y))) {
	    			Door.setOpen(t,!t.getFlag("IsOpen"));
	    		}
	    		return false;
	    	}
	    });
	    Lib.add(t);
	    
	    t=Lib.extend("heavy oak door", "strong door");
	    t.set("HPS",200);
	    t.set("OpenResistance",20);
	    t.set("ARM",10);
	    t.set("IsTownDoor",1);
	    t.set("LevelMin",10);
	    Lib.add(t);
	    
	    t=Lib.extend("goblin door", "door");
	    t.set("UName","green door");
	    t.set("Image",152);
	    t.set("HPS",60);
	    t.set("DefaultThings","[IsTrap]");
	    t.set("LevelMin",13);
	    Lib.add(t);
	    
	    t=Lib.extend("black door", "door");
	    t.set("UName","black door");
	    t.set("Image",154);
	    t.set("HPS",160);
	    t.set("IsTownDoor",1);
	    t.set("OpenResistance",20);
	    t.set("LevelMin",10);
	    Lib.add(t);
	    
	    t=Lib.extend("trapped black door", "black door");
	    t.set("LevelMin",12);
	    t.set("IsLocked",1);
	    t.set("IsTownDoor",1);
	    t.multiplyStat("Frequency",0.4);
	    t.set("DefaultThings","[IsTrap]");
	    Lib.add(t);

	    t=Lib.extend("trapped door", "door");
	    t.set("UName","black door");
	    t.set("Image",154);
	    t.set("HPS",16);
	    t.set("IsLocked",1);
	    t.multiplyStat("Frequency",0.4);
	    t.set("DefaultThings","[IsTrap]");
	    t.set("LevelMin",5);
	    Lib.add(t);
	    
	    t=Lib.extend("graveyard door", "black door");
	    t.set("Image",154);
	    t.set("IsDestructible",0);
	    t.set("Frequency",0);
	    t.set("LevelMin",13);
	    t.set("ScoreExplore",50);
	    t.set("KeyName","skull key");
	    t.set("OnAction",new Script() {
	    	private static final long serialVersionUID = 3258413915376333623L;

            public boolean handle(Thing t, Event e) {
	    		BattleMap m=t.getMap();
	    		if (m.getFlag("IsHostile")) {
	    			Door.setOpen(t,false);
	    			t.set("IsLocked",true);
	    			t.set("OnAction",null);
	    		}
	    		return false;
	    	}
	    });
	    Lib.add(t);
	    
        t=Lib.extend("unbreakable door", "door");
        t.set("UName","door");
        t.set("IsDestructible",0);
        t.set("Frequency",0);
        t.set("IsLocked",1);
        t.set("OpenResistance",0);
        t.set("LockDifficulty",-1);
        t.set("Image",154);
        Lib.add(t);

        t=Lib.extend("quest door", "unbreakable door");
        // Door will only open if hero has received the associated quest.
        t.set("Frequency",0);
        t.set("OnBump",new QuestDoorBump());
        Lib.add(t);

        t=Lib.extend("riddle door", "unbreakable door");
        // Door will only open if hero has solved a riddle.
        t.set("Frequency",0);
        t.set("OnBump",new RiddleDoorBump());
        Lib.add(t);

        t=Lib.extend("invisible riddle door", "riddle door");
        t.set("IsInvisible",1);
        Lib.add(t);

	    t=Lib.extend("portcullis", "door");
	    t.set("UName","portcullis");
	    t.set("IsPortcullis",1);
	    t.set("Image",140);
	    t.set("IsViewBlocking",0);
	    t.set("LevelMin",3);
	    t.set("IsTownDoor",1);
	    t.set("OpenResistance",0);
	    t.set("HPS",560);
	    t.set("ScoreExplore",3);
	    Lib.add(t);
	    
	    t=Lib.extend("golden portcullis", "portcullis");
	    t.set("Image",142);
	    t.set("IsViewBlocking",0);
	    t.set("HPS",1560);
	    t.set("LevelMin",18);
	    Lib.add(t);
	    
	    t=Lib.extend("invincible portcullis", "golden portcullis");
	    t.set("IsDestructible",0);
	    t.set("Frequency",0);
	    t.set("LevelMin",18);
	    t.set("IsLocked",1);
	    t.set("LockDifficulty",-1);
	    Lib.add(t);
	    
	    t=Lib.extend("stable door", "door");
	    t.set("Image",150);
	    t.set("Frequency",0); // do not generate randomly
	    t.set("IsViewBlocking",0);
	    t.set("IsJumpable",1);
	    t.set("HPS",28);
	    Lib.add(t);
	}
}