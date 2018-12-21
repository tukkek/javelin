package javelin.view.frame;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 * Represents an AWT window.
 *
 * @author alex
 */
public abstract class Frame{

	class UpdateFrame extends ComponentAdapter{
		boolean first=true;

		@Override
		public void componentShown(ComponentEvent e){
			if(first){
				first=false;
				return;
			}
			show();
		}
	}

	private static final int BLOCKINTERVAL=500;
	/** Actual visual component. */
	public JFrame frame;
	boolean hidden=false;

	/**
	 * @param title Window title.
	 */
	public Frame(String title){
		frame=new JFrame(title);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addComponentListener(new UpdateFrame());
		addaction(KeyEvent.VK_ESCAPE,new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				escape();
			}
		});
		addaction(KeyEvent.VK_ENTER,new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				enter();
			}
		});
	}

	/**
	 * @param key Key to be associated with this event. For example:
	 *          {@link KeyEvent#VK_ENTER}.
	 * @param action Action to perform.
	 */
	protected void addaction(int key,AbstractAction action){
		JRootPane root=frame.getRootPane();
		root.getInputMap().put(KeyStroke.getKeyStroke(key,0),key);
		root.getActionMap().put(key,action);
	}

	/**
	 * Called when ENTER is pressed. By default closes the frame;
	 */
	protected void enter(){
		frame.dispose();
	}

	/**
	 * Called when ESCAPE is pressed. By default closes the frame;
	 */
	protected void escape(){
		frame.dispose();
	}

	/**
	 * @param frame Positions this frame in relation to the screen.
	 *
	 * @see #getscreensize()
	 */
	protected void position(JFrame frame){
		Dimension screen=getscreensize();
		frame.setLocation(screen.width/2-frame.getSize().width/2,
				screen.height/2-frame.getSize().height/2);
	}

	/** Open dialog. */
	public void show(){
		frame.setContentPane(generate());
		frame.pack();
		position(frame);
		frame.setVisible(true);
	}

	/**
	 * @return the content pane.
	 */
	protected abstract Container generate();

	/**
	 * @return The screen size.
	 */
	public static Dimension getscreensize(){
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * @return Hopefully a decent size suggestion for {@link Frame}s and controls.
	 */
	public static Dimension getdialogsize(){
		Dimension screen=getscreensize();
		return new Dimension(screen.width/2,screen.height/2);
	}

	/**
	 * Sleeps this thread until {@link #frame} is closed. Useful when calling from
	 * main thread, if calling from UI thread consider {@link #block(Frame)}
	 * instead.
	 */
	public void defer(){
		while(hidden||frame.isVisible())
			try{
				Thread.sleep(BLOCKINTERVAL);
			}catch(InterruptedException e){
				continue;
			}
	}

	/**
	 * Hides this window until this other {@link Frame} is closed.
	 *
	 * @see #defer()
	 */
	protected void block(final Frame modal){
		hidden=true;
		frame.setVisible(false);
		final Timer timer=new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				if(!modal.frame.isVisible()&&!modal.hidden){
					frame.setVisible(true);
					hidden=false;
					timer.cancel();
				}
			}
		},500,500);
	}

	/**
	 * @param parent Like {@link #show()} but also blocks the parent frame.
	 * @see #block(Frame)
	 */
	public void show(Frame parent){
		show();
		parent.block(this);
	}

	/** Block main game window while this {@link #frame} is being displayed. */
	public void blockbackground(){
		while(frame.isDisplayable()&&frame.isVisible())
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				// keep waiting
			}
	}

	/** Adds a button with the given action to the parent. */
	protected static Button newbutton(String label,Container parent,
			ActionListener action){
		Button b=new Button(label);
		b.addActionListener(action);
		if(parent!=null) parent.add(b);
		return b;
	}

}