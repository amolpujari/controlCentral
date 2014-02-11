package controlCenter;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableModel;

public class AppJTable extends JTable {
	public AppJTable(TableModel tableModel) {
		super(tableModel);
	}

	public boolean getScrollableTracksViewportWidth() {
		if (autoResizeMode != AUTO_RESIZE_OFF) {
			if (getParent() instanceof JViewport) {
				return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
			} else
				return ((getParent()).getWidth() > getPreferredSize().width);
		}
		return false;
	}
}