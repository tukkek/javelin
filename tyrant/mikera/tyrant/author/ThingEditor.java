package tyrant.mikera.tyrant.author;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.TPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javelin.controller.old.Game;

public class ThingEditor implements Runnable {
	private final class SourceRevertPressed implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			sourceTextArea.setText(mapText);
		}
	}

	private final class SourceOKPressed implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final String newText = sourceTextArea.getText();
			if (newText.equals(mapText)) {
				return;
			}
			final javelin.model.BattleMap newMap = new MapMaker().create(
					newText, true);
			if (newMap == null) {
				sourceStatusBar.setVisible(true);
				sourceStatusLabel.setText("There is an error creating the map");
				sourceStatusBar.getParent().invalidate();
				sourceStatusBar.getParent().validate();
				new Thread(ThingEditor.this).start();
				return;
			}
			if (designer != null) {
				designer.reloadMap(newMap);
				mapText = new MapMaker().store(newMap);
				sourceTextArea.setText(mapText);
			}
		}
	}

	private Frame frame;
	private TPanel statusBar;
	private TextField textField;
	private Label textLabel;
	private ActionListener textListener;
	private Container thingPanel;
	private Thing thing;
	private Map importantAttributes;
	private final Map textFieldMapping = new HashMap();
	private final Map keyTypes = new HashMap();
	private String mapText;
	private TextArea sourceTextArea;
	private Panel sourceStatusBar;
	private Label sourceStatusLabel;
	private Designer designer;

	public void inspect(final Thing thing) {
		this.thing = thing;
		updateThingPanel();
	}

	public void inspect(final Thing[] things) {
		// TODO Support multiple things
		inspect(things == null || things.length == 0 ? null : things[0]);
	}

	public void buildUI() {
		Game.loadVersionNumber();
		createFrame();
		statusBar.setVisible(false);
	}

	public void updateThingPanel() {
		if (thingPanel != null && thingPanel.isValid()) {
			frame.remove(thingPanel);
		}
		createThingPanel();
		thingPanel.getParent().invalidate();
		thingPanel.getParent().validate();
	}

	private void createThingPanel() {
		thingPanel = new JTabbedPane(SwingConstants.TOP);
		frame.add(thingPanel, BorderLayout.CENTER);
		createThingTabbs();
		thingPanel.setBackground(SystemColor.control);
		((JTabbedPane) thingPanel).addTab("Source", createSourcePanel());
	}

	private void createThingTabbs() {
		if (thing == null) {
			return;
		}
		final BaseObject flat = BaseObject.getFlattened(thing);
		final Map locals = flat.getLocal();
		final String[] keys = (String[]) locals.keySet().toArray(
				new String[locals.keySet().size()]);
		if (keys.length == 0) {
			return;
		}
		sortKeys(keys);
		final int attriubutesPerPage = 25;
		final int tabsNeed = Math.max(1,
				(int) Math.ceil(locals.size() / attriubutesPerPage));
		char firstChar = 'A';
		for (int i = 0; i < tabsNeed; i++) {
			final Panel panel = new Panel(new BorderLayout());
			panel.setBackground(SystemColor.control);
			final char next = tabsNeed == 1 ? 'Z' : keys[(i + 1)
					* attriubutesPerPage].charAt(0);
			final String tabName = "" + firstChar + "-" + next;
			firstChar = (char) (next + 1);
			((JTabbedPane) thingPanel).addTab(tabName, panel);
			// ((JTabbedPane)thingPanel).addTab("" + (i + 1) + "/" + tabsNeed,
			// panel);
			addAttributes2(locals, keys, panel, i * attriubutesPerPage,
					attriubutesPerPage);
		}
	}

	private Component createSourcePanel() {
		final FormLayout layout = new FormLayout("fill:pref:grow",
				"fill:pref:grow");
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		mapText = new MapMaker().store(designer.battlemap);

		sourceTextArea = new TextArea(mapText, 10, 20);
		builder.append(sourceTextArea);
		builder.append(createSourceButtonPanel());
		builder.append(createSourceStatus());

		final Panel container = new Panel(new BorderLayout());
		container.add(builder.getPanel(), BorderLayout.CENTER);

		return container;
	}

	private Panel createSourceStatus() {
		sourceStatusBar = new Panel(new BorderLayout());
		sourceStatusLabel = new Label("Bob", Label.CENTER);
		sourceStatusBar.add(sourceStatusLabel, BorderLayout.CENTER);
		sourceStatusBar.setVisible(false);
		return sourceStatusBar;
	}

	private Panel createSourceButtonPanel() {
		final Panel buttonPanel = new Panel(new FlowLayout());
		final Button okButton = new Button("Apply");
		final Button cancelButton = new Button("Revert");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		okButton.addActionListener(new SourceOKPressed());
		cancelButton.addActionListener(new SourceRevertPressed());
		return buttonPanel;
	}

	private String determineType(final String key, final Object value) {
		String type = null;
		if (value instanceof Integer) {
			type = key.startsWith("Is") ? "boolean" : "int";
		} else if (value instanceof Double) {
			type = "double";
		} else if (value instanceof String) {
			type = "string";
		}
		keyTypes.put(key, type);
		return type;
	}

	private void sortKeys(final String[] keys) {
		Arrays.sort(keys, new Comparator() {
			@Override
			public boolean equals(final Object obj) {
				return false;
			}

			@Override
			public int compare(final Object o1, final Object o2) {
				final String s1 = (String) o1;
				final String s2 = (String) o2;
				final Map important = getImportantAttributes();
				final Integer sortOrder1 = (Integer) important.get(s1);
				final Integer sortOrder2 = (Integer) important.get(s2);
				if (sortOrder1 == null && sortOrder2 == null) {
					return s1.compareTo(s2);
				}
				if (sortOrder1 == null && sortOrder2 != null) {
					return 1;
				}
				if (sortOrder1 != null && sortOrder2 == null) {
					return -1;
				}
				return sortOrder1.intValue() - sortOrder2.intValue();
			}
		});
	}

	protected Map getImportantAttributes() {
		if (importantAttributes == null) {
			importantAttributes = new HashMap();
			final String[] names = { "Name", "Number", "Level", "Frequency",
					"HPS", "UName" };
			for (int i = 0; i < names.length; i++) {
				importantAttributes.put(names[i], new Integer(i));
			}
		}
		return importantAttributes;
	}

	private void createFrame() {
		frame = new Frame("ThingEditor - v" + Game.VERSION);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				frame.setVisible(false);
			}
		});
		frame.setBackground(SystemColor.control);
		frame.setLayout(new BorderLayout());
		statusBar = new TPanel(null);
		frame.add(statusBar, BorderLayout.SOUTH);
		textField = new TextField(30);
		statusBar.setLayout(new GridBagLayout());
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		textLabel = new Label("Bob:");
		textLabel.setForeground(QuestApp.INFOTEXTCOLOUR);
		statusBar.add(textLabel, gridBagConstraints);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		statusBar.add(textField, gridBagConstraints);
		textListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				doTextPerformed(e);
			}
		};
		frame.setSize(640, 800);
		frame.setMenuBar(createMenuBar());
	}

	protected void doTextPerformed(final ActionEvent e) {
		final String key = (String) textFieldMapping.get(e.getSource());
		final String value = ((TextField) e.getSource()).getText();
		final String type = (String) keyTypes.get(key);
		Object thingValue = value;
		if (!type.equals("string")) {
			if (type.equals("int")) {
				thingValue = Integer.valueOf(value);
			} else if (type.equals("double")) {
				thingValue = Double.valueOf(value);
			} else if (type.equals("boolean")) {
				thingValue = Boolean.valueOf(value);
			}
		}
		thing.set(key, thingValue);
		updateSourceTab();
	}

	private void updateSourceTab() {
		mapText = new MapMaker().store(designer.battlemap);
		sourceTextArea.setText(mapText);
	}

	protected void doCheckboxPerformed(final ItemEvent e) {
		final Checkbox checkbox = (Checkbox) e.getSource();
		final String key = checkbox.getLabel();
		final Integer value = new Integer(checkbox.getState() ? 1 : 0);
		thing.set(key, value);
	}

	private MenuBar createMenuBar() {
		return null;
	}

	public void setVisible(final boolean show) {
		frame.setVisible(show);
	}

	public boolean isVisible() {
		return frame.isVisible();
	}

	public Frame getFrame() {
		return frame;
	}

	private void addAttributes2(final Map locals, final String[] keys,
			final Container container, final int start, int toAdd) {
		final FormLayout layout = new FormLayout(
				"pref, 3dlu, fill:pref:grow(100)", "");
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		toAdd = Math.min(keys.length, toAdd);
		for (int i = start; i < start + toAdd; i++) {
			final String key = keys[i];
			if (locals.get(key) == null) {
				continue;
			}
			final Object value = locals.get(key);
			final String type = determineType(key, value);
			if (type == null) {
				System.out.println("ignoring " + key + " " + value);
				continue;
			}
			if (type.equals("boolean")) {
				final Checkbox checkbox = new Checkbox(key,
						((Integer) value).intValue() == 1);
				checkbox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent e) {
						doCheckboxPerformed(e);
					}
				});
				builder.append("");
				builder.append(checkbox);
				builder.nextLine();
				continue;
			}
			builder.append(key);

			if (key.equals("Message")) {
				final TextArea textArea = new TextArea(String.valueOf(value),
						1, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
				builder.append(textArea);
				textFieldMapping.put(textArea, key);
			} else {
				final TextField textField = new TextField(String.valueOf(value));
				builder.append(textField);
				textFieldMapping.put(textField, key);
				textField.addActionListener(textListener);
				if (key.equals("Name")) {
					textField.setEditable(false);
				}
			}
			builder.nextLine();
		}
		container.add(builder.getPanel(), BorderLayout.CENTER);

	}

	@Override
	public void run() {
		try {
			Thread.sleep(6000);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sourceStatusBar.setVisible(false);
					sourceStatusLabel
							.setText("There is an error creating the map");
					sourceStatusBar.getParent().invalidate();
					sourceStatusBar.getParent().validate();
				}
			});
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setDesigner(final Designer designer) {
		this.designer = designer;
	}
}
