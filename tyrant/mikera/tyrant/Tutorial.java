/*
 * Created on 11-Nov-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import java.util.Date;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;



/**
 * @author Mike
 *
 * This class implements a small tutorial area for beginners
 * 
 * 
 */
public class Tutorial {
    private static class RoscoBurrl extends Script
    {
        public RoscoBurrl() {
            chattedToHero = false;
        }
        private static final long serialVersionUID = 3258417222518321461L;
        private boolean chattedToHero;
        private MiiraTheRanger miira;

        public final boolean isChattedToHero() {
            return chattedToHero;
        }
        /** The method is supposed to be called only once - just when
         *  MiiraTheRanger object is available.
         *  We need to know MiiraTheRanger object to request
         *  MiiraTheRanger.isChattedToHero().
         */
        public void setMiira(final MiiraTheRanger miira) {
            this.miira = miira;
        }

        /** The method PROBABLY says hello
         */
        private void tryToSayHello(final Thing h) {
            Tutorial.sayHello(h, 0.3, 0.1, 0.1);
        }
        public boolean handle(Thing t, Event e) {
            chattedToHero = true;
            final Thing h = e.getThing("Target");
            t.incStat("ChatCount",1);
            switch (t.getStat("ChatCount")) {
                case 1:
                    sayName(t);
                    tryToSayHello(h);
                    Game.messageTyrant("He talks enthusiastically about his latest victory against a marauding " +
                            Lib.createType("IsMonster",RPG.d(16)).getName(Game.hero()) +
                            ".");
                    break;

                case 2:
                    sayName(t);
                    Game.quotedMessage("Yer know what? Best way to fite inn't to rush rite in. Yer wanna hold back a bit, let tha critta come to ya. Then ya bash it, afore it knows wats coming!");
                    break;

                case 3:
                    sayName(t);
                    Game.quotedMessage("Don't evva let yaself get s'rounded! Stick to tha narra ways where yer can take 'em one by one. Thass wot I do.");
                    break;

                case 4:
                    sayName(t);
                    if (RPG.p(0.5)) {
                        Game.quotedMessage("Tha's a narsty goblin by tha name of Borrok. Kilt sum village folk lass year. Ee's built a lair unda tha deep forest ruin. Up North ya know? An 'ero who could beat tha rogue would be right famous!");
                    } else
                    {
                        Game.quotedMessage("Hey pal, listen and advice here. When ya fitin�, best position iz to hold weapon in one hand and shield in another. Got it?");
                    }
                    break;

                case 5:
                    sayName(t);
                    if (RPG.p(0.5) && miira != null && !miira.isChattedToHero()) {
                        Game.quotedMessage("Didn't you talk to Miira the ranger (in this room)? You'd try, yeah.");
                    } else {
                        Game.quotedMessage("Yer know what? Do most 'portant ting is nevva to fite on when you is beat. Live ta fite anutha day!");
                    }
                    break;

                default:
                    Game.messageTyrant(t.getTheName() + " chats with you for some time.");
                    t.set("ChatCount", 100);
                    break;
            }
            return false;
        }
    }//class RoscoBurrl

    private static class SilasInnkeeper extends Script
    {
			public SilasInnkeeper()
            {
                chattedToHero = false;
            }
            private static final long serialVersionUID = 3258939205411305016L;
            /* Date of last conversation
             */
            private Date lastTime = null;
            /* TIME_MIN and TIME_MAX contain time (in milliseconds). They create
             * range [TIME_MIN, TIME_MAX] to select time randomly from it.
             */
            private final static int TIME_MIN = 5*60*1000;
            private final static int TIME_MAX = 6*60*1000;
            private Thing t;
            private boolean chattedToHero;
            private MiiraTheRanger miira;

            /** The method is supposed to be called only once - just when
             *  MiiraTheRanger object is available.
             *  We need to know MiiraTheRanger object to request
             *  MiiraTheRanger.isChattedToHero().
             */
            public void setMiira(final MiiraTheRanger miira) {
                this.miira = miira;
            }
            public final boolean isChattedToHero() {
                return chattedToHero;
            }
            private boolean isLongTime() {
                if (lastTime == null)
                    return false;
                final long diff = (new Date()).getTime() - lastTime.getTime();
                final long threshold = RPG.r(TIME_MIN, TIME_MAX);
                return diff > threshold;
            }

            private void sayYouAgain() {
                final String arr[] = {
                    "Ah, this is you again, traveler.",
                    "Ah, this is you again, stranger.",
                    "Ah, this is you again, foreigner.",
                    "Nice to see you again, traveler.",
                    "Nice to see you again, stranger.",
                    "Nice to see you again, foreigner."
                };
                Game.quotedMessage(RPG.pick(arr));
            }

