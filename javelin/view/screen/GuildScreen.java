package javelin.view.screen;

import java.util.List;

import javelin.Javelin;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Guild;
import javelin.model.world.location.haunt.Haunt;
import javelin.view.screen.upgrading.AcademyScreen;

/** @see Guild */
public class GuildScreen extends AcademyScreen{
	class Hire extends Option{
		boolean mercenary=true;
		Combatant c;

		public Hire(Combatant c){
			super("Hire: "+c.toString().toLowerCase()+" ($"+Javelin.format(c.pay())
					+"/day)",c.pay());
			this.c=c;
			priority=3;
		}

		public boolean pay(){
			if(Squad.active.gold<price) return false;
			Squad.active.gold-=price;
			return true;
		}
	}

	class Recruit extends Hire{
		public Recruit(Combatant c){
			super(c);
			price=Haunt.getjoinfee(c.source);
			name="Recruit: "+c.toString().toLowerCase();
			var price=Math.round(this.price);
			name+=" ("+price+" "+(price==1?"ruby":"rubies")+")";
			mercenary=false;
			priority=4;
		}

		@Override
		public boolean pay(){
			return Squad.active.equipment.pay(Ruby.class,Math.round(price));
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
		var options=super.getoptions();
		for(var c:g.gethires()){
			options.add(new Hire(c));
			options.add(new Recruit(c));
		}
		return options;
	}

	@Override
	public boolean select(Option op){
		if(op instanceof Hire){
			Hire h=(Hire)op;
			if(!h.pay()){
				print(text+"\nCan't afford it...");
				return false;
			}
			if(h.mercenary) h.c.setmercenary(true);
			g.clearhire(h.c);
			Squad.active.add(h.c);
			return true;
		}
		return super.select(op);
	}
}