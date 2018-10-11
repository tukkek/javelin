package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Debug;
import javelin.controller.action.world.Guide;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.Option;

public class GovernorScreen extends SelectScreen{
	static final String TEMPLATE="Town management:\n\n"//
			+"A town will auto-manage itself but this screen enables you to direct the process.\n"//
			+"Press h to see the description for each project." //
			+"%s\n"//
			+"%s\n"//
			+"Available projects:\n";

	Town town;

	class LaborOption extends Option{
		Labor l;

		public LaborOption(Labor l){
			super(l.name,l.cost);
			this.l=l;
		}
	}

	public GovernorScreen(Town t){
		super("",t);
		town=t;
	}

	@Override
	public String getCurrency(){
		return null;
	}

	@Override
	public String printpriceinfo(Option o){
		LaborOption l=o instanceof LaborOption?(LaborOption)o:null;
		return l==null?"":" ("+l.l.cost+" labor)";
	}

	@Override
	public boolean select(Option o){
		Labor l=((LaborOption)o).l;
		if(Debug.labor){
			l.start();
			while(town.getgovernor().getprojects().contains(l))
				l.work(1);
			return true;
		}
		l.start();
		if(l.closescreen){
			stayopen=false;
			forceclose=true;
		}
		return true;
	}

	@Override
	public List<Option> getoptions(){
		ArrayList<Labor> hand=town.getgovernor().gethand();
		ArrayList<Option> labors=new ArrayList<>();
		if(!hand.isEmpty()) for(int i=0;i<hand.size();i++){
			Labor l=hand.get(i);
			labors.add(new LaborOption(l));
		}
		return labors;
	}

	String printcurrent(ArrayList<Labor> queue){
		String output="\nOngoing projects:\n";
		if(queue.isEmpty())
			output+="  (no ongoing projects)\n";
		else
			for(Labor l:queue)
				output+="  "+l.name+" ("+l.getprogress()+"%)\n";
		return output;
	}

	String printcityinfo(Town t){
		String info="\n\nCity information for "+t.description+":";
		info+="\n  Population: "+t.population+" ("+t.getrank().title+")";
		info+="\n  Traits: ";
		if(t.traits.isEmpty())
			info+="none.";
		else{
			String traits="";
			for(String trait:t.traits)
				traits+=trait+", ";
			info+=traits.substring(0,traits.length()-2)+".";
		}
		float production=t.population*Town.DAILYLABOR;
		info+="\n  Production: "
				+String.format(production>=1?"%1.0f":"%.1f",production)
				+" labor per day";
		return info;
	}

	@Override
	public void printoptions(List<Option> options){
		text=String.format(TEMPLATE,printcityinfo(town),
				printcurrent(town.getgovernor().getprojects()))+"\n";
		if(town.getgovernor().gethand().isEmpty())
			text+="  (no labor projects available right now)\n";
		else
			super.printoptions(options);
	}

	@Override
	protected boolean select(char feedback,List<Option> options){
		if(feedback=='h'){
			Guide.DISTRICT.perform();
			return true;
		}
		return super.select(feedback,options);
	}

	@Override
	public String printinfo(){
		return "";
	}
}