            private void sayHello() {
                /*-----
                 Some races we regard as "exotic". For them we can say hello like
                 "Oh, you are hawken...we never saw them here. Welcome to my Inn!"

                 In some cases we can use race name in greeting: "Hi, gnome, welcome "
                 to my Inn!"

                 And, eventually, greeting can be simple: "Welcome to my inn!"
                 -----*/
                sayName(t);
                final String race = Game.hero().getstring("Race");
                if (isExoticRace(race)) {
                    if (RPG.p(0.3)) {
                        sayHelloNeverSawYouHere(race);
                        return;
                    }
                    if (RPG.p(0.2)) {
                        sayHelloUsingRace(race);
                        return;
                    }
                }
                final String[] hello = {
                    "Welcome to my inn!",
                    "Travelers are always welcomed in my inn!",
                    "Hi, stranger!",
                    "Hello, stranger!",
                    "Hi, traveler!",
                    "Hello, traveler!",
                    "Welcome to my inn, traveler!",
                    "Welcome to my inn, stranger!",
                    "Nice to see you in my inn!",
                    "Hello, nice to see you in my inn!",
                    "Nice to see an adventurer like you in my inn!",
                    "Nice to see you in my inn, stranger!",
                    "Nice to see you in my inn, traveler!"
                };
                Game.quotedMessage(RPG.pick(hello));
            }

            private void sayHelloNeverSawYouHere(final String race) {
                final String part1 = "Oh, you are ";
                final String part2 = "...we never saw them here.";
                final String[] part3 = {
                    " Welcome to my inn!",
                    " So welcome to my inn!",
                    " Nice to see you my inn!"
                };
                String s = part1 + race + part2 + RPG.pick(part3);
                Game.quotedMessage(s);
            }

            private void sayHelloUsingRace(final String race) {
                final String[] start = {
                    "Welcome to my inn, ",
                    "Hi, ",
                    "Hello, ",
                    "Nice to see you in my inn, ",
                    "Hello, nice to see you in my inn, "
                };
                Game.quotedMessage(RPG.pick(start) + race +  "!");
            }

            private void sayAboutMiira() {
                if (miira != null && miira.isChattedToHero()) {
                    Game.quotedMessage("I see you've already talked to Miira the " +
                        "ranger in this room. Cool girl, isn't she?");
                    return;
                }
                Game.quotedMessage("There is Miira the ranger in this room. " +
                    "Talk to her: she can say something interesting.");
            }

            public boolean handle(Thing t, Event e) {
                chattedToHero = true;
                this.t = t;
                Thing h=e.getThing("Target");
                t.incStat("ChatCount",1);
				switch (t.getStat("ChatCount")) {
					case 1:
                        {
                        sayHello();
                		break;
                        }

					case 2:
                        {
                        if (Tutorial.isBusy(0.1, h.getStat("CH"), 6)) {
                            t.set("ChatCount", 2);
                            break;
                        }
						sayName(t);
                        if (isLongTime()) {
                            sayYouAgain();
                            break;
                        }
                        final String[] arr1 = {
                            "If you're after a job to do, there are some pests down in the cellar that need clearing out.",
                            "Are you after a job to do? There are some pests down in the cellar that need clearing out.",
                            "Are you after a job to do? Some pests down in the cellar need clearing out.",
                            "You know, there are some pests down in the cellar that need clearing out.",
                            "You know, some pests down in the cellar need clearing out.",
                            "Listen, there are some pests down in the cellar that need clearing out.",
                             "You know what? Some pests down in the cellar need clearing out."};
                        final String[] arr2 = {
                            " You can help yourself to the funny old potions down there,",
                            " You may be interested in the funny old potions down there,"
                        };
                        final String[] arr3 = {
                            " I know you adventurers can always find a use for them.",
                            " you adventurers can always find a use for them.",
                            " adventurers like you can always find a use for them.",
                            " travelers like you can always find a use for them."
                        };
                        Game.quotedMessage(RPG.pick(arr1) + RPG.pick(arr2) +
                                RPG.pick(arr3));
						break;
                        }

					default:
                        {
                        sayName(t);
                        if (isLongTime())
                        {
                            sayYouAgain();
                            final String[] arr = {
                                "I hope you found something in the cellar.",
                                "I hope you found something in that cellar.",
                                "Well, I hope you found something in the cellar.",
                                "I hope you found something useful in the cellar.",
                                "I hope you already visited the cellar.",
                                "I hope you already visited the cellar I told you about."
                            };
                            Game.quotedMessage(RPG.pick(arr));
                        }
                        else
                        {
                            if (RPG.test(h.getStat("CH"), 3) && RPG.p(0.5)) {
                                sayAboutMiira();
                            }
                            else
                            {
                                final String[] arr1 = {
                                    "The cellar I told you about is to the south from this room.",
                                    "That cellar I told you about is to the south from this room.",
                                    "By the way, the cellar I told you about is to the south from this room.",
                                    "I told you about a cellar; you can find it to he south from this room.",
                                    "I told you about a cellar; it is located to he south from this room."
                                };
                                final String[] arr2 = {
                                    "",
                                    " Be careful there!",
                                    " Be careful � monsters (or something else) can happen there�",
                                    " Be careful there � you know, darkness, monsters and so on�",
                                    " Be careful � monsters can happen there.",
                                    " There can be monsters there - so be careful!"
                                };
                                Game.quotedMessage(RPG.pick(arr1) +
                                        RPG.pick(arr2));
                            }
                        }
						t.set("ChatCount", 100);
						break;
                        }
				}
                this.t = null;
                lastTime = new Date();
				return false;
			}
    }

    private static class MiiraTheRanger extends Script
    {
        public MiiraTheRanger()
        {
            chattedToHero = false;
        }

