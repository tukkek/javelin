package javelin.view.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.Town;

/**
 * Originally the main interface for the {@link Diplomacy} system, now it's just
 * an overview of discovered {@link Town}s.
 *
 * @see Town#getdiscovered()
 * @author alex
 */
public class DiplomacyScreen extends Frame{
	private static final int SPACING=5;
	List<Town> towns;

	/** Constructor. */
	DiplomacyScreen(){
		super("Diplomacy");
		towns=new ArrayList<>(Town.getdiscovered());
		towns.sort((a,b)->a.description.compareTo(b.description));
	}

	@Override
	protected Container generate(){
		var c=new Container();
		c.setLayout(new GridLayout(0,1));
		var towns=new JPanel();
		c.add(towns);
		towns.setLayout(new GridLayout(1,0));
		for(var t:this.towns)
			towns.add(draw(t));
		return c;
	}

	Component draw(Town t){
		var p=new JPanel();
		p.setBackground(Color.LIGHT_GRAY);
		p.setLayout(new GridLayout(0,1));
		p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		var name=new JLabel(t.description.toUpperCase());
		name.setFont(name.getFont().deriveFont(15f));
		name.setHorizontalAlignment(SwingConstants.CENTER);
		var realm=t.realm==null?t.originalrealm:t.realm;
		if(realm!=null) name.setForeground(realm.getawtcolor());
		p.add(space(name));
		var info=new ArrayList<>();
		var rank=new JLabel(t.getrank().toString());
		rank.setToolTipText(t.population+" population");
		info.add(rank);
		info.add(t.diplomacy.describealignment());
		if(t.ishostile()){
			var l=new JLabel("Hostile ("+Difficulty.describe(t.garrison)+")");
			l.setForeground(Color.RED);
			info.add(l);
		}else{
			var mood=new JLabel("Mood: "+t.diplomacy.describestatus().toLowerCase());
			info.add(mood);
			var resources=new JLabel(t.resources.size()+" resource(s)");
			if(!t.resources.isEmpty()){
				var tooltip=t.resources.stream().map(resource->resource.toString())
						.collect(Collectors.joining(", "));
				resources.setToolTipText(Javelin.capitalize(tooltip));
			}
			info.add(resources);
			info.add(t.getweeklylabor(false)+" labor/week");
		}
		for(var trait:t.traits)
			info.add(Javelin.capitalize(trait));
		for(var i:info){
			var l=i instanceof JLabel?(JLabel)i:new JLabel(i.toString());
			space(l);
			p.add(l);
			l.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return p;
	}

	<K extends JComponent> K space(K l){
		l.setBorder(BorderFactory.createEmptyBorder(SPACING,5,5,5));
		return l;
	}

	/** Opens this screen. */
	public static void open(){
		var w=new DiplomacyScreen();
		w.show();
		w.blockbackground();
	}
}
