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
package de.esoco.process.ui.view;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


/********************************************************************
 * Enumeration of the available actions for dialog fragments. Actions have a
 * flag that defines whether the fragment needs to be validated when the dialog
 * is closed with the respective action.
 */
public enum UiDialogAction
{
	OK(true), YES(true), APPLY(true), SAVE(true), START(true), SEND(true),
	LOGIN(true), CANCEL(false), NO(false), CLOSE(false);

	//~ Static fields/initializers ---------------------------------------------

	/** A standard set of the {@link #OK} and {@link #CANCEL} actions. */
	public static final Set<UiDialogAction> OK_CANCEL =
		Collections.unmodifiableSet(EnumSet.of(OK, CANCEL));

	/** A standard set of the {@link #YES} and {@link #NO} actions. */
	public static final Set<UiDialogAction> YES_NO =
		Collections.unmodifiableSet(EnumSet.of(YES, NO));

	/**
	 * A standard set of the {@link #YES}, {@link #NO}, and {@link #CANCEL}
	 * actions.
	 */
	public static final Set<UiDialogAction> YES_NO_CANCEL =
		Collections.unmodifiableSet(EnumSet.of(YES, NO, CANCEL));

	//~ Instance fields --------------------------------------------------------

	private final boolean bValidated;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param bValidated TRUE if the dialog needs to be validated if closed by
	 *                   this action
	 */
	UiDialogAction(boolean bValidated)
	{
		this.bValidated = bValidated;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Checks whether this action needs a dialog validation.
	 *
	 * @return TRUE if the dialog needs to be validated if closed by this action
	 */
	public final boolean isValidated()
	{
		return bValidated;
	}
}
