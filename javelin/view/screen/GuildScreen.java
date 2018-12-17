package javelin.view.screen;

import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.Guild;
import javelin.view.screen.upgrading.AcademyScreen;

public class GuildScreen extends AcademyScreen{
	class Hire extends Option{
		Combatant c;

		public Hire(Combatant c,int xpcost){
			super("Recruit: "+c.toString().toLowerCase()+" ("+xpcost+"XP)",0);
			this.c=c;
			price=xpcost;
			priority=3;
		}

		public Hire(Combatant c){
			this(c,Math.round(c.source.cr*100));
		}
	}

	Guild g;

	public GuildScreen(Guild g){
		super(g,null);
		this.g=g;
		showmoneyinfo=false;
	}

	@Override
	public List<Option> getoptions(){
		List<Option> options=super.getoptions();
		for(Combatant c:g.gethires())
			options.add(createoption(c));
		return options;
	}

	Hire createoption(Combatant c){
		Hire h=new Hire(c);
		h.price=c.pay();
		h.name="Hire: "+c.toString().toLowerCase()+" ($"+Javelin.format(h.price)
				+"/day)";
		return h;
	}

	@Override
	public boolean select(Option op){
		if(op instanceof Hire){
			Hire h=(Hire)op;
			if(canafford(h)){
				spend(h);
				h.c.setmercenary(true);
				g.clearhire(h.c);
				Squad.active.add(h.c);
				return true;
			}
			print(text+"\nCan't afford it...");
			return false;
		}
		return super.select(op);
	}

	void spend(Hire h){
		Squad.active.gold-=h.price;
	}

	boolean canafford(Hire h){
		return Squad.active.gold>=h.price;
	}
}