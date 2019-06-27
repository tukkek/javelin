package javelin.view.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.MonstersByName;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.old.RPG;

/**
 * Squad selection screen when starting a new game.
 *
 * @author alex
 */
public class SquadScreen extends InfoScreen{
	public static final ArrayList<Monster> CANDIDATES=new ArrayList<>();
	public static final float[] SELECTABLE={1f,1.25f,1.5f};

	static final String ALPHABET="abcdefghijklmnopqrstuvwxyz";
	static final int MONSTERPERPAGE=ALPHABET.indexOf('y');

	static{
		for(float cr:SELECTABLE){
			List<Monster> tier=Monster.BYCR.get(cr);
			if(tier!=null) for(Monster candidate:tier)
				if(candidate.isalive()) CANDIDATES.add(candidate);
		}
	}

	protected Squad squad=new Squad(0,0,8,null);

	List<Monster> candidates;
	boolean first=true;

	public SquadScreen(List<Monster> candidates){
		super("");
		this.candidates=candidates;
		Collections.sort(candidates,MonstersByName.INSTANCE);
	}

	public SquadScreen(){
		this(CANDIDATES);
	}

	void select(){
		page(0);
		if(World.scenario!=null) World.scenario.upgradesquad(squad);
		squad.gold+=getstartinggold();
		squad.gold=Javelin.round(squad.gold);
		squad.sort();
	}

	public int getstartinggold(){
		int gold=0;
		for(Combatant c:squad.members){
			float level=c.source.cr-1;
			if(level>=1)
				gold+=RewardCalculator.calculatepcequipment(Math.round(level));
		}
		return gold;
	}

	private void page(int index){
		text="Available monsters:\n";
		int next=index+MONSTERPERPAGE;
		int letter=printpage(index,next);
		Javelin.app.switchScreen(this);
		Character input=InfoScreen.feedback();
		if(input.equals(' '))
			page(next<candidates.size()?next:0);
		else if(input=='z'){
			while(!pickrandom())
				continue;
			if(!checkifsquadfull()) page(index);
		}else if(input=='\n'){
			if(squad.members.isEmpty()) page(index);
		}else{
			int selection=ALPHABET.indexOf(input);
			if(selection>=0&&selection<letter){
				recruit(candidates.get(index+selection));
				if(checkifsquadfull()) return;
			}
			page(index);
		}
	}

	boolean pickrandom(){
		Monster candidate=RPG.pick(candidates);
		for(Combatant m:squad.members)
			if(m.source.name.equals(candidate.name)) return false;
		recruit(candidate);
		return true;
	}

	private void recruit(Monster m){
		Combatant c=squad.recruit(m);
		c.hp=c.source.hd.maximize();
		c.maxhp=c.hp;
	}

	protected boolean checkifsquadfull(){
		return World.scenario.checkfullsquad(squad.members);
	}

	int printpage(int index,int next){
		int letter=0;
		for(int i=index;i<next&&i<candidates.size();i++){
			text+="\n"+ALPHABET.charAt(letter)+" - "+candidates.get(i).toString();
			letter+=1;
		}
		text+="\n";
		text+="\nPress letter to select character";
		if(candidates.size()>MONSTERPERPAGE) text+="\nPress SPACE to switch pages";
		text+="\nPress z to pick a random unit";
		text+="\nPress ENTER to coninue with current selection";
		text+="\n";
		text+="\nYour team:";
		text+="\n";
		for(Combatant m:squad.members)
			text+="\n"+m.source.toString();
		return letter;
	}

	public Squad open(){
		select();
		return squad;
	}
}
