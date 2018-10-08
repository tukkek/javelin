package javelin.view;

import java.awt.Button;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.TextReader;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.MonsterMadness;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.fight.minigame.battlefield.ArmySelectionScreen;
import javelin.controller.fight.minigame.battlefield.BattlefieldFight;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.controller.scenario.artofwar.ArtOfWar;
import javelin.controller.scenario.dungeonworld.DungeonWorld;
import javelin.model.world.World;

public class ScenarioSelectionDialog extends Frame{
	static final Map<String,Scenario> SCENARIOS=new LinkedHashMap<>();
	static final Map<String,Runnable> MINIGAMES=new LinkedHashMap<>();

	static{
		SCENARIOS.put("Campaign",new Campaign());
		SCENARIOS.put("Dungeon world",new DungeonWorld());
		SCENARIOS.put("Art of war",ArtOfWar.singleton);
		MINIGAMES.put("Arena",()->{
			throw new StartBattle(new ArenaFight());
		});
		MINIGAMES.put("Battlefield",()->{
			BattlefieldFight f=new BattlefieldFight();
			if(new ArmySelectionScreen().selectarmy(f)) throw new StartBattle(f);
		});
		MINIGAMES.put("Monster madness",()->{
			throw new StartBattle(new MonsterMadness());
		});
	}

	class Close extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e){
			System.exit(0);
		}
	}

	void draw(){
		setTitle("Welcome to Javelin!");
		setSize(400,400);
		setIconImages(Arrays.asList(Javelin.ICONS));
		addWindowListener(new Close());
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		for(String s:preparetext())
			addline(" "+s);
		Container scenarios=new Container();
		scenarios.setLayout(new BoxLayout(scenarios,BoxLayout.X_AXIS));
		add(scenarios);
		for(String label:SCENARIOS.keySet()){
			final Scenario s=SCENARIOS.get(label);
			addbutton(label,scenarios,e->{
				World.scenario=s;
				dispose();
			});
		}
		add(new Label(" Or a minigame:"));
		Container minigames=new Container();
		minigames.setLayout(new BoxLayout(minigames,BoxLayout.X_AXIS));
		add(minigames);
		for(String label:MINIGAMES.keySet()){
			Runnable action=MINIGAMES.get(label);
			addbutton(label,minigames,e->{
				JavelinApp.minigame=action;
				dispose();
			});
		}
	}

	void addline(String s){
		Label l=new Label();
		l.setText(s);
		add(l);
	}

	ArrayList<String> preparetext(){
		ArrayList<String> lines=new ArrayList<>();
		String file=TextReader.read(new File("doc","scenarios.txt"));
		for(String s:file.trim().split("\n")){
			String line="";
			for(String word:s.split(" ")){
				line+=word+" ";
				if(line.length()>80){
					lines.add(line);
					line="";
				}
			}
			if(line!="") lines.add(line);
		}
		lines.add("");
		lines.add("Select a game mode:");
		return lines;
	}

	void addbutton(String text,Container buttonarea,ActionListener action){
		Button b=new Button(text);
		buttonarea.add(b);
		b.addActionListener(action);
	}

	public static void choose(String[] args){
		if(choosefromcommandline(args)) return;
		ScenarioSelectionDialog s=new ScenarioSelectionDialog();
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
					JavelinApp.minigame=(Runnable)target;
				return true;
			}
		return false;
	}
}
