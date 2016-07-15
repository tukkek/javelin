// A magic scroll
//
// If read, it will cast the spell desribed upon it
//
// Since scrolls are single-use, they are useful for giving the hero
// access to powerful or unusual spells that they must use wisely.
//
// Scrolls may also contain spells which will never be available to
// the player by other means, e.g. uncurse weapon.

package tyrant.mikera.tyrant;

import java.util.*;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;



public class Scroll  {
    private static final String[] titles =
    {	"thgil eb ereht tel",
        "gniht yxes ouy",
        "ybab atsiv al atsah",
        "senob meht fo elip dlo gib",
        "ekul ecrof eht esu",
        "strohs ym tae",
        "eno ylno eb nac ereht",
        "evah reven nac i gnihtemos",
        "eloh a ekil daeh",
        "ereh morf bup eht ees nac i",
        "naeb a dna sbeeh owt hsart etihw",
        "siht tuoba gnileef egnarts a teg i",
        "gniht taht ezis eht ta kool",
        "elbmur ot ydaer teg stel",
        "kcab eb lli", "em ta gnikool uoy era",
        "su ot gnoleb era esab ruoy lla",
        "su ot gnoleb era srevres ruoy lla",
        "su ot gnoleb era slous ruoy lla",
        "su ot gnoleb era sllorcs ruoy lla",
        "su ot gnoleb si lirhtim ruoy lla",
        "dnuora uoy deciton evi",
        "nosredna leahcim yb tnaryt",
        "stunococ fo hcnub ylevol",
        "ereh saw rennureldnurt eztlawfpmad",
        "ereh saw toofgib daorb",
        "ereh saw rakabraz igons",
        "no og tsum wohs eht",
        "xinu ton sung",
        "xinu ton sung",
		"yug a tahw",
		"yawa og",
		"zelur tnaryt",
		"zelur xunil",
		"nedbeh nerrad ot xnaht",
		"nosredna ssur ot xnaht",
		"llem noj ot zteerg",
		"reywob luap ot zteerg",
		"yreffej nitram ot zteerg",
		"nwod gnillaf si egdirb nodnol",
		"ruof dna dnasuoht owt snehta dnalgne og",
		"drows ecin a tahw",
		"avaj rof nus sknaht",
		"egrofecruos ot sknaht",
		"egrofecruos yb detsoh",
		"seripmav tsniaga cilrag esu",
		"nos ym kcowrebbaj eht eraweb",
		"tnediserp rof sunil",
        "dnalrob ot xnaht",
        "nitram ot tcepser ffun",
        "ettolrahc dna sirhc gniddew yppah",
        "evol i eno eht ot detacided",
        "uoy htiw eb ecrof eht yam",
        "meht dnib to gnir eno",
        "lla meht elur to gnir eno",
		"dlrow eth fo yaw eht"
		};
  
    private static final String[] specialtitles =
    {	"?toor tog",
        "eerf eb ot stnaw erawtfos",
        "ybab atsiv al atsah",
        "senob meht fo elip dlo gib",
		"nosredna ssur ot xnaht",
		"llem noj ot zteerg",
		"reywob luap ot zteerg",
		"yreffej nitram ot zteerg",
		"nwod gnillaf si egdirb nodnol",
		"ruof dna dnasuoht owt snehta dnalgne og",
		"drows ecin a tahw",
		"avaj rof nus sknaht",
		"egrofecruos ot sknaht",
		"egrofecruos yb detsoh",
		"seripmav tsniaga cilrag esu",
		"nos ym kcowrebbaj eht eraweb",
		"tnediserp rof sunil",
        "dnalrob ot xnaht",
        "nitram ot tcepser ffun",
        "ettolrahc dna sirhc gniddew yppah",
        "evol i eno eht ot detacided",
        "uoy htiw eb ecrof eht yam",
        "meht dnib to gnir eno",
        "lla meht elur to gnir eno",
		"dlrow eth fo yaw eht",
		"ssab eht pu pmup"
    };
    
        // create scroll of approximate level value
        public static Thing createScroll(int level) {
            return Lib.createType("IsScroll",level);
        }
        
        private static class DestroyScrollScript extends Script {
	        private static final long serialVersionUID = -8023348676467373601L;

            public boolean handle(Thing t, Event e) {
                String spell=t.getString("ScrollSpell");
                if (spell==null) return false;
                Thing s=Spell.create(spell);
                Spell.castAtLocation(s,null,(BattleMap)e.get("DeathMap"),e.getStat("DeathX"),e.getStat("DeathY"));
	        	return false;
	        }
        }
        
        private static class ReadScrollScript extends Script {
	        private static final long serialVersionUID = -90663348604771008L;

