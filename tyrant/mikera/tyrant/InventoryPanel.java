package tyrant.mikera.tyrant;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.ItemSelectable;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javelin.view.MapPanel;


import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;


/**
 * Screen for displaying an list of items
 * 
 * @author Mike
 */
public class InventoryPanel extends Screen implements ItemSelectable {
	private static final long serialVersionUID = 3546926861755103544L;
	String title = "List";
	public Thing[] things;
    public Thing[] allThings;
	public static int page = 0;

	// no of items on page
	static public final int PAGESIZE = 16;
	
	// pixels per line
	private static final int LINEHEIGHT = 32;
	
	// description chars on line
	private static final int LINELENGTH = 75;

	// shopkeeper for selling screen variant
	public Thing shopkeeper=null;
    private String lastFilter;
    private IThingFilter filter;
    protected RogueLikeFilter rougeLikeFilter;
    private ItemListener itemListener;
    private Thing selected;
    private boolean isPollingMode;
    protected char charForThing;
    private ItemListener selfItemListener;
    
    public Object getObject() {
        allThings=things;
        
		boolean end = false;
		while (!end) {
		    KeyEvent k = Game.getInput();
		    Object value = handleKeyEvent(k);
            if(value == null) return null;
            if(value instanceof Thing) return value;
        }
		return null;
	}

	private Object handleKeyEvent(KeyEvent k) {
        if (k != null) {
            if (k.getKeyCode() == KeyEvent.VK_ESCAPE) return null;
            if (k.getKeyCode() == KeyEvent.VK_ENTER) return null;

            char c = Character.toLowerCase(k.getKeyChar());

            if (k.getKeyCode() == KeyEvent.VK_UP) c = '-';
            if (k.getKeyCode() == KeyEvent.VK_DOWN) c = '+';
            if (c == '-') pageUp();
            if (c == '+' || c == ' ' || c == '=') pageDown();
            if (c == ',') c = 'a';
            int kv = c - 'a';
            if ((kv >= 0) && (kv < PAGESIZE)) {
                int thingIndex = kv + page * PAGESIZE;
                if ((thingIndex >= 0) && (thingIndex < things.length)) {
                    selected = things[thingIndex];
                    fireSelectionEvent(selected);
                    return selected;
                }
            }
        }
        return this;
    }

