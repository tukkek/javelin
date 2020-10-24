package javelin.view.screen;

import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Diplomacy;
import javelin.model.world.location.unique.MercenariesGuild;

/**
 * Uses a {@link InfoScreen} to deal with succesful {@link Diplomacy} checks.
 *
 * @author alex
 */
public class BribingScreen{
	/**
	 * @param foes Opponents.
	 * @param dailyfee For hiring as mercenary (requires passing the DC by 5 or
	 *          more).
	 * @param bribe Price for getting rid of foes peacefully.
	 * @param canhire If <code>false</code> will not show the option to hire as
	 *          mercenary.
	 * @return <code>false</code> in case a battle is started! Doesn't throw
	 *         {@link StartBattle}.
	 */
	public boolean bribe(List<Combatant> foes,int dailyfee,int bribe,
			boolean canhire){
		String text=printdiplomacy(foes,dailyfee,bribe,canhire);
		InfoScreen screen=new InfoScreen("");
		while(true){
			screen.print(text);
			char choice=InfoScreen.feedback();
			if(choice=='f') return false;
			boolean nogold=false;
			if(choice=='b'){
				if(Squad.active.gold>=bribe){
					Squad.active.gold-=bribe;
					return true;
				}
				nogold=true;
			}
			//TODO this isn't taking into account the 10x for Dungeons (because payment is handled internally by #recruit)
			if(canhire&&choice=='h'){
				if(Squad.active.gold>=dailyfee){
					for(Combatant foe:foes)
						MercenariesGuild.recruit(foe,false);
					return true;
				}
				nogold=true;
			}
			if(nogold){
				text+="\nNot enough gold!";
				screen.print(text);
			}
		}
	}

	static String printdiplomacy(List<Combatant> foes,int dailyfee,int bribe,
			boolean canhire){
		String text="You are able to parley with the opponents ("
				+Difficulty.describe(foes)+"):\n\n";
		text+=Javelin.group(foes)+'.';
		text+="\n\nWhat do you want to do? You have $"
				+Javelin.format(Squad.active.gold)+".";
		text+="\n";
		text+="\nf - fight!";
		text+="\nb - bribe them ($"+Javelin.format(bribe)+")";
		if(canhire)
			text+="\nh - hire as mercenaries ($"+Javelin.format(dailyfee)+")";
		text+="\n";
		return text;
	}
}
