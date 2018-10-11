package javelin.model.world.location.unique;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/**
 * Applies class kits to low-level player units. A kit is usually a set of 3 to
 * 5 upgrades that can be applied randomly to ensure progression up to level
 * challenge rating 2.
 *
 * @author alex
 */
public class AdventurersGuild extends UniqueLocation{

	private static final String TITLE="Adventurers guild";

	private static final float TARGETLEVEL=2;
	static final String[] TITLES=new String[]{"underlings","teachers","masters",
			"legends"};
	static final char[] KEYS="1234567890abcdefghijklnoprsuvxyz/*-+.?!@#$%&()_=[]{}<>;:\"\\|"
			.toCharArray();

	transient List<Kit> selection=null;
	transient ArrayList<Combatant> students;

	/** Constructor. */
	public AdventurersGuild(){
		super(TITLE,TITLE,1,1);
		vision=2;
	}

	@Override
	protected void generategarrison(int minlevel,int maxlevel){
		// clear
	}

	@Override
	public boolean interact(){
		selection=null;
		students=new ArrayList<>();
		float mostpowerful=filterstudents(students);
		float salary=pay(rank(mostpowerful),30,Squad.active.members);
		InfoScreen screen=new InfoScreen("");
		Character input='a';
		while(input!='q'){
			screen.print(show(students,mostpowerful,salary));
			input=screen.getInput();
			int index=indexof(input);
			if(0<=index&&index<students.size()){
				change(index);
				continue;
			}
			if(input=='w'){
				Squad.active.hourselapsed+=24*5;
				Squad.active.gold+=salary/7;
				return true;
			}
			if(input=='m'){
				Squad.active.hourselapsed+=24*30;
				Squad.active.gold+=Math.round(salary);
				return true;
			}
			if(input=='t'){
				train(students);
				return true;
			}
		}
		return true;
	}

	private int indexof(Character input){
		for(int i=0;i<KEYS.length;i++)
			if(input.equals(KEYS[i])) return i;
		return -1;
	}

	public float filterstudents(ArrayList<Combatant> students){
		float mostpowerful=0;
		for(Combatant c:Squad.active.members){
			if(c.mercenary) continue;
			float cr=ChallengeCalculator.calculatecr(c.source);
			if(cr<TARGETLEVEL&&c.xp.floatValue()>=0) students.add(c);
			mostpowerful=Math.max(mostpowerful,cr);
		}
		return mostpowerful;
	}

	void change(int index){
		Kit current=selection.get(index);
		List<Kit> possible=getcourses(index);
		final int to=1+(current==null?-1:possible.indexOf(current));
		selection.set(index,to>=possible.size()?null:possible.get(to));
	}

	List<Kit> getcourses(int index){
		return Kit.getpreferred(students.get(index).source);
	}

	String show(ArrayList<Combatant> students,float mostpowerful,float pay){
		String t="You enter a tall building where people are training and studying in several large rooms.\n\n";
		if(!students.isEmpty()){
			while(students.size()>=10)
				students.remove(0);
			if(selection==null){
				selection=new ArrayList<>(students.size());
				for(int i=0;i<students.size();i++)
					selection.add(RPG.pick(getcourses(i)));
			}
			for(int i=0;i<students.size();i++){
				Kit k=selection.get(i);
				t+=KEYS[i]+" - "+students.get(i)+" ("
						+(k==null?"don't study":k.toString())+")\n";
			}
			t+="\nPress the respective number to change careers.\n\n";
			t+="t - begin training\n";
		}
		t+="w - work as "+TITLES[rank(mostpowerful)]+" for a week ($"
				+Javelin.format(pay/7)+" minus expenses)\n";
		t+="m - work as "+TITLES[rank(mostpowerful)]+" for a month ($"
				+Javelin.format(Math.round(pay))+" minus expenses)\n";
		t+="q - quit";
		return t;
	}

	void train(ArrayList<Combatant> students){
		if(!validateselection(students)) return;
		Squad.active.hourselapsed+=24*7;
		for(int i=0;i<students.size();i++){
			Kit kit=selection.get(i);
			if(kit==null) continue;
			Combatant student=students.get(i);
			String training=student+" learns:\n\n";
			float cr=ChallengeCalculator.calculatecr(student.source);
			float original=cr;
			ClassLevelUpgrade classlevel=null;
			ArrayList<Upgrade> upgrades=new ArrayList<>(kit.basic);
			while(cr<TARGETLEVEL){
				Upgrade u=RPG.pick(upgrades);
				if(u.upgrade(student)) training+=u.name+"\n";
				cr=ChallengeCalculator.calculatecr(student.source);
				if(u instanceof ClassLevelUpgrade) classlevel=(ClassLevelUpgrade)u;
			}
			student.xp=student.xp.subtract(new BigDecimal(cr-original));
			training+="\nPress ENTER to continue...";
			InfoScreen screen=new InfoScreen(training);
			Javelin.app.switchScreen(screen);
			while(screen.getInput()!='\n'){
				// wait
			}
			student.postupgrade(classlevel);
		}
	}

	public boolean validateselection(ArrayList<Combatant> students){
		for(int i=0;i<students.size();i++)
			if(selection.get(i)!=null) return true;
		return false;
	}

	/**
	 * Calculates a paycheck that is twice the food cost for a character per day.
	 *
	 * @param bonus Each unit here means an entire dayworth of food bonus to the
	 *          paycheck per day.
	 * @param days This method does not update {@link Squad#hourselapsed}.
	 * @return Amount in gold pieces ($).
	 */
	public static float pay(int bonus,float days,List<Combatant> workers){
		float pay=0;
		for(Combatant c:workers)
			pay+=c.source.size()*(2+bonus)*days;
		return pay;
	}

	int rank(float cr){
		if(cr<5) return 0;
		if(cr<10) return 1;
		if(cr<15) return 2;
		return 3;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}
}
