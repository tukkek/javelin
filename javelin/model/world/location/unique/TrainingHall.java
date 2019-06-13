package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.TrainingSession;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.view.screen.SquadScreen;

/**
 * Designed as a manner of making the lower levels faster, this receives the
 * amount of treasure in gold as a fee and offers a fight which, if won, will
 * offer a free feat upgrade. There are 4 levels and each is a difficult fight
 * for parties of 4 of the same level. The encounter levels are: 2, 7, 10, 17.
 *
 * Inspired by the tower in WUtai on Final Fantasy VII.
 *
 * @author alex
 */
public class TrainingHall extends UniqueLocation{
	static final boolean DEBUG=false;
	private static final String DESCRIPTION="The Training Hall";
	public final static ArrayList<Monster> CANDIDATES=new ArrayList<>();

	static{
		for(Monster sensei:SquadScreen.CANDIDATES)
			if(sensei.think(0)) CANDIDATES.add(sensei);
	}
	/**
	 * Actual encounter level for each {@link TrainingSession}, by using the
	 * shifted-to-zero index. Supposed to be difficult (EL-1) encounter party of
	 * levels 1 to 4.
	 */
	public static int[] EL=new int[]{4,8,10,12};
	/**
	 * From 1 upwards, the current training session difficulty, supposed to
	 * represent different floors.
	 *
	 * TODO this is being initialized in 2 places
	 */
	public Integer currentlevel=1;

	/** Constructor. */
	public TrainingHall(){
		super(DESCRIPTION,DESCRIPTION,0,0);
		discard=false;
		impermeable=true;
		vision=2;
		allowedinscenario=false;
		realm=null;
	}

	@Override
	protected void generategarrison(int minel,int maxel){
		if(currentlevel==null) currentlevel=1;
		int nstudents=Squad.active.members.size();
		int nsenseis=RPG.r(Math.min(3,nstudents),Math.max(nstudents,5));
		while(isweak()&&garrison.size()<nsenseis)
			garrison.add(new Combatant(RPG.pick(CANDIDATES).clone(),true));
		while(isweak())
			garrison.getweakest().upgrade();
	}

	boolean isweak(){
		return ChallengeCalculator.calculateel(garrison)<EL[currentlevel-1];
	}

	@Override
	public boolean interact(){
		var price=getfee();
		if(price>Squad.active.gold){
			var m="Not enough money to pay the training fee ($"+Javelin.format(price)
					+")...";
			Javelin.message(m,false);
			return false;
		}
		var prompt="Do you want to pay a fee of $"+Javelin.format(price)
				+" for a lesson?\n"+"Press s to study or any other key to leave...";
		if(Javelin.prompt(prompt)!='s') return false;
		Squad.active.gold-=price;
		throw new StartBattle(new TrainingSession(this));
	}

	public int getfee(){
		return Javelin.round(RewardCalculator.receivegold(garrison));
	}

	@Override
	public boolean drawgarisson(){
		return false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	/** Bumps the {@link #currentlevel}. */
	public void level(){
		currentlevel+=1;
		boolean done=currentlevel-1>=EL.length;
		String prefix;
		if(done)
			prefix="This has been the final lesson.\n\n";
		else
			prefix="Congratulations, you've graduated this level!\n\n";
		Combatant student=Squad.active.members
				.get(Javelin.choose(prefix+"Which student will learn a feat?",
						Squad.active.members,true,true));
		var feats=Upgrade.getall(FeatUpgrade.class);
		ArrayList<FeatUpgrade> options=new ArrayList<>();
		while(options.size()<3&&!feats.isEmpty()){
			FeatUpgrade f=RPG.pick(feats);
			feats.remove(f);
			if(!options.contains(f)&&f.upgrade(student.clone().clonesource()))
				options.add(f);
		}
		options.get(Javelin.choose("Learn which feat?",options,true,true))
				.upgrade(student);
		if(done)
			remove();
		else
			generategarrison(0,0);
	}

	@Override
	public String toString(){
		return super.toString()/*+" ($"+Javelin.format(getfee())+")"*/;
	}

	@Override
	public int watch(){
		return vision;
	}
}