        private SilasInnkeeper silasInnkeeper;
        private RoscoBurrl rosco;
        private boolean chattedToHero;

        private static final long serialVersionUID = 4049633512149890352L;

        /* Date of last conversation
         */
        private Date lastTime = null;
        /* TIME_MIN and TIME_MAX contain time (in milliseconds). They create
         * range [TIME_MIN, TIME_MAX] to select time randomly from it.
         */
        private final static int TIME_MIN = 1*60*1000;
        private final static int TIME_MAX = 2*60*1000;


        public final boolean isChattedToHero() {
            return chattedToHero;
        }
        /** The method is supposed to be called only once - just when
         *  RoscoBurrl object is available.
         *  We need to know RoscoBurrl object to request
         *  RoscoBurrl.isChattedToHero().
         */
        public void setRosco(final RoscoBurrl rosco) {
            this.rosco = rosco;
        }
        /** The method is supposed to be called only once - just when
         *  SilasInnkeeper object is available.
         *  We need to know SilasInnkeeper object to request
         *  SilasInnkeeper.isChattedToHero().
         */
        public void setSilas(final SilasInnkeeper silasInnkeeper) {
            this.silasInnkeeper = silasInnkeeper;
        }

        private boolean isLongTime() {
            if (lastTime == null)
                return false;
            final long diff = (new Date()).getTime() - lastTime.getTime();
            final long threshold = RPG.r(TIME_MIN, TIME_MAX);
            return diff > threshold;
        }

        /** The method PROBABLY says hello
         */
        private void tryToSayHello(final Thing h) {
            Tutorial.sayHello(h, 0.5, 0.3, 0.3);
        }

        private void sayAboutSilasInnkeeper() {
            final String[] word = {
                "miser",
                "skinflint"
            };
            if (silasInnkeeper != null && silasInnkeeper.isChattedToHero()) {
                Game.quotedMessage("Ah, you've already talked to Silas Innkeeper in this room. " +
                    "He's just fat " + RPG.pick(word) + ", right?");
                return;
            }
            Game.quotedMessage("You see Silas Innkeeper to the left? He is " +
                    RPG.pick(word) +
                    "; don't even think he will give you somethin' for free.");
        }

        private void sayFinalPhrase() {
            if (RPG.p(0.8) && rosco != null && !rosco.isChattedToHero()) {
                Game.quotedMessage("See Rosco Burrl in the room? Come to him and chat.");
                return;
            }
            sayIamBored();
        }

        private void sayIamBored() {
            final String[] bored = {
                "I�m little bored talking to you.",
                "I�m little bored talking to you, sorry.",
                "Sorry, I�m little bored talking to you.",
                "Sorry pal, I�m little bored talking to you.",
                "I�m afraid I have nothing more to tell you, sorry.",
                "I�m afraid I have nothing more to tell you, pal.",
                "I�m afraid I have nothing more to tell you, my friend.",
                "I think I have nothing more to tell you, sorry."
            };
            Game.quotedMessage(RPG.pick(bored));
        }

        public boolean handle(Thing t, Event e) {
            chattedToHero = true;
            Thing h=e.getThing("Target");
            t.incStat("ChatCount",1);
            switch (t.getStat("ChatCount")) {
                case 1:
                    sayName(t);
                    tryToSayHello(h);
                    Game.messageTyrant("You exchange some adventurer banter.");
                    break;
                case 2:
                    sayName(t);
                    if (Tutorial.isBusy(0.1, h.getStat("CH"), 6)) {
                        t.set("ChatCount", 2);
                        break;
                    }
                    if (RPG.test(h.getStat("CH"), 20)) {
                        Item.identify(Lib.create("kahnflower"));
                        Game.messageTyrant(t.getTheName() +
                                " warns you not to pick kahnflowers without wearing gloves.");
                    }
                    else
                    {
                        sayAboutSilasInnkeeper();
                    }
                    break;
                case 3:
                    if (RPG.test(h.getStat("CH"), 3)) {
                        //-Let's give an advice to Hero
                        sayName(t);
                        if (RPG.p(0.5)) {
                            Game.quotedMessage("Remember: " +
                                    "to climb on a mountain, you must have Climbing skill.");
                        } else {
                            Game.quotedMessage("Haven't you still visited the little altar room to the north from here?");
                        }
                        break;
                    }
                    Game.messageTyrant("You chat for a while");
                    if (!RPG.test(h.getStat("CH"),3)) {
                        Game.messageTyrant(t.getTheName()+ " yawns.");
                        t.set("ChatCount",100);
                    }
                    break;
                case 4:
                    Game.messageTyrant("You talk about how best to defeat " +
                            Lib.createType("IsMonster",RPG.d(16)).getAName() +
                            ".");
                    if (RPG.test(h.getStat("CH"), 13)) {
                        Game.messageTyrant(t.getTheName()+" seems impressed with your insight.");
                        Game.messageTyrant("She gives you an identify scroll.");
                        Game.quotedMessage("Read this if you need to figure out what a mysterious item does.");
                        Thing it=Lib.create("scroll of Identify");
                        Item.identify(it);
                        h.addThing(it);
                    }
                    break;

                default:
                    sayName(t);
                    if (isLongTime()) {
                        Game.quotedMessage("Ah, this is you again; wassup?");
                    }
                    else {
                        sayFinalPhrase();
                    }
                    t.set("ChatCount",100);
                    break;

            }
            lastTime = new Date();
            return false;
        }
    }

