package javelin.old.messagepanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javelin.old.QuestApp;
import javelin.old.TPanel;
import javelin.view.screen.BattleScreen;

public class MessagePanel extends TPanel{
  public final TextZone textzone=new TextZone();
  /// TODO remove in favor of {@link #get()}
  public static MessagePanel active;

  public MessagePanel(){
    setLayout(new BorderLayout());
    textzone.setBackground(QuestApp.PANELCOLOUR);
    textzone.setForeground(QuestApp.INFOTEXTCOLOUR);
    add("Center",textzone);
    MessagePanel.active=this;
  }

  public void clear(){
    textzone.setText("");
  }

  public void add(final String s){
    add(s,Color.lightGray);
  }

  public void add(final String s,final Color c){
    final var t=textzone.getText();
    if(t.length()>2000) textzone.setText(t.substring(t.length()-2000));
    textzone.setText(textzone.getText()+s);
  }

  @Override
  public Dimension getPreferredSize(){
    return new Dimension(500,120);
  }

  /// @return Instance for {@link BattleScreen#active}.
  public static MessagePanel get(){
    return BattleScreen.active.messagepanel;
  }
}
