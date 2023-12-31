//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.step;

import de.esoco.lib.event.EditListener;
import de.esoco.lib.event.EditListener.EditAction;
import de.esoco.lib.expression.Action;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.Orientation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.HEIGHT;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StyleProperties.EDITABLE;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.ORIENTATION;
import static de.esoco.lib.property.StyleProperties.STYLE;

/**
 * A fragment that displays a text from a certain parameter and allows to edit
 * it.
 *
 * @author eso
 */
public class EditText extends InteractionFragment {

	/**
	 * Enumeration of the available actions for the display or edited text.
	 */
	enum TextAction {NEW, EDIT}

	private static final long serialVersionUID = 1L;

	private final RelationType<String> rValueParam;

	private final EditListener<String> rEditListener;

	private boolean bAllowCreate;

	private boolean bAllowEdit;

	private RelationType<List<RelationType<?>>> aActionPanelParam;

	private RelationType<TextAction> aTextActionParam;

	private RelationType<EditAction> aEditActionParam;

	private RelationType<String> aEditInfoParam;

	private List<RelationType<?>> aInteractionParams;

	private List<RelationType<?>> aInputParams;

	private List<RelationType<?>> aActionPanelParams;

	private Map<RelationType<? extends Enum<?>>, Action<?>> aAdditionalActions;

	/**
	 * Creates a new instance.
	 *
	 * @param rValueParam   The parameter that stores the edited value
	 * @param rEditListener An optional listener for edit events or NULL for
	 *                      none
	 * @param bAllowCreate  TRUE to add an action to create new values, FALSE
	 *                      for edit only
	 * @param bAllowEdit    TRUE to allow the editing of the displayed value
	 */
	public EditText(RelationType<String> rValueParam,
		EditListener<String> rEditListener, boolean bAllowCreate,
		boolean bAllowEdit) {
		this.rValueParam = rValueParam;
		this.rEditListener = rEditListener;
		this.bAllowCreate = bAllowCreate;
		this.bAllowEdit = bAllowEdit;
	}

	/**
	 * Adds an additional action that should be displayed in this fragment. The
	 * initialization of the action parameter and the handling of occurring
	 * actions must be performed by the code that defines the action. The
	 * second
	 * argument is an action function that will be invoked to handle the action
	 * parameter. This may be NULL if the action is handled in a subsequent
	 * process step because it is defined as a continuation parameter.
	 *
	 * <p>Action parameters should be added immediately after creating an
	 * instance of this class but latest before it's {@link #setup()} method is
	 * invoked.</p>
	 *
	 * @param rActionParam The parameter relation type for the action enum
	 * @param fHandler     The handler for the action or NULL for none
	 */
	public <E extends Enum<E>> void addAction(RelationType<E> rActionParam,
		Action<E> fHandler) {
		if (aAdditionalActions == null) {
			aAdditionalActions = new LinkedHashMap<>();
		}

		aAdditionalActions.put(rActionParam, fHandler);
	}

