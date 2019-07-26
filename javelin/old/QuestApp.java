package javelin.old;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javelin.model.unit.Squad;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.screen.WorldScreen;

public abstract class QuestApp extends Applet implements Runnable{
	public static final Image DEFAULTTEXTURE=Images.get("texture");
	public static final Color TEXTCOLOUR=new Color(192,192,192);
	public static final Color PANELCOLOUR=new Color(64,64,64);
	public static final Color PANELHIGHLIGHT=new Color(120,80,20);
	public static final Color PANELSHADOW=new Color(40,20,5);
	public static final Color INFOSCREENCOLOUR=new Color(0,0,0);
	public static final Color INFOTEXTCOLOUR=new Color(240,200,160);

	public static Font mainfont=new Font("Monospaced",Font.BOLD,15);

	static QuestApp instance;

	static{
		final Applet applet=new Applet();
		// Create mediatracker for the images
		final MediaTracker mediaTracker=new MediaTracker(applet);
		mediaTracker.addImage(QuestApp.DEFAULTTEXTURE,1);
		// Wait for images to load
		try{
			mediaTracker.waitForID(1);
		}catch(final Exception e){
			System.out.println("Error loading images.");
			e.printStackTrace();
		}
	}

	public Component mainComponent=null;
	// Thread for recieveing user input
	public static Thread thread;

	@Override
	public Dimension getPreferredSize(){
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	// inits the applet, loading all necessary resources
	// also kicks off the actual game thread
	@Override
	public void init(){
		// recreate lib in background
		instance=this;
		super.init();
		setLayout(new BorderLayout());
		setBackground(Color.black);
		setFont(QuestApp.mainfont);
		// set game in action
		Interface.userinterface=new Interface();
		QuestApp.thread=new Thread(this);
		QuestApp.thread.start();
	}

	// switches to a new screen, discarding the old one
	public void switchScreen(final Component s){
		//		if(s==null) return;
		//		if(mainComponent==s){
		//			//TODO using switch to repaint is bad practice but done in many places
		//			s.repaint();
		//			return;
		//		}
		setVisible(false);
		MapPanel.overlay=null;
		if(mainComponent instanceof Screen) ((Screen)mainComponent).close();
		removeAll();
		add(s);
		//		s.validate();
		//		invalidate();
		revalidate();
		//		repaint();
		//		repaint();
		var world=s instanceof WorldScreen?(WorldScreen)s:null;
		if(world!=null&&Squad.active!=null) world.firstdraw=true;
		try{
			Thread.sleep(100);
		}catch(InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setVisible(true);
		/*
		 * CBG This is needed to give the focus to the contained screen.
		 * RequestFocusInWindow is preferable to requestFocus.
		 */
		s.requestFocus();
		mainComponent=s;
	}

	@Override
	public void destroy(){
		removeAll();
	}
}