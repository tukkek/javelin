package javelin.view.screen;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.skill.Skill;

/**
 * Shows unit information.
 *
 * @author alex
 */
public class StatisticsScreen extends InfoScreen{
	static final boolean DEBUG=false;

	/**
	 * @param c Shows statistic for this unit.
	 */
	public StatisticsScreen(Combatant c){
		super("");
		text=gettext(c,true);
		if(updatescreens().equals('v')){
			text="(The text below is taken from the d20 SRD and doesn't necessarily reflect the in-game enemy)\n\n"
					+Javelin.DESCRIPTIONS.get(c.source.name);
			updatescreens();
		}
		Javelin.app.switchScreen(BattleScreen.active);
	}

	/**
	 * @param toggle If <code>true</code> shows instruction on how to toggle more
	 *          information.
	 * @return Information for the given {@link Combatant}.
	 */
	@SuppressWarnings("deprecation")
	static public String gettext(Combatant c,boolean toggle){
		Monster m=c.source;
		ArrayList<String> lines=new ArrayList<>();
		showheader(c,m,lines);
		lines.add(showhp(c,m));
		lines.add("Initiative   "+Skill.getsigned(m.initiative));
		lines.add("Speed        "+showspeed(m));
		lines.add("Armor class  "+alignnumber(c.getac()));
		lines.add("");
		showattacks(m,lines);
		describequalities(c,lines);
		showsaves(m,lines);
		showabilities(m,lines);
		showfeats(m,lines);
		showskills(c,lines);
		if(toggle){
			String describe="Press v to see the monster description, any other key to exit";
			lines.add(describe);
		}
		return String.join("\n",lines);
	}

	static void showheader(Combatant c,Monster m,ArrayList<String> lines){
		String monstername=m.name;
		if(!m.group.isEmpty()) monstername+=" ("+m.group+")";
		lines.add(monstername);
		String type=m.type.toString().replaceAll("_"," ").toLowerCase();
		lines.add(capitalize(Monster.SIZES[m.size])+" "+type);
		lines.add("");
		if(c.mercenary) lines.add("Mercenary ($"+Javelin.format(c.pay())+"/day)");
		lines.add(
				"Challenge rating "+Math.round(ChallengeCalculator.calculatecr(m)));
		for(ClassLevelUpgrade classlevels:ClassLevelUpgrade.classes){
			int level=classlevels.getlevel(m);
			if(level>0) lines.add(classlevels.descriptivename+" level "+level);
		}
		lines.add(m.alignment.toString());
		lines.add("");
	}

	static void showfeats(Monster m,ArrayList<String> lines){
		if(!m.feats.isEmpty()){
			String feats="Feats: ";
			for(javelin.model.unit.feat.Feat f:m.feats)
				feats+=f+", ";
			lines.add(feats.substring(0,feats.length()-2)+".");
			lines.add("");
		}
	}

	static void showabilities(Monster m,ArrayList<String> lines){
		lines.add(printability(m.strength,"Strength"));
		lines.add(printability(m.dexterity,"Dexterity"));
		lines.add(printability(m.constitution,"Constitution"));
		lines.add(printability(m.intelligence,"Intelligence"));
		lines.add(printability(m.wisdom,"Wisdom"));
		lines.add(printability(m.charisma,"Charisma"));
		lines.add("");
	}

	static void showsaves(Monster m,ArrayList<String> lines){
		lines.add("Saving throws");
		lines.add(" Fortitude   "+save(m.fort));
		lines.add(" Reflex      "+save(m.ref));
		lines.add(" Will        "+save(m.will));
		lines.add("");
	}

	static void showattacks(Monster m,ArrayList<String> lines){
		lines.add("Melee attacks");
		listattacks(lines,m.melee);
		lines.add("");
		lines.add("Ranged attacks");
		listattacks(lines,m.ranged);
		lines.add("");
	}

