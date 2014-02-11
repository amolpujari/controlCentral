package controlCenter;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class NotificationDialog extends JDialog {

    public final static int DO_NOTHING_DIALOG_TYPE = 1;
    public final static int EXIT_DIALOG_TYPE = 2;
    public final static int PLEASE_WAIT_DIALOG_TYPE = 3;
    public final static int DISCONNECT_DIALOG_TYPE = 4;

    private final int type;
    private JButton okButton;
    private ControlCenter controlCenter;
    private String databaseName;

    public NotificationDialog(int type, String title, String text, ControlCenter controlCenter, String databaseName) {
        this(type, title, text,controlCenter);
        this.controlCenter = controlCenter;
        this.databaseName = databaseName;
    }

    public NotificationDialog(String title, String text, Frame parent) {
        this(DO_NOTHING_DIALOG_TYPE, title, text,parent);
    }

    public NotificationDialog(int type, String title, String text, Frame parent) {
    	super(parent,true);
    	if(type==PLEASE_WAIT_DIALOG_TYPE)
    		this.setModal(false);
        this.type = type;

        try {
            this.addWindowListener(new MyWindowAdapter(this));

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            //setSize(new Dimension((int)(screen.width * 3.0 / 4),
            // (int)(screen.height * 3.0 / 4)));
            //setLocationRelativeTo(parent);
            //setSize(new Dimension(300, 350));
            setLocation(screen.width / 5, screen.height / 5);
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            okButton = new JButton();
            okButton.setText("OK");
            okButton.addActionListener(new ActionAdapter(this));

            final ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(okButton);
            final Box buttonGroupBox = Box.createHorizontalBox();
            buttonGroupBox.add(okButton);

            this.setTitle(title);

            final Color panelBackgroundColor = (new JPanel()).getBackground();

            JTextArea textTextArea = new JTextArea(text);
            //textTextArea.setBorder(new EmptyBorder(5, 10, 5, 10));
            textTextArea.setFont(ChooseDialog.NORMAL_FONT);
            textTextArea.setEditable(false);
            textTextArea.setBackground(panelBackgroundColor);
            textTextArea.setText(text);

            //
            // Root panel
            //
            GridBagLayout gridBagLayout = new GridBagLayout();
            this.getContentPane().setLayout(gridBagLayout);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            //c.fill = GridBagConstraints.VERTICAL;

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(textTextArea, c);
            this.getContentPane().add(textTextArea);

            if ((type == EXIT_DIALOG_TYPE) || (type == DO_NOTHING_DIALOG_TYPE) || (type == DISCONNECT_DIALOG_TYPE)) {
                c.weightx = 1.0;
                c.gridx = 0;
                c.gridy++;
                c.gridwidth = 1;
                gridBagLayout.setConstraints(buttonGroupBox, c);
                this.getContentPane().add(buttonGroupBox);
            }

            pack();
            setResizable(false);
            //setLocation(getParent().getLocation());
       

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void this_windowClosed(WindowEvent e) {
    };

    class MyWindowAdapter extends WindowAdapter {
        private NotificationDialog adaptee;

        MyWindowAdapter(NotificationDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void windowClosed(WindowEvent e) {
            adaptee.this_windowClosed(e);
        }
    }

    class ActionAdapter implements ActionListener {
        private NotificationDialog adaptee;

        ActionAdapter(NotificationDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                if (type == DO_NOTHING_DIALOG_TYPE) {
                    hide();
                    // Do nothing.
                }
                else if (type == EXIT_DIALOG_TYPE) {
                    System.exit(1);
                }
                else if (type == DISCONNECT_DIALOG_TYPE) {
                    hide();

                    if (controlCenter.resourceManager.closeConnection(databaseName)) {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Success", "Changes to database " + databaseName
                                + " successfully saved.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        final String text = "Cannot disconnect from database \"" + databaseName + "\".";
                        final NotificationDialog errorNotificationDialog = new NotificationDialog("Error", text,controlCenter);
                        hide();
                        errorNotificationDialog.show();
                    }
                }
            }
        }
    }
}
