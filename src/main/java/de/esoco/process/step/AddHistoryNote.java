//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.entity.Configuration;
import de.esoco.entity.Entity;
import de.esoco.entity.EntityFunctions;

import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;
import de.esoco.history.HistoryRecord.HistoryType;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.history.HistoryManager.DEFAULT_HISTORY_NOTE_TEMPLATES;
import static de.esoco.history.HistoryManager.HISTORY_NOTE_TEMPLATES;

import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE_ID;
import static de.esoco.lib.property.UserInterfaceProperties.ROWS;

import static de.esoco.process.ProcessRelationTypes.CONFIGURATION;
import static de.esoco.process.ProcessRelationTypes.HISTORY_TARGET_PARAM;

import static org.obrel.core.RelationTypes.newFlagType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * An interactive process step that adds a history note. Multiple target
 * entities for the history records can be stored in a parameter with the type
 * {@link #HISTORY_TARGETS}. Alternatively a single history target can either be
 * set directly in a parameter with the type {@link HistoryRecord#TARGET} or, if
 * none of the above parameters is set, the type of the parameter containing the
 * target must be stored in {@link ProcessRelationTypes#HISTORY_TARGET_PARAM}.
 *
 * @author eso
 */
public class AddHistoryNote extends Interaction
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A parameter containing a list of multiple history targets. This parameter
	 * has precedence over all other history target parameters.
	 */
	public static final RelationType<List<Entity>> HISTORY_TARGETS =
		RelationTypes.newListType();

	/**
	 * A reference to another parameter that contains the target entities for a
	 * history record.
	 */
	public static final RelationType<RelationType<? extends List<? extends Entity>>> HISTORY_TARGETS_PARAM =
		RelationTypes.newType();

	/**
	 * An optional input parameter that signals that the note input is optional.
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

	static
	{
		RelationTypes.init(AddHistoryNote.class);
	}

	//~ Instance fields --------------------------------------------------------

	private Map<String, String> aNoteTemplateMap;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public AddHistoryNote()
	{
		set(MetaTypes.TRANSACTIONAL);
		addDisplayParameters(HISTORY_NOTE_TARGETS);
		addInputParameters(HISTORY_NOTE_TEMPLATE,
						   HISTORY_NOTE_TITLE,
						   HISTORY_NOTE_VALUE);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to disallow rollback.
	 *
	 * @see RollbackStep#canRollback()
	 */
	@Override
	protected boolean canRollback()
	{
		return false;
	}

	/***************************************
	 * @see Interaction#execute()
	 */
	@Override
	protected void execute() throws Exception
	{
		RelationType<?> rInteractionParam = getInteractiveInputParameter();

		if (rInteractionParam == HISTORY_NOTE_TEMPLATE)
		{
			String sTitle = getParameter(HISTORY_NOTE_TEMPLATE);

			if (sTitle != null)
			{
				setParameter(HISTORY_NOTE_TITLE, sTitle);
				setParameter(HISTORY_NOTE_VALUE, aNoteTemplateMap.get(sTitle));
			}
		}
		else if (rInteractionParam == null)
		{
			List<Entity> rTargets = getHistoryTargets();
			String		 sTitle   = getParameter(HISTORY_NOTE_TITLE);

			if (sTitle != null && sTitle.length() > 0)
			{
				String sValue = getParameter(HISTORY_NOTE_VALUE);

				sValue = sTitle + '\n' + sValue;

				Entity rOrigin = getProcessUser();

				storeNotes(rOrigin, rTargets, sValue);

				setParameter(HISTORY_NOTE_TITLE, null);
				setParameter(HISTORY_NOTE_VALUE, null);
			}
		}
	}

	/***************************************
	 * @see ProcessStep#prepareParameters()
	 */
	@Override
	protected void prepareParameters() throws Exception
	{
		List<Entity> rTargets = getHistoryTargets();

		aNoteTemplateMap = new LinkedHashMap<String, String>();

		int nCount = rTargets.size();

		setParameter(HISTORY_NOTE_TARGETS,
					 CollectionUtil.toString(rTargets,
											 EntityFunctions.formatEntity(""),
											 ", "));

		if (nCount > 1)
		{
			setUIProperty(Math.min(nCount / 2, 5), ROWS, HISTORY_NOTE_TARGETS);
		}

		setUIProperty(RESOURCE_ID, "HistoryNoteValue", HISTORY_NOTE_VALUE);
		setUIProperty(-1, ROWS, HISTORY_NOTE_VALUE);
		setUIProperty(HTML_HEIGHT, "100%", HISTORY_NOTE_VALUE);
		setUIProperty(INTERACTIVE_INPUT_MODE,
					  InteractiveInputMode.CONTINUOUS,
					  HISTORY_NOTE_TEMPLATE);

		Configuration rConfiguration = getParameter(CONFIGURATION);

		if (rConfiguration != null)
		{
			Map<String, String> rHistoryTemplateMap =
				rConfiguration.getExtraAttribute(HISTORY_NOTE_TEMPLATES, null);

			if (rHistoryTemplateMap != null)
			{
				parseTemplates(aNoteTemplateMap,
							   rHistoryTemplateMap.get(getProcess().get(StandardTypes.NAME)));
				parseTemplates(aNoteTemplateMap,
							   rHistoryTemplateMap.get(DEFAULT_HISTORY_NOTE_TEMPLATES));
			}
			else
			{
				aNoteTemplateMap.put("Interne Änderung", "");
				aNoteTemplateMap.put("DNS-Änderung", "Alt:\nNeu:");
				aNoteTemplateMap.put("Kundenkontakt", "");
			}
		}

		setAllowedValues(HISTORY_NOTE_TEMPLATE, aNoteTemplateMap.keySet());

		if (!hasFlagParameter(HISTORY_NOTE_OPTIONAL))
		{
			setParameterNotEmptyValidations(HISTORY_NOTE_TITLE);
		}
	}

	/***************************************
	 * Stores history notes on a list of target entities.
	 *
	 * @param  rOrigin  The origin of the note
	 * @param  rTargets The target entities
	 * @param  sNote    The note string
	 *
	 * @throws Exception If storing a history record fails
	 */
	protected void storeNotes(Entity				 rOrigin,
							  List<? extends Entity> rTargets,
							  String				 sNote) throws Exception
	{
		for (Entity rTarget : rTargets)
		{
			HistoryManager.record(HistoryType.NOTE, rOrigin, rTarget, sNote);
		}
	}

	/***************************************
	 * Retrieves the list of history targets from the process parameters.
	 *
	 * @return The history targets
	 *
	 * @throws ProcessException If no history target is available
	 */
	@SuppressWarnings("unchecked")
	private List<Entity> getHistoryTargets() throws ProcessException
	{
		List<Entity> rTargets = getParameter(HISTORY_TARGETS);

		if (rTargets == null || rTargets.isEmpty())
		{
			RelationType<? extends List<? extends Entity>> rHistoryTargetsParam =
				getParameter(HISTORY_TARGETS_PARAM);

			if (rHistoryTargetsParam != null)
			{
				rTargets = (List<Entity>) getParameter(rHistoryTargetsParam);
			}

			if (rTargets == null || rTargets.isEmpty())
			{
				Entity rTarget = getParameter(HistoryRecord.TARGET);

				if (rTarget == null)
				{
					rTarget =
						getParameter(checkParameter(HISTORY_TARGET_PARAM));

					if (rTarget == null)
					{
						throw new ProcessException(this,
												   "MissingHistoryNoteTarget");
					}
				}

				rTargets = Arrays.asList(rTarget);
			}
		}

		return rTargets;
	}

	/***************************************
	 * Parses a raw template string into a map.
	 *
	 * @param rTemplateMap  The target template map
	 * @param sRawTemplates The raw templates string to parse
	 */
	private void parseTemplates(
		Map<String, String> rTemplateMap,
		String				sRawTemplates)
	{
		if (sRawTemplates != null)
		{
			String[] aTemplates = sRawTemplates.split("\n");

			for (String sTemplate : aTemplates)
			{
				int    nSeparator = sTemplate.indexOf('|');
				String sTitle     = sTemplate.substring(0, nSeparator);
				String sValue     = sTemplate.substring(nSeparator + 1);

				sValue = sValue.replaceAll("\r", "");
				sValue = sValue.replaceAll("\\$n", "\n");

				rTemplateMap.put(sTitle, sValue);
			}
		}
	}
}
