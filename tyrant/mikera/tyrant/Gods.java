/*
 * Created on 30-Aug-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import java.util.*;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;



public class Gods {
	public static final String YANTHRALL="Yanthrall";
	public static final String ARVALLON="Arvallon";
	public static final String ARAMIS="Aramis";
	public static final String DURVAL="Durval";	
	public static final String ZUROCH="Zuroch";	
	public static final String MOLKOTH="Molkoth";	
	public static final String BZAKKARATH="B'Zakkarath";	
	public static final String TROTHGAR="Trothgar";	
	public static final String NASSARRIK="Nassarrik";	
	public static final String GATHAARL="Gathaarl";
	
	public static Thing get(String s) {
		HashMap h=getGods();
		Thing t=(s==null)?null:(Thing)h.get(s);
		if (t==null) Game.warn("God ["+s+"] does not exist!");
		return t;
	}
	
	public static Thing getGod(Thing being) {
		return get(getGodName(being));
	}
	
	public static String getGodName(Thing being) {
		return being.getString("Religion");
	}
	
	public static HashMap getGods() {
		HashMap h=(HashMap)Lib.instance().getObject("Gods");
		if (h==null) {
			h=new HashMap();
            Lib.instance().set("Gods", h);
//			Game.hero.set("Gods",h);
		}
		return h;
	}
	
	/**
	 * Gets the possible list of gods for a given character
	 * 
	 * @param h
	 * @return
	 */
	public static ArrayList getPossibleGods(Thing h) {
		ArrayList al=new ArrayList();
		
		Iterator it=getGods().keySet().iterator();
		while(it.hasNext()) {
			String s=(String)it.next();
			if (acceptsFollower(s,h)) al.add(s);
		}
		
		return al;
	}
	
	public static void sacrifice(Thing hero, Thing sac) {
		hero.incStat("APS",-100);
		sac.remove();
		
		Thing god=getGod(hero);
		int value=Item.value(sac);
		Game.messageTyrant(sac.getTheName()+" "+sac.is()+" "+god.getString("SacrificeDescription"));
		
		// modify by level difference
		// exact level=100% value
		// each additional level=+20% value
		int heroLevel=hero.getLevel();
		int ld=sac.getLevel()-hero.getLevel();
		ld=RPG.max(0,ld);
		value=(int)(value*(1.0+0.2*ld));
		
		double relativeValue=value/Item.levelValue(heroLevel);
		int points=0;
		if (relativeValue>1.0) points++;
		if (relativeValue>10.0) points++;
		if (relativeValue>100.0) points++;
		if (relativeValue>1000.0) points++;
		if (relativeValue>10000.0) points++;
		
		// See whether god likes this particular sacrifice
		String likes=god.getString("SacrificePreference");
		boolean like=false;
		String[] ss=likes.split(",");
		for (int i=0; i<ss.length; i++) {
			if (sac.getFlag(ss[i])) like=true;
		}
		if (like) {
			points*=3;
		}
		
		if (sac.getFlag("IsArtifact")) {
			Game.messageTyrant("\"Your loyalty shall be rewarded.\"");
			points*=50;
		} else if (points>0) {
			Gods.impress(hero,points);
		} else if (points==0) {
			Game.messageTyrant("You feel that "+god.name()+" is unimpressed by your pathetic offering");
			
		} else if (points<0) {
			Gods.anger(hero,-points);
		}
	}
	
	public static boolean acceptsFollower(String god, Thing h) {
		Thing g=get(god);
		boolean accepts=true;
		
		String ar=g.getString("AcceptableRaces");
		String r=h.getString("Race");
		if (ar.equals("any")||(ar.indexOf(r)>=0)) {
			accepts&=true;
		} else {
			accepts=false;
			//Game.warn(god+" rejects due to race ["+r+"]");
		}
		
		String ap=g.getString("AcceptableProfessions");
		String p=h.getString("Profession");
		if (ap.equals("any")||(ap.indexOf(p)>=0)) {
			accepts&=true;
		} else {
			accepts=false;
			//Game.warn(god+" rejects due to profession ["+r+"]");
		}
		
		int ad=g.getStat("AcceptableAlignmentDifference");
		if (Math.abs(getGoodness(h)-getGoodness(g))>ad) {
			accepts=false;
			//Game.warn(god+" rejects due to alignment ["+r+"]");

		}
		
		return accepts;
	}
	
	private static void addGod(Thing t) {
		getGods().put(t.name(),t);
		Lib.add(t);
		// Game.warn("Adding god - "+t.name());
	}
	
	/* Goodness is in the eye of divinity ;) */
	/* This is code speak to mention that other classes use this method as well */
	public static int getGoodness(Thing t) {
		String al=t.getString("Alignment");
		if( al == null ){
		  return 0;
		}
		char base=al.charAt(0);
		int g=0;
		if (base=='G') g+=3;
		else if (base=='E') g-=3;
		
		if (al.length()>1) {
			char mod=al.charAt(1);
			if (mod=='+') g+=1;
			else if (mod=='-') g-=1;
		}
		
		return g;
	}
	
	public static String getAlignment(int goodness) {
		switch(goodness) {
			case -3: return "E";
			case -2: return "E+";
			case -1: return "N-";
			case 0: return "N";
			case 1: return "N+";
			case 2: return "G-";
			case 3: return "G";
		}
		if (goodness>0) return "G+";
		return "E-";
	}
	
	/**
	 * Call when a being does something that angers his/her god
	 */ 
	public static boolean anger(Thing b, int amount) {
		if (amount<0) amount=0;

		b.incStat("Sin",amount);
		
		b.set(RPG.ST_PEITY,RPG.max(0,b.getStat(RPG.ST_PEITY)-amount));
		
		if (amount>0) {
			b.message("You feel that "+getGodName(b)+" is angry with you");
		}
		
		return (amount>0);
	}

	public static void randomPrayer(Thing h, int level) {
		switch (RPG.d(8)) {
			case 1: Wish.makeWish("identification",level); break;
			case 2: Wish.makeWish("cash",level); break;
			case 3: Wish.makeWish("experience",level); break;
			case 4: Wish.makeWish("[IsMagicItem]",level); break;
			case 5: {
				if (h.getStat(RPG.ST_SKILLPOINTS)>=1) {
					String[] ss=Skill.fullList();
					Wish.makeWish(ss[RPG.r(ss.length)],100); break;
				} 
				// fallback if no skillpoints
				Wish.makeWish("identification",level); break;
			}
			case 6: Wish.makeWish("blessing",level); break;
			case 7: Wish.makeWish("potion of holy water",100); break;
			
			default:
				Wish.makeWish("[IsMagicItem]",level); break;
		}
	}
	
	public static boolean help(Thing h, int level) {
		if (h.getBaseStat(RPG.ST_PEITY)>50) {
			h.incStat(RPG.ST_PEITY,-50);
			if (h.getStat(RPG.ST_HPS)<h.getStat(RPG.ST_HPSMAX)) {
				Wish.makeWish("healing",level);
			} else {
				randomPrayer(h,level);
			}
			return true;
		} else {
			Game.messageTyrant(h.getString("Religion")+" does not seem interested in helping you");
			return false;
		}
		
	}
	
	/**
	 * Basic prayer entry point - called when the hero prays
	 * 
	 * @param h The hero
	 */
	public static void pray(Thing h) {
		int prayer=h.getStat(Skill.PRAYER);
		int peity=h.getStat(RPG.ST_PEITY);
		int level=h.getLevel()+(prayer*peity/100);

		int cost=50/(2+prayer);
		
		if (prayer>0) {
			if (peity>=cost) {				
				if (Gods.doPray(h,level)) {
					Game.messageTyrant("Praise be to "+h.getString("Religion")+"!");
					
					h.incStat(RPG.ST_PEITY,-cost);
					
					if (h.getBaseStat(RPG.ST_PEITY)==0) {
						Game.messageTyrant("The ground rumbles");
					}
				}				
			} else {
				Game.messageTyrant(h.getString("Religion")+" ignores you");
				h.addAttribute(Lib.create("hex"));
			}
		} else {
			help(h,level);
		}
	}
	
	/**
	 * Call when a being does something that angers his/her god
	 */ 
	public static boolean impress(Thing b, int amount) {
		if (amount<0) return anger(b,-amount);

		int increment=0;
		while (amount>0) {
			int peity=b.getStat(RPG.ST_PEITY);
			int prayer=b.getStat(Skill.PRAYER);
			
			if (peity>=100) {
				Game.messageTyrant(getGodName(b)+" remains supremely pleased with you");
				return false;
			}
			
			int pointsToImpress=1+peity;
			pointsToImpress*=(100/(100-peity));
			pointsToImpress/=(1+prayer*prayer);
			pointsToImpress=RPG.max(1,pointsToImpress);
			
			// random chance if amount insufficient
			if (amount<pointsToImpress) {
				amount=(RPG.r(pointsToImpress)<amount)?pointsToImpress:0;
			}
			
			if (amount>=pointsToImpress) {
				increment+=1;
				amount-=pointsToImpress;
				b.incStat(RPG.ST_PEITY,1);
			}
		}
		
		if (increment>0) {
			b.message("You feel that "+getGodName(b)+" is pleased");
			return true;
		}
		
		return false;
	}
	
	public static void checkEat(Thing b, Thing food) {
		Thing god=Gods.getGod(b);
		if (!Food.isVegetarian(food)) {
			int veggie=god.getStat("RequiresVegetarian");
			if (veggie>0) {
				anger(b,veggie);
			}
		}
	}
	
	public static int checkTheft(Thing b) {
		Thing god=getGod(b);
		int goodness=getGoodness(god);
		int likesTheft=god.getStat("LikesTheft");
		
		int amount=0;
		if (goodness>=0) {
			amount=-(goodness+1)*(goodness+2)/2;
		} else if (likesTheft>0) {
			amount=likesTheft;
		} else if (goodness<=-4) {
			amount=1;
		} 
		
		Gods.impress(b,amount);
		return amount;
	}
	
	public static void init() {
		Thing t;
		
		t=Lib.extend("base god","base thing");
		t.set("AcceptableAlignmentDifference",2);
		t.set("SacrificeDescription","consumed by flames");
		t.set("SacrificePreference",null);
		t.set("LevelMin",1);
		Lib.add(t);
		
		t=Lib.extend(ARAMIS,"base god");
		t.set("GodTitle","God of Healing");
		t.set("AcceptableRaces","any");
		t.set("SacrificePreference","IsArmour,IsPotion");
		t.set("AcceptableProfessions","ranger,priest,shaman,healer,paladin");
		t.set("SacrificeDescription","consumed in a brillient light");
		t.set("Alignment","G+");
		t.set("UpbringingText","In accordance with the teachings of Aramis, you learnt to respect all living things and in particular never to eat the flesh of animals.");
		t.set("RequiresVegetarian",1);
		addGod(t);
		
		t=Lib.extend(DURVAL,"base god");
		t.set("GodTitle","God of the Mountains");
		t.set("AcceptableRaces","hobbit,dwarf,gnome,half troll,argonian,hawken");
		t.set("AcceptableProfessions","any");
		t.set("SacrificePreference","IsCoin,IsWeapon");
		t.set("UpbringingText","You learnt of the secret treasures buried in the Deep, which you should always strive to acquire to the greater glory of Durval.");
		t.set("Alignment","N+");
		addGod(t);
		
		t=Lib.extend(ARVALLON,"base god");
		t.set("GodTitle","Benevolent God of Enlightenment");
		t.set("AcceptableRaces","human,high elf,wood elf,pensadorian");
		t.set("AcceptableProfessions","any");
		t.set("SacrificePreference","IsScroll,IsBook");
		t.set("SacrificeDescription","consumed by a blinding light");
		t.set("UpbringingText","You learnt of how Arvallon alone was granted the key to all knowledge by the Creator, and would bestow the greatest of secrets upon His loyal followers.");
		t.set("Alignment","G");
		addGod(t);
		
		t=Lib.extend(YANTHRALL,"base god");
		t.set("GodTitle","Warrior-God of the Daedorian Empire");
		t.set("AcceptableRaces","human,wood elf,hobbit,dwarf,gnome");
		t.set("AcceptableProfessions","any");
		t.set("SacrificePreference","IsWeapon,IsArmour");
		t.set("SacrificeDescription","consumed in an awesome column of roaring fire");
		t.set("UpbringingText","As with all followers of Yanthrall, you came to appreciate that life is a ceaseless battle, and that victory belongs to The Strong and The Just. You dreamed of one day being initiated into the mighty ranks of the Blood Guard.");
		t.set("Alignment","N+");
		addGod(t);
		
		t=Lib.extend(ZUROCH,"base god");
		t.set("GodTitle","God of Prosperity");
		t.set("AcceptableRaces","any");
		t.set("AcceptableProfessions","any");
		t.set("AcceptableAlignmentDifference",3);
		t.set("SacrificeDescription","teleported away");
		t.set("SacrificePreference","IsCoin,IsFood");
		t.set("UpbringingText","You heeded well the command of Zuroch, that His followers should seek to find joy in the accumulation of wealth and the making of sizeable donations to temple coffers.");
		t.set("Alignment","N-");
		addGod(t);
		
		t=Lib.extend(MOLKOTH,"base god");
		t.set("GodTitle","Dark God of the Underworld");
		t.set("AcceptableRaces","human,gnome,dark elf,half troll,half orc,pensadorian");
		t.set("AcceptableProfessions","any");
		t.set("AcceptableAlignmentDifference",3);
		t.set("SacrificePreference","IsPotion,IsWeapon");
		t.set("SacrificeDescription","burnt away by malevolent black fire");
		t.set("UpbringingText","You learnt that all existence will one day be swallowed by Darkness, when Molkoth and His invincible demonic armies of destruction sweep to victory aginst the pathetic Forces of Light. Only those who serve Molkoth will be granted a place in the Afterworld, with the souls of all others destined for eternal pain.");
		t.set("Alignment","E-");
		addGod(t);
		
		t=Lib.extend(BZAKKARATH,"base god");
		t.set("GodTitle","Chaos God of The Underworld");
		t.set("AcceptableRaces","any");
		t.set("AcceptableProfessions","any");
		t.set("SacrificePreference","IsWeapon,IsGem");
		t.set("UpbringingText","You learnt to see beauty in mayhem and destruction, pleasure in pain and rejoice in the relentless spread of Chaos.");
		t.set("Alignment","E");
		addGod(t);
		
		t=Lib.extend(TROTHGAR,"base god");
		t.set("GodTitle","God of The Wild");
		t.set("AcceptableRaces","human,dwarf,dark elf,half orc,half troll,argonian,hawken");
		t.set("AcceptableProfessions","barbarian,thief,fighter,ranger,shaman");
		t.set("SacrificePreference","IsFood,IsPotion");
		t.set("UpbringingText","You learnt of the colossal power of the Spirits of Nature, and that you should offer sacrifices to Trothgar to earn His protection.");
		t.set("Alignment","N-");
		addGod(t);
		
		t=Lib.extend(NASSARRIK,"base god");
		t.set("GodTitle","God of Thieves");
		t.set("AcceptableRaces","any");
		t.set("AcceptableProfessions","barbarian,thief");
		t.set("LikesTheft",1);
		t.set("SacrificePreference","IsCoin,IsPotion");
		t.set("UpbringingText","You learn of the dark cults that pervade the lands of men, and how you should seek to serve them as a loyal follower of Nassarrik.");
		t.set("Alignment","E+");
		addGod(t);
		
		t=Lib.extend(GATHAARL,"base god");
		t.set("GodTitle","Goblin God of Battle");
		t.set("AcceptableRaces","half orc,half troll");
		t.set("AcceptableProfessions","any");
		t.set("SacrificePreference","IsFood,IsWeapon");
		t.set("SacrificeDescription","dissolved in a cloud of green smoke");
		t.set("UpbringingText","You learn to rejoice in the thrill of battle, that you should multiply to cover the earth, and that you should raise great totems to the glory of Gathaarl and the Goblin Spirits.");
		t.set("Alignment","N-");
		addGod(t);
		
	}

	public static boolean doPray(Thing h,int level) {
		String s = Game.getLine("What do you pray for? ");
		s=s.trim();
		if (s.equals("")||s.equals("ESC")) {
			Game.messageTyrant("");
			return false;
		}
		
		if (s.equals("help")) {
			return help(h,level);
		}
		
		return Wish.makeWish(s,level);
	}
}
