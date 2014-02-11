package controlCenter;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

class IntervalPanel extends NewWizardPanel implements KeyChangedInformer {
	private final JTextField startText;

	private final JTextField endText;

	private Timestamp start;

	private Timestamp end;

	private final Logger logger = Logger.getLogger(getClass().getName());

	public IntervalPanel(final NewWizard wiz, Timestamp begin, Timestamp end) {
		super();
		logger.debug("started initiating interval panel");
		this.wiz = wiz;
		final JLabel startLabel = new JLabel(ResourceManager
				.getResource("int.start.date"));
		final JLabel endLabel = new JLabel(ResourceManager
				.getResource("int.end.date"));

		if (begin == null)
			begin = new Timestamp(System.currentTimeMillis());

		if (end == null)
			end = new Timestamp(System.currentTimeMillis());

		if (end.equals(begin))
			end.setDate(end.getDate() + 1);

		startText = new JTextField((begin.getYear() + 1900) + "/"
				+ (begin.getMonth() + 1) + "/" + begin.getDate());
		endText = new JTextField((end.getYear() + 1900) + "/"
				+ (end.getMonth() + 1) + "/" + (end.getDate()));
		startLabel.setBounds(10, 10, 80, 20);
		startText.setBounds(70, 10, 200, 20);
		endLabel.setBounds(10, 40, 80, 20);
		endText.setBounds(70, 40, 200, 20);
		add(startLabel);
		add(startText);
		add(endLabel);
		add(endText);
		setBounds(2, 10, getWidth() - 2, getHeight() - 10);

		startText.addKeyListener(new CommonKeyListener(this) {
		});
		endText.addKeyListener(new CommonKeyListener(this) {
		});

		startText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				if (startText.getText().trim().length() < 1)
					wiz.setMessage(ResourceManager
							.getResource("int.enter.start"));
			}

			public void focusLost(FocusEvent arg0) {
				if (startText.getText().trim().length() < 1) {
					wiz.setMessage(ResourceManager
							.getResource("int.start.empty"));
					wiz.setErrorIcon();
				}
			}
		});

		endText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				if (endText.getText().trim().length() < 1)
					wiz
							.setMessage(ResourceManager
									.getResource("int.enter.end"));
			}

			public void focusLost(FocusEvent arg0) {
				if (endText.getText().trim().length() < 1) {
					wiz
							.setMessage(ResourceManager
									.getResource("int.end.empty"));
					wiz.setErrorIcon();
				}
			}
		});

		refresh();
	}

	public Timestamp getBegin() {
		return start;
	}

	public Timestamp getEnd() {
		return end;
	}

	/**
	 * Get called when this panel get activated
	 */
	public void refresh() {
		finished = false;
		wiz.setFinishEnabled(false);

		try {
			start = new Timestamp(new SimpleDateFormat("yyyy/MM/dd").parse(
					startText.getText().trim()).getTime());
		} catch (ParseException e) {
			wiz.setMessage(ResourceManager.getResource("int.invalid.start"),
					false);
			wiz.setErrorIcon();
			return;
		}

		try {
			end = new Timestamp(new SimpleDateFormat("yyyy/MM/dd").parse(
					endText.getText().trim()).getTime());
		} catch (ParseException e) {
			wiz.setMessage(ResourceManager.getResource("int.invalid.end"),
					false);
			wiz.setErrorIcon();
			return;
		}

		if (start.compareTo(end) > 0) {
			wiz.setMessage(ResourceManager.getResource("int.invalid.0"), false);
			wiz.setErrorIcon();
		} else {
			finished = true;
			wiz.panelRefreshed(-1);
			wiz.setMessage("", true);
		}
	}

	public void checkInput() {
		refresh();
	}
}
