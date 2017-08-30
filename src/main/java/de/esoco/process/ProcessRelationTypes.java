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

import de.esoco.data.element.DataElement;
import de.esoco.data.process.ProcessState;

import de.esoco.entity.Entity;

import de.esoco.history.HistoryManager;

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Function;
import de.esoco.lib.net.ExternalServiceAccess;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.Updatable;

import de.esoco.process.step.InteractionFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.ProvidesConfiguration;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.ListenerType;
import org.obrel.type.MetaTypes;

import static de.esoco.entity.EntityRelationTypes.newEntityAttribute;

import static org.obrel.core.RelationTypes.newFlagType;
import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newIntType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newSetType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Contains process-specific relation type declarations.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.process")
public class ProcessRelationTypes
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * An enumeration of generic actions that can be applied to lists of
	 * objects.
	 */
	public enum ListAction
	{
		ADD_ALL, ADD_SELECTED, REMOVE_SELECTED, REMOVE_ALL
	}

	//~ Static fields/initializers ---------------------------------------------

	/** The process itself, to allow generic access as a parameter. */
	public static final RelationType<Process> PROCESS = newType();

	/** A unique process ID. */
	public static final RelationType<Integer> PROCESS_ID = newType();

	/** The process name. */
	public static final RelationType<String> PROCESS_NAME = newType();

	/** An information string describing the process or it's current state. */
	public static final RelationType<String> PROCESS_INFO = newType();

	/**
	 * A reference to a {@link ProcessExecutor} instance that can be used to run
	 * additional processes.
	 */
	public static final RelationType<ProcessExecutor> PROCESS_EXECUTOR =
		newType();

	/**
	 * A list of process states that will be evaluated to spawn new processes
	 * separate from the current process context. The process states are
	 * typically created by a {@link ProcessExecutor} instance that can be
	 * queried from the process parameter {@link #PROCESS_EXECUTOR}. This list
	 * will be cleared by an interaction (which will first spawn the new
	 * processes).
	 */
	public static final RelationType<List<ProcessState>> SPAWN_PROCESSES =
		newListType();

	/** A reference to a process definition. */
	public static final RelationType<ProcessDefinition> PROCESS_DEFINITION =
		newType();

	/**
	 * The definition of a process that shall be executed as a sub-process of
	 * the current process.
	 */
	public static final RelationType<ProcessDefinition> SUB_PROCESS_DEFINITION =
		newType();

	/**
	 * A flag that indicates that a sub-process should run in it's own context
	 * instead of the parent process.
	 */
	public static final RelationType<Boolean> SUB_PROCESS_SEPARATE_CONTEXT =
		newType();

	/** The (person) entity of the user that executes the process. */
	public static final RelationType<Entity> PROCESS_USER =
		newEntityAttribute();

	/** The locale of a process instance. */
	public static final RelationType<Locale> PROCESS_LOCALE = newType();

	/** The information about the client (web browser) of the process user. */
	public static final RelationType<String> CLIENT_INFO = newType();

	/** The information about the current user's locale. */
	public static final RelationType<Locale> CLIENT_LOCALE = newType();

	/** The width of the client area available to a process. */
	public static final RelationType<Integer> CLIENT_WIDTH = newType();

	/** The height of the client area available to a process. */
	public static final RelationType<Integer> CLIENT_HEIGHT = newType();

	/** The start time of the process. */
	public static final RelationType<Date> PROCESS_START_TIME = newType();

	/** The last time the process has been suspended. */
	public static final RelationType<Date> PROCESS_SUSPEND_TIME = newType();

	/**
	 * If the process execution failed the causing exception will be stored in
	 * this parameter.
	 */
	public static final RelationType<ProcessException> PROCESS_EXCEPTION =
		newType();

	/** A list of process instances. */
	public static final RelationType<List<Process>> PROCESS_LIST =
		newListType();

	/** The relation type for the registration of process listeners. */
	public static final ListenerType<ProcessListener, Process> PROCESS_LISTENERS =
		new ListenerType<ProcessListener, Process>();

	/**
	 * Contains a mapping from process parameter types to functions that
	 * initialize the parameter value from the process (typically from a certain
	 * different parameter).
	 */
	public static final RelationType<Map<RelationType<?>,
										 Function<? super Process, ?>>>			 PARAM_INITIALIZATIONS =
		RelationTypes.newMapType(false);

	/**
	 * A list of parameter relation types that are required to be available when
	 * the process is executed.
	 */
	public static final RelationType<List<RelationType<?>>> REQUIRED_PROCESS_INPUT_PARAMS =
		newListType();

	/**
	 * A list of parameter relation types that can optionally be set as input
	 * values when the process is executed.
	 */
	public static final RelationType<List<RelationType<?>>> OPTIONAL_PROCESS_INPUT_PARAMS =
		newListType();

	/**
	 * A list of the parameter relation types to be used in an interaction. This
	 * relation must be set on a process step.
	 */
	public static final RelationType<List<RelationType<?>>> INTERACTION_PARAMS =
		newListType();

	/**
	 * A set of the parameter relation types that are used as input parameters
	 * in an interaction. These must be a subset of the parameters stored in
	 * {@link #INTERACTION_PARAMS}.
	 */
	public static final RelationType<Set<RelationType<?>>> INPUT_PARAMS =
		newSetType(false);

	/**
	 * A set of the parameter relation types that should be displayed in
	 * additional views.
	 */
	public static final RelationType<Set<RelationType<List<RelationType<?>>>>> VIEW_PARAMS =
		newSetType(true);

	/**
	 * Contains the relation type of the {@link #INTERACTION_PARAMS} parameter
	 * that caused an interactive input event during an interaction. This
	 * parameter should preferably be queried by steps through the method {@link
	 * ProcessFragment#getInteractiveInputParameter()}.
	 */
	public static final RelationType<RelationType<?>> INTERACTIVE_INPUT_PARAM =
		newType();

	/** The event type of an interaction. */
	public static final RelationType<InteractionEventType> INTERACTIVE_INPUT_EVENT_TYPE =
		newType();

	/**
	 * During an interactive input event this parameter is TRUE for an action
	 * event or FALSE for a continuous selection event.
	 */
	public static final RelationType<Boolean> INTERACTIVE_INPUT_ACTION_EVENT =
		newType();

	/**
	 * A set of the interaction parameters that will continue the process if
	 * they cause an interaction. This are typically enumerated interaction
	 * parameters with the style {@link ListStyle#IMMEDIATE}. This relation must
	 * be set on a process step.
	 */
	public static final RelationType<Set<RelationType<?>>> CONTINUATION_PARAMS =
		newSetType(false);

	/**
	 * A process parameter that contains the interaction parameter that caused
	 * the most recent continuation. See parameter {@link #CONTINUATION_PARAMS}
	 * for more information.
	 */
	public static final RelationType<RelationType<?>> CONTINUATION_PARAM =
		newType();

	/**
	 * A process parameter that contains the class of the {@link
	 * InteractionFragment} that caused the most recent continuation. See
	 * parameter {@link #CONTINUATION_PARAMS} for more information.
	 */
	public static final RelationType<Class<? extends ProcessFragment>> CONTINUATION_FRAGMENT_CLASS =
		newType();

	/**
	 * If this flag is set on a process it should not be executed again if an
	 * error (i.e. an exception) occurred. This is mainly intended to be used on
	 * automatically executed background processes.
	 */
	public static final RelationType<Boolean> STOP_PROCESS_ON_ERROR =
		newFlagType();

	/**
	 * If this flag is set on a process it should gracefully end its execution
	 * as soon as possible. This flag should be queried and responded to in
	 * reasonable intervals.
	 */
	public static final RelationType<Boolean> STOP_PROCESS_EXECUTION =
		newFlagType();

	/**
	 * Signals that a process should automatically continue to run after a
	 * process interaction. This can be used to interactively display
	 * intermediate results of a process. This can be used to create repeated
	 * process loops where an interactive step displays the results or the
	 * progress of an operation. The re-execution of the process must be handled
	 * by the code which handles the interaction.
	 */
	public static final RelationType<Boolean> AUTO_CONTINUE = newFlagType();

	/**
	 * Similar to {@link #AUTO_CONTINUE} but stays in the same interaction when
	 * the process is re-executed (instead of continuing with the next process
	 * step). This can be used to inform the user while an interactive step
	 * prepares data during it's initialization or because of an interaction.
	 * The update of the interaction will continue as long as this flag is set.
	 * Because this is basically an endless loop the code setting this flag must
	 * take appropriate measures to reduce the processing load on the client and
	 * server (e.g. by invoking {@link Thread#sleep(long)} between invocations.
	 */
	public static final RelationType<Boolean> AUTO_UPDATE = newFlagType();

	/**
	 * A flag to indicate that a certain process step is the final interactive
	 * step. This can be evaluated by the user interface code to show a hint
	 * that the process will be finished afterwards.
	 */
	public static final RelationType<Boolean> FINAL_STEP = newFlagType();

	/**
	 * A flag to indicate that a certain interactive process step performs some
	 * kind of detail handling in the enclosing context. This can be evaluated
	 * by the user interface code to show a hint that the process will return to
	 * the main branch when the interaction ends.
	 */
	public static final RelationType<Boolean> DETAIL_STEP = newFlagType();

	/**
	 * A flag that can be set to mark a process step that has one or more
	 * interactive parameters which will cause an immediate execution of the
	 * process. This can be used by the user interface as a hint that no extra
	 * execution control ('next button') needs to be displayed.
	 */
	public static final RelationType<Boolean> IMMEDIATE_INTERACTION =
		newFlagType();

	/**
	 * This relation type serves as an indicator that the current step should be
	 * reloaded (i.e. prepared again) during an interaction. It doesn't need to
	 * be set as a parameter but should instead only be set in the relation with
	 * the type {@link #INTERACTIVE_INPUT_PARAM}.
	 */
	public static final RelationType<Boolean> RELOAD_CURRENT_STEP =
		newFlagType();

	/**
	 * Indicates that the process step on which this flag is set should be the
	 * starting point of a transaction. The transaction will be committed when a
	 * step is reached that has the flag {@link #TRANSACTION_END} set. Setting
	 * both flags on the same step would have the same effect as setting the
	 * single flag {@link MetaTypes#TRANSACTIONAL} which should therefore be
	 * preferred.
	 */
	public static final RelationType<Boolean> TRANSACTION_START = newFlagType();

	/**
	 * Indicates that the process step on which this flag is set should be the
	 * end point of a transaction. A transaction that had been started
	 * previously by a step with the flag {@link #TRANSACTION_START} will be
	 * committed. If the numbers of start and end points of transactions don't
	 * match an exception will occur.
	 */
	public static final RelationType<Boolean> TRANSACTION_END = newFlagType();

	/**
	 * Indicates that the process step on which this flag is set should be the
	 * starting point of a history group. The group will be closed when a step
	 * is reached that has the flag {@link #HISTORY_END} set. Setting both flags
	 * on the same step would have the same effect as setting the single flag
	 * {@link HistoryManager#HISTORIZED} which should therefore be preferred.
	 *
	 * <p>The History flags implicitly include the {@link #TRANSACTION_START}
	 * and {@link #TRANSACTION_END} flags to group history changes and
	 * transactional code of the process steps in a transaction. It would be
	 * redundant to set these flags too and it should therefore be avoided.</p>
	 */
	public static final RelationType<Boolean> HISTORY_START = newFlagType();

	/**
	 * Indicates that the process step on which this flag is set should be the
	 * end point of a history group. A group that had been started previously by
	 * a step with the flag {@link #HISTORY_START} will be closed. If the
	 * numbers of start and end points of history groups don't match an
	 * exception will occur.
	 */
	public static final RelationType<Boolean> HISTORY_END = newFlagType();

	/**
	 * A reference to another parameter that contains the target entity for a
	 * history record.
	 */
	public static final RelationType<RelationType<? extends Entity>> HISTORY_TARGET_PARAM =
		newType();

	/** A string parameter that can contain the value for a history entry. */
	public static final RelationType<String> HISTORY_VALUE = newType();

	/** A string containing a message for the current process step. */
	public static final RelationType<String> PROCESS_STEP_MESSAGE = newType();

	/** An information string for the current process step. */
	public static final RelationType<String> PROCESS_STEP_INFO = newType();

	/** A string containing result informations for the current process step. */
	public static final RelationType<String> PROCESS_RESULT = newType();

	/**
	 * An style string for the current process step. This style can be used to
	 * influence the rendering of interactive steps in user interfaces.
	 */
	public static final RelationType<String> PROCESS_STEP_STYLE = newType();

	/** A reference to a source parameter type. */
	public static final RelationType<RelationType<?>> SOURCE_PARAM = newType();

	/** A reference to a target parameter type. */
	public static final RelationType<RelationType<?>> TARGET_PARAM = newType();

	/** A reference to an entity parameter type. */
	public static final RelationType<RelationType<? extends Entity>> ENTITY_PARAM =
		newType();

	/** A reference to an action that will be invoked to update an entity. */
	public static final RelationType<Action<Entity>> ENTITY_UPDATE_ACTION =
		newType();

	/** A string containing an entity type name for display or selection. */
	public static final RelationType<String> ENTITY_TYPE = newType();

	/**
	 * A mapping from parameter relation types to functions that validate
	 * parameter values. If the parameter value is valid the function must
	 * return NULL and else a string containing the reason why the validation
	 * failed (typically a resource identifier that will be displayed in the
	 * user interface). The relation will always return at least an empty map,
	 * never NULL.
	 */
	public static final RelationType<Map<RelationType<?>, Function<?, String>>> PARAM_VALIDATIONS =
		newMapType(false);

	/**
	 * A mapping from parameter relation types to functions that validate
	 * parameter values already during interactions while the validations stored
	 * in {@link #PARAM_VALIDATIONS} will only be performed if the process
	 * progresses to the next step.
	 */
	public static final RelationType<Map<RelationType<?>, Function<?, String>>> INTERACTION_PARAM_VALIDATIONS =
		newMapType(false);

	/**
	 * An ordered list of strings that constrains the values from which a
	 * certain process parameter can be selected. Created as a standard relation
	 * type without an initial value to allow easier detection of the
	 * constraint.
	 */
	public static final RelationType<Collection<?>> ALLOWED_VALUES = newType();

	/**
	 * A reference to a the original relation type of a derived parameter type.
	 * See method {@link #deriveParameter(String, RelationType)} for details.
	 */
	public static final RelationType<RelationType<?>> ORIGINAL_RELATION_TYPE =
		newType();

	/** A reference to a data element that has been created by this factory. */
	public static final RelationType<DataElement<?>> DATA_ELEMENT = newType();

	/** A relation type for references to process scheduler instances. */
	public static final RelationType<ProcessScheduler> PROCESS_SCHEDULER =
		newType();

	/**
	 * A reference to an object that provides access to external services
	 * through an {@link ExternalServiceAccess} interface.
	 */
	public static final RelationType<ExternalServiceAccess> EXTERNAL_SERVICE_ACCESS =
		newType();

	/**
	 * An integer value containing a value indicating the progress of a process
	 * in percent.
	 */
	public static final RelationType<Integer> PROGRESS = newIntType();

	/** The maximum value for the progress indicator. */
	public static final RelationType<Integer> PROGRESS_MAXIMUM =
		RelationTypes.newIntType();

	/** A string indicator for the progress of a process. */
	public static final RelationType<String> PROGRESS_INDICATOR = newType();

	/**
	 * A string containing the formatting template for the progress indication
	 * parameter {@link #PROGRESS_INDICATOR}. It must contain two integer
	 * formatting references (%d), the first for the current progress and the
	 * second for the progress maximum.
	 */
	public static final RelationType<String> PROGRESS_INDICATOR_TEMPLATE =
		newInitialValueType("%d/%d");

	/** A string description of the current process progress. */
	public static final RelationType<String> PROGRESS_DESCRIPTION = newType();

	/**
	 * A process parameter that contains the configuration of the application in
	 * which the process is running.
	 */
	public static final RelationType<ProvidesConfiguration> CONFIGURATION =
		newType();

	/**
	 * A list of process definition classes for background process that shall be
	 * resumed by a {@link ProcessScheduler}. Will be evaluated after a process
	 * has finished execution of it's steps.
	 */
	public static final RelationType<List<Class<? extends ProcessDefinition>>> RESUME_PROCESSES =
		newListType();

	/** An interactive enum parameter to perform list actions. */
	public static final RelationType<ListAction> LIST_ACTION = newType();

	/**
	 * A relation that refers to a set of parameter update listeners that will
	 * be notified if a parameter is modified. This relation is intended to be
	 * set on interactions, not as a process parameter.
	 */
	public static final RelationType<Set<Updatable>> PARAM_UPDATE_LISTENERS =
		newSetType(false);

	/**
	 * A string parameter to be used as an empty placeholder in an interaction
	 * layout. Typically without content, label, and tooltip.
	 */
	public static final RelationType<String> INTERACTION_FILL = newType();

	/**
	 * A string parameter that can be added or removes as an "invisible"
	 * interaction parameter to force a layout rebuild
	 */
	public static final RelationType<String> DUMMY_PARAMETER = newType();

	//- Internal types ---------------------------------------------------------

	/**
	 * A package-internal set of the temporary parameter relation types used by
	 * a process.
	 */
	static final RelationType<Set<RelationType<?>>> TEMPORARY_PARAM_TYPES =
		newSetType(false);

	/**
	 * A package-internal usage count for the management of temporary parameter
	 * relation types.
	 */
	static final RelationType<Integer> PARAM_USAGE_COUNT = newIntType();

	static
	{
		RelationTypes.init(ProcessRelationTypes.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private ProcessRelationTypes()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new parameter relation type that is derived from an existing
	 * relation type. The new type will have the same target datatype as the
	 * original type and a name that is created from the process package, the
	 * optional prefix, and the simple name of the original type. The original
	 * type will be stored in a relation of type {@link #ORIGINAL_RELATION_TYPE}
	 * on the derived type. The prefix must be in relation type notation, i.e.
	 * upper case words separated by underscores.
	 *
	 * @param  sPrefix       The optional prefix to prepend to the original
	 *                       type's name (can be NULL or empty for none)
	 * @param  rRelationType The relation type to derive the parameter from
	 *
	 * @return The new derived parameter type
	 */
	public static <T> RelationType<T> deriveParameter(
		String			sPrefix,
		RelationType<T> rRelationType)
	{
		StringBuilder aName =
			new StringBuilder(Process.class.getPackage().getName());

		aName.append('.');

		if (sPrefix != null && sPrefix.length() > 0)
		{
			aName.append(sPrefix);
			aName.append('_');
		}

		aName.append(rRelationType.getSimpleName());

		RelationType<T> aDerivedType =
			RelationTypes.<T>newRelationType(aName.toString(),
											 rRelationType.getValueType(),
											 rRelationType.getDefaultValue(),
											 rRelationType
											 .getInitialValueFunction());

		aDerivedType.set(ORIGINAL_RELATION_TYPE, rRelationType);

		return aDerivedType;
	}

	/***************************************
	 * Creates a list of derived parameter relation types. For details see
	 * method {@link #deriveParameter(String, RelationType)}. The returned list
	 * can be manipulated freely.
	 *
	 * @param  sPrefix        The optional prefix to prepend to the original
	 *                        type names (can be NULL or empty for none)
	 * @param  rRelationTypes The relation types to derive the parameters from
	 *
	 * @return A list of new derived parameters types
	 */
	public static List<RelationType<?>> deriveParameters(
		String			   sPrefix,
		RelationType<?>... rRelationTypes)
	{
		return deriveParameters(sPrefix, Arrays.asList(rRelationTypes));
	}

	/***************************************
	 * Creates a list of derived parameter relation types. For details see
	 * method {@link #deriveParameter(String, RelationType)}. The returned list
	 * can be manipulated freely.
	 *
	 * @param  sPrefix        The optional prefix to prepend to the original
	 *                        type names (can be NULL or empty for none)
	 * @param  rRelationTypes The relation types to derive the parameters from
	 *
	 * @return A list of new derived parameters types
	 */
	public static List<RelationType<?>> deriveParameters(
		String				  sPrefix,
		List<RelationType<?>> rRelationTypes)
	{
		List<RelationType<?>> aDerivedTypes =
			new ArrayList<RelationType<?>>(rRelationTypes.size());

		for (RelationType<?> rRelationType : rRelationTypes)
		{
			aDerivedTypes.add(deriveParameter(sPrefix, rRelationType));
		}

		return aDerivedTypes;
	}

	/***************************************
	 * Returns the derived parameter relation type for a certain original
	 * relation type from a list of derived types.
	 *
	 * @param  rOriginalType  The original relation type
	 * @param  rDerivedParams A list of derived parameter types
	 *
	 * @return The derived parameter relation type or NULL if not found
	 */
	@SuppressWarnings("unchecked")
	public static <T> RelationType<T> getDerivedParameter(
		RelationType<T>		  rOriginalType,
		List<RelationType<?>> rDerivedParams)
	{
		for (RelationType<?> rParam : rDerivedParams)
		{
			if (rParam.get(ORIGINAL_RELATION_TYPE) == rOriginalType)
			{
				return (RelationType<T>) rParam;
			}
		}

		return null;
	}
}