            public boolean handle(Thing s, Event e) {
	            Thing user=e.getThing("Reader");
	        	
	        	if (!s.getFlag("IsScroll")) {
	                System.out.println("Not a scroll!");
	                return false;
	            }
	            
	            if (!user.isHero())
	                return false;
	            
                Item.identify(s);
                
                String spellName=s.getString("ScrollSpell");
                if (spellName!=null) {
                	Thing spell=Spell.create(spellName);
                	// TODO: modify spell power for IN/literacy
                	QuestApp.getInstance().getScreen().castSpell(user,spell);
                	s.remove(1);
                }
                

                return true;
	        }
        }
        
        public static void init() {
            Thing t = Lib.extend("base scroll", "base item");
            t.set("IsScroll", 1);
            t.set("IsReadable", 1);
            t.set("OnRead",new ReadScrollScript());
            t.set("OnDeath",new DestroyScrollScript());
            t.set("IsMagicItem", 1);
            t.set("Image", 280);
            t.set("HPS", 2);
            t.set("RES:water",-10);
            t.set("ValueBase", 450);
            t.set("ItemWeight", 200);
            t.set("Frequency", 60);
            t.set("ASCII","?");
            Lib.add(t);
            
            
            initNotes();
            initSpellScrolls();
            initSpecialScrolls();
        }
        
        private static void addScroll(Thing t) {
        	Lib.add(t);
        }
        
        public static Thing note(String note, String text) {
        	Thing t=Lib.create(note);
        	t.set("Text",text);
        	return t;
        }
        
        private static void initNotes() {
        	Thing t;
        	
        	t=Lib.extend("base note","base scroll");
        	t.set("IsMagicItem",0);
        	t.set("IsNote",1);
        	t.set("OnRead",new Script() {
        		private static final long serialVersionUID = 3256722862197913397L;

                public boolean handle(Thing t,Event e) {
        			Game.warn("note read script:");
        			if (e.getThing("Reader")!=Game.hero()) return false;
        			
        			String text=t.getString("Text");
        			Game.warn(text);
        			Game.infoScreen(text);
        		
        			return false;
        		}
        	});
        	t.set("OnDeath",null);
        	t.set("ValueBase",1);
        	t.set("ItemWeight",100);
        	t.set("LevelMin",1);
        	t.set("Frequency",0);
        	Lib.add(t);
        
        	t=Lib.extend("scribbled note","base note");
        	t.set("Text","This note is blank");
        	Lib.add(t);
        	
        	t=Lib.extend("note","base note");
        	t.set("Text","Reminder: feed wood urchins");
        	Lib.add(t);
        	
        	t=Lib.extend("pamphlet","base note");
        	t.set("Text","Visit Rusty's Potions for bargain prices!");
        	Lib.add(t);
        }
        
        private static void addSpecialScroll(Thing t) {
            ArrayList ta=new ArrayList();
            for (int i=0;i<specialtitles.length;i++) {
                ta.add(specialtitles[i]);
            }        	
        	
            String title=(String)ta.remove(RPG.r(ta.size()));
            t.set("UName","weird scroll titled \""+title+"\"");
            t.set("UNamePlural","weird scrolls titled \""+title+"\"");
            addScroll(t);
        }
        
        public static void initSpellScrolls() {
            // assemble list of titles
            ArrayList ta=new ArrayList();
            for (int i=0;i<titles.length;i++) {
                ta.add(titles[i]);
            }
            
            // assemble list of spells
            ArrayList sa=Spell.getSpellNames();
            
            int ns=sa.size();
            
            if (ns>ta.size()) {
            	Game.warn("Not enough scroll titles!!");
            }
            
            for (int i=0; i<ns; i++) {
                String spellName=(String)sa.get(i);
                
                Thing spell=Spell.create(spellName);
                
                String title=(String)ta.remove(RPG.r(ta.size()));
                Thing t=Lib.extend("xxx scroll","base scroll");
                t.set("Name","scroll of "+spellName);
                t.set("Image",280+RPG.r(4));
                t.set("NamePlural","scrolls of "+spellName);
                t.set("UName","scroll titled \""+title+"\"");
                t.set("UNamePlural","scrolls titled \""+title+"\"");
                t.set("ScrollSpell",spellName);
                t.set("Frequency",(RPG.d(2,100)*spell.getStat("Frequency"))/100);
                if (spellName.equals("Identify")) {
                	t.set("Frequency",100);
                	t.set("IsIdentifyScroll",1);
                }
                
                //	TODO: make spell specific
                t.set("ScrollPower",10);
                t.set("LevelMin",RPG.max(1,spell.getLevel()-10));
             
                addScroll(t);
            }
        }

        public static void initSpecialScrolls() {
        	Thing t;
        	
        	t=Lib.extend("scroll of amnesia","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3761967168350336050L;

                public boolean handle(Thing t, Event e) {
            		t.remove(1);
            		Thing r=e.getThing("Reader");
            		r.message("Your head spins... where are you?");
            		LevelMap.forget(r.getMap(),80);
            		return true;
            	}
            });           	
            t.set("LevelMin",1);
        	addSpecialScroll(t);

