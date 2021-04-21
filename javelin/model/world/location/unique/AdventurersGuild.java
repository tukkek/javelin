package javelin.model.world.location.unique;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
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
	static final String TITLE="Adventurers guild";
	static final float TARGETLEVEL=2;

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
			input=screen.getinput();
			int index=indexof(input);
			if(0<=index&&index<students.size()){
				change(index);
				continue;
			}
			if(input=='w'){
				Squad.active.delay(24*5);
				Squad.active.gold+=salary/7;
				return true;
			}
			if(input=='m'){
				Squad.active.delay(24*30);
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

	float filterstudents(ArrayList<Combatant> students){
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
		return Kit.getpreferred(students.get(index).source,false);
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
						+(k==null?"don't study":k.toString().toLowerCase())+")\n";
			}
			t+="\nPress the respective number to change careers.\n\n";
			t+="t - begin training\n";
		}
		t+="w - work as "+TITLES[rank(mostpowerful)]+" for a week ($"
				+Javelin.format(pay/7)+" minus living expenses)\n";
		t+="m - work as "+TITLES[rank(mostpowerful)]+" for a month ($"
				+Javelin.format(Math.round(pay))+" minus living expenses)\n";
		t+="q - quit";
		return t;
	}

	void train(ArrayList<Combatant> students){
		if(!validateselection(students)) return;
		Squad.active.delay(24*7);
		for(int i=0;i<students.size();i++){
			Kit kit=selection.get(i);
			if(kit==null) continue;
			Combatant student=students.get(i);
			train(student,kit.basic,1);
		}
	}

	/**
	 * Will also show a summary screen after done.
	 *
	 * @param upgrades Randomly applies these upgrades to the given
	 *          {@link Combatant}, until all possibilities are exhausted (if any).
	 * @param xp How much experience to spend at most. Will also be subtracted
	 *          from {@link Combatant#xp}.
	 * @return <code>true</code> if at least one {@link Upgrade} has been applied.
	 */
	static public boolean train(Combatant student,Collection<Upgrade> upgrades,
			float xp){
		var learned=new ArrayList<Upgrade>();
		while(xp>0){
			Upgrade applied=null;
			Float cost=null;
			for(var upgrade:RPG.shuffle(new ArrayList<>(upgrades))){
				cost=upgrade.getcost(student);
				if(cost!=null&&0<cost&&cost<=xp){
					applied=upgrade;
					break;
				}
			}
			if(applied==null||cost==null) break;
			applied.upgrade(student);
			learned.add(applied);
			student.xp=student.xp.subtract(new BigDecimal(cost));
			xp-=cost;
		}
		var haslearned=!learned.isEmpty();
		if(haslearned){
			student.postupgrade();
			ChallengeCalculator.calculatecr(student.source);
		}
		printresult(student,learned);
		return haslearned;
	}

	static void printresult(Combatant student,ArrayList<Upgrade> learned){
		String training;
		if(learned.isEmpty())
			training=student+" was unable to learn anything at this time...\n";
		else{
			training=student+" learns:\n\n";
			learned.sort((a,b)->a.getname().compareTo(b.getname()));
			for(Upgrade u:learned)
				training+=u.getname()+"\n";
		}
		training+="\nPress ENTER to continue...";
		InfoScreen screen=new InfoScreen(training);
		Javelin.app.switchScreen(screen);
		while(screen.getinput()!='\n')
			continue;
	}

	boolean validateselection(ArrayList<Combatant> students){
		for(int i=0;i<students.size();i++)
			if(selection.get(i)!=null) return true;
		return false;
	}

	/**
	 * Calculates a paycheck that is twice the food cost for a character per day.
	 *
	 * @param bonus Each unit here means an entire dayworth of food bonus to the
	 *          paycheck per day.
	 * @param days This method does not update {@link Squad#time}.
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
