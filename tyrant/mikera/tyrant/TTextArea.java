/*
 * Created on 30-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import java.awt.Graphics;
import java.awt.TextArea;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TTextArea extends TextArea {
	private static final long serialVersionUID = 3545795472913347378L;

    public TTextArea(String s, int r, int c, int sb) {
		super(s,r,c,sb);
	}
	
	public void update(Graphics g) {
		paint(g);
	}
}