    private static class BurthaTheTownsWoman extends Script
    {
        static final long serialVersionUID = -3409510129139664108L;

        public boolean handle(Thing t, Event e) {
            t.incStat("ChatCount",1);
            switch (t.getStat("ChatCount")) {
                case 1:
                    {
                        final String firstPhrase[] = {
                            "I'm just a townswoman...whatchoo want?",
                            "I'm just a townswoman...what is you wantin'?",
                            "What is you wantin'?",
                            "What is you wantin', pal?",
                            "Whatchoo want, pal?",
                            "What?",
                            "Wassup?",
                            "Wassup, pal?",
                            "Hey, wassup?",
                            "What?",
                            "Wanna somethin'?",
                            "U wanna somethin'?",
                            "Wanna somethin', ah?",
                            "Wanna somethin', pal?"};
                        sayName(t);
                        Game.quotedMessage(RPG.pick(firstPhrase));
                    }
                    break;
                case 2:
                    sayName(t);
                    Game.quotedMessage("See piece of ham in this room? It's MINE!");
                    break;
                default:
                    {
                        sayName(t);
                        final String finalPhrase[] = {
                            "I tole you: da piece of ham is MINE! Don't you dare to take it!",
                            "I told you: da piece of ham is MINE! Don't hope i'll give it to you!"
                        };
                        Game.quotedMessage(RPG.pick(finalPhrase));
                        t.set("ChatCount",100);
                        break;
                    }
            }//switch
            return false;
        }
    }

    private static class RotchyTheFarmer extends Script
    {
        private static final long serialVersionUID = 6595782575469702238L;

        public boolean handle(Thing t, Event e) {
            t.incStat("ChatCount",1);
            switch (t.getStat("ChatCount")) {
                case 1:
                    sayName(t);
                    Game.quotedMessage("Hello, Sir. I'm just a farmer. Can I help you?");
                    break;
                case 2:
                    sayName(t);
                    Game.quotedMessage("There is nice inn to the north. Visit it if you haven't been there yet.");
                    break;
                case 3:
                    sayName(t);
                    Game.quotedMessage("You see stairs down nearby? This is entrance to cellar; but I did not ever dare to go down there.");
                    break;
                default:
                    sayName(t);
                    if (RPG.p(0.5))
                    {
                        Game.quotedMessage("I could just add: beware of monsters!");
                    }
                    else
                    {
                        Game.quotedMessage("I'm afraid, Sir, there is nothing more I can tell you.");
                    }
                    t.set("ChatCount",100);
                    break;
            }//switch
            return false;
        }
    }

