//package tyrant.mikera.tyrant;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import tyrant.mikera.engine.Thing;
port tyrant.mikera.engine.Thing;
//
//
///**
// * Screen for displaying an list of items
// * 
// * @author Mike
// */
//public class InventoryScreen extends Screen {
//    
//	private final class FindFieldTextListener implements TextListener {
//        public void textValueChanged(TextEvent e) {
//            inventoryPanel.filterThings(findField.getText());
//        }
//    }
//
//    private final class FindFieldKeyListener extends KeyAdapter {
//        public void keyPressed(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                hideFindField();
//                inventoryPanel.clearFilter();
//            } else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
//                hideFindField();
//            } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
//                inventoryPanel.pageDown();
//            } else if(e.getKeyCode() == KeyEvent.VK_UP) {
//                inventoryPanel.pageUp();
//            }
//        }
//    }
//
//    private final class FindKeyOpener extends KeyAdapter {
//        public void keyPressed(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_F && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
//                showFindField();
//            else if(e.getKeyCode() == KeyEvent.VK_SLASH) showFindField();
//            else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                inventoryPanel.filterThings(null);
//                inventoryPanel.repaint();
//            }
//            else {
//                String aChar = "" + e.getKeyChar();
//                if(inventoryPanel.rougeLikeFilter.isRougeMatch(aChar)) {
//                    inventoryPanel.filterThings(aChar);
//                    inventoryPanel.repaint();
//                }
//            }
//        }
//
//        private void showFindField() {
//            findPanel.setVisible(true);
//            findField.requestFocus();
//            findField.selectAll();
//            inventoryPanel.filterThings(findField.getText());
//            findPanel.getParent().invalidate();
//            findPanel.getParent().validate();
//        }
//    }
//
//    private static final long serialVersionUID = 3546926861755103544L;
//	String title = "List";
//	static int page = 0;
//
//	// shopkeeper for selling screen variant
//	public Thing shopkeeper=null;
//	protected TextField findField;
//    protected InventoryPanel inventoryPanel;
//    protected FindKeyOpener findKeyListener;
//    protected Panel findPanel;
//    protected boolean simplifiedFind;
//    
//    public void select() {
//		getParent().remove(this);
//	}
//
//    protected void hideFindField() {
//        findPanel.setVisible(false);
//        findPanel.getParent().invalidate();
//        findPanel.getParent().validate();
//    }
//    public InventoryScreen() {
//        this(true, false);
//    }
//    
//	public InventoryScreen(boolean inPollingMode) {
//        this(inPollingMode, false);
//    }
//
//    public InventoryScreen(boolean inPollingMode, boolean simplifiedFind) {
//		super(Game.getQuestapp());
//        this.simplifiedFind = simplifiedFind;
//		setLayout(new BorderLayout(0, 0));
//        createFindPanel();
//        
//        inventoryPanel = new InventoryPanel(inPollingMode);
//        add(inventoryPanel, BorderLayout.CENTER);
//        
//        findKeyListener = new FindKeyOpener();
//        addKeyListener(findKeyListener);
//        inventoryPanel.addKeyListener(findKeyListener);
//        
//		setBackground(QuestApp.INFOSCREENCOLOUR);
//		setForeground(QuestApp.INFOTEXTCOLOUR);
//		setFont(QuestApp.mainfont);
//        inventoryPanel.requestFocus();
//	}
//
//    public InventoryPanel getInventoryPanel() {
//        return inventoryPanel;
//    }
//    
//    private void createFindPanel() {
//        findPanel = new TPanel(questapp);
//        findPanel.setLayout(new GridBagLayout());
//        add(findPanel, BorderLayout.SOUTH);
//        
//        GridBagConstraints gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
//        
//        if (!simplifiedFind) {
//            ImageGadget imageGadget = new ImageGadget("/images/cancel.png", "Filter: ");
//            imageGadget.setBackgroundImage(getTexture());
//            imageGadget.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    hideFindField();
//                }
//            });
//            findPanel.add(imageGadget, gridBagConstraints);
//        }
//        findField = new TextField();
//        findField.setForeground(SystemColor.controlText);
//        findField.setVisible(true);
//        findField.addKeyListener(new FindFieldKeyListener());
//        findField.addTextListener(new FindFieldTextListener());
//        
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
//        
//        findPanel.add(findField, gridBagConstraints);
//        
//        if (!simplifiedFind) {
//            ImageGadget label = ImageGadget.noImage("By name or by type %$[]=?+(/");
//            label.setBackgroundImage(getTexture());
//            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
//            gridBagConstraints.weightx = .1;
//            gridBagConstraints.insets = new Insets(2, 10, 2, 2);
//            findPanel.add(label, gridBagConstraints);
//        }
//        findPanel.setVisible(false);
//    }
//
//    public void setUp(String title, Thing buyer, Thing[] things) {
//    	
//    	inventoryPanel.allThings=things;
//    	inventoryPanel.title=title;
//    	inventoryPanel.shopkeeper=buyer;
//    	inventoryPanel.filterThings();
//    }
//    
//    public Thing getObject() {
//    	return (Thing) inventoryPanel.getObject(); 
//    }
//
//    public String getLine() {
//        return inventoryPanel.getLine();
//    }
// }