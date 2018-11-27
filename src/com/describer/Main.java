package com.describer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.describer.context.Context;
import com.describer.optimization.Optimizer;
import com.describer.presentation.Presenter;
import com.describer.util.Utils;

@SuppressWarnings("serial")
public class Main extends JFrame implements ActionListener {

	private Context ctx;
	private Presenter presenter;
	private Optimizer optimizer;
	private File currentFile;

	// These are the actions defined for the application
	private CtxAbstractAction newAction, openAction, saveAction, saveAsAction,
			importXmlAction, exportXmlAction, printAction, exportPicAction,
			exitAction, tagsEditAction, aboutAction, optRemSuperkeysAction,
			optRemDuplKeysAction, optDiscoverCyclesAction, optShowOptAction,
			optRunWizardAction;

	private MouseHandler mouseHandler;

	public Main() {
		// Set window title
		super("Describer");
		// Set window size
		this.setSize(600, 600);
		// Posit in DesktopCenter
		this.setLocationRelativeTo(null);
		// Maximize window
		this.setExtendedState(MAXIMIZED_BOTH);
		// Encapsulate closing in the action
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitAction.actionPerformed(new ActionEvent(evt.getSource(), evt
						.getID(), exitAction.getActionCommand()));
			}
		});

		// Action initialization
		initActions();

		JLabel status = createStatusBar();

		mouseHandler = new MouseHandler(status);

		// BuildMenu
		this.setJMenuBar(createMenuBar());

		Container container = this.getContentPane();

		ctx = new Context();
		presenter = new Presenter();
		presenter.setCtx(ctx);
		ctx.addContextListener(presenter);

		container.add(presenter);

		JToolBar toolbar = createToolBar();
		toolbar.setFloatable(false);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(status, BorderLayout.SOUTH);

		optimizer = new Optimizer();

	}

	// Creates the status bar.
	private JLabel createStatusBar() {
		JLabel status = new JLabel("Ready...");
		status.setBorder(BorderFactory.createEtchedBorder());

		return status;
	}

	private void initActions() {

		class NewAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "new-command";
			private static final String NAME = "New";
			private static final String SMALL_ICON = "_16x16/New.png";
			private static final String LARGE_ICON = "_24x24/New.png";
			private static final String SHORT_DESCRIPTION = "Create new document";
			private static final String LONG_DESCRIPTION = "Create new entity tree document";
			private static final int MNEMONIC_KEY = 'O';

			public NewAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		newAction = new NewAction();
		registerAction(newAction);

		class OpenAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "open-command";
			private static final String NAME = "Open";
			private static final String SMALL_ICON = "_16x16/Open.png";
			private static final String LARGE_ICON = "_24x24/Open.png";
			private static final String SHORT_DESCRIPTION = "Open etf document";
			private static final String LONG_DESCRIPTION = "Open existing entity tree document";
			private static final int MNEMONIC_KEY = 'O';

			public OpenAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		openAction = new OpenAction();
		registerAction(openAction);

		class SaveAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "save-command";
			private static final String NAME = "Save";
			private static final String SMALL_ICON = "_16x16/Save.png";
			private static final String LARGE_ICON = "_24x24/Save.png";
			private static final String SHORT_DESCRIPTION = "Save document";
			private static final String LONG_DESCRIPTION = "Save current document";
			private static final int MNEMONIC_KEY = 'O';

			public SaveAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		saveAction = new SaveAction();
		registerAction(saveAction);

		class SaveAsAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "save-as-command";
			private static final String NAME = "Save as...";
			private static final String SMALL_ICON = "_16x16/Save As.png";
			private static final String LARGE_ICON = "_24x24/Save As.png";
			private static final String SHORT_DESCRIPTION = "Save document as";
			private static final String LONG_DESCRIPTION = "Save document as...";
			private static final int MNEMONIC_KEY = 'O';

			public SaveAsAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		saveAsAction = new SaveAsAction();
		registerAction(saveAsAction);

		class ImportXmlAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "xml-import-command";
			private static final String NAME = "Import XML";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Import XML-file";
			private static final String LONG_DESCRIPTION = "Import XML-file with entity tree structure";
			private static final int MNEMONIC_KEY = 'O';

			public ImportXmlAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		importXmlAction = new ImportXmlAction();
		registerAction(importXmlAction);

		class ExportXmlAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "xml-export-command";
			private static final String NAME = "Export XML";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Export XML-file";
			private static final String LONG_DESCRIPTION = "Export entity tree structure";
			private static final int MNEMONIC_KEY = 'O';

			public ExportXmlAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		exportXmlAction = new ExportXmlAction();
		registerAction(exportXmlAction);

		class tagsEditAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "launch-propsed-command";
			private static final String NAME = "Edit XML tags";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Edit tag-names";
			private static final String LONG_DESCRIPTION = "Edit tag-names used in XML-documents";
			private static final int MNEMONIC_KEY = 'O';

			public tagsEditAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		tagsEditAction = new tagsEditAction();
		registerAction(tagsEditAction);

		class PrintAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "print-command";
			private static final String NAME = "Print...";
			private static final String SMALL_ICON = "_16x16/Print.png";
			private static final String LARGE_ICON = "_24x24/Print.png";
			private static final String SHORT_DESCRIPTION = "Print/preview";
			private static final String LONG_DESCRIPTION = "Print/preview";
			private static final int MNEMONIC_KEY = 'O';

			public PrintAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		printAction = new PrintAction();
		registerAction(printAction);

		class ExportPicAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "pic-export-command";
			private static final String NAME = "Export pic";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Export image";
			private static final String LONG_DESCRIPTION = "Save graph to png or jpg file";
			private static final int MNEMONIC_KEY = 'O';

			public ExportPicAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		exportPicAction = new ExportPicAction();
		registerAction(exportPicAction);

		class ExitAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "app-exit-command";
			private static final String NAME = "Exit";
			private static final String SMALL_ICON = "_16x16/Exit.png";
			private static final String LARGE_ICON = "_24x24/Exit.png";
			private static final String SHORT_DESCRIPTION = "Exit application";
			private static final String LONG_DESCRIPTION = "Exit application";
			private static final int MNEMONIC_KEY = 'O';

			public ExitAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY));
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		exitAction = new ExitAction();
		registerAction(exitAction);

		class AboutAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "about-command";
			private static final String NAME = "About";
			private static final String SMALL_ICON = "_16x16/About.png";
			private static final String LARGE_ICON = "_24x24/About.png";
			private static final String SHORT_DESCRIPTION = "About...";
			private static final String LONG_DESCRIPTION = "About...";

			public AboutAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		aboutAction = new AboutAction();
		registerAction(aboutAction);

		class OptRemSuperkeysAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "opt-superkeys-command";
			private static final String NAME = "Remove superkeys";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Remove superkeys";
			private static final String LONG_DESCRIPTION = "Remove superkeys";

			public OptRemSuperkeysAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		optRemSuperkeysAction = new OptRemSuperkeysAction();
		registerAction(optRemSuperkeysAction);

		class OptRemDuplKeysAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "opt-duplkeys-command";
			private static final String NAME = "Remove duplicate keys";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Remove duplicate keys";
			private static final String LONG_DESCRIPTION = "Remove duplicate keys";

			public OptRemDuplKeysAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		optRemDuplKeysAction = new OptRemDuplKeysAction();
		registerAction(optRemDuplKeysAction);

		class OptDiscoverCyclesAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "opt-discover-cycles-command";
			private static final String NAME = "Discover cycles";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Discover cycles";
			private static final String LONG_DESCRIPTION = "Discover cycles";

			public OptDiscoverCyclesAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		optDiscoverCyclesAction = new OptDiscoverCyclesAction();
		registerAction(optDiscoverCyclesAction);

		class OptShowOptAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "opt-show-window-command";
			private static final String NAME = "Show optimizer window";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Show optimizer window";
			private static final String LONG_DESCRIPTION = "Show optimizer window";

			public OptShowOptAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		optShowOptAction = new OptShowOptAction();
		registerAction(optShowOptAction);

		class OptRunWizardAction extends CtxAbstractAction {

			private static final String ACTION_COMMAND_KEY = "opt-run-opt-wizard-command";
			private static final String NAME = "Run optimization wizard";
			private static final String SMALL_ICON = "";
			private static final String LARGE_ICON = "";
			private static final String SHORT_DESCRIPTION = "Run optimization wizard";
			private static final String LONG_DESCRIPTION = "Run optimization wizard";

			public OptRunWizardAction() {
				putValue(Action.NAME, NAME);
				putValue(Action.SMALL_ICON, getIcon(SMALL_ICON));
				putValue(CtxAbstractAction.LARGE_ICON, getIcon(LARGE_ICON));
				putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
				putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION);
				putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY);
			}

		}
		optRunWizardAction = new OptRunWizardAction();
		registerAction(optRunWizardAction);

	}

	private void registerAction(CtxAbstractAction action) {
		action.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {

		if (newAction.isMyEvent(e)) {
			newDoc();
		} else if (openAction.isMyEvent(e)) {
			open();
		} else if (saveAction.isMyEvent(e)) {
			save();
		} else if (saveAsAction.isMyEvent(e)) {
			saveAs();
		} else if (importXmlAction.isMyEvent(e)) {
			importXml();
		} else if (exportXmlAction.isMyEvent(e)) {
			exportXml();
		} else if (tagsEditAction.isMyEvent(e)) {
			ctx.manageTagNames(this);
		} else if (printAction.isMyEvent(e)) {
			presenter.showPreview(Main.this);
		} else if (exportPicAction.isMyEvent(e)) {
			exportPic();
		} else if (optRemSuperkeysAction.isMyEvent(e)) {
			optimizer.removeSuperkeys(ctx);
		} else if (optRemDuplKeysAction.isMyEvent(e)) {
			optimizer.removeDuplicateKeys(ctx);
		} else if (optDiscoverCyclesAction.isMyEvent(e)) {
			optimizer.findCycles(ctx, presenter);
		} else if (optShowOptAction.isMyEvent(e)) {
			optimizer.showOptWindow(this, ctx, presenter);
		} else if (optRunWizardAction.isMyEvent(e)) {
			optimizer.runWizard(this, ctx, presenter);
		} else if (aboutAction.isMyEvent(e)) {
			showAboutDlg();
		} else if (exitAction.isMyEvent(e)) {
			System.exit(NORMAL);
		}

	}

	/**
	 * This adapter is constructed to handle mouse over component events.
	 */
	private class MouseHandler extends MouseAdapter {

		private JLabel label;

		/**
		 * ctor for the adapter.
		 * 
		 * @param label
		 *            the JLabel which will recieve value of the
		 *            Action.LONG_DESCRIPTION key.
		 */
		public MouseHandler(JLabel label) {
			setLabel(label);
		}

		public void setLabel(JLabel label) {
			this.label = label;
		}

		public void mouseEntered(MouseEvent evt) {
			if (evt.getSource() instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) evt.getSource();
				Action action = button.getAction(); // getAction is new in JDK
				// 1.3
				if (action != null) {
					String message = (String) action
							.getValue(Action.LONG_DESCRIPTION);
					label.setText(message);
				}
			}
		}

		public void mouseExited(MouseEvent evt) {
			label.setText("Ready...");
		}

	}

	private JMenuBar createMenuBar() {
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		// Build the first menu.
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription(
				"Operations on opening, saving or printing.");
		menuBar.add(fileMenu);

		JMenuItem menuItem = fileMenu.add(newAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(openAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(saveAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(saveAsAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(importXmlAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(exportXmlAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(tagsEditAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(printAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(exportPicAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = fileMenu.add(exitAction);
		menuItem.addMouseListener(mouseHandler);

		JMenu optimizationMenu = new JMenu("Optimization");
		menuBar.add(optimizationMenu);

		menuItem = optimizationMenu.add(optRemDuplKeysAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = optimizationMenu.add(optRemSuperkeysAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = optimizationMenu.add(optDiscoverCyclesAction);
		menuItem.addMouseListener(mouseHandler);

		menuItem = optimizationMenu.add(optShowOptAction);
		menuItem.addMouseListener(mouseHandler);
		
		menuItem = optimizationMenu.add(optRunWizardAction);
		menuItem.addMouseListener(mouseHandler);

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		menuItem = helpMenu.add(aboutAction);
		menuItem.addMouseListener(mouseHandler);

		return menuBar;
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();

		JButton button;

		button = toolbar.add(newAction);
		button.addMouseListener(mouseHandler);
		button = toolbar.add(openAction);
		button.addMouseListener(mouseHandler);
		button = toolbar.add(saveAction);
		button.addMouseListener(mouseHandler);
		button = toolbar.add(printAction);
		button.addMouseListener(mouseHandler);
		toolbar.addSeparator();
		button = toolbar.add(exitAction);
		button.addMouseListener(mouseHandler);

		return toolbar;
	}

	private void newDoc() {
		ctx.clean();
		currentFile = null;
		setTitle("Describer - Document*");
	}

	private void importXml() {
		JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "XML - Files (*.xml)";
			}

			public boolean accept(File file) {
				String name = file.getName();
				return name.toLowerCase().endsWith(".xml")
						|| file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int rVal = c.showOpenDialog(Main.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			try {
				ctx.buildFromXmlFile(c.getSelectedFile());
				presenter.autoLayout();
				currentFile = null;
				Main.this.setTitle("Describer - "
						+ Utils.getFileNameWithoutExtension(c.getSelectedFile()
								.getName()));
			} catch (Exception er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), er
						.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private void open() {
		JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "Entity tree files (*.etf)";
			}

			public boolean accept(File file) {
				String name = file.getName();
				return name.toLowerCase().endsWith(".etf")
						|| file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		if (currentFile == null)
			c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		else
			c.setCurrentDirectory(currentFile);
		int rVal = c.showOpenDialog(Main.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {

			currentFile = c.getSelectedFile();

			Main.this.setTitle("Describer - "
					+ Utils.getFileNameWithoutExtension(currentFile.getName()));

			Archiver arch = new Archiver();

			try {
				arch.open(currentFile);

				byte[] content;

				content = arch.getContent("xmlNames.xml");
				if (content != null) {
					ctx.loadXmlTagNames(new ByteArrayInputStream(content));
				}

				content = arch.getContent("document.xml");
				if (content != null) {
					ctx.buildFromXmlIStream(new ByteArrayInputStream(content));
				}

				content = arch.getContent("documentCoords.xml");
				if (content != null) {
					presenter.loadCoords(new ByteArrayInputStream(content));
				} else {
					presenter.autoLayout();
				}
			} catch (InvalidPropertiesFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Invalid tag-names error: \n" + e.getMessage(), e
								.getClass().getName(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				JOptionPane.showMessageDialog(this,
						"XML-parser-config error: \n" + e.getMessage(), e
								.getClass().getName(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (SAXException e) {
				JOptionPane.showMessageDialog(this, "XML-parsing error: \n"
						+ e.getMessage(), e.getClass().getName(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Input/output error: \n"
						+ e.getMessage(), e.getClass().getName(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void save() {
		if (currentFile != null)
			saveDocument(currentFile);
		else
			saveAs();
	}

	private void saveAs() {
		JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "Entity tree files (*.etf)";
			}

			public boolean accept(File file) {
				return Utils.getFileExtension(file).equals(".etf")
						|| file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		c.setSelectedFile(new File("document"));
		int rVal = c.showSaveDialog(Main.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			File f = c.getSelectedFile();
			if (c.getFileFilter().getDescription().contains(".etf")) {
				if (!Utils.getFileExtension(f).equals(".etf")) {
					f = new File(f.getAbsolutePath() + ".etf");
				}
			}
			if (f.exists()) {
				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Вы уверены, что хотите заменить существующий файл?",
								"Подтверждение замены файла",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
					return;
			}

			saveDocument(f);

			currentFile = f;

			Main.this.setTitle("Describer - "
					+ Utils.getFileNameWithoutExtension(f.getName()));

		}
	}

	private void saveDocument(File f) {
		try {
			Archiver arch = new Archiver();
			ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
			ctx.saveToXmlOStream(bAOS);
			arch.setContent("document.xml", bAOS.toByteArray());
			bAOS.reset();

			ctx.saveXmlTagNames(bAOS);
			arch.setContent("propNames.xml", bAOS.toByteArray());
			bAOS.reset();

			presenter.saveCoords(bAOS);
			arch.setContent("documentCoords.xml", bAOS.toByteArray());
			bAOS.close();

			arch.save(f);
		} catch (InvalidPropertiesFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid tag-names error: \n"
					+ e.getMessage(), e.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			JOptionPane.showMessageDialog(this, "XML-parser-config error: \n"
					+ e.getMessage(), e.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Input/output error: \n"
					+ e.getMessage(), e.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TransformerException e) {
			JOptionPane.showMessageDialog(this,
					"Transforamtion during saving XML error: \n"
							+ e.getMessage(), e.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void exportXml() {
		JFileChooser c = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "XML - Files (*.xml)";
			}

			public boolean accept(File file) {
				return Utils.getFileExtension(file).equals(".xml")
						|| file.isDirectory();
			}
		};
		c.setFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		c.setSelectedFile(new File("document"));
		int rVal = c.showSaveDialog(Main.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			try {
				File f = c.getSelectedFile();
				if (c.getFileFilter().getDescription().contains(".xml")) {
					if (!Utils.getFileExtension(f).equals(".xml")) {
						f = new File(f.getAbsolutePath() + ".xml");
					}
				}
				if (f.exists()) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"Вы уверены, что хотите заменить существующий файл?",
									"Подтверждение замены файла",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.NO_OPTION)
						return;
				}
				ctx.saveToXmlFile(f);
				Main.this.setTitle("Describer - "
						+ Utils.getFileNameWithoutExtension(f.getName()));
			} catch (Exception er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), er
						.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void exportPic() {
		JFileChooser c = new JFileChooser();
		// setting filters
		c.setAcceptAllFileFilterUsed(false);
		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "PNG files (*.png)";
			}

			public boolean accept(File file) {
				return Utils.getFileExtension(file).equals(".png");
			}
		};
		c.addChoosableFileFilter(filter);
		filter = new FileFilter() {
			public String getDescription() {
				return "JPEG files (*.jpg)";
			}

			public boolean accept(File file) {
				return Utils.getFileExtension(file).equals(".jpg");
			}
		};
		c.addChoosableFileFilter(filter);
		c.setCurrentDirectory(new File(System.getProperty("user.dir")));
		c.setSelectedFile(new File("graph"));
		int rVal = c.showSaveDialog(Main.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			try {
				File f = c.getSelectedFile();
				if (c.getFileFilter().getDescription().contains(".jpg")) {
					if (!Utils.getFileExtension(f).equals(".jpg")) {
						f = new File(f.getAbsolutePath() + ".jpg");
					}
				} else if (c.getFileFilter().getDescription().contains(".png")) {
					if (!Utils.getFileExtension(f).equals(".png")) {
						f = new File(f.getAbsolutePath() + ".png");
					}
				}
				if (f.exists()) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"Вы уверены, что хотите заменить существующий файл?",
									"Подтверждение замены файла",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.NO_OPTION)
						return;
				}
				presenter.exportGraph(f);
			} catch (Exception er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), er
						.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showAboutDlg() {
		AboutDlg dlg = new AboutDlg(this, "About...", true);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}

	public static void main(String[] args) {

		try {
			// Set System L&F
			UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
		} catch (Exception e) {

		}

		Main app = new Main();
		app.setVisible(true);
	}

	/*
	 * private static void printBranch(Entity ent, int depth) { for
	 * (Iterator<Node> it1 = ent.getChildren().iterator(); it1.hasNext();){ Node
	 * elem = it1.next(); if (elem instanceof Entity) { for(int i = depth; i >
	 * 0; i--){ System.out.print("| "); } // System.out.println("|-пон." // +
	 * "#" + elem.getId() // + ", " + elem.getName() // + ", " +
	 * elem.getSemantics() // ); printBranch((Entity)elem, depth + 1); } else if
	 * (elem instanceof Requisite) { for(int i = depth; i > 0; i--){
	 * System.out.print("| "); } System.out.println("|-рекв." + "#" +
	 * elem.getId() + ", " + elem.getName() //+ ", " + elem.getSemantics() //+
	 * ", обязат.=" + ((Requisite)elem).isNullable() ); } } }
	 * 
	 * 
	 * private void printCtxToStdout(Context ctx){ // Вывод данных о каждом
	 * понятии System.out.println("Все_понятия"); for (Iterator<Entity> it1 =
	 * ctx.getEntities().iterator(); it1.hasNext();){ Entity ent = it1.next();
	 * System.out.println("|-пон." + "#" + ent.getId() + ", " + ent.getName() +
	 * ", " + ent.getSemantics() ); System.out.println("| |-Родители:"); for
	 * (Iterator<Entity> it2 = ent.getParents().iterator(); it2.hasNext();){
	 * Entity prnt = it2.next(); System.out.println("| | |-пон." + "#" +
	 * prnt.getId() + ", " + prnt.getName() + ", " + prnt.getSemantics() ); }
	 * System.out.println("| |-Дети:"); for (Iterator<Node> it2 =
	 * ent.getChildren().iterator(); it2.hasNext();){ Node chld = it2.next(); if
	 * (chld instanceof Entity){ System.out.println("| |-пон." + "#" +
	 * chld.getId() + ", " + chld.getName() + ", " + chld.getSemantics() ); }
	 * else if (chld instanceof Requisite){ System.out.println("| |-рекв." + "#"
	 * + chld.getId() + ", " + chld.getName() + ", " + chld.getSemantics() +
	 * ", обязат.=" + ((Requisite)chld).getRequired() ); } }
	 * System.out.println("| |-Ключи:"); for (Iterator<Key> it2 =
	 * ent.getKeysHas().iterator(); it2.hasNext();){ Key key = it2.next();
	 * System.out.println("| | |-Кл.#" + key.getId() + " для пон.#" +
	 * key.getEntity().getId()); for (Iterator<Node> it3 =
	 * key.getKeyElements().iterator(); it3.hasNext();){ Node keyEl =
	 * it3.next(); if (keyEl instanceof Entity){
	 * System.out.println("| | | |-пон." + "#" + keyEl.getId() + ", " +
	 * keyEl.getName() + ", " + keyEl.getSemantics() ); } else if (keyEl
	 * instanceof Requisite){ System.out.println("| | | |-рекв." + "#" +
	 * keyEl.getId() + ", " + keyEl.getName() + ", " + keyEl.getSemantics() +
	 * ", обязат.=" + ((Requisite)keyEl).getRequired() ); } } }
	 * System.out.println("| |-Связи ММ:"); for (Iterator<MM> it2 =
	 * ent.getMMs().iterator(); it2.hasNext();){ MM mM = it2.next();
	 * System.out.println("| | |-Мм.#" + mM.getId()); for (Iterator<Node> it3 =
	 * mM.getMMElements().iterator(); it3.hasNext();){ Node mMEl = it3.next();
	 * if (mMEl instanceof Entity){ System.out.println("| | | |-пон." + "#" +
	 * mMEl.getId() + ", " + mMEl.getName() + ", " + mMEl.getSemantics() ); }
	 * else if (mMEl instanceof Requisite){ System.out.println("| | | |-рекв." +
	 * "#" + mMEl.getId() + ", " + mMEl.getName() + ", " + mMEl.getSemantics() +
	 * ", обязат.=" + ((Requisite)mMEl).getRequired() ); } } } }
	 * 
	 * // Вывод дерева System.out.println("\nДерево"); for (Iterator<Entity> it1
	 * = ctx.getEntities().iterator(); it1.hasNext();){ Entity ent = it1.next();
	 * if (ent.getParents().isEmpty()){ System.out.println("|-пон." + "#" +
	 * ent.getId() + ", " + ent.getName() ); printBranch(ent, 1); } } }
	 */

}