        	t=Lib.extend("scroll of mapping","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3760560888616269105L;

                public boolean handle(Thing t, Event e) {
            		Thing r=e.getThing("Reader");
            		r.message("You suddenly know this place like the back of your hand");
            		LevelMap.reveal(r.getMap());
            		t.remove(1);
            		return true;
            	}
            });           	
            t.set("LevelMin",1);
        	addSpecialScroll(t);
        	
        	t=Lib.extend("scroll of item destruction","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3763091964450451513L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
            		Thing[] its=r.getItems();
            		if (its.length==0) {
            			Game.messageTyrant("Your head hurts for a second");
            			return true;
            		}
            		Thing it=its[RPG.r(its.length)];
            		
            		if (t.getFlag("IsBlessed")) it=Game.selectItem("Select an item to target: ",its);
            		if (it==null) return false;
            		
            		if (!it.getFlag("IsArtifact")) {
            			it.die();
            		} else {
            			r.message(it.getTheName()+" shudders for a second");
            		}
            		return true;
            	}
            });
            t.set("LevelMin",1);
        	addSpecialScroll(t);
        	
        	t=Lib.extend("scroll of great identification","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3834315042081354553L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
            		Thing[] its=r.getItems();
            		for (int i=0; i<its.length; i++) {
            			Item.identify(its[i]);
            		}
            		Game.messageTyrant("Your mind is filled with great knowledge about all your posessions");
            		return true;
            	}
            });
            t.set("ValueBase",5000);
            t.set("LevelMin",1);
        	t.set("IsIdentifyScroll",1);
        	addSpecialScroll(t);
        	
        	t=Lib.extend("scroll of item redistribution","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3257571710911001395L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
               		BattleMap m=r.getMap();
            		Thing[] its=r.getItems();
            		boolean displaced=false;
            		for (int i=0; i<its.length; i++) {
            			if ((!t.getFlag("IsCursed"))&&(RPG.d(2)==1)) continue;
            		
            			if ((t.getFlag("IsBlessed"))||(!its[i].getFlag("IsCursed"))) {
            				m.addThing(its[i]);
            				displaced=true;
            			}
            			
            		}
            		if (displaced) {
            			Game.messageTyrant("Some of your posessions have been teleported away");
            		} else {
            			Game.messageTyrant("You feel a strange force around you... but it passes");
            		}
            		return true;
            	}
            });
            t.set("LevelMin",4);
        	addSpecialScroll(t);
        	
        	t=Lib.extend("scroll of total identification","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3761409733168806195L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
            		Thing[] its=r.getItems();
            		Game.messageTyrant("Your mind is filled with great knowledge about all your posessions");
            		for (int i=0; i<its.length; i++) {
            			Item.identify(its[i]);
            		}
            		return true;
            	}
            });
            t.set("LevelMin",10);
        	t.set("IsIdentifyScroll",1);
            t.set("ValueBase",5000);
        	addSpecialScroll(t);
        	
           	t=Lib.extend("scroll of curse detection","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3257290244557582388L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
            		Thing[] its=r.getItems();
            		boolean detected=false;
            		for (int i=0; i<its.length; i++) {
            			Thing it=its[i];
            			if (it.getFlag("IsCursed")) {
            				it.set("IsStatusKnown",1);
            				detected=true;
            			}
            		}
            		if (detected) Game.messageTyrant("You shudder with an uneasy chill");
            		else Game.messageTyrant("You feel a strong sense of relief");
            		return true;
            	}
            });
            t.set("LevelMin",1);
        	addSpecialScroll(t);
        	
           	t=Lib.extend("scroll of item cursing","base scroll");
            t.set("OnRead",new Script() {
            	private static final long serialVersionUID = 3016187993514949388L;

                public boolean handle(Thing t, Event e) {
              		t.remove(1);
               		Thing r=e.getThing("Reader");
            		Thing[] its=r.getItems();
            		boolean cursed=false;
            		for (int i=RPG.d(6); i>0; i--) {
            			Thing it=its[RPG.r(its.length)];
            			if ((RPG.d(4)==1)&&!it.getFlag("IsArtifact")&&!it.getFlag("IsCursed")) {
            				r.message(it.getYourName()+" turns black for a second");
            				it.set("IsCursed",1);
            				it.set("IsBlessed",0);
            				it.set("IsStatusKnown",1);
            				cursed=true;
            			}
            		}
            		if (cursed) Game.messageTyrant("You shudder with an uneasy chill");
            		else Game.messageTyrant("You feel a strong sense of relief");
            		return true;
            	}
            });
            t.set("LevelMin",1);
        	addSpecialScroll(t);
        }
}