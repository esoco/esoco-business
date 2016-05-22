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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityDefinition;
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes;
import de.esoco.entity.ExtraAttribute;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.ListStyle;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.entity.EntityFunctions.getEntityId;
import static de.esoco.entity.EntityFunctions.getExtraAttributeValue;
import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityRelationTypes.ENTITY_TAGS;

import static de.esoco.lib.expression.CollectionFunctions.collectAllInto;
import static de.esoco.lib.expression.CollectionFunctions.collectInto;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.HAS_IMAGES;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.LIST_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;

import static de.esoco.storage.StoragePredicates.like;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newSetType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A generic interaction fragment for the viewing and editing of entity tags
 * that are stored in the {@link EntityRelationTypes#ENTITY_TAGS} extra
 * attribute.
 *
 * @author eso
 */
public class FilterEntityTags<E extends Entity> extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available actions for editing entity tags.
	 */
	private enum TagFilterAction { REMOVE, CLEAR }

	/********************************************************************
	 * Enumeration of the possible joins between tag filters.
	 */
	private enum TagFilterJoin { OR, AND }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>> FILTER_ENTITY_TAGS_FRAGMENT =
		newListType();

	private static final RelationType<Set<String>> FILTER_ENTITY_TAGS =
		newSetType(true);

	private static final RelationType<TagFilterJoin> TAG_FILTER_JOIN =
		newInitialValueType(TagFilterJoin.OR);

	private static final RelationType<Boolean> TAG_FILTER_NEGATE = newType();

	private static final RelationType<TagFilterAction> TAG_FILTER_ACTION =
		newType();

	private static final List<RelationType<?>> INTERACTION_PARAMS =
		Arrays.<RelationType<?>>asList(FILTER_ENTITY_TAGS,
									   TAG_FILTER_JOIN,
									   TAG_FILTER_NEGATE,
									   TAG_FILTER_ACTION);

	private static final List<RelationType<?>> INPUT_PARAMS =
		Arrays.<RelationType<?>>asList(FILTER_ENTITY_TAGS,
									   TAG_FILTER_JOIN,
									   TAG_FILTER_NEGATE,
									   TAG_FILTER_ACTION);

	static
	{
		RelationTypes.init(FilterEntityTags.class);
	}

	//~ Instance fields --------------------------------------------------------

	private Class<E>    rEntityType;
	EntityDefinition<E> rEntityDefinition;

	private Entity				 rTagOwner;
	private TagFilterListener<E> rTagFilterListener;
	private String				 sLabel;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityType        The type of entity to filter
	 * @param rTagOwner          The owner of the tags to filter by or NULL to
	 *                           filter global tags
	 * @param rTagFilterListener The listener for filter changes
	 * @param sLabel             An optional label in this fragment (empty
	 *                           string for none, NULL for the default)
	 */
	public FilterEntityTags(Class<E>			 rEntityType,
							Entity				 rTagOwner,
							TagFilterListener<E> rTagFilterListener,
							String				 sLabel)
	{
		this.rEntityType	    = rEntityType;
		this.rTagOwner		    = rTagOwner;
		this.rTagFilterListener = rTagFilterListener;
		this.sLabel			    = sLabel;

		rEntityDefinition = EntityManager.getEntityDefinition(rEntityType);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns all tags that are stored for a certain entity type.
	 *
	 * @param  rEntityType The entity class to return the tags for
	 * @param  rTagOwner   The owner entities of which to include additional
	 *                     tags or NULL for global tags only
	 *
	 * @return A set containing the distinct tags of the given entity type
	 *
	 * @throws StorageException If reading the tags fails
	 */
	public static Set<String> getAllEntityTags(
		Class<? extends Entity> rEntityType,
		Entity					rTagOwner) throws StorageException
	{
		Predicate<Relatable> pExtraAttr =
			getEntityTagsPredicate(rEntityType, rTagOwner);

		Set<String> aAllTags = new HashSet<>();

		QueryPredicate<ExtraAttribute> qExtraAttributes =
			forEntity(ExtraAttribute.class, pExtraAttr);

		Function<ExtraAttribute, Set<String>> fCollect =
			collectAllInto(aAllTags).from(getExtraAttributeValue(ENTITY_TAGS));

		EntityManager.evaluateEntities(qExtraAttributes, null, fCollect);

		List<String> aSortedTags = new ArrayList<>(aAllTags);

		Collections.sort(aSortedTags);
		aAllTags = new LinkedHashSet<>(aSortedTags);

		return aAllTags;
	}

	/***************************************
	 * Returns a predicate that can be used to query all tag extra attributes
	 * for the given entity type.
	 *
	 * @param  rEntityType The entity class to return the predicate for
	 * @param  rTagOwner   An owner entity of which to include additional tags
	 *                     or NULL for global tags only
	 *
	 * @return A new predicate that matches all tags for the given entity type
	 */
	private static Predicate<Relatable> getEntityTagsPredicate(
		Class<? extends Entity> rEntityType,
		Entity					rTagOwner)
	{
		String sIdPrefix =
			EntityManager.getEntityDefinition(rEntityType).getIdPrefix();

		Predicate<Relatable> pHasTagOwner =
			ExtraAttribute.OWNER.is(equalTo(rTagOwner));

		Predicate<Relatable> pExtraAttr =
			ExtraAttribute.KEY.is(equalTo(ENTITY_TAGS)).and(pHasTagOwner);

		Predicate<Object> pEntityPrefix =
			like(sIdPrefix + EntityManager.GLOBAL_ID_PREFIX_SEPARATOR +
				 "%");

		pExtraAttr = pExtraAttr.and(ExtraAttribute.ENTITY.is(pEntityPrefix));

		return pExtraAttr;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a set of the filtered entity IDs for the currently selected
	 * filter tags.
	 *
	 * @return The filter predicate for the selected tags or NULL if the filter
	 *         is empty
	 *
	 * @throws StorageException
	 */
	public Set<Integer> getFilteredEntityIds() throws StorageException
	{
		Set<String>  rFilterTags  = getParameter(FILTER_ENTITY_TAGS);
		Set<Integer> aFilteredIds = null;

		if (rFilterTags.size() > 0)
		{
			aFilteredIds = new HashSet<Integer>();

			Predicate<Relatable> pHasFilterTags = null;

			boolean bOr = (getParameter(TAG_FILTER_JOIN) == TagFilterJoin.OR);

			for (String sTag : rFilterTags)
			{
				Predicate<Relatable> pTag;

				pTag = ExtraAttribute.VALUE.is(like("%," + sTag + ",%"));
				pTag = pTag.or(ExtraAttribute.VALUE.is(like("%," + sTag)));
				pTag = pTag.or(ExtraAttribute.VALUE.is(like(sTag + ",%")));
				pTag = pTag.or(ExtraAttribute.VALUE.is(equalTo(sTag)));

				if (bOr)
				{
					pHasFilterTags = Predicates.or(pHasFilterTags, pTag);
				}
				else
				{
					pHasFilterTags = Predicates.and(pHasFilterTags, pTag);
				}
			}

			Predicate<Relatable> pIsEntityTag =
				getEntityTagsPredicate(rEntityType, rTagOwner);

			QueryPredicate<ExtraAttribute> qExtraAttributes =
				forEntity(ExtraAttribute.class,
						  pIsEntityTag.and(pHasFilterTags));

			Function<Relatable, Set<Integer>> fCollect =
				getEntityId().from(ExtraAttribute.ENTITY)
							 .then(collectInto(aFilteredIds));

			EntityManager.evaluateEntities(qExtraAttributes, null, fCollect);
		}

		return aFilteredIds;
	}

	/***************************************
	 * Returns a predicate that filters the IDs of the this instance's entity
	 * type that represents the current tag selection.
	 *
	 * @param  rIdAttr The entity ID attribute to filter
	 *
	 * @return The predicate on the filtered entity IDs or NULL if no tags are
	 *         filtered
	 *
	 * @throws StorageException If determining the filtered entity IDs fails
	 */
	@SuppressWarnings("boxing")
	public Predicate<Entity> getFilteredIdsPredicate(
		RelationType<Integer> rIdAttr) throws StorageException
	{
		Predicate<Entity> pTagFilter   = null;
		Set<Integer>	  rFilteredIds = getFilteredEntityIds();

		if (rFilteredIds != null)
		{
			if (rFilteredIds.size() > 0)
			{
				Predicate<Object> pHasFilteredId = elementOf(rFilteredIds);

				if (isNegateFilter())
				{
					pHasFilteredId = not(pHasFilteredId);
				}

				pTagFilter = rIdAttr.is(pHasFilteredId);
			}
			else
			{
				pTagFilter = rIdAttr.is(equalTo(-1));
			}
		}

		return pTagFilter;
	}

	/***************************************
	 * Returns a collection of the names of the filtered tags. The tags are in
	 * the same order as displayed in the UI.
	 *
	 * @return A new collection containing the tag strings
	 */
	public Collection<String> getFilteredTags()
	{
		return new LinkedHashSet<>(getParameter(FILTER_ENTITY_TAGS));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return INPUT_PARAMS;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		return INTERACTION_PARAMS;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		Set<String> rFilterTags = getParameter(FILTER_ENTITY_TAGS);

		if (rInteractionParam == TAG_FILTER_ACTION)
		{
			switch (getParameter(TAG_FILTER_ACTION))
			{
				case CLEAR:
					rFilterTags.clear();
					break;

				case REMOVE:

					// remove the last element in the tag set
					Iterator<String> rIterator = rFilterTags.iterator();

					while (rIterator.hasNext())
					{
						rIterator.next();
					}

					rIterator.remove();
					break;
			}

			markParameterAsModified(FILTER_ENTITY_TAGS);
		}

		rTagFilterListener.filterTagsChanged(this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws StorageException
	{
		setImmediateAction(TAG_FILTER_ACTION);

		setInteractive(InteractiveInputMode.CONTINUOUS, TAG_FILTER_JOIN);
		setInteractive(InteractiveInputMode.ACTION, TAG_FILTER_NEGATE);
		setInteractive(FILTER_ENTITY_TAGS,
					   null,
					   ListStyle.EDITABLE,
					   getAllEntityTags(rEntityType, rTagOwner));

		setAllowedValues(TAG_FILTER_JOIN, TagFilterJoin.OR, TagFilterJoin.AND);

		setUIFlag(SAME_ROW,
				  TAG_FILTER_JOIN,
				  TAG_FILTER_NEGATE,
				  TAG_FILTER_ACTION);
		setUIFlag(HAS_IMAGES, TAG_FILTER_ACTION);

		setUIProperty(2, COLUMNS, TAG_FILTER_ACTION);

		setUIProperty(LIST_STYLE, ListStyle.DROP_DOWN, TAG_FILTER_JOIN);

		if (sLabel != null)
		{
			if (sLabel.length() > 0)
			{
				setUIProperty(LABEL, sLabel, FILTER_ENTITY_TAGS);
			}
			else
			{
				setUIFlag(HIDE_LABEL, FILTER_ENTITY_TAGS);
			}
		}
	}

	/***************************************
	 * Checks whether the filter negation flag is set.
	 *
	 * @return The filter negation flag state
	 */
	public boolean isNegateFilter()
	{
		return hasFlagParameter(TAG_FILTER_NEGATE);
	}

	/***************************************
	 * Updates this list of all tags that can be selected for the entity type.
	 *
	 * @throws StorageException
	 */
	public void updateTagList() throws StorageException
	{
		setAllowedElements(FILTER_ENTITY_TAGS,
						   getAllEntityTags(rEntityType, rTagOwner));
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * A listener interface for changes to the list of filter tags.
	 *
	 * @author eso
	 */
	public static interface TagFilterListener<E extends Entity>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked when the list of filter tags has changed.
		 *
		 * @param rFilter The entity tag filter instance in which the change
		 *                occurred
		 */
		public void filterTagsChanged(FilterEntityTags<E> rFilter);
	}
}
