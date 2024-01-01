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
import org.obrel.core.RelationType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	private final RelationType<String> valueParam;

	private final EditListener<String> editListener;

	private final boolean allowCreate;

	private boolean allowEdit;

	private RelationType<List<RelationType<?>>> actionPanelParam;

	private RelationType<TextAction> textActionParam;

	private RelationType<EditAction> editActionParam;

	private RelationType<String> editInfoParam;

	private List<RelationType<?>> interactionParams;

	private List<RelationType<?>> inputParams;

	private List<RelationType<?>> actionPanelParams;

	private Map<RelationType<? extends Enum<?>>, Action<?>> additionalActions;

	/**
	 * Creates a new instance.
	 *
	 * @param valueParam   The parameter that stores the edited value
	 * @param editListener An optional listener for edit events or NULL for
	 *                     none
	 * @param allowCreate  TRUE to add an action to create new values, FALSE
	 *                       for
	 *                     edit only
	 * @param allowEdit    TRUE to allow the editing of the displayed value
	 */
	public EditText(RelationType<String> valueParam,
		EditListener<String> editListener, boolean allowCreate,
		boolean allowEdit) {
		this.valueParam = valueParam;
		this.editListener = editListener;
		this.allowCreate = allowCreate;
		this.allowEdit = allowEdit;
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
	 * @param actionParam The parameter relation type for the action enum
	 * @param handler     The handler for the action or NULL for none
	 */
	public <E extends Enum<E>> void addAction(RelationType<E> actionParam,
		Action<E> handler) {
		if (additionalActions == null) {
			additionalActions = new LinkedHashMap<>();
		}

		additionalActions.put(actionParam, handler);
	}

	/**
	 * Activates or deactivates the possibility to edit the text.
	 *
	 * @param allow TRUE to allow editing, FALSE to disable the action
	 */
	public void allowEdit(boolean allow) {
		allowEdit = allow;

		if (allow) {
			enableAllElements(textActionParam);
		} else {
			disableElements(textActionParam, TextAction.EDIT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return inputParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return interactionParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		if (interactionParam == valueParam) {
			if (allowEdit && !hasUIFlag(EDITABLE, valueParam)) {
				startEditing(false);
			}
		} else if (interactionParam == textActionParam) {
			startEditing(getParameter(textActionParam) == TextAction.NEW);
		} else if (interactionParam == editActionParam) {
			stopEditing(getParameter(editActionParam));
		} else {
			for (RelationType<? extends Enum<?>> actionParam :
				additionalActions.keySet()) {
				if (interactionParam == actionParam) {
					@SuppressWarnings("unchecked")
					Action<Enum<?>> handler =
						(Action<Enum<?>>) additionalActions.get(actionParam);

					if (handler != null) {
						handler.execute(getParameter(actionParam));
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
		setInteractive(textActionParam, null, ListStyle.IMMEDIATE);
		setInteractive(editActionParam, null, ListStyle.IMMEDIATE);

		setUIFlag(HIDE_LABEL, valueParam, editInfoParam);
		setUIFlag(HIDE_LABEL, textActionParam, editActionParam);
		setUIFlag(HAS_IMAGES, textActionParam, editActionParam);
		setUIFlag(SAME_ROW, editActionParam, editInfoParam);

		if (additionalActions != null) {
			setUIFlag(SAME_ROW, additionalActions.keySet());
		}

		if (allowCreate) {
			setUIProperty(2, COLUMNS, textActionParam);
		} else {
			setAllowedValues(textActionParam, TextAction.EDIT);
		}

		setUIProperty(-1, ROWS, valueParam);
		setUIProperty(2, COLUMNS, editActionParam);

		setUIProperty(RESOURCE_ID, "EditTextActions", actionPanelParam);
		setUIProperty(RESOURCE_ID, "EditTextTextAction", textActionParam);
		setUIProperty(RESOURCE_ID, "EditTextEditAction", editActionParam);
		setUIProperty(RESOURCE_ID, "EditTextInfo", editInfoParam);

		setUIProperty(STYLE, "EditTextValue", valueParam);
		setUIProperty(STYLE, "EditTextActions", actionPanelParam);

		addPanel(actionPanelParam, LayoutType.TABLE, actionPanelParams);

		setUIProperty(34, HEIGHT, actionPanelParam);
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
		return hasUIFlag(EDITABLE, valueParam);
	}

	/**
	 * Sets an info string that describes the currently displayed or edited
	 * data.
	 *
	 * @param info The info string (empty to clear)
	 */
	public void setEditInfo(String info) {
		setVisible(info.length() > 0, editInfoParam);
		setParameter(editInfoParam, info);
	}

	/**
	 * Creates the temporary interaction parameters of this instance.
	 */
	@Override
	public void setup() {
		String name = valueParam.getSimpleName();

		actionPanelParam =
			getTemporaryListType(name + "_ACTION_PANEL", RelationType.class);
		textActionParam =
			getTemporaryParameterType(name + "_TEXT_ACTION", TextAction.class);
		editActionParam =
			getTemporaryParameterType(name + "_EDIT_ACTION", EditAction.class);
		editInfoParam =
			getTemporaryParameterType(name + "_EDIT_INFO", String.class);

		interactionParams = params(valueParam, actionPanelParam);
		inputParams = params(valueParam, actionPanelParam);
		actionPanelParams =
			params(textActionParam, editActionParam, editInfoParam);

		inputParams.add(textActionParam);
		inputParams.add(editActionParam);

		if (additionalActions != null) {
			actionPanelParams.addAll(additionalActions.keySet());
			inputParams.addAll(additionalActions.keySet());
		}
	}

	/**
	 * Starts editing the text.
	 *
	 * @param newValue TRUE if a new value should be created
	 */
	public void startEditing(boolean newValue) {
		if (additionalActions != null) {
			setVisible(false, additionalActions.keySet());
		}

		setVisible(false, textActionParam);
		setVisible(true, editActionParam);
		setUIFlag(EDITABLE, valueParam);

		if (newValue) {
			setParameter(valueParam, "");
		}

		if (editListener != null) {
			editListener.editStarted(
				newValue ? null : getParameter(valueParam));
		}
	}

	/**
	 * Starts editing the value.
	 *
	 * @param action The action to stop the editing with
	 */
	public void stopEditing(EditAction action) {
		if (action != null && editListener != null) {
			editListener.editFinished(getParameter(valueParam), action);
		}

		clearUIFlag(EDITABLE, valueParam);
		setVisible(false, editActionParam);
		setVisible(true, textActionParam);

		if (additionalActions != null) {
			setVisible(true, additionalActions.keySet());
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
