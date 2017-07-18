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
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.Updatable;
import de.esoco.lib.property.UserInterfaceProperties;

import de.esoco.process.Parameter;
import de.esoco.process.ProcessFragment;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.EditText;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

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
import static de.esoco.lib.property.StyleProperties.VERTICAL;

import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_ACTION_EVENT;

import static de.esoco.storage.StoragePredicates.sortBy;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
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
	implements Updatable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final String ITEM_ENTITY_HISTORY_ORIGIN_ALL =
		"$itmEntityHistoryOriginAll";

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>> ENTITY_HISTORY_FRAGMENT =
		newListType();

	/** Input parameter: the target entity to display and create history for. */
	public static final RelationType<Entity> ENTITY_HISTORY_TARGET = newType();

	/**
	 * Input parameter (optional): the root target entity to create history for.
	 */
	public static final RelationType<Entity> ENTITY_HISTORY_ROOT_TARGET =
		newType();

	/**
	 * Optional: a predicate containing the target query criteria. If set the
	 * history target parameter will be ignored.
	 */
	public static final RelationType<Predicate<Relatable>> ENTITY_HISTORY_TARGET_CRITERIA =
		newType();

	/** Input parameter: the origin entities to display the history for. */
	public static final RelationType<List<Entity>> ENTITY_HISTORY_ORIGINS =
		newListType();

	/** Input parameter: the history types to display. */
	public static final RelationType<Set<HistoryType>> ENTITY_HISTORY_TYPES =
		newType();

	/**
	 * Input parameter: the hierarchical query mode (defaults to {@link
	 * HierarchicalQueryMode#UNCONSTRAINED}).
	 */
	public static final RelationType<HierarchicalQueryMode> ENTITY_HISTORY_QUERY_MODE =
		newInitialValueType(HierarchicalQueryMode.UNCONSTRAINED);

	/** The entity history table parameter. */
	public static final RelationType<HistoryRecord> ENTITY_HISTORY = newType();

	/** The history date range to be displayed. */
	public static final RelationType<StandardDateRange> ENTITY_HISTORY_DATE =
		newType();

	private static final RelationType<Set<HistoryType>> HISTORY_TYPE_OPTIONS =
		newType();

	private static final RelationType<Boolean> ENABLE_HISTORY_ORIGIN =
		newType();

	private static final RelationType<String> ENTITY_HISTORY_ORIGIN = newType();

	private static final RelationType<String> ENTITY_HISTORY_VALUE = newType();

	private static final Function<Relatable, String> HISTORY_TARGET_FUNCTION =
		formatEntity("<NULL>").from(HistoryRecord.TARGET);

	private static final Function<Relatable, String> HISTORY_ROOT_TARGET_FUNCTION =
		formatEntity("$lblNoRootTarget").from(HistoryRecord.ROOT_TARGET);

	private static final Function<Relatable, String> HISTORY_ORIGIN_FUNCTION =
		formatEntity("").from(HistoryRecord.ORIGIN);

	static
	{
		RelationTypes.init(DisplayEntityHistory.class);
	}

	//~ Instance fields --------------------------------------------------------

	private List<RelationType<?>> aInteractionParams =
		params(ENTITY_HISTORY, HistoryData.HISTORY_DATA_FRAGMENT);

	private List<RelationType<?>> aInputParams = params(ENTITY_HISTORY);

	private Entity	    rHistoryOrigin;
	private Entity	    rCurrentTarget;
	private HistoryData aHistoryData;

	private boolean bShowRootTarget;
	private boolean bShowTarget;

	private Map<String, Entity> aHistoryOrigins = new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rHistoryOrigin   The origin entity of to always limit the history
	 *                         to or NULL for all history
	 * @param bNoteEditAllowed TRUE to allow the editing of existing history
	 *                         notes
	 * @param bShowRootTarget  TRUE to display the root target column
	 * @param bShowTarget      TRUE to display the target column
	 */
	public DisplayEntityHistory(Entity  rHistoryOrigin,
								boolean bNoteEditAllowed,
								boolean bShowRootTarget,
								boolean bShowTarget)
	{
		this.rHistoryOrigin  = rHistoryOrigin;
		this.bShowRootTarget = bShowRootTarget;
		this.bShowTarget     = bShowTarget;

		aHistoryData =
			new HistoryData(this, bNoteEditAllowed, rHistoryOrigin == null);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A helper method that returns a list of history record attributes to be
	 * used in a query.
	 *
	 * @param  bIncludeRootTarget TRUE to include{@link
	 *                            HistoryRecord#ROOT_TARGET}
	 * @param  bIncludeTarget     TRUE to include{@link HistoryRecord#TARGET}
	 *
	 * @return The list of history attributes
	 */
	public static List<Function<? super HistoryRecord, ?>> getHistoryQueryAttributes(
		boolean bIncludeRootTarget,
		boolean bIncludeTarget)
	{
		List<Function<? super HistoryRecord, ?>> aAttributes =
			new ArrayList<Function<? super HistoryRecord, ?>>();

		aAttributes.add(HistoryRecord.TIME);
		aAttributes.add(HistoryRecord.TYPE);

		if (bIncludeRootTarget)
		{
			aAttributes.add(HISTORY_ROOT_TARGET_FUNCTION);
		}

		if (bIncludeTarget)
		{
			aAttributes.add(HISTORY_TARGET_FUNCTION);
		}

		aAttributes.add(HistoryRecord.VALUE);
		aAttributes.add(HISTORY_ORIGIN_FUNCTION);

		return aAttributes;
	}

	/***************************************
	 * Initializes the a history parameter with a history query for certain
	 * targets.
	 *
	 * @param  rProcessFragment The process fragment
	 * @param  rHistoryParam    The history parameter to initialize
	 * @param  rRootTargets     The target entities to query the history for
	 * @param  bIncludeTarget   TRUE to always include the history target column
	 *                          or else only if multiple targets exist
	 * @param  rExcludedTypes   An optional list of excluded history record
	 *                          types
	 *
	 * @return The criteria predicate for the history record query
	 */
	public static Predicate<Relatable> initHistoryParameter(
		ProcessFragment				rProcessFragment,
		RelationType<HistoryRecord> rHistoryParam,
		Collection<Entity>			rRootTargets,
		boolean						bIncludeTarget,
		HistoryType... 				rExcludedTypes)
	{
		assert rRootTargets.size() > 0;

		boolean bMultipleRootTargets = rRootTargets.size() > 1;

		List<Function<? super HistoryRecord, ?>> aAttributes =
			getHistoryQueryAttributes(bMultipleRootTargets, bIncludeTarget);

		Predicate<Relatable> pHasTarget;
		Predicate<Object>    pTargets = elementOf(rRootTargets);

		pHasTarget =
			HistoryRecord.ROOT_TARGET.is(pTargets)
									 .or(HistoryRecord.TARGET.is(pTargets));

		if (rExcludedTypes != null && rExcludedTypes.length > 0)
		{
			pHasTarget =
				pHasTarget.and(HistoryRecord.TYPE.is(not(elementOf((Object[])
																   rExcludedTypes))));
		}

		QueryPredicate<HistoryRecord> qHistory =
			new QueryPredicate<>(HistoryRecord.class, pHasTarget);

		qHistory.set(HIERARCHICAL_QUERY_MODE,
					 HierarchicalQueryMode.UNCONSTRAINED);
		rProcessFragment.annotateForEntityQuery(rHistoryParam,
												qHistory,
												sortBy(HistoryRecord.TIME,
													   false),
												aAttributes);

		return pHasTarget;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return aInputParams;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		return aInteractionParams;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		if (rInteractionParam == ENTITY_HISTORY)
		{
			HistoryRecord rHistoryRecord = getParameter(ENTITY_HISTORY);

			if (hasFlagParameter(INTERACTIVE_INPUT_ACTION_EVENT))
			{
				performHistoryRecordAction(rHistoryRecord);
			}
			else
			{
				aHistoryData.update(rHistoryRecord, true);
			}
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		addSubFragment(HistoryData.HISTORY_DATA_FRAGMENT, aHistoryData);

		RelationType<List<RelationType<?>>> rFragmentParam =
			getFragmentParameter();

		setUIFlag(VERTICAL, rFragmentParam);
		setUIProperty(UserInterfaceProperties.LAYOUT,
					  LayoutType.SPLIT,
					  rFragmentParam);

		setInteractive(InteractiveInputMode.BOTH, ENTITY_HISTORY);

		setUIFlag(HIDE_LABEL, aInteractionParams);
		setUIProperty(450, HEIGHT, ENTITY_HISTORY);
	}

	/***************************************
	 * Resets all history input criteria.
	 */
	public void resetHistoryCriteria()
	{
		setParameter(ENTITY_HISTORY_ORIGIN, ITEM_ENTITY_HISTORY_ORIGIN_ALL);
		deleteParameters(ENTITY_HISTORY_TYPES,
						 ENTITY_HISTORY_TARGET,
						 ENTITY_HISTORY_TARGET_CRITERIA,
						 ENTITY_HISTORY_DATE);
	}

	/***************************************
	 * Updates the history display to reflect the state of the input parameters
	 * of this fragment.
	 */
	@Override
	@SuppressWarnings("boxing")
	public void update()
	{
		Set<HistoryType>     rHistoryTypes = getParameter(ENTITY_HISTORY_TYPES);
		Predicate<Relatable> pHistory;

		Entity rTarget = getParameter(ENTITY_HISTORY_TARGET);

		if (rCurrentTarget != rTarget)
		{
			rCurrentTarget = rTarget;

			Parameter<String> rOrigin = param(ENTITY_HISTORY_ORIGIN);

			param(ENABLE_HISTORY_ORIGIN).value(false);
			rOrigin.setEnabled(false);
			rOrigin.value(ITEM_ENTITY_HISTORY_ORIGIN_ALL);
		}

		if (rTarget != null ||
			rHistoryOrigin != null ||
			!getParameter(ENTITY_HISTORY_ORIGINS).isEmpty())
		{
			if (rHistoryTypes == null || rHistoryTypes.size() == 0)
			{
				rHistoryTypes = EnumSet.of(HistoryType.NOTE);
			}

			pHistory = HistoryRecord.TYPE.is(elementOf(rHistoryTypes));
			pHistory = addTargetFilter(rTarget, pHistory);
			pHistory = addDateRangeFilter(pHistory);
			pHistory = addOriginFilter(pHistory);
		}
		else
		{
			// dummy query that yields no result if no target or origins have
			// been set
			pHistory = HistoryRecord.TARGET.is(equalTo("<NOTHING>"));
		}

		QueryPredicate<HistoryRecord> qHistory =
			new QueryPredicate<>(HistoryRecord.class, pHistory);

		List<Function<? super HistoryRecord, ?>> aAttributes =
			getHistoryQueryAttributes(bShowRootTarget, bShowTarget);

		qHistory.set(HIERARCHICAL_QUERY_MODE,
					 getParameter(ENTITY_HISTORY_QUERY_MODE));

		annotateForEntityQuery(ENTITY_HISTORY,
							   qHistory,
							   sortBy(HistoryRecord.TIME, false),
							   aAttributes);

		setParameter(ENTITY_HISTORY, null);
		setUIProperty(-1, CURRENT_SELECTION, ENTITY_HISTORY);

		aHistoryData.update(null, true);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initComplete()
	{
		update();
	}

	/***************************************
	 * This method can be overridden by subclasses to react to an action event
	 * for a certain history record. The default implementation does nothing.
	 *
	 * @param rHistoryRecord The history record for which the even occurred
	 */
	protected void performHistoryRecordAction(HistoryRecord rHistoryRecord)
	{
	}

	/***************************************
	 * Adds a date range filter if the corresponding parameter is set.
	 *
	 * @param  pHistory The filter predicate to amend
	 *
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addDateRangeFilter(
		Predicate<Relatable> pHistory)
	{
		StandardDateRange eSelectedDateRange =
			getParameter(ENTITY_HISTORY_DATE);

		if (eSelectedDateRange != null &&
			eSelectedDateRange != StandardDateRange.NONE)
		{
			DateRange aDateRange = DateRange.calculateFor(eSelectedDateRange);

			pHistory =
				pHistory.and(HistoryRecord.TIME.is(greaterOrEqual(aDateRange
																  .getStart()))
							 .and(HistoryRecord.TIME.is(lessThan(aDateRange
																 .getEnd()))));
		}

		return pHistory;
	}

	/***************************************
	 * Adds a history origin filter to a predicate if necessary.
	 *
	 * @param  pHistory The original filter predicate
	 *
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addOriginFilter(Predicate<Relatable> pHistory)
	{
		if (rHistoryOrigin != null)
		{
			pHistory =
				pHistory.and(HistoryRecord.ORIGIN.is(equalTo(rHistoryOrigin)));
		}
		else if (!param(ENTITY_HISTORY_ORIGIN).is(DISABLED))
		{
			String sSelectedOrigin = getParameter(ENTITY_HISTORY_ORIGIN);

			if (ITEM_ENTITY_HISTORY_ORIGIN_ALL.equals(sSelectedOrigin))
			{
				List<Entity> rOrigins = getParameter(ENTITY_HISTORY_ORIGINS);

				if (rOrigins.size() > 0)
				{
					pHistory =
						pHistory.and(HistoryRecord.ORIGIN.is(elementOf(rOrigins)));
				}

				setHistoryOrigins(pHistory);
			}
			else
			{
				Entity rOrigin = aHistoryOrigins.get(sSelectedOrigin);

				pHistory =
					pHistory.and(HistoryRecord.ORIGIN.is(equalTo(rOrigin)));
			}
		}

		return pHistory;
	}

	/***************************************
	 * Adds a history target filter to a predicate if necessary.
	 *
	 * @param  rTarget  The main target to display the history for
	 * @param  pHistory The original filter predicate
	 *
	 * @return The predicate, modified if necessary
	 */
	private Predicate<Relatable> addTargetFilter(
		Entity				 rTarget,
		Predicate<Relatable> pHistory)
	{
		Predicate<Relatable> rTargetCriteria =
			getParameter(ENTITY_HISTORY_TARGET_CRITERIA);

		if (rTargetCriteria != null)
		{
			pHistory = pHistory.and(rTargetCriteria);
		}
		else if (rTarget != null)
		{
			Predicate<Object> pTarget = equalTo(rTarget);

			pHistory =
				pHistory.and(HistoryRecord.ROOT_TARGET.is(pTarget)
							 .or(HistoryRecord.TARGET.is(pTarget)));
		}

		return pHistory;
	}

	/***************************************
	 * Sets the selectable distinct history origins from a history query.
	 *
	 * @param pHistory The history filter predicate
	 */
	private void setHistoryOrigins(Predicate<Relatable> pHistory)
	{
		try
		{
			Collection<Entity> rOrigins =
				EntityManager.getDistinct(HistoryRecord.ORIGIN,
										  new QueryPredicate<>(HistoryRecord.class,
															   pHistory));

			aHistoryOrigins.clear();

			for (Entity rOrigin : rOrigins)
			{
				if (rOrigin != null)
				{
					aHistoryOrigins.put(EntityFunctions.format(rOrigin),
										rOrigin);
				}
			}

			List<String> aOriginNames =
				new ArrayList<>(aHistoryOrigins.keySet());

			aOriginNames.add(0, ITEM_ENTITY_HISTORY_ORIGIN_ALL);
			setAllowedValues(ENTITY_HISTORY_ORIGIN, aOriginNames);
		}
		catch (StorageException e)
		{
			throw new IllegalStateException(e);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A fragment that displays the value of a history record.
	 *
	 * @author eso
	 */
	public static class HistoryData extends InteractionFragment
		implements EditListener<String>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		/**
		 * A standard parameter that can be used to display this fragment as a
		 * sub-fragment.
		 */
		public static final RelationType<List<RelationType<?>>> HISTORY_DATA_FRAGMENT =
			newListType();

		private static final RelationType<List<RelationType<?>>> HISTORY_VALUE_FRAGMENT =
			newListType();

		static
		{
			RelationTypes.init(HistoryData.class);
		}

		//~ Instance fields ----------------------------------------------------

		private List<RelationType<?>> aInteractionParams =
			params(HISTORY_VALUE_FRAGMENT,
				   HistoryOptions.HISTORY_OPTIONS_FRAGMENT);

		private List<RelationType<?>> aInputParams = params();

		private boolean bNoteEditAllowed;

		private HistoryRecord  rEditedHistoryNote;
		private EditText	   aEditHistoryValue;
		private HistoryOptions aHistoryOptions;

		private Entity rHistoryTarget;

		private Entity rHistoryRootTarget;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rOptionsListener  A listener that will be notified of option
		 *                          changes
		 * @param bNoteEditAllowed  TRUE to allow the editing of history records
		 *                          of type {@link HistoryType#NOTE}
		 * @param bShowOriginFilter TRUE to display a list of history origins to
		 *                          select from
		 */
		public HistoryData(Updatable rOptionsListener,
						   boolean   bNoteEditAllowed,
						   boolean   bShowOriginFilter)
		{
			this.bNoteEditAllowed = bNoteEditAllowed;

			aHistoryOptions =
				new HistoryOptions(rOptionsListener, bShowOriginFilter);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void editFinished(String sValue, EditAction eFinishAction)
		{
			if (eFinishAction == EditAction.SAVE)
			{
				try
				{
					storeHistoryNote(sValue);
				}
				catch (TransactionException e)
				{
					throw new RuntimeProcessException(this, e);
				}
			}

			rEditedHistoryNote = null;
			update(getParameter(ENTITY_HISTORY), false);
			setEnabled(true, ENTITY_HISTORY);
			aHistoryOptions.enableEdit(true);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void editStarted(String sValue)
		{
			aHistoryOptions.enableEdit(false);
			setEnabled(false, ENTITY_HISTORY);

			if (sValue != null)
			{
				rEditedHistoryNote = getParameter(ENTITY_HISTORY);
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInputParameters()
		{
			return aInputParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInteractionParameters()
		{
			return aInteractionParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
			rEditedHistoryNote = null;

			aEditHistoryValue =
				new EditText(ENTITY_HISTORY_VALUE, this, true, false);

			addSubFragment(HISTORY_VALUE_FRAGMENT, aEditHistoryValue);
			addSubFragment(HistoryOptions.HISTORY_OPTIONS_FRAGMENT,
						   aHistoryOptions);

			setUIFlag(HIDE_LABEL, aInteractionParams);
			setUIFlag(SAME_ROW,
					  HISTORY_VALUE_FRAGMENT,
					  HistoryOptions.HISTORY_OPTIONS_FRAGMENT);

			setUIProperty(LABEL,
						  "",
						  HISTORY_VALUE_FRAGMENT,
						  HISTORY_DATA_FRAGMENT);

			setHtmlSize("100%", "100%", HISTORY_VALUE_FRAGMENT);
		}

		/***************************************
		 * Updates the display and state for a certain history record.
		 *
		 * @param rRecord   The history record (NULL to clear)
		 * @param bStopEdit TRUE if an active editor should be stopped
		 */
		public void update(HistoryRecord rRecord, boolean bStopEdit)
		{
			if (bStopEdit &&
				aEditHistoryValue != null &&
				aEditHistoryValue.isEditing())
			{
				aEditHistoryValue.stopEditing(EditAction.SAVE);
			}

			String  sValue     = "";
			boolean bAllowEdit = false;

			rHistoryTarget     = getParameter(ENTITY_HISTORY_TARGET);
			rHistoryRootTarget = getParameter(ENTITY_HISTORY_ROOT_TARGET);

			if (rRecord != null)
			{
				sValue     = rRecord.get(HistoryRecord.VALUE);
				bAllowEdit =
					rRecord.get(HistoryRecord.TYPE) == HistoryType.NOTE &&
					(bNoteEditAllowed ||
					 getProcessUser().equals(rRecord.get(HistoryRecord.ORIGIN)));
			}

			setParameter(ENTITY_HISTORY_VALUE, sValue);

			String sEditInfo = "";

			if (rHistoryTarget != null)
			{
				sEditInfo =
					rHistoryTarget.getClass().getSimpleName() + ": " +
					EntityFunctions.format(rHistoryTarget);
			}

			if (aEditHistoryValue != null)
			{
				aEditHistoryValue.setEditInfo(sEditInfo);
				aEditHistoryValue.allowEdit(bAllowEdit);
			}
		}

		/***************************************
		 * Stores the currently edited history note. A new note will be stored
		 * with {@link HistoryManager#record(HistoryType, Entity, Entity,
		 * String)}, an edited note will simply be stored.
		 *
		 * @param  sValue The new value for the history note
		 *
		 * @throws TransactionException
		 */
		private void storeHistoryNote(String sValue) throws TransactionException
		{
			if (rEditedHistoryNote != null)
			{
				rEditedHistoryNote.set(HistoryRecord.VALUE, sValue);
				EntityManager.storeEntity(rEditedHistoryNote, getProcessUser());
				setParameter(ENTITY_HISTORY, rEditedHistoryNote);
			}
			else
			{
				HistoryManager.record(HistoryType.NOTE,
									  getProcessUser(),
									  rHistoryTarget,
									  rHistoryRootTarget,
									  sValue,
									  null,
									  null);
			}

			markParameterAsModified(ENTITY_HISTORY);
		}
	}

	/********************************************************************
	 * A fragment that displays the value and options for an {@link
	 * DisplayEntityHistory} parent fragment.
	 *
	 * @author eso
	 */
	public static class HistoryOptions extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		/**
		 * A standard parameter that can be used to display this fragment as a
		 * sub-fragment.
		 */
		public static final RelationType<List<RelationType<?>>> HISTORY_OPTIONS_FRAGMENT =
			newListType();

		static
		{
			RelationTypes.init(HistoryOptions.class);
		}

		//~ Instance fields ----------------------------------------------------

		private List<RelationType<?>> aInteractionParams =
			params(HISTORY_TYPE_OPTIONS,
				   ENTITY_HISTORY_DATE,
				   ENABLE_HISTORY_ORIGIN,
				   ENTITY_HISTORY_ORIGIN);

		private List<RelationType<?>> aInputParams =
			params(HISTORY_TYPE_OPTIONS,
				   ENTITY_HISTORY_DATE,
				   ENABLE_HISTORY_ORIGIN,
				   ENTITY_HISTORY_ORIGIN);

		private Updatable rChangeListener;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rChangeListener   An {@link Updatable} instance to be notified
		 *                          if the history options have been modified
		 * @param bShowOriginFilter TRUE to display a list of history origins to
		 *                          select from
		 */
		public HistoryOptions(
			Updatable rChangeListener,
			boolean   bShowOriginFilter)
		{
			this.rChangeListener = rChangeListener;

			if (!bShowOriginFilter)
			{
				removeInteractionParameters(ENABLE_HISTORY_ORIGIN,
											ENTITY_HISTORY_ORIGIN);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInputParameters()
		{
			return aInputParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInteractionParameters()
		{
			return aInteractionParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(RelationType<?> rInteractionParam)
			throws Exception
		{
			if (rInteractionParam == HISTORY_TYPE_OPTIONS)
			{
				setParameter(ENTITY_HISTORY_TYPES,
							 new LinkedHashSet<>(getParameter(HISTORY_TYPE_OPTIONS)));
			}

			rChangeListener.update();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
			Set<HistoryType> rHistoryTypes = getParameter(ENTITY_HISTORY_TYPES);

			if (rHistoryTypes != null && rHistoryTypes.size() > 1)
			{
				setParameter(HISTORY_TYPE_OPTIONS,
							 EnumSet.of(HistoryType.NOTE));
				setParameter(ENTITY_HISTORY_TYPES,
							 new LinkedHashSet<>(getParameter(HISTORY_TYPE_OPTIONS)));
			}
			else
			{
				removeInteractionParameters(HISTORY_TYPE_OPTIONS);
			}

			setInteractive(ENTITY_HISTORY_ORIGIN,
						   ITEM_ENTITY_HISTORY_ORIGIN_ALL,
						   ListStyle.DROP_DOWN,
						   ITEM_ENTITY_HISTORY_ORIGIN_ALL);
			setInteractive(ENTITY_HISTORY_DATE,
						   StandardDateRange.NONE,
						   ListStyle.DROP_DOWN,
						   StandardDateRange.NONE,
						   StandardDateRange.TODAY,
						   StandardDateRange.YESTERDAY,
						   StandardDateRange.BEFORE_YESTERDAY,
						   StandardDateRange.CURRENT_WEEK,
						   StandardDateRange.LAST_WEEK,
						   StandardDateRange.CURRENT_MONTH,
						   StandardDateRange.LAST_MONTH,
						   StandardDateRange.CURRENT_QUARTER,
						   StandardDateRange.LAST_QUARTER,
						   StandardDateRange.CURRENT_YEAR,
						   StandardDateRange.LAST_YEAR);

			setInteractive(HISTORY_TYPE_OPTIONS,
						   null,
						   ListStyle.DISCRETE,
						   rHistoryTypes);

			param(ENABLE_HISTORY_ORIGIN).label("")
										.onAction(this::setOriginsEnabled);
			param(ENTITY_HISTORY_ORIGIN).sameRow().disable();
			param(HISTORY_TYPE_OPTIONS).colSpan(2);
			param(ENTITY_HISTORY_DATE).colSpan(2);

			setUIFlag(HIDE_LABEL, aInteractionParams);
			setUIProperty(RESOURCE_ID,
						  StandardDateRange.class.getSimpleName(),
						  ENTITY_HISTORY_DATE);
		}

		/***************************************
		 * Sets the state of the history origins display.
		 *
		 * @param bEnable TRUE to enable
		 */
		private void setOriginsEnabled(boolean bEnable)
		{
			Parameter<String> rOrigin = param(ENTITY_HISTORY_ORIGIN);

			rOrigin.setEnabled(bEnable);

			if (!bEnable)
			{
				rOrigin.value(ITEM_ENTITY_HISTORY_ORIGIN_ALL);
			}
		}
	}
}
