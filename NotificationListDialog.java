package controlCenter;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.sql.Connection;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import controlCenter.ChooseDialog.ActionAdapter;

public class NotificationListDialog extends JDialog {
    private JList list;

    private JScrollPane textPane;
    private JScrollPane listPane;
    private final JButton okButton;
    private final Box buttonGroupBox;

    public NotificationListDialog(String title, String message, String listLabel, Vector vector, Frame parent) {
    	super(parent,true);
        this.setTitle(title);

        this.addWindowListener(new MyWindowAdapter(this));
        final ActionAdapter actionAdapter = new ActionAdapter(this);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width / 5, screen.height / 5);
        final Color backgroundColor = (new JPanel()).getBackground();

        final JTextArea textArea = new JTextArea(5, 1);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(ChooseDialog.NORMAL_FONT);
        textArea.setBackground(ChooseDialog.BACKGROUND_COLOR);
        textPane = new JScrollPane(textArea);
        textPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X, 100));
        textArea.setText(message);
        textPane.setBorder(null);

        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(actionAdapter);
        list.setListData(vector);
        list.setBackground(ChooseDialog.BACKGROUND_COLOR);
        listPane = new JScrollPane(list);
        listPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X, ChooseDialog.SCROLL_PANEL_DIMENSION_Y / 2));
        final TitledBorder border = new TitledBorder(ChooseDialog.BORDER, listLabel);
        border.setTitleFont(ChooseDialog.BOLD_FONT);
        listPane.setBorder(border);

        okButton = new JButton();
        okButton.setText("OK");
        okButton.addActionListener(actionAdapter);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(okButton);
        buttonGroupBox = Box.createHorizontalBox();
        buttonGroupBox.add(okButton);
		setModal(true);
        showPanel();
    }

    public void showPanel() {
        this.getContentPane().removeAll();

        //
        // Root panel
        //
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.getContentPane().setLayout(gridBagLayout);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(ChooseDialog.INSETS_PADDING, ChooseDialog.INSETS_PADDING, ChooseDialog.INSETS_PADDING,
                ChooseDialog.INSETS_PADDING);

        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(textPane, c);
        this.getContentPane().add(textPane);

        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(listPane, c);
        this.getContentPane().add(listPane);

        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(buttonGroupBox, c);
        this.getContentPane().add(buttonGroupBox);

        pack();
        setResizable(false);
        //setLocation(getParent().getLocation());
    }

    public void thisWindowClosed(WindowEvent e) {
    };

    class MyWindowAdapter extends WindowAdapter {
        private NotificationListDialog adaptee;

        MyWindowAdapter(NotificationListDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void windowClosed(WindowEvent e) {
            adaptee.thisWindowClosed(e);
        }
    }

    class ActionAdapter implements ActionListener, ListSelectionListener {
        final NotificationListDialog dialog;

        public ActionAdapter(NotificationListDialog dialog) {
            this.dialog = dialog;
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == list) {
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                hide();
            }
        }
    }
}
