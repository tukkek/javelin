package tyrant.mikera.tyrant.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.old.Game;
import tyrant.mikera.tyrant.IMessageHandler;
import tyrant.mikera.tyrant.TPanel;



public class NullHandler implements IMessageHandler {
    public List messages;
    public void clear() {
        messages.clear();
    }

    public void add(String s) {
        messages.add(s);
    }

    public void add(String s, Color c) {
        add(s);
    }

    public TPanel getPanel() {
        return null;
    }

    public static List installNullMessageHandler() {
    	List messages=new ArrayList();
        installNullMessageHandler(messages);
        return messages;
    }

    public static void installNullMessageHandler(List messages) {
        NullHandler aMessageHandler = new NullHandler();
        aMessageHandler.messages = messages;
        Game.messagepanel = aMessageHandler;
        Game.instance().getMessageList().clear();
    }
}