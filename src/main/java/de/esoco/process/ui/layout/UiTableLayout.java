package de.esoco.process.ui.layout;

import de.esoco.lib.property.LayoutType;
import de.esoco.process.ui.UiLayout;

/**
 * A layout that is based on an HTML table structure. This should not be used
 * for the outer layout structure of an application because that will typically
 * result in layout errors if multiple tables are used in such a structure.
 *
 * @author eso
 */
public class UiTableLayout extends UiLayout {

	/**
	 * Creates a new instance.
	 *
	 * @param nColumns The number of layout columns
	 */
	public UiTableLayout(int nColumns) {
		super(LayoutType.TABLE, nColumns);
	}
}