package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.diplomacy.mandate.Mandate;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 *
 * @author alex
 */
public class TownScreen extends PurchaseScreen{
	static final Option SETTLE=new Option("Settle worker",0,'s');
	static final boolean DEBUGMANAGEMENT=false;
	static final Option RENAME=new Option("Rename town",0,'r');
	static final Option TREATISE=new Option("Claim treaty",0,'t');

	class Manage extends ScreenOption{
		public Manage(Town town){
			super("Manage town",town,'m');
		}

		@Override
		public SelectScreen getscreen(){
			return new GovernorScreen(t);
		}
	}

	/** Constructor. */
	public TownScreen(final Town t){
		super(title(t),t);
	}

	static String title(final Actor t){
		return "Welcome to "+t+"!";
	}

	class TreatyOption extends Option{
		Mandate m;

		public TreatyOption(Mandate m,char key){
			super(m.name,0,key);
			this.m=m;
		}
	}

	class SelectTreaty extends SelectScreen{
		List<Option> options;

		public SelectTreaty(){
			super("Select a treaty to implement:",TownScreen.this.town);
			stayopen=false;
			var treaties=town.diplomacy.treaties;
			options=new ArrayList<>(treaties.size());
			var i=0;
			for(var t:treaties)
				options.add(new TreatyOption(t,KEYS[i++]));
		}

		@Override
		public String getCurrency(){
			return "";
		}

		@Override
		public String printinfo(){
			return "";
		}

		@Override
		public String printpriceinfo(Option o){
			return "";
		}

		@Override
		public List<Option> getoptions(){
			return options;
		}

		@Override
		public boolean select(Option o){
			town.diplomacy.enact(((TreatyOption)o).m);
			return true;
		}
	}

	@Override
	public boolean select(final Option o){
		if(o instanceof ScreenOption){
			SelectScreen screen=((ScreenOption)o).getscreen();
			screen.show();
			if(screen.forceclose) stayopen=false;
			return true;
		}
		if(!super.select(o)) return false;
		if(o==RENAME){
			town.rename();
			title=title(town)+"\n\n";
			return true;
		}
		if(o==SETTLE) return retire(town);
		if(o==TREATISE) new SelectTreaty().show();
		stayopen=false;
		return true;
	}

	boolean retire(Town town){
		List<Combatant> retirees=new ArrayList<>();
		for(Combatant c:Squad.active.members)
			if(!c.mercenary) retirees.add(c);
		if(retirees.isEmpty()) return false;
		int choice=Javelin.choose(
				"Which member should retire and become local labor?",retirees,true,
				false);
		if(choice<0) return false;
		Squad.active.remove(retirees.get(choice));
		town.population+=1;
		return true;
	}

	@Override
	public List<Option> getoptions(){
		final ArrayList<Option> list=new ArrayList<>();
		if(World.scenario.labormodifier>0) list.add(new Manage(town));
		list.add(RENAME);
		list.add(SETTLE);
		if(town.diplomacy.claim()) list.add(TREATISE);
		if(town.ishosting())
			list.add(new TournamentScreenOption("Enter tournament",town,'t'));
		return list;
	}

	@Override
	public String printpriceinfo(Option o){
		return o.price>0?super.printpriceinfo(o):"";
	}

	@Override
	public void onexit(){
		Javelin.app.switchScreen(BattleScreen.active);
	}

	@Override
	public String printinfo(){
		var info=new ArrayList<String>(0);
		if(!town.quests.isEmpty()){
			var quests="Active quests:\n";
			quests+=town.quests.stream().sorted((a,b)->a.daysleft-b.daysleft)
					.map(q->"- "+q+", "+q.getdeadline()+", reward: "
							+q.describereward().toLowerCase())
					.collect(Collectors.joining("\n"));
			info.add(quests);
		}
		var r=town.diplomacy;
		if(!r.treaties.isEmpty()){
			var treaties="Available treaties:\n";
			treaties+=r.treaties.parallelStream().map(m->"- "+m)
					.collect(Collectors.joining("\n"));
			treaties+="\n\nReputation: "+r.describestatus().toLowerCase()+".";
			info.add(treaties);
		}
		return String.join("\n\n",info);
	}

	@Override
	protected Comparator<Option> sort(){
		return (o1,o2)->o1.key.compareTo(o2.key);
	}
}
