//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.data.element;

import de.esoco.lib.model.DataSet;
import de.esoco.lib.property.Color;
import de.esoco.lib.property.PropertyName;

import java.util.Set;


/********************************************************************
 * A data element that stores a {@link DataSet} for a chart.
 *
 * @author eso
 */
public class DataSetDataElement extends DataElement<DataSet<?>>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Redefinition of chart types to make them available on the server side.
	 */

	public enum ChartType { AREA, BAR, COLUMN, GEO_MAP, LINE, PIE, GAUGE }

	/********************************************************************
	 * Redefinition of legend positions to make them available on the server
	 * side.
	 */
	public enum LegendPosition { TOP, BOTTOM, LEFT, RIGHT, NONE }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * Enum property: the type of the chart to render (for possible values see
	 * {@link ChartType})
	 */
	public static final PropertyName<ChartType> CHART_TYPE =
		PropertyName.newEnumName("CHART_TYPE", ChartType.class);

	/**
	 * Enum property: the legend position of the chart (for possible values see
	 * {@link LegendPosition})
	 */
	public static final PropertyName<LegendPosition> CHART_LEGEND_POSITION =
		PropertyName.newEnumName("CHART_LEGEND_POSITION", LegendPosition.class);

	/** The chart background color */
	public static final PropertyName<Color> CHART_BACKGROUND =
		PropertyName.newName("CHART_BACKGROUND", Color.class);

	/** Boolean property: display chart in 3D */
	public static final PropertyName<Boolean> CHART_3D =
		PropertyName.newBooleanName("CHART_3D");

	//~ Instance fields --------------------------------------------------------

	private DataSet<?> rDataSet;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName    The name of the data element
	 * @param rDataSet The data set for the chart
	 * @param rFlags   The flags for this instance
	 */
	public DataSetDataElement(String	 sName,
							  DataSet<?> rDataSet,
							  Set<Flag>  rFlags)
	{
		super(sName, null, rFlags);

		this.rDataSet = rDataSet;
	}

	/***************************************
	 * Creates a new instance with certain display properties set.
	 *
	 * @param sName            The name of the data element
	 * @param aDataSet         The chart data
	 * @param eChartType       The chart type
	 * @param eLegendPosition  The legend position
	 * @param sBackgroundColor The chart background color
	 * @param b3D              TRUE for a 3D chart
	 */
	public DataSetDataElement(String		 sName,
							  DataSet<?>	 aDataSet,
							  ChartType		 eChartType,
							  LegendPosition eLegendPosition,
							  String		 sBackgroundColor,
							  boolean		 b3D)
	{
		this(sName, aDataSet, DISPLAY_FLAGS);

		setProperty(CHART_BACKGROUND, Color.valueOf(sBackgroundColor));
		setProperty(CHART_LEGEND_POSITION, eLegendPosition);
		setProperty(CHART_TYPE, eChartType);

		if (b3D)
		{
			setFlag(CHART_3D);
		}
	}

	/***************************************
	 * Default constructor for GWT serialization.
	 */
	DataSetDataElement()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * This method should be invoked to initialize the property name constants
	 * for de-serialization.
	 */
	public static void init()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public DataSetDataElement copy(CopyMode eMode, PropertyName<?>... rCopyProperties)
	{
		return (DataSetDataElement) super.copy(eMode, rCopyProperties);
	}

	/***************************************
	 * @see DataElement#getValue()
	 */
	@Override
	public DataSet<?> getValue()
	{
		return rDataSet;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected DataSetDataElement newInstance()
	{
		return new DataSetDataElement();
	}

	/***************************************
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected void updateValue(DataSet<?> rNewDataSet)
	{
		rDataSet = rNewDataSet;
	}
}
