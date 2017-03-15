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
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes;
import de.esoco.entity.ExtraAttribute;

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.StyleProperties;

import de.esoco.process.CollectionParameter.SetParameter;
import de.esoco.process.EnumParameter;
import de.esoco.process.Parameter;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import static de.esoco.entity.EntityFunctions.getEntityId;
import static de.esoco.entity.EntityFunctions.getExtraAttributeValue;
import static de.esoco.entity.EntityRelationTypes.ENTITY_TAGS;

import static de.esoco.lib.expression.CollectionFunctions.collectAllInto;
import static de.esoco.lib.expression.CollectionFunctions.collectInto;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.not;

import static de.esoco.storage.StoragePredicates.like;


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
	public enum TagFilterAction { REMOVE, CLEAR }

	/********************************************************************
	 * Enumeration of the possible joins between tag filters.
	 */
	public enum TagFilterJoin { OR, AND }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Class<E> rEntityType;
	private Entity   rTagOwner;

	private TagFilterListener<E> rTagFilterListener;
	private String				 sLabel;
	private Layout				 eLayout;
	private boolean				 bSingleRow;

	private boolean     bUseHeaderLabel = false;
	private ButtonStyle eButtonStyle    = ButtonStyle.DEFAULT;

	private SetParameter<String>     aTagInput;
	private Parameter<TagFilterJoin> aFilterJoin;
	private Parameter<Boolean>		 aFilterNegate;

	private EnumParameter<TagFilterAction> aFilterAction;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityType The type of entity to filter
	 * @param rTagOwner   The owner of the tags to filter by or NULL to filter
	 *                    global tags
	 */
	public FilterEntityTags(Class<E> rEntityType, Entity rTagOwner)
	{
		this(rEntityType, rTagOwner, null, null);
	}

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
		this(rEntityType,
			 rTagOwner,
			 rTagFilterListener,
			 sLabel,
			 Layout.TABLE,
			 true);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityType        The type of entity to filter
	 * @param rTagOwner          The owner of the tags to filter by or NULL to
	 *                           filter global tags
	 * @param rTagFilterListener The listener for filter changes
	 * @param sLabel             An optional label in this fragment (empty
	 *                           string for none, NULL for the default)
	 * @param eLayout            The layout for this fragment
	 * @param bSingleRow         TRUE to display all elements in a single row
	 */
	public FilterEntityTags(Class<E>			 rEntityType,
							Entity				 rTagOwner,
							TagFilterListener<E> rTagFilterListener,
							String				 sLabel,
							Layout				 eLayout,
							boolean				 bSingleRow)
	{
		this.rEntityType	    = rEntityType;
		this.rTagOwner		    = rTagOwner;
		this.rTagFilterListener = rTagFilterListener;
		this.sLabel			    = sLabel;
		this.eLayout		    = eLayout;
		this.bSingleRow		    = bSingleRow;
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
		Predicate<Relatable> pIsEntityTag =
			getEntityTagsPredicate(rEntityType, rTagOwner);

		Set<String> aAllTags = new HashSet<>();

		Action<ExtraAttribute> fCollect =
			collectAllInto(aAllTags).from(getExtraAttributeValue(ENTITY_TAGS));

		EntityManager.forEach(ExtraAttribute.class, pIsEntityTag, fCollect);

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
	 * Returns the tags that can be selected in this tag filter.
	 *
	 * @return A new ordered set instance containing the allowed tag string
	 *         which may be modified by the receiver
	 */
	public Set<String> getAllowedTags()
	{
		LinkedHashSet<String> aAllowedTags = new LinkedHashSet<String>();

		if (aTagInput != null)
		{
			aAllowedTags.addAll(aTagInput.allowedElements());
		}

		return aAllowedTags;
	}

	/***************************************
	 * Returns the entity type on which this tag filter operates.
	 *
	 * @return The entity type
	 */
	public Class<E> getEntityType()
	{
		return rEntityType;
	}

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
		Set<String> rFilterTags =
			aTagInput != null ? aTagInput.value()
							  : Collections.<String>emptySet();

		Set<Integer> aFilteredIds = null;

		if (rFilterTags.size() > 0)
		{
			aFilteredIds = new HashSet<Integer>();

			Predicate<Relatable> pHasFilterTags = null;

			boolean bOr = (aFilterJoin.value() == TagFilterJoin.OR);

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

			Predicate<Relatable> pIfEntityTag =
				getEntityTagsPredicate(rEntityType, rTagOwner);

			Action<Relatable> fCollect =
				collectInto(aFilteredIds).from(getEntityId().from(ExtraAttribute.ENTITY));

			EntityManager.forEach(ExtraAttribute.class,
								  pIfEntityTag.and(pHasFilterTags),
								  fCollect);
		}

		return aFilteredIds;
	}

	/***************************************
	 * Returns a predicate that filters the IDs of the this instance's entity
	 * type that represents the current tag selection.
	 *
	 * @param  rTagFilterAttr The entity ID or reference attribute to filter
	 *
	 * @return The predicate on the filtered entity IDs or NULL if no tags are
	 *         filtered
	 *
	 * @throws StorageException If determining the filtered entity IDs fails
	 */
	@SuppressWarnings("boxing")
	public Predicate<Entity> getFilteredIdsPredicate(
		RelationType<?> rTagFilterAttr) throws StorageException
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

				pTagFilter = rTagFilterAttr.is(pHasFilteredId);
			}
			else
			{
				pTagFilter = rTagFilterAttr.is(equalTo(-1));
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
		return new LinkedHashSet<>(aTagInput.value());
	}

	/***************************************
	 * Returns the listener for tag filter changes.
	 *
	 * @return The current tag filter listener or NULL for none
	 */
	public final TagFilterListener<E> getTagFilterListener()
	{
		return rTagFilterListener;
	}

	/***************************************
	 * Returns the owner of which the tags are filtered.
	 *
	 * @return The tag owner entity
	 */
	public final Entity getTagOwner()
	{
		return rTagOwner;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		Set<String> rFilterTags = aTagInput.value();

		if (rInteractionParam == aFilterAction.type())
		{
			switch (aFilterAction.value())
			{
				case CLEAR:
					rFilterTags.clear();
					break;

				case REMOVE:

					// remove the last element in the tag set
					Iterator<String> rIterator = rFilterTags.iterator();

					if (rIterator.hasNext())
					{
						while (rIterator.hasNext())
						{
							rIterator.next();
						}

						rIterator.remove();
					}

					break;
			}

			aTagInput.modified();
		}

		rTagFilterListener.filterTagsChanged(this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws StorageException
	{
		layout(eLayout).resid("FilterEntityTagsFragment");

		if (aTagInput == null)
		{
			Set<String> rAllTags = getAllEntityTags(rEntityType, rTagOwner);

			aTagInput =
				inputTags(rAllTags).resid("FilterEntityTags")
								   .continuousEvents();

			if (bUseHeaderLabel)
			{
				aTagInput.set(StyleProperties.HEADER_LABEL);
			}

			aFilterJoin =
				dropDown(TagFilterJoin.class).resid("TagFilterJoin")
											 .continuousEvents();

			if (bSingleRow)
			{
				aFilterJoin.sameRow();
			}

			aFilterNegate =
				checkBox("TagFilterNegate").sameRow().actionEvents();

			aFilterAction =
				imageButtons(TagFilterAction.class).buttonStyle(eButtonStyle)
												   .layout(Layout.TABLE)
												   .sameRow()
												   .columns(2)
												   .resid("TagFilterAction");
		}

		if (sLabel != null)
		{
			if (sLabel.length() > 0)
			{
				aTagInput.label(sLabel);
			}
			else
			{
				aTagInput.hideLabel();
			}
		}
	}

	/***************************************
	 * Checks whether the filter negation flag is set.
	 *
	 * @return The filter negation flag state
	 */
	@SuppressWarnings("boxing")
	public boolean isNegateFilter()
	{
		return aFilterNegate.value();
	}

	/***************************************
	 * Sets the button style to be used for the fragment buttons.
	 *
	 * @param eStyle The new button style
	 */
	public final void setButtonStyle(ButtonStyle eStyle)
	{
		eButtonStyle = eStyle;
	}

	/***************************************
	 * Sets the entity type on which this tag filter operates.
	 *
	 * @param  rEntityType The new entity type
	 *
	 * @throws StorageException If querying the allowed tags fails
	 */
	public void setEntityType(Class<E> rEntityType) throws StorageException
	{
		this.rEntityType = rEntityType;
		updateAllowedTags();
	}

	/***************************************
	 * Sets the listener for tag filter changes.
	 *
	 * @param rListener The new tag filter listener
	 */
	public final void setTagFilterListener(TagFilterListener<E> rListener)
	{
		rTagFilterListener = rListener;
	}

	/***************************************
	 * Controls whether the label of the tag input should be displayed above the
	 * field instead of in front.
	 *
	 * @param bUseHeaderLabel TRUE to use a header label
	 */
	public final void setUseHeaderLabel(boolean bUseHeaderLabel)
	{
		this.bUseHeaderLabel = bUseHeaderLabel;
	}

	/***************************************
	 * Updates the set of tags that can be selected for the entity type and tag
	 * owner.
	 *
	 * @throws StorageException
	 */
	public void updateAllowedTags() throws StorageException
	{
		aTagInput.allowElements(getAllEntityTags(rEntityType, rTagOwner));
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
