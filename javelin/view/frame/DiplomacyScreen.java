package javelin.view.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;

/**
 * @see Diplomacy
 * @author alex
 */
public class DiplomacyScreen extends Frame{
	private static final int SPACING=5;
	HashMap<Town,Relationship> towns;
	List<Town> townsbyname;
	Diplomacy d=Diplomacy.instance;

	/** Constructor. */
	DiplomacyScreen(){
		super("Diplomacy");
		towns=Diplomacy.instance.getdiscovered();
		townsbyname=new ArrayList<>(towns.keySet());
		townsbyname.sort((a,b)->a.description.compareTo(b.description));
	}

	@Override
	protected Container generate(){
		var c=new Container();
		c.setLayout(new GridLayout(0,1));
		var towns=new JPanel();
		c.add(towns);
		towns.setLayout(new GridLayout(1,0));
		for(var t:townsbyname)
			towns.add(draw(t,this.towns.get(t)));
		var actions=new JPanel();
		actions.setLayout(new GridLayout(0,1));
		c.add(actions);
		boolean enabled=d.reputation>=Diplomacy.TRIGGER;
		showactions(actions,enabled);
		if(!enabled) showprogress(actions);
		return c;
	}

	void showactions(JPanel p,boolean enabled){
		d.validate();
		if(enabled&&d.hand.isEmpty()){
			var b=new JButton("No possible actions to perform at this time...");
			b.addActionListener(e->frame.dispose());
			p.add(b);
			return;
		}
		for(var card:d.hand){
			var b=new JButton(card.name);
			b.setEnabled(enabled);
			b.addActionListener(e->{
				frame.dispose();
				d.hand.remove(card);
				d.reputation=0;
				BattleScreen.perform(()->card.act(d));
			});
			p.add(b);
		}
	}

	void showprogress(JPanel p){
		var progress=Diplomacy.getdailyprogress(false);
		var estimate="";
		if(progress!=0){
			var left=Diplomacy.TRIGGER-d.reputation;
			var eta=left/progress;
			estimate=" (next action estimate: "+Math.max(eta,1)+" days)";
		}
		var b=new JButton(
				"Gaining around "+progress+" reputation daily"+estimate+".");
		b.addActionListener(e->frame.dispose());
		b.setMaximumSize(new Dimension(1000,20));
		p.add(b);
	}

	Component draw(Town t,Relationship r){
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
		info.add(r.describestatus());
		info.add(r.describealignment());
		if(t.ishostile()){
			var l=new JLabel("Hostile ("+Difficulty.describe(t.garrison)+")");
			l.setForeground(Color.RED);
			info.add(l);
		}else{
			var mood=new JLabel("Mood: "+t.describehappiness().toLowerCase());
			mood.setToolTipText(
					t.generatereputation()+" reputation/day (on average)");
			info.add(mood);
			var resources=new JLabel(t.resources.size()+" resource(s)");
			if(!t.resources.isEmpty()){
				var tooltip=t.resources.stream().map(resource->resource.toString())
						.collect(Collectors.joining(", "));
				resources.setToolTipText(Javelin.capitalize(tooltip));
			}
			info.add(resources);
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