    public String getLine() {
        String result = "";
        while (true) {
            KeyEvent k = Game.getInput();
            char ch = k.getKeyChar();
            if (k.getKeyCode() == KeyEvent.VK_ENTER) {
                break;
            }
            if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
                result="ESC";
                break;
            }
            if (k.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                // add the character to the input string if typed
                // i.e. don't include SHIFT, ALT etc.
                if (Character.isLetterOrDigit(ch)||(" -.:+'[]()=<>".indexOf(ch)>=0)) {
                    result = result + ch;
                    title = title + ch;
                    repaint();
                }
            } else if (result.length() > 0) {
                result = result.substring(0, result.length() - 1);
                repaint();
            }
        }
        return result;
    }

    public void pageUp() {
        if (page > 0) {
            page--;
            repaint();
        }
    }

    public void pageDown() {
        if ((page + 1) * PAGESIZE < things.length) {
            page++;
            repaint();
        }
    }

    /*
     * When in polling mode you should call getObject. When in event-based mode
     * add an item listener to get the selected object. Mixing the modes is
     * likely to cause problems.
     */
    public InventoryPanel(boolean inPollingMode) {
        this();
        this.isPollingMode = inPollingMode;
        if(isPollingMode) {
            selfHookItemListener();
        } else {
            selfHookKeyEvents();
        }
    }
    
	private void selfHookItemListener() {
        selfItemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                removeItemListener(selfItemListener);
                Game.simulateKey(charForThing);
                addItemListener(selfItemListener);
            }
        };
        addItemListener(selfItemListener);
    }

    public InventoryPanel() {
		super(Game.getQuestapp());

		setBackground(QuestApp.INFOSCREENCOLOUR);
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setFont(QuestApp.mainfont);

 		rougeLikeFilter = new RogueLikeFilter();
        filter = new OrFilter(rougeLikeFilter, new NameFilter(), new IsFilter());
	
        addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		int y=e.getY();
        		for (int i = 0; i < Math.min(things.length - (page * PAGESIZE), PAGESIZE); i++) {
                    int ypos = getYPosition(i);

                    if ((y >= ypos) && (y < ypos + LINEHEIGHT)) {
                        int thingIndex = i + page * PAGESIZE;
                        charForThing = getDisplayChar(i);
                        fireSelectionEvent(things[thingIndex]);
                    }
                }
        	}
        });
	}

    public void selfHookKeyEvents() {
        removeKeyListener(Game.getQuestapp().keyadapter);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyEvent(e);
            }
        });
    }

    //IF an item is identified but on the floor,
    //we should be able to get the same information!!
    //mayhaps this needs to become part of the Thing class
    //TODO: mikera to take a decision on this and act accordingly
    private String getItemText(Thing t) {
		Thing h=Game.hero();
		
		if (t.getFlag("IsSpell")) {
			return Spell.selectionString(h,t,LINELENGTH);
		}
		
		String s = t.getName(null);
		
		if (t.getStat("HPS")<t.getStat("HPSMAX")) {
			s="damaged "+s;
		}
		
		if (t.getFlag("IsWeapon")&&Item.isIdentified(t)) {		
			s=s+"  "+Weapon.statString(t);
		}
		
		if (t.getFlag("IsArmour")&&Item.isIdentified(t)) {		
			s=s+"  "+Armour.statString(t);
		}
		
		String ws = Lib.wieldDescription(t.y);
		if ((t.place==h)&&(ws != null)) {
			ws = "(" + ws + ")";
		}else {
			ws = "";
		}
		
		if (shopkeeper==null) {
			s = Text.centrePad(s, ws, LINELENGTH);
			s = s + Text.centrePad("  ", t.getWeight() / 100 + "s", 7);
		} else {
			
			s = Text.centrePad(s, ws, LINELENGTH-15);
			
			if (!t.getFlag("IsMoney")) s = s + Text.centrePad("  ", Coin.valueString(Item.shopValue(t,Game.hero(),shopkeeper)), 20);	
		}
		return s;
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(QuestApp.INFOTEXTCOLOUR);
		if (title!=null) g.drawString(title, 20, 1 * LINEHEIGHT);

		if ((things==null)||(allThings==null)) {
			return;
		}
		
		if ((page * PAGESIZE) > things.length)
			page = 0;
		
		for (int i = 0; i < Math.min(things.length - (page * PAGESIZE),
				PAGESIZE); i++) {
			Thing t = things[i + page * PAGESIZE];
			String s = "[" + getDisplayChar(i) + "] " + getItemText(t);
			
			int ypos=getYPosition(i);
			
			Color c=QuestApp.INFOTEXTCOLOUR;
			if (t.getFlag("IsStatusKnown")) {
				if (t.getFlag("IsBlessed")) {
					c=Color.GREEN.darker();
				} else if (t.getFlag("IsCursed")) {
					c=Color.RED;
				} else {
					c=c.darker();
				}
			}
			g.setColor(c);
			g.drawString(s, 70, ypos+18);
			
//			if (t.getFlag("IsItem") || t.getFlag("IsTile")) {
			if (t.containsKey("Image")) {
				int image = t.getStat("Image");
				int sx = (image % 20) * MapPanel.TILEWIDTH;
				int sy = (image / 20) * MapPanel.TILEHEIGHT;
				int px = 30;
				int py = ypos;
				g.drawImage((Image)QuestApp.images.get(t.get("ImageSource")), px, py, px + MapPanel.TILEWIDTH, py
						+ MapPanel.TILEHEIGHT, sx, sy, sx + MapPanel.TILEWIDTH,
						sy + MapPanel.TILEHEIGHT, null);
			}
		}
		


		g.setColor(QuestApp.INFOTEXTCOLOUR);
		
		String bottomString = "ESC=Cancel    /=Filter";
        
		if (page > 0)
			bottomString = bottomString + " Up=Previous ";
		if (((page + 1) * PAGESIZE) < things.length)
			bottomString = bottomString + " Down=Next ";
		String weightstring = "Weight: " + Game.hero().getInventoryWeight() / 100 + " / " + Being.maxCarryingWeight(Game.hero()) / 100 + "s";
        if(allThings.length != things.length) {
            weightstring += "    *Filtering* " + lastFilter; 
        }
		g.drawString(Text.centrePad(bottomString, weightstring, 80), 20, getSize().height - 10);
	}
	
	public int getYPosition(int i) {
		return LINEHEIGHT * (i + 1) + 8;
	}
	
	public char getDisplayChar(int i) {
		return (char)('a'+i);
	}
    
    public void filterThings(String query) {
        lastFilter = query;
        try {
            if(query == null) {
                things = allThings;
                lastFilter = "";
                return;
            }
            List filtered = new ArrayList();
            for (int i = 0; i < allThings.length; i++) {
                Thing thing = allThings[i];
                if(filter.accept(thing, query)) filtered.add(thing);
            }
            things = (Thing[]) filtered.toArray(new Thing[filtered.size()]);
        } finally {
            repaint();
        }
    }
    
    public void filterThings() {
    	filterThings(lastFilter);
    }

    public void clearFilter() {
        filterThings(null);
    }
    
    public synchronized void addItemListener(ItemListener l) {
        if (l == null) { return; }
        itemListener = AWTEventMulticaster.add(itemListener, l);
    }
    
    private void fireSelectionEvent(Thing selected) {
        this.selected = selected; 
        if(itemListener == null) return;
        itemListener.itemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, selected, ItemEvent.SELECTED));
    }

    public void removeItemListener(ItemListener l) {
        if(itemListener == null) return;
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    public Object[] getSelectedObjects() {
        return new Object[] {selected};
    }
}