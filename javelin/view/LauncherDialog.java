package javelin.view;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.TextReader;
import javelin.controller.fight.minigame.CrimsonWar;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.minigame.MonsterMadness;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.controller.fight.minigame.battlefield.Battlefield;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.controller.scenario.artofwar.ArtOfWar;
import javelin.controller.scenario.dungeondelve.DungeonDelve;
import javelin.controller.scenario.dungeonworld.DungeonWorld;
import javelin.model.world.World;

/**
 * First dialog the player sees, allowing him to start or load a
 * {@link Scenario} or play a {@link Minigame}.
 *
 * @author alex
 */
public class LauncherDialog extends JFrame{
	static final Map<String,Class<? extends Scenario>> SCENARIOS=new LinkedHashMap<>();
	static final Map<String,Class<? extends Minigame>> MINIGAMES=new LinkedHashMap<>();

	static{
		SCENARIOS.put("Campaign",Campaign.class);
		SCENARIOS.put("Dungeon delve",DungeonDelve.class);
		SCENARIOS.put("Dungeon world",DungeonWorld.class);
		SCENARIOS.put("Art of war",ArtOfWar.class);
		MINIGAMES.put("Arena",Arena.class);
		MINIGAMES.put("Battlefield",Battlefield.class);
		MINIGAMES.put("Crimson war",CrimsonWar.class);
		MINIGAMES.put("Monster madness",MonsterMadness.class);
	}

	class RunScenario implements ActionListener{
		final String label;

		RunScenario(String label){
			this.label=label;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			try{
				run();
				dispose();
			}catch(ReflectiveOperationException e1){
				throw new RuntimeException(e1);
			}
		}

		void run() throws ReflectiveOperationException{
			World.scenario=SCENARIOS.get(label).getConstructor().newInstance();
		}
	}

	class RunMinigame extends RunScenario{
		RunMinigame(String label){
			super(label);
		}

		@Override
		void run() throws ReflectiveOperationException{
			JavelinApp.minigame=MINIGAMES.get(label).getConstructor().newInstance();
		}
	}

	class Close extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e){
			System.exit(0);
		}
	}

	JPanel north=new JPanel();
	JPanel center=new JPanel();
	JPanel south=new JPanel();

	void draw(){
		setTitle("Welcome to Javelin!");
		setIconImages(Arrays.asList(Javelin.ICONS));
		addWindowListener(new Close());
		setPreferredSize(new Dimension(600,500));
		setLayout(new BorderLayout(10,10));
		north.setLayout(new BoxLayout(north,BoxLayout.X_AXIS));
		add(north,BorderLayout.NORTH);
		BoxLayout mgr=new BoxLayout(center,BoxLayout.Y_AXIS);
		center.setLayout(mgr);
		add(center,BorderLayout.CENTER);
		south.setLayout(new BoxLayout(south,BoxLayout.X_AXIS));
		add(south,BorderLayout.SOUTH);
		addmodetoggle();
		addlogo();
		printmodes(SCENARIOS);
	}

	void addmodetoggle(){
		addbutton("Scenarios",north,e->printmodes(SCENARIOS));
		addbutton("Minigames",north,e->printmodes(MINIGAMES));
	}

	void printmodes(Map<String,?> modes){
		center.removeAll();
		south.removeAll();
		String filename=modes==SCENARIOS?"scenarios":"minigames";
		JLabel text=write(TextReader.read(new File("doc",filename+".txt")).trim()
				.replaceAll("\n","<br>"));
		center.add(text);
		for(String mode:modes.keySet())
			addbutton(mode,south,
					modes==SCENARIOS?new RunScenario(mode):new RunMinigame(mode));
		pack();
		setLocationRelativeTo(null);
		center.repaint();
		south.repaint();
	}

	void addlogo(){
		Image i=Images.get("javelin");
		Canvas c=new Canvas(){
			@Override
			public void paint(Graphics g){
				super.paint(g);
				g.drawImage(i,10,10,null);
			}
		};
		c.setSize(i.getWidth(null)+10,i.getHeight(null)+10);
		add(c,BorderLayout.WEST);
	}

	JLabel write(String s){
		JLabel l=new JLabel();
		l.setText("<html>"+s+"<html>");
		//		l.setMaximumSize(new Dimension(400,1000));
		return l;
	}

	void addbutton(String text,Container buttonarea,ActionListener action){
		Button b=new Button(text);
		buttonarea.add(b);
		b.addActionListener(action);
	}

	public static void choose(String[] args){
		if(choosefromcommandline(args)) return;
		LauncherDialog s=new LauncherDialog();
		s.draw();
		s.pack();
		s.setLocationRelativeTo(null);
		s.setVisible(true);
		while(s.isVisible())
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}
	}

	static boolean choosefromcommandline(String[] args){
		if(args.length==0) return false;
		Map<String,Object> scenarios=new HashMap<>(SCENARIOS);
		scenarios.putAll(MINIGAMES);
		for(String mode:scenarios.keySet())
			if(args[0].compareToIgnoreCase(mode.replaceAll(" ",""))==0){
				Object target=scenarios.get(mode);
				if(target instanceof Scenario)
					World.scenario=(Scenario)target;
				else
					JavelinApp.minigame=(Minigame)target;
				return true;
			}
		return false;
	}
}