	public static BattleMap buildLoft() {
		BattleMap m=new BattleMap(5,9);
		m.setTheme("caves");
		m.set("Level",1);
		m.set("Description","Small Loft");
		
		// first room
		m.fillArea(1,1,3,3,m.floor());
		m.setEntrance(Lib.create("ladder down"));
		m.addThing(m.getEntrance(),1,1);
		
		// second room
		m.setTile(2,4,m.floor());
		Thing door=Lib.create("trapped black door");
		door.set("KeyName","inn loft key");
		m.addThing(door,2,4);
        m.addThing(
            Special.messagePoint("To open the door to the south, you need a key.")
            ,2,3);

		m.fillArea(1,5,3,7,m.floor());
		
		m.addThing(
				Special.messagePoint("To the south of you is a chest. You can try to open it by running into it. It may be locked, in which case you will either need to break it open or find a way to unlock it. If you manage to open it and find an item inside, press \",\" to pick it up.")
				,2,6
		);
		m.addThing(Lib.create("[IsChest]"),2,7);
		
		m.replaceTiles(0,m.wall());
		return m;
	}

	
	public static BattleMap buildCellar() {
		BattleMap m=new BattleMap(25,25);
		m.setTheme("caves");
		m.set("Level",1);
		m.set("Description","The Dark Cellar");
		
		// initial room
		m.setEntrance(Lib.create("stairs up"));
		m.fillArea(1,1,5,5,Tile.CAVEFLOOR);
		m.addThing(m.getEntrance(),2,2);
		
		Thing h=Game.hero();
		
		if (h.getFlag(Skill.LITERACY)) {
			m.addThing(Scroll.note("note","Rotchy - There are too many rats down here. It is getting dangerous. See if you can find some poison next time you go to the town."),3,3);
			m.addThing(
					Special.messagePoint("There is a note here on the ground. You can pick it up with the \",\" key. Because you are have the Literacy skill, you can then press \"r\" to read it.")
					,3,3
			);
		}
		
		// the maze
		Maze.buildMaze(m,6,0,12,12);
		m.setTile(6,3,Tile.CAVEFLOOR);
		m.addThing(Lib.create("portcullis"),6,3);
		m.addThing(
				Special.messagePoint("These twisty passages have a strange, almost labyrinthine design. Perhaps this cellar was once an old dungeon.")
				,6,3
		);
		
		// rat room
		m.fillArea(1,7,5,11,m.floor());
		m.setTile(6,11,m.floor());
		m.addThing(Lib.create("door"),6,11);
		m.addThing(
				Special.messagePoint("You can hear the scurrying of rats behind this door. Are you ready to fight? To prepare for battle, you can press \"w\" to wield a weapon or wear armour. To attack an opponent, just run into it with the cursor keys.")
				,7,11
		);
		for (int i=0; i<6; i++) {
			m.addThing(Lib.create("[IsRat]"),1,7,5,11);
		}
		m.addThing(
				Special.messagePoint("Sometimes there are hidden areas within dungeons. Press \"s\" to search your current surroundings.")
				,1,11
		);
		m.addThing(Lib.create("secret door"),1,12);
		m.addThing(
				Special.messagePoint("There is a potion lying here. Potions often have magical properties that can greatly assist you in your adventures. However, it is dangerous to drink unidentified potions because some of them may have nasty side effects. You can quaff a potion by pressing the \"q\" key.")
				,1,7
		);
		m.addThing(Lib.create("[IsPotion]"),1,7);

		
		// secret room
		m.fillArea(1,13,5,17,m.floor());
		m.addThing(Lib.create("rat cave"),2,17);
		m.addThing(Lib.create("[IsFood]"),3,14);
		m.addThing(
				Special.messagePoint("If you become hungry during your adventures, you will need to find food. The item here can be eaten by picking it up and pressing \"e\"")
				,3,14
		);
		m.addThing(Lib.create("[IsPotion]"),4,14);
		m.addThing(Lib.create("[IsSnake]"),3,14);
		m.addThing(
				Special.messagePoint("This small tunnel is too tight for you to enter. However, you could easily be surprised by rats coming out of it. Be careful!")
				,2,17
		);
		
		// big room
		m.setTile(6,15,m.floor());
		m.addThing(Lib.create("portcullis"),6,15);
		m.fillArea(7,13,12,23,m.floor());
		for (int y=14; y<=22; y+=2) {
			m.setTile(8,y,m.wall());
			m.setTile(11,y,m.wall());
		}
		m.addThing("bone",9,15);
		m.addThing(
				Special.messagePoint("Many items in Tyrant have multiple uses. A bone for example, can be wielded as a weapon by pressing \"w\". Alternatively, if you are feeling hungry you can eat it by pressing \"e\".")
				,9,15
		);
		
		// back room
		m.setTile(6,21,m.floor());
		m.addThing(Lib.create("rotten door"),6,21);
		m.fillArea(1,19,5,23,m.floor());
		m.addThing(Lib.create("kobold"),3,21);
		{
			Thing key=Lib.create("inn loft key");
			m.addThing(key,3,21);
		}
		
		// viewing room
		m.fillArea(13,1,23,3,m.floor());
		m.setTile(12,3,m.floor());
		m.addThing("door",12,3);
		Thing mon=Lib.createType("IsMonster",2);
		m.addThing(
				Special.messagePoint("You hear something moving behind this door. Sounds like "+mon.getAName()+"! If you open the door, be ready to fight. Wield your weapons and wear armour by pressing \"w\". In close combat, you can attack by simply running into the monster.")
				,11,3
		);
		m.addThing(mon,15,3);
		m.addThing(
				Special.messagePoint("This portcullis (to the south of you) can never be opened. It may be useful though, since you can always get a clear view of the room on the other side! Press \"l\" to look at a distant object.")
				,14,3
		);
		
		for (int x=14; x<=22; x+=4) {
			m.setTile(x,4,m.floor());
			m.addThing("invincible portcullis",x,4);
			m.fillArea(x-1,5,x+1,7,m.floor());
			String th=RPG.pick(new String[] {"[IsChest]","[IsStoreItem]","[IsMonster]","fire snake"});
		    m.addThing(th,x,6);	
		    if (th.equals("[IsChest]")) {
				m.addThing(
					Special.messagePoint("To the north of you is a chest. You can try to open it by running into it. It may be locked, in which case you will either need to break it open or find a way to unlock it. If you manage to open it and find an item inside, press \",\" to pick it up.")
						,x,7
				);
		    }
		   
			m.setTile(x,8,m.floor());
			m.addThing("door",x,8);
		}
		m.fillArea(13,9,23,10,m.floor());
		
		// tunnels
		m.makeRandomPath(13,21,18,12,14,12,21,23,m.floor(),false);
		m.makeRandomPath(18,12,21,23,14,12,21,23,m.floor(),false);
		m.setTile(14,21,m.floor());
		m.setTile(23,23,m.floor());
		m.addThing("[IsTrap]",23,23);
		m.addThing("[IsStoreItem]",23,23);
		m.setTile(22,23,m.floor());
		m.addThing(
				Special.messagePoint("An item lies to the west, but be careful! It looks like a trap has been set. You can search for traps by pressing \"s\". If you have the disarm traps skill, you can attempt to disarm the trap by pressing \"a\" to apply the Disarm Trap skill. Otherwise, you can risk stepping onto the trap if you dare....")
				,22,23
		);
		
		// tunnel end
		m.setTile(18,11,m.floor());
		m.addThing("locked door",18,11);
		m.addThing(
				Special.messagePoint("Some doors may be locked. You can unlock them if you have keys or lock picks and the Lockpicking skill. Otherwise, your best chance of getting through is kicking them down by pressing \"k\".")
				,18,12
		);
		
		
		m.set("WanderingRate",0);
		m.replaceTiles(0,Tile.CAVEWALL);
		return m;
	}
	