	/**
	 * Activates or deactivates the possibility to edit the text.
	 *
	 * @param bAllow TRUE to allow editing, FALSE to disable the action
	 */
	public void allowEdit(boolean bAllow) {
		bAllowEdit = bAllow;

		if (bAllow) {
			enableAllElements(aTextActionParam);
		} else {
			disableElements(aTextActionParam, TextAction.EDIT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return aInputParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return aInteractionParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception {
		if (rInteractionParam == rValueParam) {
			if (bAllowEdit && !hasUIFlag(EDITABLE, rValueParam)) {
				startEditing(false);
			}
		} else if (rInteractionParam == aTextActionParam) {
			startEditing(getParameter(aTextActionParam) == TextAction.NEW);
		} else if (rInteractionParam == aEditActionParam) {
			stopEditing(getParameter(aEditActionParam));
		} else {
			for (RelationType<? extends Enum<?>> rActionParam :
				aAdditionalActions.keySet()) {
				if (rInteractionParam == rActionParam) {
					@SuppressWarnings("unchecked")
					Action<Enum<?>> fHandler =
						(Action<Enum<?>>) aAdditionalActions.get(rActionParam);

					if (fHandler != null) {
						fHandler.execute(getParameter(rActionParam));
					}

					break;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		setInteractive(aTextActionParam, null, ListStyle.IMMEDIATE);
		setInteractive(aEditActionParam, null, ListStyle.IMMEDIATE);

		setUIFlag(HIDE_LABEL, rValueParam, aEditInfoParam);
		setUIFlag(HIDE_LABEL, aTextActionParam, aEditActionParam);
		setUIFlag(HAS_IMAGES, aTextActionParam, aEditActionParam);
		setUIFlag(SAME_ROW, aEditActionParam, aEditInfoParam);

		if (aAdditionalActions != null) {
			setUIFlag(SAME_ROW, aAdditionalActions.keySet());
		}

		if (bAllowCreate) {
			setUIProperty(2, COLUMNS, aTextActionParam);
		} else {
			setAllowedValues(aTextActionParam, TextAction.EDIT);
		}

		setUIProperty(-1, ROWS, rValueParam);
		setUIProperty(2, COLUMNS, aEditActionParam);

		setUIProperty(RESOURCE_ID, "EditTextActions", aActionPanelParam);
		setUIProperty(RESOURCE_ID, "EditTextTextAction", aTextActionParam);
		setUIProperty(RESOURCE_ID, "EditTextEditAction", aEditActionParam);
		setUIProperty(RESOURCE_ID, "EditTextInfo", aEditInfoParam);

		setUIProperty(STYLE, "EditTextValue", rValueParam);
		setUIProperty(STYLE, "EditTextActions", aActionPanelParam);

		addPanel(aActionPanelParam, LayoutType.TABLE, aActionPanelParams);

		setUIProperty(34, HEIGHT, aActionPanelParam);
		setUIProperty(ORIENTATION, Orientation.VERTICAL,
			getFragmentParameter());
		setLayout(LayoutType.DOCK, getFragmentParameter());

		setEditInfo("");
		stopEditing(null);
	}

	/**
	 * Checks whether this instance is currently in edit mode.
	 *
	 * @return TRUE if edit mode is active
	 */
	public boolean isEditing() {
		return hasUIFlag(EDITABLE, rValueParam);
	}

	/**
	 * Sets an info string that describes the currently displayed or edited
	 * data.
	 *
	 * @param sInfo The info string (empty to clear)
	 */
	public void setEditInfo(String sInfo) {
		setVisible(sInfo.length() > 0, aEditInfoParam);
		setParameter(aEditInfoParam, sInfo);
	}

	/**
	 * Creates the temporary interaction parameters of this instance.
	 */
	@Override
	public void setup() {
		String sName = rValueParam.getSimpleName();

		aActionPanelParam =
			getTemporaryListType(sName + "_ACTION_PANEL", RelationType.class);
		aTextActionParam =
			getTemporaryParameterType(sName + "_TEXT_ACTION",
				TextAction.class);
		aEditActionParam =
			getTemporaryParameterType(sName + "_EDIT_ACTION",
				EditAction.class);
		aEditInfoParam =
			getTemporaryParameterType(sName + "_EDIT_INFO", String.class);

		aInteractionParams = params(rValueParam, aActionPanelParam);
		aInputParams = params(rValueParam, aActionPanelParam);
		aActionPanelParams =
			params(aTextActionParam, aEditActionParam, aEditInfoParam);

		aInputParams.add(aTextActionParam);
		aInputParams.add(aEditActionParam);

		if (aAdditionalActions != null) {
			aActionPanelParams.addAll(aAdditionalActions.keySet());
			aInputParams.addAll(aAdditionalActions.keySet());
		}
	}

	/**
	 * Starts editing the text.
	 *
	 * @param bNewValue TRUE if a new value should be created
	 */
	public void startEditing(boolean bNewValue) {
		if (aAdditionalActions != null) {
			setVisible(false, aAdditionalActions.keySet());
		}

		setVisible(false, aTextActionParam);
		setVisible(true, aEditActionParam);
		setUIFlag(EDITABLE, rValueParam);

		if (bNewValue) {
			setParameter(rValueParam, "");
		}

		if (rEditListener != null) {
			rEditListener.editStarted(
				bNewValue ? null : getParameter(rValueParam));
		}
	}

	/**
	 * Starts editing the value.
	 *
	 * @param eAction The action to stop the editing with
	 */
	public void stopEditing(EditAction eAction) {
		if (eAction != null && rEditListener != null) {
			rEditListener.editFinished(getParameter(rValueParam), eAction);
		}

		clearUIFlag(EDITABLE, rValueParam);
		setVisible(false, aEditActionParam);
		setVisible(true, aTextActionParam);

		if (aAdditionalActions != null) {
			setVisible(true, aAdditionalActions.keySet());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void abort() {
		stopEditing(EditAction.CANCEL);
	}
}
