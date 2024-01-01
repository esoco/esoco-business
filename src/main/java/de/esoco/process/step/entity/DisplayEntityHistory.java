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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityFunctions;
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes.HierarchicalQueryMode;
import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;
import de.esoco.history.HistoryRecord.HistoryType;
import de.esoco.lib.datatype.DateRange;
import de.esoco.lib.datatype.DateRange.StandardDateRange;
import de.esoco.lib.event.EditListener;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.Orientation;
import de.esoco.lib.property.Updatable;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.process.ProcessFragment;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.param.CollectionParameter.SetParameter;
import de.esoco.process.param.Parameter;
import de.esoco.process.step.EditText;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.esoco.entity.EntityFunctions.formatEntity;
import static de.esoco.entity.EntityRelationTypes.HIERARCHICAL_QUERY_MODE;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.greaterOrEqual;
import static de.esoco.lib.expression.Predicates.lessThan;
import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.HEIGHT;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;
import static de.esoco.lib.property.StyleProperties.ORIENTATION;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_EVENT_TYPE;
import static de.esoco.storage.StoragePredicates.sortBy;
import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;

/**
 * A fragment that displays the history of an arbitrary Entity and optionally
 * allows to create new history entries.
 *
 * <p><b>Input parameters:</b></p>
 *
 * <ul>
 *   <li>{@link #ENTITY_HISTORY_TARGET}: The entity to display and create the
 *     history for.</li>
 *   <li>{@link #ENTITY_HISTORY_ROOT_TARGET}: An optional root entity to create
 *     history for.</li>
 *   <li> {@link #ENTITY_HISTORY_TYPES}: The types of the history records to
 *     display.</li>
 * </ul>
 *
 * @author eso
 */