	@SuppressWarnings("unused")
	static String showhp(Combatant c,Monster m){
		boolean isally=Fight.state==null?Squad.active.members.contains(c)
				:Fight.state.blueTeam.contains(c);
		final String hp;
		if(Javelin.DEBUG&&DEBUG)
			hp=Integer.toString(c.hp);
		else if(isally)
			hp=Integer.toString(c.maxhp);
		else
			hp="~"+c.source.hd.average();
		return "Hit dice     "+m.hd+" ("+hp+"hp)";
	}

	static String showspeed(Monster m){
		long speed=m.fly;
		boolean fly=true;
		if(speed==0){
			fly=false;
			speed=m.walk;
		}
		String speedtext=alignnumber(speed)+" feet";
		if(fly) speedtext+=" flying";
		long squares=speed/5;
		speedtext+=" ("+squares+" squares, "
				+String.format("%.2f",ActionCost.MOVE/squares)+"ap per square)";
		if(m.swim>0) speedtext+=", swim "+m.swim+" feet";
		if(m.burrow>0) speedtext+=", burrow "+m.burrow+" feet";
		return speedtext;
	}

	static void showskills(Combatant c,ArrayList<String> lines){
		if(c.source.ranks.isEmpty()) return;
		Monster m=c.source;
		String output="";
		for(Skill s:Skill.ALL){
			if(s.getranks(c)==0) continue;
			String trained=m.trained.contains(s.name)?"":" (untrained)";
			output+=s.name+" "+s.getsignedbonus(c)+trained+", ";
		}
		if(output.isEmpty()) return;
		output=output.substring(0,output.length()-2);
		lines.add("Skills: "+output+".\n");
	}

	private static String save(int x){
		String sign="";
		if(x>=0) sign="+";
		return sign+x;
	}

	static private String printability(int score,String abilityname){
		abilityname+=" ";
		while(abilityname.length()<13)
			abilityname+=" ";
		return abilityname+alignnumber(score)+" ("+Monster.getsignedbonus(score)
				+")";
	}

	static void describequalities(Combatant c,ArrayList<String> lines){
		Monster m=c.source;
		String s=printqualities("Maneuvers",c.disciplines.getmaneuvers());
		ArrayList<String> spells=new ArrayList<>(c.spells.size());
		for(Spell spell:c.spells)
			spells.add(spell.toString());
		s+=printqualities("Spells",spells);
		ArrayList<String> attacks=new ArrayList<>();
		for(BreathWeapon breath:m.breaths)
			attacks.add(breath.toString());
		if(m.touch!=null) attacks.add(m.touch.toString());
		s+=printqualities("Special attacks",attacks);
		ArrayList<String> qualities=new ArrayList<>();
		for(Quality q:Quality.qualities)
			if(q.has(m)){
				String description=q.describe(m);
				if(description!=null) qualities.add(description);
			}
		s+=printqualities("Special qualities",qualities);
		if(!s.isEmpty()) lines.add(s.substring(0,s.length()-1));
	}

	static String printqualities(String header,ArrayList<?> qualities){
		if(qualities.isEmpty()) return "";
		header+=": ";
		qualities.sort(null);
		for(Object quality:qualities)
			header+=quality.toString().toLowerCase()+", ";
		return header.substring(0,header.length()-2)+".\n\n";
	}

	static void listattacks(ArrayList<String> lines,List<AttackSequence> melee){
		if(melee.isEmpty()){
			lines.add(" None");
			return;
		}
		for(AttackSequence sequence:melee)
			lines.add(" "+sequence.toString());
	}

	/**
	 * @return Given {@link String} with only first {@link Character} as
	 *         uppercase.
	 */
	public static String capitalize(String size){
		return Character.toUpperCase(size.charAt(0))
				+size.substring(1).toLowerCase();
	}

	private static String alignnumber(long score){
		return score<10?" "+score:Long.toString(score);
	}

	Character updatescreens(){
		Javelin.app.switchScreen(this);
		return InfoScreen.feedback();
	}
}
