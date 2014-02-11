package controlCenter;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * 	This is the commom key listener used through out the application.
 *  helpful to force any validation on key pressed/released/typed from
 *  one place over here only. Making use of KeyChangedInformer this is
 *  informing the observer/informer that a key has been processed.
 */
public class CommonKeyListener implements KeyListener {
	public static final int CHECK_CAPS = 00000001;

	public static final int CHECK_NUMERICK = 00000010;

	public static final int CHECK_ALPHA_NUMERICK = 00000100;

	public final int validation;

	private KeyChangedInformer informer;

	public CommonKeyListener(KeyChangedInformer informer) {
		this.informer = informer;
		validation = 0;
	}

	public CommonKeyListener(KeyChangedInformer informer, int validation) {
		this.informer = informer;
		this.validation = validation;
	}

	public void keyPressed(KeyEvent e) {
		informer.checkInput();
	}

	public void keyReleased(KeyEvent e) {
		informer.checkInput();
	}

	public void keyTyped(KeyEvent e) {
		if ((validation & CHECK_CAPS) > 0)
			e.setKeyChar((String.valueOf(e.getKeyChar()).toUpperCase()
					.toCharArray())[0]);

		// if(!Character.isISOControl(e.getKeyChar()))
		// e.setKeyChar((String.valueOf(e.getKeyChar()).toUpperCase().toCharArray())[0]);

		if ((validation & CHECK_ALPHA_NUMERICK) > 0)
			if (!Character.isLetterOrDigit(e.getKeyChar()))
				e.setKeyChar((String.valueOf(e.getKeyChar()).toUpperCase()
						.toCharArray())[0]);

		if ((validation & CHECK_NUMERICK) > 0)
			if (!Character.isDigit(e.getKeyChar()))
				e.setKeyChar((String.valueOf(e.getKeyChar()).toUpperCase()
						.toCharArray())[0]);

		// if(!Character.isWhitespace(e.getKeyChar()))
		// e.setKeyChar('_');
	}

}
