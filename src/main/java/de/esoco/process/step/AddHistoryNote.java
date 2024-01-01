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

import de.esoco.entity.Entity;
import de.esoco.entity.EntityFunctions;
import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;
import de.esoco.history.HistoryRecord.HistoryType;
import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessRelationTypes;
import org.obrel.core.ProvidesConfiguration;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.esoco.history.HistoryManager.DEFAULT_HISTORY_NOTE_TEMPLATES;
import static de.esoco.history.HistoryManager.HISTORY_NOTE_TEMPLATES;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.StateProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.process.ProcessRelationTypes.CONFIGURATION;
import static de.esoco.process.ProcessRelationTypes.HISTORY_TARGET_PARAM;
import static org.obrel.core.RelationTypes.newFlagType;
import static org.obrel.core.RelationTypes.newType;

/**
 * An interactive process step that adds a history note. Multiple target
 * entities for the history records can be stored in a parameter with the type
 * {@link #HISTORY_TARGETS}. Alternatively a single history target can either be
 * set directly in a parameter with the type {@link HistoryRecord#TARGET} or, if
 * none of the above parameters is set, the type of the parameter containing the
 * target must be stored in {@link ProcessRelationTypes#HISTORY_TARGET_PARAM}.
 *
 * @author eso
 */
public class AddHistoryNote extends Interaction {

	/**
	 * A parameter containing a list of multiple history targets. This
	 * parameter
	 * has precedence over all other history target parameters.
	 */
	public static final RelationType<List<Entity>> HISTORY_TARGETS =
		RelationTypes.newListType();

	/**
	 * A reference to another parameter that contains the target entities for a
	 * history record.
	 */
	public static final RelationType<RelationType<? extends List<?
		extends Entity>>>
		HISTORY_TARGETS_PARAM = RelationTypes.newType();

	/**
	 * An optional input parameter that signals that the note input is
	 * optional.
	 */
	public static final RelationType<Boolean> HISTORY_NOTE_OPTIONAL =
		newFlagType();

	/**
	 * A string parameter that is used for the selection of history entry
	 * templates.
	 */
	public static final RelationType<String> HISTORY_NOTE_TARGETS = newType();

	/**
	 * A string parameter that is used for the selection of history entry
	 * templates.
	 */
	public static final RelationType<String> HISTORY_NOTE_TEMPLATE = newType();

	/**
	 * A string parameter that is used for the input of the first line of the
	 * history entry.
	 */
	public static final RelationType<String> HISTORY_NOTE_TITLE = newType();

