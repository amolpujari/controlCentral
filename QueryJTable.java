package controlCenter;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.TableModel;

/*
 * This class is created due to the bug in JTable: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4127936
 */
public class QueryJTable extends JTable {

		public QueryJTable(TableModel tableModel) {
				super(tableModel);
		}

    public boolean getScrollableTracksViewportWidth() {
				if (autoResizeMode != AUTO_RESIZE_OFF)
						if (getParent() instanceof JViewport)
								return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
						else
								return ((getParent()).getWidth() > getPreferredSize().width);
				return false;
    }
}