package controlCenter;

import Utility;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ScheduleDialog extends JDialog {
    private ResourceManager resourceManager;
    private ResourceManager.Task currentTask;
    private Wizard wizard;

    private JTextField beginTextField;
    private JTextField endTextField;
    private JButton cancelButton;
    private JButton backButton;
    private JButton nextButton;

    private final static String DATE_FORMAT = "yyyy/MM/dd";
    private final static int TEXT_FIELD_LENGTH = 12;

    public ScheduleDialog(ResourceManager resourceManager, Wizard wizard) {
    	super(resourceManager.controlCenter,true);
        this.resourceManager = resourceManager;
        this.wizard = wizard;

        setTitle("Audit interval");
        ActionAdapter actionAdapter = new ActionAdapter();

        try {
            final JLabel introductionLabel = new JLabel();
            introductionLabel.setText("Enter the date interval in the format year/month/day, e.g., 2004/2/29.");

            final JLabel beginLabel = new JLabel("Start date:");
            beginTextField = new JTextField(TEXT_FIELD_LENGTH);
            beginTextField.addActionListener(actionAdapter);

            final JLabel endLabel = new JLabel("End date:");
            endTextField = new JTextField(TEXT_FIELD_LENGTH);
            endTextField.addActionListener(actionAdapter);

            beginTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    checkInput();
                }

                public void keyTyped(KeyEvent e) {
                    checkInput();
                }

                public void keyReleased(KeyEvent e) {
                    checkInput();
                }
            });

            endTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    checkInput();
                }

                public void keyTyped(KeyEvent e) {
                    checkInput();
                }

                public void keyReleased(KeyEvent e) {
                    checkInput();
                }
            });

            backButton = new JButton(ChooseDialog.LEFT_ARROW + " Back");
            cancelButton = new JButton("Cancel");
            nextButton = new JButton("Finish");

            backButton.addActionListener(actionAdapter);
            cancelButton.addActionListener(actionAdapter);
            nextButton.addActionListener(actionAdapter);

            final JPanel navigationButtonPanel = new JPanel();
            navigationButtonPanel.setLayout(new FlowLayout());
            navigationButtonPanel.add(backButton);
            navigationButtonPanel.add(cancelButton);
            navigationButtonPanel.add(nextButton);

            JPanel textFieldPanel = new JPanel();
            GridBagLayout gridBagLayout = new GridBagLayout();
            textFieldPanel.setLayout(gridBagLayout);
            GridBagConstraints c = new GridBagConstraints();

            //c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(2, 2, 2, 2);

            c.anchor = GridBagConstraints.LINE_START;

            c.weightx = 0.1;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(beginLabel, c);
            textFieldPanel.add(beginLabel);

            c.weightx = 1.0;
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(beginTextField, c);
            textFieldPanel.add(beginTextField);

            c.weightx = 0.1;
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(endLabel, c);
            textFieldPanel.add(endLabel);

            c.weightx = 1.0;
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(endTextField, c);
            textFieldPanel.add(endTextField);

            //
            // Root panel
            //
            gridBagLayout = new GridBagLayout();
            this.getContentPane().setLayout(gridBagLayout);
            c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            //c.fill = GridBagConstraints.VERTICAL;

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(introductionLabel, c);
            this.getContentPane().add(introductionLabel);

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(textFieldPanel, c);
            this.getContentPane().add(textFieldPanel);

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(navigationButtonPanel, c);
            this.getContentPane().add(navigationButtonPanel);

            final Date now = new Date();
            final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            final String nowText = format.format(now);

            beginTextField.setText(nowText);
            endTextField.setText(nowText);

            pack();
            setModal(true);
            setResizable(false);
            setLocation(Utility.getTopLeftPoint(this));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        updateButtons();
    }

    public void showDialog(ResourceManager.Task currentTask) {
        this.currentTask = currentTask;

        final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        final String beginText = format.format(new Date(currentTask.begin.getTime()));
        final String endText = format.format(new Date(currentTask.end.getTime()));
        beginTextField.setText(beginText);
        endTextField.setText(endText);

        updateButtons();
        setVisible(true);
    }

    private void checkInput() {
        updateButtons();
    }

    /**
     * Expects a valid date of the form YYYY/MM/DD.
     * 
     * @return a corresponding timestamp if the date is a valid date.
     */
    private boolean checkDates(String beginDateText, String endDateText) {
        try {
            final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            format.setLenient(false);
            final Date beginDate = format.parse(beginDateText);
            final Date endDate = format.parse(endDateText);

            /*******************************************************************
             * GregorianCalendar gc = new GregorianCalendar();
             * gc.setTime(beginDate); gc.setTime(endDate);
             ******************************************************************/

            currentTask.begin = new Timestamp(beginDate.getTime());
            currentTask.end = new Timestamp(endDate.getTime());

            if (beginDate.compareTo(endDate) <= 0)
                return true;
            else {
                //final String text = "End date lies before begin date.";
                //(new Utility()).err(this, "checkDates()", text);
                return false;
            }
        }
        catch (Exception e) {
            //final String text = "Invalid date format.";
            //(new Utility()).err(this, "actionPerformed()", text);
        }
        return false;
    }

    class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cancelButton)
                setVisible(false);

            else if (e.getSource() == beginTextField)
                ;

            else if (e.getSource() == endTextField)
                ;

            else if ((e.getSource() == nextButton) || (e.getSource() == backButton)) {
                final String beginDateText = beginTextField.getText().trim();
                final String endDateText = endTextField.getText().trim();

                if (checkDates(beginDateText, endDateText)) {
                    hide();
                    final int dialog;
                    if (e.getSource() == backButton)
                        dialog = Wizard.ENTER_CONDITIONS_ADVANCED;
                    else if (e.getSource() == nextButton)
                        dialog = Wizard.FINISHED;
                    else {
                        (new Utility()).applicationError(this, "actionPerformed()", "Unknown button type.");
                        dialog = -1;
                    }
                    wizard.changeDialog(dialog, currentTask);
                }
                /***************************************************************
                 * else { final String text = "Error in setting the schedule
                 * times."; (new Utility()).err(this, "actionPerformed()",
                 * text); }
                 **************************************************************/
            }
        }
    }

    /**
     * Should be called after each interaction and when a dialog is displayed
     * for the first time. Checks which buttons should be enabled and disabled.
     */
    public void updateButtons() {
        final String beginDateText = beginTextField.getText().trim();
        final String endDateText = endTextField.getText().trim();
        nextButton.setEnabled(checkDates(beginDateText, endDateText));
    }
}
