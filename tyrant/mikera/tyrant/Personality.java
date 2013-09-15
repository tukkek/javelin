package tyrant.mikera.tyrant;

import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

// The personality base class

public class Personality extends Script {
	private static final long serialVersionUID = 3257001072949539128L;
    // personalities
	public static final int CHATTER = 1;
	public static final int TEACHER = 2;
	// added by ppirrip
	public static final int BLACKSMITH = 3;
	
	// chatter subtypes
	public static final int CHATTER_TOWNIE = 0;
	public static final int CHATTER_GUARD = 1;
	public static final int CHATTER_WISE = 2;
	public static final int CHATTER_PLEASANT = 3;
	public static final int CHATTER_GOODLY = 4;
	public static final int CHATTER_ADVENTURER = 5;
	public static final int CHATTER_GOBLIN = 6;
	public static final int CHATTER_ORC = 7;
	public static final int CHATTER_SHOPKEEPER = 8;


    /**
     * Object of class ChatterComment describes comments that may be generated
     * for certain chatter type.
     */
    protected static class ChatterComment {
        public String[] comments;
        /**
         * "hello" may be empty.
         */
        public String[] hello;
        /**
         * Probability (0..1) of applying of the "hello" (may be 0.)
         */
        public double helloProbability;

        public ChatterComment(final String[] comments, final String[] hello, 
                final double helloProbability) {
            this.comments = comments;
            this.hello = hello;
            this.helloProbability = helloProbability;
        }
    }

    protected static final ChatterComment[] chattercomments = {
        // normal townie
        new ChatterComment(new String[]{"Nice day, innit?",
					"Bugger off.",
					"There be evil beasties in that forest!",
					"Mark my words, eh?", 
					"Arrr?", 
					"What is you wantin'?",
					"You be wantin' some gruel?",
					"There'll be much drinkin' at the inn tonight!"},
                new String[]{"Hi.",
                    "Hi there."},
                0.3),
        // guard
        new ChatterComment(new String[]{"You better watch yerself!",
                    "Don't mess with me!",
					"They say I'm a nooligan.",
					"I be eatin' dragon steak for breakfast.",
					"I'm 'ard.",
					"Don't you go causin' no trouble.",
					"I'm the toughest guard in town",
					"Wanna fight?",
					"Ra! I'm the mightiest hero in the world!"},
                    new String[]{},
                    0.),
        // wise words
        new ChatterComment(new String[]{"Mithril is light!",
					"Eat mushrooms for beauty and strength!",
					"Don't play with fire!",
					"Never step on strange runes.",
					"Always wear tough boots!",
					"Don't pick herbs unless you know what you are doing!",
					"You will find great beauty in the darkest places"},
                new String[]{},
                0.),
        // pleasant chat
        new ChatterComment(new String[]{
                    "So what's it like being an adventurer?",
					"Don't go near the forest... it's dangerous!",
					"That shopkeeper is a real stingy villain.",
					"I hear that rings are in fashion...",
					"Meet me later at the inn."},
                new String[]{"Hi.",
                    "Hi!",
                    "Hello.",
                    "Hello!",
                    "Hi there.",
                    "Nice to meet ya."},
                0.6),
        // goodly priest
        new ChatterComment(new String[]{"Peace be with you!",
                    "Seek the healing waters of Aramis!",
					"May the blessings of Aramis be upon you.",
					"Learn Healing so that you may help people recover from their wounds.",
					"There is great joy in learning the art of Prayer.",
					"Let us pray for peace in these troubled times."},
                 new String[]{"Greetings, my child."},
                0.7),
        // adventurer banter
        new ChatterComment(new String[]{
					"Let us swap tales in the tavern, my friend.",
					"They say the Eastern Isles are the most wondrous of all places.",
					"'Tis a great day to wander wild and free!",
					"Improve your Defence skill and you will use armour more effectively.",
					"Improve your Attack skill to give yourself greater accuracy and power in combat.",
					"They say the forests are beautiful this time of year.",
					"Practice Athletics - you never know when you might need to run swiftly.",
					"Warriors who train in Ferocity will hit their opponents more often!",
					"'Tis always best to travel light and free."},
                new String[]{"Greetings friend"},
                0.4),
        // goblin
        new ChatterComment(new String[]{
					"Is you wanting da mushrooms?",
					"You is fick.",
					"Wot is you wanting?",
					"Gimme da mushrooms, hooman.",
					"Der is gold in dem hills.",
					"Gargash is da tuffest.",
					"He He.",
					"Orcs are scary.",
					"I is tuffer dan a B'Zekroi Lord!",
					"Is good day for findin' mushrooms",
					"'oomans are yucky",
					"i like huntin' in da forests"},
                new String[]{"Hello hooman."},
                0.5),
        // orc
        new ChatterComment(new String[]{"'Ooman.",
					"**BELCH**",
					"You fick.",
					"You ugly.",
					"You stooopid.",
					"Ug.",
					"Want fooood. Now.",
					"**GROWL**"},
                new String[]{}, 0.),
        // shopkeeper
        new ChatterComment(new String[]{"Best prices here!",
					"I've got something just for you...",
					"Got anything to trade?",
					"Let's make a deal",
					"Are you a Trader? Might be able to do some good business...",
					"Here, take a look at these trinkets"},
                new String[]{}, 0.)
    };

	public Personality(int t) {
		set("Type", t);
	}

	public Personality(int t, int st) {
		this(t);
		set("SubType", st);
		
	}

	public boolean handle(Thing t, Event e) {
		talk(getStat("Type"), t);
		return true;
	}

	public static Script sayLine(String line) {
		Script s=new Script() {
			private static final long serialVersionUID = 3258694307937596213L;

            public boolean handle(Thing t, Event e) {
				String l=getString("Line");
				Game.messageTyrant("\""+l+"\"");
				return false;
			}
		};
		s.set("Line",line);
		return s;
		
	}

	private void handleChatter(final int subtype, final int chats,
                               final Thing t) {
        final ChatterComment cc = chattercomments[subtype];
        switch (chats) {
            case 1:
                //-this is first converstaion to Hero - try to say hello
                if (cc.hello.length != 0 &&
                        RPG.p(cc.helloProbability)) {
                    Game.quotedMessage(RPG.pick(cc.hello));
                    break;
                }
            default:
                //say comment
                Game.messageTyrant("\"" + RPG.pick(cc.comments) + "\"");
                t.set("ChatCount", 100);
        }

    }

    private void talk(int p, final Thing t) {
		int subtype = getStat("SubType");
		Thing h = Game.hero();
		try {
			switch (p) {
				case CHATTER :
                    t.incStat("ChatCount", 1);
                    handleChatter(subtype, t.getStat("ChatCount"), t);
					return;
				case TEACHER :
					if (h.getStat(RPG.ST_SKILLPOINTS)>0) {
						Game.messageTyrant("\"I see you have potential\"");
						Game.messageTyrant("\"I can teach you for a small fee...\"");
					} else {
						Game.messageTyrant("\"There is nothing more I can teach you now.\"");
					}
					return;
				// added by ppirrip
				case BLACKSMITH :
					Game.messageTyrant("\"I can repair your gear for a small fee...\"");
					return;
				default :
					Game.messageTyrant("You get no response.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Game.messageTyrant("\"Mumble mumble mumble\"");
		}
	}

}