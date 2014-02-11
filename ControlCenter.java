package controlCenter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

public class ControlCenter extends JFrame {

	public static final int LOGIN_PANEL = 1;

	public static final int POLICY_EDITOR = 2;

	public static final int AUDITOR = 3;

	public ResourceManager resourceManager;

	public final LoginPanel loginPanel;

	public final PolicyEditor policyEditor;

	public final Auditor auditor;

	private int currentComponentType;

	private ControlCenterComponent currentComponent;

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final Font applicationFont = new Font("Default",
			Font.TRUETYPE_FONT, 12);

	public ControlCenter() {
		logger.debug("Starting HDBCC");
		// super("Hippocratic Database Control Center");
		resourceManager = new ResourceManager(this);

		try {
			logger.debug("Setting look and feel");
			UIManager.setLookAndFeel(ResourceManager
					.getResource("software.look_and_feel"));
			setDefaultLookAndFeelDecorated(true);
		} catch (ClassNotFoundException e) {
			logger.warn("LookAndFeel class could not be found", e);
		} catch (InstantiationException e) {
			logger.warn("new instance of the class couldn't be created", e);
		} catch (IllegalAccessException e) {
			logger.warn("the class or initializer isn't accessible", e);
		} catch (UnsupportedLookAndFeelException e) {
			logger.warn("lnf.isSupportedLookAndFeel() is false", e);
		}

		try {
			UIDefaults uiDefaults = UIManager.getDefaults();
			Enumeration e = uiDefaults.keys();
			while (e.hasMoreElements()) {
				Object key = e.nextElement();
				// Object val = uiDefaults.get(key);
				if (key.toString().indexOf(".font") > 0)
					UIManager.put(key.toString(), applicationFont);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final Image icon = getToolkit().getImage(
				ResourceManager.getResource("image.file.icon.hdb"));
		setIconImage(icon);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(new Dimension((int) (screen.width * 2.3 / 3),
				(int) (screen.height * 2.3 / 3)));
		// setLocation(screen.width / 4, screen.height / 4);
		setLocationRelativeTo(null); // Centers on screen
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		setTitle(ResourceManager.getResource("software.name"));

		loginPanel = new LoginPanel(this);

		logger.debug("Initiating Auditor");
		auditor = new Auditor(this);

		logger.debug("Initiating Policy Editor");
		policyEditor = new PolicyEditor(this);

		changePanel(LOGIN_PANEL);
		setVisible(true);
		logger.debug("Started HDBCC");
	}

	public int getCurrentComponentType() {
		return currentComponentType;
	}

	public void changePanel(int nextComponentType) {
		this.currentComponentType = nextComponentType;
		final JComponent content = (JComponent) getContentPane();
		if (currentComponent != null)
			content.remove(currentComponent);
		content.setLayout(new BorderLayout());

		if (nextComponentType == LOGIN_PANEL) {
			currentComponent = loginPanel;
		} else if (nextComponentType == POLICY_EDITOR) {
			currentComponent = policyEditor;
			setTitle(ResourceManager.getResource("software.name.short")
					+ ": "
					+ ResourceManager
							.getResource("software.tool.name.policyeditor"));

		} else if (nextComponentType == AUDITOR) {
			currentComponent = auditor;
			setTitle(ResourceManager.getResource("software.name.short") + ": "
					+ ResourceManager.getResource("software.tool.name.auditor"));

		}
		setJMenuBar(currentComponent.getMenuBar());
		content.add(currentComponent, BorderLayout.CENTER);
		currentComponent.repaint();
		setVisible(true);

		if (nextComponentType == AUDITOR) {
			auditor.checkBacklogSchemaChange();
		}

	}

	static public void main(String[] args) throws Exception {
		new ControlCenter();
	}

	public void exit() {
		resourceManager.closeAllConnections();
		logger.debug("exiting HDBCC");
		System.exit(0);
	}

}
