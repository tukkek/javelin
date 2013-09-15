//package tyrant.mikera.tyrant;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Frame;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//// import mikera.tyrant.util.PlugInUtility;
//
///**
// * Main class for Tyrant when run as an application
// * 
// * Just creates a simple frame for a QuestApp component and 
// * calls QuestApp.init() to initialise the game
// * 
// * @author Mike
// *
// */
//public class QuestApplication {	
//	public static void main(String args[]) {
//		Game.loadVersionNumber();
//		
//		Frame f = new Frame("Tyrant - The Adventure - v"+Game.VERSION);
//		f.setBackground(Color.black);
//		f.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				System.exit(0);
//			}
//		});
//		
//		f.setLayout(new BorderLayout());
//		QuestApp q=new QuestApp();
//		QuestApp.isapplet=false;
//        if (args.length > 0) {
//            QuestApp.gameFileFromCommandLine = args[0];
//        }
//
//		// do we have a script to execute?
//		// activate debug mode if we do....
//		java.io.File script=new java.io.File("mikeradebug");
//    
//		// old method to activate debug cheat
//		Game.setDebug(script.exists());
//   
//		q.setVisible(false);
//		f.add(q);
//		
//		f.setSize(q.getPreferredSize().width,q.getPreferredSize().height);
//
//		f.addKeyListener(q.keyadapter);
//
//		q.setVisible(true);
//		f.setVisible(true);	
//		
//		q.init(); 
//
//	}    
// }