	/**
	 * A string parameter that contains the additional text of the history
	 * entry.
	 */
	public static final RelationType<String> HISTORY_NOTE_VALUE = newType();

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(AddHistoryNote.class);
	}

	private Map<String, String> noteTemplateMap;

	/**
	 * Creates a new instance.
	 */
	public AddHistoryNote() {
		set(MetaTypes.TRANSACTIONAL);
		addDisplayParameters(HISTORY_NOTE_TARGETS);
		addInputParameters(HISTORY_NOTE_TEMPLATE, HISTORY_NOTE_TITLE,
			HISTORY_NOTE_VALUE);
	}

	/**
	 * Overridden to disallow rollback.
	 *
	 * @see RollbackStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return false;
	}

	/**
	 * @see Interaction#execute()
	 */
	@Override
	protected void execute() throws Exception {
		RelationType<?> interactionParam = getInteractiveInputParameter();

		if (interactionParam == HISTORY_NOTE_TEMPLATE) {
			String title = getParameter(HISTORY_NOTE_TEMPLATE);

			if (title != null) {
				setParameter(HISTORY_NOTE_TITLE, title);
				setParameter(HISTORY_NOTE_VALUE, noteTemplateMap.get(title));
			}
		} else if (interactionParam == null) {
			List<Entity> targets = getHistoryTargets();
			String title = getParameter(HISTORY_NOTE_TITLE);

			if (title != null && title.length() > 0) {
				String value = getParameter(HISTORY_NOTE_VALUE);

				value = title + '\n' + value;

				Entity origin = getProcessUser();

				storeNotes(origin, targets, value);

				setParameter(HISTORY_NOTE_TITLE, null);
				setParameter(HISTORY_NOTE_VALUE, null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareParameters() throws Exception {
		List<Entity> targets = getHistoryTargets();

		noteTemplateMap = new LinkedHashMap<String, String>();

		int count = targets.size();

		setParameter(HISTORY_NOTE_TARGETS,
			CollectionUtil.toString(targets, EntityFunctions.formatEntity(""),
				", "));

		if (count > 1) {
			setUIProperty(Math.min(count / 2, 5), ROWS, HISTORY_NOTE_TARGETS);
		}

		setUIProperty(RESOURCE_ID, "HistoryNoteValue", HISTORY_NOTE_VALUE);
		setUIProperty(-1, ROWS, HISTORY_NOTE_VALUE);
		setUIProperty(HTML_HEIGHT, "100%", HISTORY_NOTE_VALUE);
		setUIProperty(INTERACTIVE_INPUT_MODE, InteractiveInputMode.CONTINUOUS,
			HISTORY_NOTE_TEMPLATE);

		ProvidesConfiguration configuration = getParameter(CONFIGURATION);

		if (configuration != null) {
			Map<String, String> historyTemplateMap =
				configuration.getConfigValue(HISTORY_NOTE_TEMPLATES, null);

			if (historyTemplateMap != null) {
				parseTemplates(noteTemplateMap, historyTemplateMap.get(
					getProcess().get(StandardTypes.NAME)));
				parseTemplates(noteTemplateMap,
					historyTemplateMap.get(DEFAULT_HISTORY_NOTE_TEMPLATES));
			} else {
				noteTemplateMap.put("Interne Änderung", "");
				noteTemplateMap.put("DNS-Änderung", "Alt:\nNeu:");
				noteTemplateMap.put("Kundenkontakt", "");
			}
		}

		setAllowedValues(HISTORY_NOTE_TEMPLATE, noteTemplateMap.keySet());

		if (!hasFlagParameter(HISTORY_NOTE_OPTIONAL)) {
			setParameterNotEmptyValidations(HISTORY_NOTE_TITLE);
		}
	}

	/**
	 * Stores history notes on a list of target entities.
	 *
	 * @param origin  The origin of the note
	 * @param targets The target entities
	 * @param note    The note string
	 * @throws Exception If storing a history record fails
	 */
	protected void storeNotes(Entity origin, List<? extends Entity> targets,
		String note) throws Exception {
		for (Entity target : targets) {
			HistoryManager.record(HistoryType.NOTE, origin, target, note);
		}
	}

	/**
	 * Retrieves the list of history targets from the process parameters.
	 *
	 * @return The history targets
	 * @throws ProcessException If no history target is available
	 */
	@SuppressWarnings("unchecked")
	private List<Entity> getHistoryTargets() throws ProcessException {
		List<Entity> targets = getParameter(HISTORY_TARGETS);

		if (targets == null || targets.isEmpty()) {
			RelationType<? extends List<? extends Entity>> historyTargetsParam =
				getParameter(HISTORY_TARGETS_PARAM);

			if (historyTargetsParam != null) {
				targets = (List<Entity>) getParameter(historyTargetsParam);
			}

			if (targets == null || targets.isEmpty()) {
				Entity target = getParameter(HistoryRecord.TARGET);

				if (target == null) {
					target =
						getParameter(checkParameter(HISTORY_TARGET_PARAM));

					if (target == null) {
						throw new ProcessException(this,
							"MissingHistoryNoteTarget");
					}
				}

				targets = Collections.singletonList(target);
			}
		}

		return targets;
	}

	/**
	 * Parses a raw template string into a map.
	 *
	 * @param templateMap  The target template map
	 * @param rawTemplates The raw templates string to parse
	 */
	private void parseTemplates(Map<String, String> templateMap,
		String rawTemplates) {
		if (rawTemplates != null) {
			String[] templates = rawTemplates.split("\n");

			for (String template : templates) {
				int separator = template.indexOf('|');
				String title = template.substring(0, separator);
				String value = template.substring(separator + 1);

				value = value.replaceAll("\r", "");
				value = value.replaceAll("\\$n", "\n");

				templateMap.put(title, value);
			}
		}
	}
}