	public static BattleMap buildTutorialMap() {
		BattleMap m=new BattleMap(25,25);
		m.setTheme("village");
		m.set("Description","The Tradepost Inn");
		m.set("Level",1);
		
		// overall building structure
		Outdoors.buildOutdoors(m,0,0,24,24,Tile.GRASS);
		m.clearArea(0,0,18,24);
		m.fillArea(6,6,14,14,Tile.CAVEWALL);
		m.fillArea(7,7,13,13,Tile.CAVEFLOOR);
		m.fillArea(3,7,6,14,Tile.CAVEWALL);
		m.fillArea(4,8,5,13,Tile.CAVEFLOOR);
		m.setTile(10,14,Tile.CAVEFLOOR);
		m.addThing(Fire.create(4),10,14);
		
		// mini temple
		{
			int x=10;
			int y=1;
		
			m.fillArea(x,y,x+4,y+3,m.wall());
			m.fillArea(x+1,y+1,x+3,y+2,Tile.STONEFLOOR);
			m.setTile(x+2,y+3,Tile.STONEFLOOR);
			m.addThing("ornate door",x+2,y+3);
			m.addThing("stone altar",x+2,y+1);
			m.addThing(
					Special.messagePoint("The people of North Karrain are highly religious, and altars can be found throughout the land. You can pray for divine assistance at any time with the \"_\" key.")
					,x+2,y+2
			);
		}
		
		// well garden
		m.addThing("[IsWell]",8,2);
		for (int x=6; x<=10; x++) m.addThing("tree",x,0);
		m.addThing("tree",2,0);
		m.addThing("bush",1,2);
		m.addThing("tree",0,3);
		m.addThing("tree",0,1);
		m.addThing("[IsWeapon]",4,0);
		
		//bottom trees
		m.addThing("tree",16,23);
		m.addThing("tree",14,23);
		m.addThing("tree",14,24);
		m.addThing("tree",12,24);
		
		// back garden
		m.setTile(0,7,Tile.RIVER);
		m.fillArea(0,8,2,9,Tile.RIVER);
		m.addThing(
				Special.messagePoint("This small backwater is easily crossable if you have the Swimming skill. Alternatively, you may be able to find a boat.")
				,1,7
		);
		
		// herb garden
		for (int y=20; y<=23; y++) {
			for (int x=1; x<=5; x++) {
				if (RPG.d(2)==1) m.addThing(Lib.createType("IsHerb",RPG.d(6)),x,y);
			}
		}
		m.addThing(
				Special.messagePoint("Herbs can be found wherever there is fertile soil. Many herbs are treasured for their magical properties. Masters of the Herb Lore skill are able to use them most effectively. Some herbs may be dangerous, so it is wise to identify them first.")
				,3,22
		);
		
		// mini outhouse
		{
			int x=2;
			int y=1;
		
			m.fillArea(x,y,x+4,y+6,m.wall());
			m.fillArea(x+1,y+1,x+3,y+2,Tile.STONEFLOOR);
			m.fillArea(x+1,y+4,x+3,y+5,Tile.CAVEFLOOR);
			m.setTile(x+2,y+3,Tile.STONEFLOOR);
			m.setTile(x+4,y+4,Tile.CAVEFLOOR);
			m.setTile(x+0,y+4,Tile.CAVEFLOOR);
			m.addThing("stable door",x+2,y+3);
			m.addThing("stable door",x+4,y+4);
			m.addThing("portcullis",x+0,y+4);
			Thing p=Lib.create("townswoman");
			AI.name(p,"Burtha");
			AI.setGuard(p,m,x+1,y+1);
			m.addThing(p,x+1,y+1);
			m.addThing(Lib.create("ham"),x+1,y+1).set("IsOwned",1);
			m.addThing(Fire.create(10),x+3,y+1);
			m.addThing("stone bench",x+3,y+2);
            p.set("OnChat", new BurthaTheTownsWoman());
		}
		
		// passageways
		m.fillArea(6,10,14,10,Tile.CAVEFLOOR);
		m.addThing(Lib.create("door"),14,10);
		m.addThing(
				Special.messagePoint("You can open the door automatically by running into it. You can also use the \"o\" key to open or close the door.")
				,15,10
		);
		m.addThing(Lib.create("inn sign"),15,9);
		m.addThing(Lib.create("potted flower"),15,11);
		m.setTile(10,6,Tile.CAVEFLOOR);
		m.addThing(Lib.create("door"),10,6);
		
		
		
		// main room message points
		m.addThing(
				Special.messagePoint("You can talk to the people in the inn by standing next to them and pressing \"c\" to chat. Many characters that you meet on your travels will be able to give you useful information or aid you in your quests in some way.")
				,13,10
		);
		
		m.addThing(
				Special.messagePoint("The door next to you is locked. You may be able to find a way to open it. You can kick down doors in dungeons with the \"k\" key but the locals won't like you trying that around here!")
				,7,10
		);
		
		// stable area
		m.fillBorder(7,14,18,22,Tile.CAVEWALL); // wall
		m.fillArea(8,15,9,21,Tile.CAVEFLOOR); // stable floor
		m.fillBorder(7,14,10,22,Tile.CAVEWALL); // stable wall
		m.fillArea(15,14,16,14,Tile.GRASS);// entrance
		m.setTile(10,14,Tile.CAVEFLOOR);
		m.setTile(10,16,Tile.CAVEFLOOR);
		m.fillArea(9,15,11,15,Tile.CAVEWALL);
		m.addThing(Lib.create("stable door"),10,16);
		m.addThing(
				Special.messagePoint("The stables here are filled with the smell of fresh manure. You spy a ladder going upstairs to some kind of loft. There is also a crude knife on the floor, that seems to have been discarded.")
				,10,16
		);
		m.addThing(Lib.create("stone knife"),8,17);
		m.addThing(
				Special.messagePoint("You can pick up and use this knife as a weapon. Press \",\" (comma) or \"p\" to pick it up. Then press \"w\" to wield it as a weapon.")
				,8,17
		);
		{
			Thing t=Lib.create("tutorial loft");
			m.addThing(t,8,15);
		}
		m.addThing(
				Special.messagePoint("Press \"x\" to climb this ladder.")
				,8,15
		);
		m.fillArea(7,18,10,18,Tile.CAVEWALL);
		m.setTile(10,20,Tile.CAVEFLOOR);
		m.addThing(Lib.create("secret door"),7,19);
		m.addThing(Lib.create("stable door"),10,20);
		Thing p=Lib.create("farmer");
		AI.name(p,"Rotchy");
		AI.setGuard(p,m,13,18,17,22);
		m.addThing(p,15,19);
        m.addThing(
            Special.messagePoint("There is Rotchy the farmer to the south. " +
            "You can go to him and chat by pressing \"c\"."),16,14);
        p.set("OnChat",new RotchyTheFarmer());

		m.addBlockingThing(Lib.create("[IsBarrel]"),13,18,17,22);
		m.addBlockingThing(Lib.create("[IsBarrel]"),13,18,17,22);
		m.addBlockingThing(Lib.create("[IsBarrel]"),13,18,17,22);
	
		// cellar stairs
		m.setTile(13,15,Tile.CAVEWALL);
		{
			Thing t=Lib.create("tutorial cellar");
			m.addThing(t,12,15);
		}
		m.addThing(
				Special.messagePoint("You see some stairs here heading down into a dark cellar. You can hear the sounds of rats scuttling below. Move onto the stairs and press \"x\" if you want to venture into the dark cellar.")
				,12,16
		);
		
		
		// storeroom
		m.addThing(Lib.create("locked door"),6,10);
		m.addThing(Lib.create("fish"),4,8);
		m.addThing(Lib.create("salmon"),5,8);
		m.addThing(Lib.create("[IsBarrel]"),4,13);
		m.addThing(Lib.create("[IsBarrel]"),5,13);
		m.setTile(4,7,Tile.CAVEFLOOR);
		m.addThing("locked door",4,7);
		m.fillArea(2,9,4,9,Tile.RIVER);
		m.addThing("invincible portcullis",3,9);

		
		// entrance
		m.setEntrance(Portal.create("invisible portal"));
		m.addThing(m.getEntrance(),m.width-2,10);
		
		// pathway
		int treegap=RPG.d(3);
		String tree=RPG.pick(new String[] {"tree","pine tree","large tree","bush"});
		for (int x=18; x<m.width; x+=treegap) {
			m.addThing(tree,x,8);
			m.addThing(tree,x,12);
		}
		
		m.fillArea(15,10,m.width-1,10,Tile.STONEFLOOR);
		
		// map settings
		m.set("WanderingRate",0);
		m.set("EnterMessageFirst",
			"Welcome to Tyrant!\n"+	
			"The Tradepost Inn lies to your west, where friendly locals will be glad to give novices an introduction to the Tyrant world. If you are already an experienced adventurer, you can leave this area and start adventuring by heading east.\n"+
			"\n"+
			"We hope you enjoy playing Tyrant!"
		);
		addPeople(m);
		
		return m;
	}

