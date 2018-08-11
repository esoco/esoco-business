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
package de.esoco.process.param;

import de.esoco.data.element.DataSetDataElement.ChartType;
import de.esoco.data.element.DataSetDataElement.LegendPosition;

import de.esoco.lib.model.DataSet;

import de.esoco.process.step.InteractionFragment;

import org.obrel.core.RelationType;

import static de.esoco.data.element.DataSetDataElement.CHART_3D;
import static de.esoco.data.element.DataSetDataElement.CHART_LEGEND_POSITION;
import static de.esoco.data.element.DataSetDataElement.CHART_TYPE;


/********************************************************************
 * A parameter wrapper with additional functions for {@link DataSet} values.
 *
 * @author eso
 */
public class DataSetParameter<T, D extends DataSet<T>>
	extends ParameterBase<D, DataSetParameter<T, D>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see ParameterBase#ParameterBase(InteractionFragment, RelationType)
	 */
	public DataSetParameter(
		InteractionFragment rFragment,
		RelationType<D>		rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the chart type.
	 *
	 * @param  eChartType eLegendPosition The chart type
	 *
	 * @return This parameter instance for concatenation
	 */
	public DataSetParameter<T, D> chartType(ChartType eChartType)
	{
		return set(CHART_TYPE, eChartType);
	}

	/***************************************
	 * Enables the chart legend and sets it'S position.
	 *
	 * @param  eLegendPosition The legend position
	 *
	 * @return This parameter instance for concatenation
	 */
	public DataSetParameter<T, D> legend(LegendPosition eLegendPosition)
	{
		return set(CHART_LEGEND_POSITION, eLegendPosition);
	}

	/***************************************
	 * Sets whether the chart should be displayed in 3D (if suported by the
	 * chart type).
	 *
	 * @param  b3D TRUE for a 3D chart, FALSE for 2D
	 *
	 * @return This parameter instance for concatenation
	 */
	@SuppressWarnings("boxing")
	public DataSetParameter<T, D> set3D(boolean b3D)
	{
		return set(CHART_3D, b3D);
	}
}