public class DisplayEntityHistory extends InteractionFragment
	implements Updatable {

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>>
		ENTITY_HISTORY_FRAGMENT = newListType();

	/**
	 * Input parameter: the target entity to display and create history for.
	 */
	public static final RelationType<Entity> ENTITY_HISTORY_TARGET = newType();

	/**
	 * Input parameter (optional): the root target entity to create history
	 * for.
	 */
	public static final RelationType<Entity> ENTITY_HISTORY_ROOT_TARGET =
		newType();

	/**
	 * Optional: a predicate containing the target query criteria. If set the
	 * history target parameter will be ignored.
	 */
	public static final RelationType<Predicate<Relatable>>
		ENTITY_HISTORY_TARGET_CRITERIA = newType();

	/**
	 * Input parameter: the origin entities to display the history for.
	 */
	public static final RelationType<List<Entity>> ENTITY_HISTORY_ORIGINS =
		newListType();

	/**
	 * Input parameter: the history types to display.
	 */
	public static final RelationType<Set<HistoryType>> ENTITY_HISTORY_TYPES =
		newType();

	/**
	 * Input parameter: the hierarchical query mode (defaults to
	 * {@link HierarchicalQueryMode#UNCONSTRAINED}).
	 */
	public static final RelationType<HierarchicalQueryMode>
		ENTITY_HISTORY_QUERY_MODE =
		newInitialValueType(HierarchicalQueryMode.UNCONSTRAINED);

	/**
	 * The entity history table parameter.
	 */
	public static final RelationType<HistoryRecord> ENTITY_HISTORY = newType();

	/**
	 * The history date range to be displayed.
	 */
	public static final RelationType<StandardDateRange> ENTITY_HISTORY_DATE =
		newType();

	private static final long serialVersionUID = 1L;

	private static final String ITEM_ENTITY_HISTORY_ORIGIN_ALL =
		"$itmEntityHistoryOriginAll";

	private static final RelationType<Set<HistoryType>> HISTORY_TYPE_OPTIONS =
		newType();

	private static final RelationType<Boolean> ENABLE_HISTORY_ORIGIN =
		newType();

	private static final RelationType<String> ENTITY_HISTORY_ORIGIN =
		newType();

	private static final RelationType<String> ENTITY_HISTORY_VALUE = newType();

	private static final Function<Relatable, String> HISTORY_TARGET_FUNCTION =
		formatEntity("<NULL>").from(HistoryRecord.TARGET);

	private static final Function<Relatable, String>
		HISTORY_ROOT_TARGET_FUNCTION =
		formatEntity("$lblNoRootTarget").from(HistoryRecord.ROOT_TARGET);

	private static final Function<Relatable, String> HISTORY_ORIGIN_FUNCTION =
		formatEntity("").from(HistoryRecord.ORIGIN);

	static {
		RelationTypes.init(DisplayEntityHistory.class);
	}

	private final List<RelationType<?>> interactionParams =
		params(ENTITY_HISTORY, HistoryData.HISTORY_DATA_FRAGMENT);

	private final List<RelationType<?>> inputParams = params(ENTITY_HISTORY);

	private final Entity historyOrigin;

	private final HistoryData historyData;

	private final boolean showRootTarget;

	private final boolean showTarget;

	private final Map<String, Entity> historyOrigins = new HashMap<>();

	private Entity currentTarget;

	/**
	 * Creates a new instance.
	 *
	 * @param historyOrigin   The origin entity of to always limit the history
	 *                        to or NULL for all history
	 * @param noteEditAllowed TRUE to allow the editing of existing history
	 *                        notes
	 * @param showRootTarget  TRUE to display the root target column
	 * @param showTarget      TRUE to display the target column
	 */
	public DisplayEntityHistory(Entity historyOrigin, boolean noteEditAllowed,
		boolean showRootTarget, boolean showTarget) {
		this.historyOrigin = historyOrigin;
		this.showRootTarget = showRootTarget;
		this.showTarget = showTarget;

		historyData =
			new HistoryData(this, noteEditAllowed, historyOrigin == null);
	}

	/**
	 * A helper method that returns a list of history record attributes to be
	 * used in a query.
	 *
	 * @param includeRootTarget TRUE to
	 *                          include{@link HistoryRecord#ROOT_TARGET}
	 * @param includeTarget     TRUE to include{@link HistoryRecord#TARGET}
	 * @return The list of history attributes
	 */
	public static List<Function<? super HistoryRecord, ?>> getHistoryQueryAttributes(
		boolean includeRootTarget, boolean includeTarget) {
		List<Function<? super HistoryRecord, ?>> attributes =
			new ArrayList<Function<? super HistoryRecord, ?>>();

		attributes.add(HistoryRecord.TIME);
		attributes.add(HistoryRecord.TYPE);

		if (includeRootTarget) {
			attributes.add(HISTORY_ROOT_TARGET_FUNCTION);
		}

		if (includeTarget) {
			attributes.add(HISTORY_TARGET_FUNCTION);
		}

		attributes.add(HistoryRecord.VALUE);
		attributes.add(HISTORY_ORIGIN_FUNCTION);

		return attributes;
	}

	/**
	 * Initializes the a history parameter with a history query for certain
	 * targets.
	 *
	 * @param processFragment The process fragment
	 * @param historyParam    The history parameter to initialize
	 * @param rootTargets     The target entities to query the history for
	 * @param includeTarget   TRUE to always include the history target column
	 *                        or else only if multiple targets exist
	 * @param excludedTypes   An optional list of excluded history record types
	 * @return The criteria predicate for the history record query
	 */
	public static Predicate<Relatable> initHistoryParameter(
		ProcessFragment processFragment,
		RelationType<HistoryRecord> historyParam,
		Collection<Entity> rootTargets, boolean includeTarget,
		HistoryType... excludedTypes) {
		assert rootTargets.size() > 0;

		boolean multipleRootTargets = rootTargets.size() > 1;

		List<Function<? super HistoryRecord, ?>> attributes =
			getHistoryQueryAttributes(multipleRootTargets, includeTarget);

		Predicate<Relatable> hasTarget;
		Predicate<Object> targets = elementOf(rootTargets);

		hasTarget = HistoryRecord.ROOT_TARGET
			.is(targets)
			.or(HistoryRecord.TARGET.is(targets));

		if (excludedTypes != null && excludedTypes.length > 0) {
			hasTarget = hasTarget.and(HistoryRecord.TYPE.is(
				not(elementOf((Object[]) excludedTypes))));
		}

		QueryPredicate<HistoryRecord> history =
			new QueryPredicate<>(HistoryRecord.class, hasTarget);

		history.set(HIERARCHICAL_QUERY_MODE,
			HierarchicalQueryMode.UNCONSTRAINED);
		processFragment.annotateForEntityQuery(historyParam, history,
			sortBy(HistoryRecord.TIME, false), attributes);

		return hasTarget;
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
		if (interactionParam == ENTITY_HISTORY) {
			HistoryRecord historyRecord = getParameter(ENTITY_HISTORY);

			if (getParameter(INTERACTION_EVENT_TYPE) ==
				InteractionEventType.ACTION) {
				performHistoryRecordAction(historyRecord);
			} else {
				historyData.update(historyRecord, true);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		addSubFragment(HistoryData.HISTORY_DATA_FRAGMENT, historyData);

		RelationType<List<RelationType<?>>> fragmentParam =
			getFragmentParameter();

		setUIProperty(ORIENTATION, Orientation.VERTICAL, fragmentParam);
		setUIProperty(UserInterfaceProperties.LAYOUT, LayoutType.SPLIT,
			fragmentParam);

		setInteractive(InteractiveInputMode.BOTH, ENTITY_HISTORY);

		setUIFlag(HIDE_LABEL, interactionParams);
		setUIProperty(450, HEIGHT, ENTITY_HISTORY);
	}

	/**
	 * Resets all history input criteria.
	 */
	public void resetHistoryCriteria() {
		setParameter(ENTITY_HISTORY_ORIGIN, ITEM_ENTITY_HISTORY_ORIGIN_ALL);
		deleteParameters(ENTITY_HISTORY_TYPES, ENTITY_HISTORY_TARGET,
			ENTITY_HISTORY_TARGET_CRITERIA, ENTITY_HISTORY_DATE);
	}

	/**
	 * Updates the history display to reflect the state of the input parameters
	 * of this fragment.
	 */
	@Override
	@SuppressWarnings("boxing")
	public void update() {
		Set<HistoryType> historyTypes = getParameter(ENTITY_HISTORY_TYPES);
		Predicate<Relatable> history;

		Entity target = getParameter(ENTITY_HISTORY_TARGET);

		if (currentTarget != target) {
			currentTarget = target;

			Parameter<String> origin = param(ENTITY_HISTORY_ORIGIN);

			param(ENABLE_HISTORY_ORIGIN).value(false);
			origin.setEnabled(false);
			origin.value(ITEM_ENTITY_HISTORY_ORIGIN_ALL);
		}

		if (target != null || historyOrigin != null ||
			!getParameter(ENTITY_HISTORY_ORIGINS).isEmpty()) {
			if (historyTypes == null || historyTypes.size() == 0) {
				historyTypes = EnumSet.of(HistoryType.NOTE);
			}

			history = HistoryRecord.TYPE.is(elementOf(historyTypes));
			history = addTargetFilter(target, history);
			history = addDateRangeFilter(history);
			history = addOriginFilter(history);
		} else {
			// dummy query that yields no result if no target or origins have
			// been set
			history = HistoryRecord.TARGET.is(equalTo("<NOTHING>"));
		}

		QueryPredicate<HistoryRecord> queryHistory =
			new QueryPredicate<>(HistoryRecord.class, history);

		List<Function<? super HistoryRecord, ?>> attributes =
			getHistoryQueryAttributes(showRootTarget, showTarget);

		queryHistory.set(HIERARCHICAL_QUERY_MODE,
			getParameter(ENTITY_HISTORY_QUERY_MODE));

		annotateForEntityQuery(ENTITY_HISTORY, queryHistory,
			sortBy(HistoryRecord.TIME, false), attributes);

		setParameter(ENTITY_HISTORY, null);
		setUIProperty(-1, CURRENT_SELECTION, ENTITY_HISTORY);

		historyData.update(null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initComplete() {
		update();
	}

	/**
	 * This method can be overridden by subclasses to react to an action event
	 * for a certain history record. The default implementation does nothing.
	 *
	 * @param historyRecord The history record for which the even occurred
	 */
	protected void performHistoryRecordAction(HistoryRecord historyRecord) {
	}

	/**
	 * Adds a date range filter if the corresponding parameter is set.
	 *
	 * @param history The filter predicate to amend
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addDateRangeFilter(
		Predicate<Relatable> history) {
		StandardDateRange selectedDateRange =
			getParameter(ENTITY_HISTORY_DATE);

		if (selectedDateRange != null &&
			selectedDateRange != StandardDateRange.NONE) {
			DateRange dateRange = DateRange.calculateFor(selectedDateRange);

			history = history.and(HistoryRecord.TIME
				.is(greaterOrEqual(dateRange.getStart()))
				.and(HistoryRecord.TIME.is(lessThan(dateRange.getEnd()))));
		}

		return history;
	}

	/**
	 * Adds a history origin filter to a predicate if necessary.
	 *
	 * @param history The original filter predicate
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addOriginFilter(Predicate<Relatable> history) {
		if (historyOrigin != null) {
			history =
				history.and(HistoryRecord.ORIGIN.is(equalTo(historyOrigin)));
		} else if (!param(ENTITY_HISTORY_ORIGIN).is(DISABLED)) {
			String selectedOrigin = getParameter(ENTITY_HISTORY_ORIGIN);

			if (ITEM_ENTITY_HISTORY_ORIGIN_ALL.equals(selectedOrigin)) {
				List<Entity> origins = getParameter(ENTITY_HISTORY_ORIGINS);

				if (origins.size() > 0) {
					history = history.and(
						HistoryRecord.ORIGIN.is(elementOf(origins)));
				}

				setHistoryOrigins(history);
			} else {
				Entity origin = historyOrigins.get(selectedOrigin);

				history =
					history.and(HistoryRecord.ORIGIN.is(equalTo(origin)));
			}
		}

		return history;
	}

	/**
	 * Adds a history target filter to a predicate if necessary.
	 *
	 * @param target  The main target to display the history for
	 * @param history The original filter predicate
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addTargetFilter(Entity target,
		Predicate<Relatable> history) {
		Predicate<Relatable> targetCriteria =
			getParameter(ENTITY_HISTORY_TARGET_CRITERIA);

		if (targetCriteria != null) {
			history = history.and(targetCriteria);
		} else if (target != null) {
			Predicate<Object> equalToTarget = equalTo(target);

			history = history.and(HistoryRecord.ROOT_TARGET
				.is(equalToTarget)
				.or(HistoryRecord.TARGET.is(equalToTarget)));
		}

		return history;
	}

	/**
	 * Sets the selectable distinct history origins from a history query.
	 *
	 * @param history The history filter predicate
	 */
	private void setHistoryOrigins(Predicate<Relatable> history) {
		try {
			Collection<Entity> origins =
				EntityManager.getDistinct(HistoryRecord.ORIGIN,
					new QueryPredicate<>(HistoryRecord.class, history));

			historyOrigins.clear();

			for (Entity origin : origins) {
				if (origin != null) {
					historyOrigins.put(EntityFunctions.format(origin), origin);
				}
			}

			List<String> originNames =
				new ArrayList<>(historyOrigins.keySet());

			originNames.add(0, ITEM_ENTITY_HISTORY_ORIGIN_ALL);
			setAllowedValues(ENTITY_HISTORY_ORIGIN, originNames);
		} catch (StorageException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * A fragment that displays the value of a history record.
	 *
	 * @author eso
	 */
	public static class HistoryData extends InteractionFragment
		implements EditListener<String> {

		/**
		 * A standard parameter that can be used to display this fragment as a
		 * sub-fragment.
		 */
		public static final RelationType<List<RelationType<?>>>
			HISTORY_DATA_FRAGMENT = newListType();

		private static final long serialVersionUID = 1L;

		private static final RelationType<List<RelationType<?>>>
			HISTORY_VALUE_FRAGMENT = newListType();

		static {
			RelationTypes.init(HistoryData.class);
		}

		private final List<RelationType<?>> interactionParams =
			params(HISTORY_VALUE_FRAGMENT,
				HistoryOptions.HISTORY_OPTIONS_FRAGMENT);

		private final List<RelationType<?>> inputParams = params();

		private final boolean noteEditAllowed;

		private final HistoryOptions historyOptions;

		private HistoryRecord editedHistoryNote;

		private EditText editHistoryValue;

		private Entity historyTarget;

		private Entity historyRootTarget;

		/**
		 * Creates a new instance.
		 *
		 * @param optionsListener  A listener that will be notified of option
		 *                         changes
		 * @param noteEditAllowed  TRUE to allow the editing of history records
		 *                         of type {@link HistoryType#NOTE}
		 * @param showOriginFilter TRUE to display a list of history origins to
		 *                         select from
		 */
		public HistoryData(Updatable optionsListener, boolean noteEditAllowed,
			boolean showOriginFilter) {
			this.noteEditAllowed = noteEditAllowed;

			historyOptions =
				new HistoryOptions(optionsListener, showOriginFilter);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void editFinished(String value, EditAction finishAction) {
			if (finishAction == EditAction.SAVE) {
				try {
					storeHistoryNote(value);
				} catch (TransactionException e) {
					throw new RuntimeProcessException(this, e);
				}
			}

			editedHistoryNote = null;
			update(getParameter(ENTITY_HISTORY), false);
			setEnabled(true, ENTITY_HISTORY);
			historyOptions.enableEdit(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void editStarted(String value) {
			historyOptions.enableEdit(false);
			setEnabled(false, ENTITY_HISTORY);

			if (value != null) {
				editedHistoryNote = getParameter(ENTITY_HISTORY);
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
		public void init() throws Exception {
			editedHistoryNote = null;

			editHistoryValue =
				new EditText(ENTITY_HISTORY_VALUE, this, true, false);

			addSubFragment(HISTORY_VALUE_FRAGMENT, editHistoryValue);
			addSubFragment(HistoryOptions.HISTORY_OPTIONS_FRAGMENT,
				historyOptions);

			setUIFlag(HIDE_LABEL, interactionParams);
			setUIFlag(SAME_ROW, HISTORY_VALUE_FRAGMENT,
				HistoryOptions.HISTORY_OPTIONS_FRAGMENT);

			setUIProperty(LABEL, "", HISTORY_VALUE_FRAGMENT,
				HISTORY_DATA_FRAGMENT);

			setHtmlSize("100%", "100%", HISTORY_VALUE_FRAGMENT);
		}

		/**
		 * Updates the display and state for a certain history record.
		 *
		 * @param record   The history record (NULL to clear)
		 * @param stopEdit TRUE if an active editor should be stopped
		 */
		public void update(HistoryRecord record, boolean stopEdit) {
			if (stopEdit && editHistoryValue != null &&
				editHistoryValue.isEditing()) {
				editHistoryValue.stopEditing(EditAction.SAVE);
			}

			String value = "";
			boolean allowEdit = false;

			historyTarget = getParameter(ENTITY_HISTORY_TARGET);
			historyRootTarget = getParameter(ENTITY_HISTORY_ROOT_TARGET);

			if (record != null) {
				value = record.get(HistoryRecord.VALUE);
				allowEdit =
					record.get(HistoryRecord.TYPE) == HistoryType.NOTE &&
						(noteEditAllowed || getProcessUser().equals(
							record.get(HistoryRecord.ORIGIN)));
			}

			setParameter(ENTITY_HISTORY_VALUE, value);

			String editInfo = "";

			if (historyTarget != null) {
				editInfo = historyTarget.getClass().getSimpleName() + ": " +
					EntityFunctions.format(historyTarget);
			}

			if (editHistoryValue != null) {
				editHistoryValue.setEditInfo(editInfo);
				editHistoryValue.allowEdit(allowEdit);
			}
		}

		/**
		 * Stores the currently edited history note. A new note will be stored
		 * with
		 * {@link HistoryManager#record(HistoryType, Entity, Entity, String)},
		 * an edited note will simply be stored.
		 *
		 * @param value The new value for the history note
		 */
		private void storeHistoryNote(String value)
			throws TransactionException {
			if (editedHistoryNote != null) {
				editedHistoryNote.set(HistoryRecord.VALUE, value);
				EntityManager.storeEntity(editedHistoryNote, getProcessUser());
				setParameter(ENTITY_HISTORY, editedHistoryNote);
			} else {
				HistoryManager.record(HistoryType.NOTE, getProcessUser(),
					historyTarget, historyRootTarget, value, null, null);
			}

			markParameterAsModified(ENTITY_HISTORY);
		}
	}

	/**
	 * A fragment that displays the value and options for an
	 * {@link DisplayEntityHistory} parent fragment.
	 *
	 * @author eso
	 */
	public static class HistoryOptions extends InteractionFragment {

		/**
		 * A standard parameter that can be used to display this fragment as a
		 * sub-fragment.
		 */
		public static final RelationType<List<RelationType<?>>>
			HISTORY_OPTIONS_FRAGMENT = newListType();

		private static final long serialVersionUID = 1L;

		static {
			RelationTypes.init(HistoryOptions.class);
		}

		private final Updatable changeListener;

		private final boolean showOriginFilter;

		/**
		 * Creates a new instance.
		 *
		 * @param changeListener   An {@link Updatable} instance to be notified
		 *                         if the history options have been modified
		 * @param showOriginFilter TRUE to display a list of history origins to
		 *                         select from
		 */
		public HistoryOptions(Updatable changeListener,
			boolean showOriginFilter) {
			this.changeListener = changeListener;
			this.showOriginFilter = showOriginFilter;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			checkAddHistoryTypeOptions();

			Parameter<StandardDateRange> dateRange =
				param(ENTITY_HISTORY_DATE);

			dateRange
				.input()
				.colSpan(2)
				.set(LIST_STYLE, ListStyle.DROP_DOWN)
				.allow(StandardDateRange.NONE, StandardDateRange.TODAY,
					StandardDateRange.YESTERDAY,
					StandardDateRange.BEFORE_YESTERDAY,
					StandardDateRange.CURRENT_WEEK,
					StandardDateRange.LAST_WEEK,
					StandardDateRange.CURRENT_MONTH,
					StandardDateRange.LAST_MONTH,
					StandardDateRange.CURRENT_QUARTER,
					StandardDateRange.LAST_QUARTER,
					StandardDateRange.CURRENT_YEAR,
					StandardDateRange.LAST_YEAR)
				.onUpdate(v -> changeListener.update());

			if (dateRange.value() == null) {
				dateRange.value(StandardDateRange.NONE);
			}

			checkAddOriginFilter();

			setUIFlag(HIDE_LABEL, getInteractionParameters());
			setUIProperty(RESOURCE_ID, StandardDateRange.class.getSimpleName(),
				ENTITY_HISTORY_DATE);
		}

		/**
		 * Updates the display if the selected history types change.
		 *
		 * @param newTypes The new selected history types
		 */
		private void changeHistoryTypes(Set<HistoryType> newTypes) {
			param(ENTITY_HISTORY_TYPES).value(new LinkedHashSet<>(newTypes));

			changeListener.update();
		}

		/**
		 * Adds the interactive history type options parameter if necessary.
		 */
		private void checkAddHistoryTypeOptions() {
			Set<HistoryType> historyTypes = getParameter(ENTITY_HISTORY_TYPES);

			if (historyTypes != null && historyTypes.size() > 1) {
				EnumSet<HistoryType> selectedTypeOptions =
					EnumSet.of(HistoryType.NOTE);

				SetParameter<HistoryType> historyTypeParam =
					new SetParameter<>(this, HISTORY_TYPE_OPTIONS);

				historyTypeParam
					.input()
					.set(LIST_STYLE, ListStyle.DISCRETE)
					.colSpan(2)
					.allowElements(historyTypes)
					.value(selectedTypeOptions)
					.onAction(this::changeHistoryTypes);

				param(ENTITY_HISTORY_TYPES).value(
					new LinkedHashSet<>(selectedTypeOptions));
			}
		}

		/**
		 * Adds the history origin filter if enabled.
		 */
		private void checkAddOriginFilter() {
			if (showOriginFilter) {
				param(ENABLE_HISTORY_ORIGIN)
					.label("")
					.onAction(this::setOriginsEnabled);
				param(ENTITY_HISTORY_ORIGIN)
					.input()
					.sameRow()
					.disable()
					.set(LIST_STYLE, ListStyle.DROP_DOWN)
					.value(ITEM_ENTITY_HISTORY_ORIGIN_ALL)
					.allow(ITEM_ENTITY_HISTORY_ORIGIN_ALL)
					.onUpdate(v -> changeListener.update());
			}
		}

		/**
		 * Sets the state of the history origins display.
		 *
		 * @param enable TRUE to enable
		 */
		private void setOriginsEnabled(boolean enable) {
			Parameter<String> origin = param(ENTITY_HISTORY_ORIGIN);

			origin.setEnabled(enable);

			if (!enable) {
				origin.value(ITEM_ENTITY_HISTORY_ORIGIN_ALL);
			}

			changeListener.update();
		}
	}
}