    private static boolean isBusy(
            final double p, final int ch, final int chToTest) {
        if (!RPG.p(p))
            return false;
        if (RPG.test(ch, chToTest))
            return false;
        final int chPolite = (int) (0.8*chToTest);
        String[] busy;
        if (RPG.test(ch, chPolite)) {
            busy = new String[] {
                //-More polite answer
                "Sorry, I'm busy; come a little later.",
                "I'm busy now, sorry; come a little later.",
                "Sorry, I'm busy now; so come a little later.",
                "Sorry, I'm busy now; come little later, all right?",
            };
        } else {
            busy = new String[] {
                "I'm busy; come a little later.",
                "I'm busy now; come a little later.",
                "I'm busy now; so come a little later.",
                "I'm busy now; come little later, all right?",
            };
        }
        Game.quotedMessage(RPG.pick(busy));
        return true;
    }



    private static void sayName(Thing t) {
        Game.messageTyrant("You talk to " + t.getTheName());
    }

    private static boolean isExoticRace(final String race) {
        return !race.equals("human") && !race.equals("hobbit");
    }
    /**
     * Some races are considered as "unpleasant". We take this in account while
     * talking to such Hero.
     */
    private static boolean isUnpleasantRace(final String race) {
        if (race.equals("half orc"))
            return true;
        if (race.equals("half troll"))
            return true;
        return false;
    }

