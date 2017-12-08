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
package de.esoco.process;

import de.esoco.data.FileType;

import de.esoco.lib.expression.Function;

import de.esoco.process.step.InteractionFragment;

import java.util.Collection;

import org.obrel.core.RelationType;


/********************************************************************
 * A parameter wrapper with additional functions for enum values.
 *
 * @author eso
 */
public class EnumParameter<E extends Enum<E>>
	extends ParameterBase<E, EnumParameter<E>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see ParameterBase#ParameterBase(InteractionFragment, RelationType)
	 */
	public EnumParameter(
		InteractionFragment rFragment,
		RelationType<E>		rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Disables certain values of the parameter enum.
	 *
	 * @param  rDisabledElements The elements to disable
	 *
	 * @return This parameter instance
	 */
	@SuppressWarnings("unchecked")
	public EnumParameter<E> disable(E... rDisabledElements)
	{
		fragment().disableElements(type(), rDisabledElements);

		return this;
	}

	/***************************************
	 * Disables certain values of the parameter enum.
	 *
	 * @param  rDisabledElements A collection of the elements to disable
	 *
	 * @return This parameter instance
	 */
	public EnumParameter<E> disable(Collection<E> rDisabledElements)
	{
		fragment().disableElements(type(), rDisabledElements);

		return this;
	}

	/***************************************
	 * Enables all enum values.
	 *
	 * @return This parameter instance
	 */
	public EnumParameter<E> enableAll()
	{
		fragment().enableAllElements(type());

		return this;
	}

	/***************************************
	 * Prepares a download that is associated with an event on this enum
	 * parameter. This method must be invoked during the handling of the event
	 * and the download will then be executed as the result of the event. After
	 * the being processed by the process interaction the generated download URL
	 * will be removed from the parameter.
	 *
	 * @param  sFileName          The file name of the download
	 * @param  eFileType          The file type of the download
	 * @param  fDownloadGenerator The function that generated the download data
	 *
	 * @throws Exception If the download preparation fails
	 */
	public void prepareDownload(String				  sFileName,
								FileType			  eFileType,
								Function<FileType, ?> fDownloadGenerator)
		throws Exception
	{
		initiateDownload(this, sFileName, eFileType, fDownloadGenerator);
	}
}