    /**
     * @param h  Hero
     * @return true when hello has been said; false otherwise
     */
    private static boolean sayHello(final Thing h, final double p1,
            final double p2, final double p3) {
        final String race = h.getstring("Race");
        if (!RPG.p(p1))
            return false;
        if (isUnpleasantRace(race) &&
                RPG.p(p2) &&
                !RPG.test(h.getStat("CH"),3)) {
            final String[] part1 = {
                    "Oh, you're ",
                    "I see, you're "
            };
            final String part2 = "...to tell truth, we don't very like 'em here in the village. ";
            final String[] part3 = {
                    "But welcome anyway.",
                    "Anyway, welcome.",
                    "Anyway, hello.",
                    "Anyway, hi there."
            };
            Game.quotedMessage(RPG.pick(part1) + race + part2 + RPG.pick(part3));
            return true;
        }//if

        if (isExoticRace(race)) {
            if (RPG.p(p2)) {
                final String[] part1 = {
                    "Oh, you're ",
                    "Ah, you're ",
                    "I see, you're "
                };
                final String[] part2 = {
                    "...we never saw them here. ",
                    "...we rarely see them here. ",
                    "...we did not see them here yet. "
                };
                final String[] part3 = {
                    "Hi",
                    "Hi!",
                    "Hello.",
                    "Hello!",
                    "So hi there.",
                    "Welcome!"
                };
                Game.quotedMessage(RPG.pick(part1) + race + RPG.pick(part2) + RPG.pick(part3));
                return true;
            }
            if (RPG.p(p3)) {
                final String[] begin = {
                    "Hi, ",
                    "Hello, ",
                    "Welcome, ",
                    "Nice to see you, "
                };
                Game.quotedMessage(RPG.pick(begin) + race + ".");
                return true;
            }
        }//if race is "exotic"
        final String[] hello = {
            "Hi.",
            "Hi!",
            "Hi there.",
            "Hello.",
            "Hello!",
            "Nice to see you.",
            "Welcome!",
            "Hi, pal.",
            "Nice to meet you.",
            "Hi. I see you're traveler like me.",
            "Hello. I see you're traveler like me."
        };
        Game.quotedMessage(RPG.pick(hello));
        return true;
    }

    private static void addPeople(BattleMap m) {
		Thing t;
		
		t=Lib.create("blacksmith");
		AI.name(t,"Silas Innkeep");
		AI.setGuard(t,m,6,7,8,9);
		m.addThing(Lib.create("NS table"),8,7);
		m.addThing(Lib.create("NS table"),8,8);
		m.addThing(Lib.create("NS table"),8,9);
		m.addThing(Lib.create("stool"),9,7);
		m.addThing(Lib.create("stool"),9,9);
		m.addThing(Lib.create("ham"),8,7);
        final SilasInnkeeper silas = new SilasInnkeeper();
		t.set("OnChat",silas);
		m.addThing(t,7,8);


		
		t=Lib.create("guard");
		AI.name(t, "Rosco Burrl");
		AI.setGuard(t,m,12,8,12,8);
		m.addThing(t,12,8);
		m.addThing(Lib.create("stool"),12,8);
		m.addThing(Lib.create("stool"),11,7);
		m.addThing(Lib.create("table"),11,8);
        final RoscoBurrl rosco = new RoscoBurrl();
		t.set("OnChat", rosco);
		// girl
		t=Lib.create("village girl");
		AI.setGuard(t,m,9,8,9,8);
		m.addThing(t,9,8);
		
		//Jeweller
		t=Lib.create("jeweller");
		AI.setGuard(t,m,10,13,10,13);
		m.addThing(t,10,13);
		
		// teacher
		t=Lib.create("teacher");
		AI.setGuard(t,m,12,13,12,13);
		m.addThing(t,12,13);
		m.addThing(Lib.create("table"),11,13);
		m.addThing(
				Special.messagePoint("The robed gentleman next to you is a learned teacher. You can enhance your skills by training with him assuming that you have gained sufficient experience. If you chat to him with the \"c\" key he will tell you whether you are able to enhance your skills. You can view your character's cureent skills and other useful information at any time by pressing  \"v\".")
				,12,12
		);
		
		// Miira the ranger
		t=Lib.create("ranger");
		t.set("ChatCount",0);
		AI.name(t,"Miira");
		AI.setGuard(t,m,11,7,11,7);
		m.addThing(t,11,7);
        final MiiraTheRanger miira = new MiiraTheRanger();
		t.set("OnChat", miira);

        //-Object ssilas, miira and rosco contain several cross-rererences:
        silas.setMiira(miira);
        miira.setSilas(silas);
        miira.setRosco(rosco);
        rosco.setMiira(miira);

	}
	
	public static void init() {
		Thing t;
		
		t=Lib.extend("inn loft key","iron key");
		t.set("Frequency",0);
		Lib.add(t);
		
	}
}
