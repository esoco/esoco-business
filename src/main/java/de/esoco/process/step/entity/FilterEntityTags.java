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
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes;
import de.esoco.entity.ExtraAttribute;
import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.StyleProperties;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.param.CollectionParameter.SetParameter;
import de.esoco.process.param.EnumParameter;
import de.esoco.process.param.Parameter;
import de.esoco.process.param.ParameterList;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.entity.EditEntityTags.TagEditListener;
import de.esoco.storage.StorageException;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.esoco.entity.EntityFunctions.getEntityId;
import static de.esoco.entity.EntityFunctions.getExtraAttributeValue;
import static de.esoco.entity.EntityRelationTypes.ENTITY_TAGS;
import static de.esoco.lib.expression.CollectionFunctions.collectAllInto;
import static de.esoco.lib.expression.CollectionFunctions.collectInto;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.storage.StoragePredicates.like;

/**
 * A generic interaction fragment for the viewing and editing of entity tags
 * that are stored in the {@link EntityRelationTypes#ENTITY_TAGS} extra
 * attribute.
 *
 * @author eso
 */
public class FilterEntityTags<E extends Entity> extends InteractionFragment
	implements TagEditListener {

	/**
	 * Enumeration of the available actions for editing entity tags.
	 */
	public enum TagFilterAction {REMOVE, CLEAR, HELP}

	/**
	 * Enumeration of the possible joins between tag filters.
	 */
	public enum TagFilterJoin {OR, AND}

	private static final long serialVersionUID = 1L;

	private final Entity tagOwner;

	private final Runnable helpAction;

	private final String label;

	private final LayoutType layout;

	private final boolean singleRow;

	private Class<E> entityType;

	private TagFilterListener<E> tagFilterListener;

	private boolean useHeaderLabel = false;

	private ButtonStyle buttonStyle = ButtonStyle.DEFAULT;

	private SetParameter<String> tagInput;

	private Parameter<TagFilterJoin> filterJoin;

	private Parameter<Boolean> filterNegate;

	private EnumParameter<TagFilterAction> filterAction;

	private ParameterList optionsPanel;

	/**
	 * Creates a new instance.
	 *
	 * @param entityType The type of entity to filter
	 * @param tagOwner   The owner of the tags to filter by or NULL to filter
	 *                   global tags
	 * @param layout     The layout for this fragment
	 * @param helpAction A runnable object to be invoked if the user clicks on
	 *                   the help button; if NULL no help button will be
	 *                   displayed
	 */
	public FilterEntityTags(Class<E> entityType, Entity tagOwner,
		LayoutType layout, Runnable helpAction) {
		this(entityType, tagOwner, null, helpAction, null, layout, true);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param entityType        The type of entity to filter
	 * @param tagOwner          The owner of the tags to filter by or NULL to
	 *                          filter global tags
	 * @param tagFilterListener The listener for filter changes
	 * @param label             An optional label in this fragment (empty
	 *                             string
	 *                          for none, NULL for the default)
	 */
	public FilterEntityTags(Class<E> entityType, Entity tagOwner,
		TagFilterListener<E> tagFilterListener, String label) {
		this(entityType, tagOwner, tagFilterListener, null, label,
			LayoutType.TABLE, true);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param entityType        The type of entity to filter
	 * @param tagOwner          The owner of the tags to filter by or NULL to
	 *                          filter global tags
	 * @param tagFilterListener The listener for filter changes
	 * @param helpAction        A runnable object to be invoked if the user
	 *                          clicks on the help button; if NULL no help
	 *                          button will be displayed
	 * @param label             An optional label in this fragment (empty
	 *                             string
	 *                          for none, NULL for the default)
	 * @param layout            The layout for this fragment
	 * @param singleRow         TRUE to display all elements in a single row
	 */
	public FilterEntityTags(Class<E> entityType, Entity tagOwner,
		TagFilterListener<E> tagFilterListener, Runnable helpAction,
		String label, LayoutType layout, boolean singleRow) {
		this.entityType = entityType;
		this.tagOwner = tagOwner;
		this.tagFilterListener = tagFilterListener;
		this.helpAction = helpAction;
		this.label = label;
		this.layout = layout;
		this.singleRow = singleRow;
	}

	/**
	 * Returns all tags that are stored for a certain entity type.
	 *
	 * @param entityType The entity class to return the tags for
	 * @param tagOwner   The owner entities of which to include additional tags
	 *                   or NULL for global tags only
	 * @return A set containing the distinct tags of the given entity type
	 * @throws StorageException If reading the tags fails
	 */
	public static Set<String> getAllEntityTags(
		Class<? extends Entity> entityType, Entity tagOwner)
		throws StorageException {
		Predicate<Relatable> isEntityTag =
			getEntityTagsPredicate(entityType, tagOwner);

		Set<String> allTags = new HashSet<>();

		Action<ExtraAttribute> collect =
			collectAllInto(allTags).from(getExtraAttributeValue(ENTITY_TAGS));

		EntityManager.forEach(ExtraAttribute.class, isEntityTag, collect);

		List<String> sortedTags = new ArrayList<>(allTags);

		Collections.sort(sortedTags);
		allTags = new LinkedHashSet<>(sortedTags);

		return allTags;
	}

	/**
	 * Returns a predicate that can be used to query all tag extra attributes
	 * for the given entity type.
	 *
	 * @param entityType The entity class to return the predicate for
	 * @param tagOwner   An owner entity of which to include additional tags or
	 *                   NULL for global tags only
	 * @return A new predicate that matches all tags for the given entity type
	 */
	private static Predicate<Relatable> getEntityTagsPredicate(
		Class<? extends Entity> entityType, Entity tagOwner) {
		String idPrefix =
			EntityManager.getEntityDefinition(entityType).getIdPrefix();

		Predicate<Relatable> hasTagOwner =
			ExtraAttribute.OWNER.is(equalTo(tagOwner));

		Predicate<Relatable> extraAttr =
			ExtraAttribute.KEY.is(equalTo(ENTITY_TAGS)).and(hasTagOwner);

		Predicate<Object> entityPrefix =
			like(idPrefix + EntityManager.GLOBAL_ID_PREFIX_SEPARATOR + "%");

		extraAttr = extraAttr.and(ExtraAttribute.ENTITY.is(entityPrefix));

		return extraAttr;
	}

	/**
	 * Returns the tags that can be selected in this tag filter.
	 *
	 * @return A new ordered set instance containing the allowed tag string
	 * which may be modified by the receiver
	 */
	public Set<String> getAllowedTags() {
		LinkedHashSet<String> allowedTags = new LinkedHashSet<String>();

		if (tagInput != null) {
			allowedTags.addAll(tagInput.allowedElements());
		}

		return allowedTags;
	}

	/**
	 * Returns the entity type on which this tag filter operates.
	 *
	 * @return The entity type
	 */
	public Class<E> getEntityType() {
		return entityType;
	}

	/**
	 * Returns a set of the filtered entity IDs for the currently selected
	 * filter tags.
	 *
	 * @return The filter predicate for the selected tags or NULL if the filter
	 * is empty
	 */
	public Set<Number> getFilteredEntityIds() throws StorageException {
		Set<String> filterTags =
			tagInput != null ? tagInput.value() : Collections.emptySet();

		Set<Number> filteredIds = null;

		if (filterTags.size() > 0) {
			filteredIds = new HashSet<>();

			Predicate<Relatable> hasFilterTags = null;

			for (String tag : filterTags) {
				Predicate<Relatable> hasTag;

				hasTag = ExtraAttribute.VALUE.is(like("%," + tag + ",%"));
				hasTag = hasTag.or(ExtraAttribute.VALUE.is(like("%," + tag)));
				hasTag = hasTag.or(ExtraAttribute.VALUE.is(like(tag + ",%")));
				hasTag = hasTag.or(ExtraAttribute.VALUE.is(equalTo(tag)));

				if (filterJoin.value() == TagFilterJoin.OR) {
					hasFilterTags = Predicates.or(hasFilterTags, hasTag);
				} else {
					hasFilterTags = Predicates.and(hasFilterTags, hasTag);
				}
			}

			Predicate<Relatable> ifEntityTag =
				getEntityTagsPredicate(entityType, tagOwner);

			Action<Relatable> collect = collectInto(filteredIds).from(
				getEntityId().from(ExtraAttribute.ENTITY));

			EntityManager.forEach(ExtraAttribute.class,
				ifEntityTag.and(hasFilterTags), collect);
		}

		return filteredIds;
	}

	/**
	 * Returns a predicate that filters the IDs of the this instance's entity
	 * type that represents the current tag selection.
	 *
	 * @param tagFilterAttr The entity ID or reference attribute to filter
	 * @return The predicate on the filtered entity IDs or NULL if no tags are
	 * filtered
	 * @throws StorageException If determining the filtered entity IDs fails
	 */
	@SuppressWarnings("boxing")
	public Predicate<Entity> getFilteredIdsPredicate(
		RelationType<?> tagFilterAttr) throws StorageException {
		Predicate<Entity> tagFilter = null;
		Set<Number> filteredIds = getFilteredEntityIds();

		if (filteredIds != null) {
			if (filteredIds.size() > 0) {
				Predicate<Object> hasFilteredId = elementOf(filteredIds);

				if (isNegateFilter()) {
					hasFilteredId = not(hasFilteredId);
				}

				tagFilter = tagFilterAttr.is(hasFilteredId);
			} else {
				tagFilter = tagFilterAttr.is(equalTo(-1));
			}
		}

		return tagFilter;
	}

	/**
	 * Returns a collection of the names of the filtered tags. The tags are in
	 * the same order as displayed in the UI.
	 *
	 * @return A new collection containing the tag strings
	 */
	public Collection<String> getFilteredTags() {
		return new LinkedHashSet<>(tagInput.value());
	}

	/**
	 * Returns the listener for tag filter changes.
	 *
	 * @return The current tag filter listener or NULL for none
	 */
	public final TagFilterListener<E> getTagFilterListener() {
		return tagFilterListener;
	}

	/**
	 * Returns the owner of which the tags are filtered.
	 *
	 * @return The tag owner entity
	 */
	public final Entity getTagOwner() {
		return tagOwner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws StorageException {
		clearInteractionParameters();

		layout(layout).resid("FilterEntityTagsFragment");

		Set<String> allTags = getAllEntityTags(entityType, tagOwner);

		tagInput = inputTags(allTags)
			.resid("FilterEntityTags")
			.onUpdate(a -> tagFilterListener.filterTagsChanged(this));

		if (useHeaderLabel) {
			tagInput.set(StyleProperties.HEADER_LABEL);
		}

		optionsPanel = panel(this::initOptionsPanel);

		optionsPanel.alignHorizontal(Alignment.BEGIN).style(
			"TagFilterOptions");

		if (singleRow) {
			optionsPanel.sameRow(5);
		}

		if (label != null) {
			if (label.length() > 0) {
				tagInput.label(label);
			} else {
				tagInput.hideLabel();
			}
		}
	}

	/**
	 * Checks whether the filter negation flag is set.
	 *
	 * @return The filter negation flag state
	 */
	@SuppressWarnings("boxing")
	public boolean isNegateFilter() {
		return filterNegate.value();
	}

	/**
	 * Sets the button style to be used for the fragment buttons.
	 *
	 * @param style The new button style
	 */
	public final void setButtonStyle(ButtonStyle style) {
		buttonStyle = style;
	}

	/**
	 * Sets the entity type on which this tag filter operates.
	 *
	 * @param entityType The new entity type
	 * @throws StorageException If querying the allowed tags fails
	 */
	public void setEntityType(Class<E> entityType) throws StorageException {
		this.entityType = entityType;
		updateAllowedTags();
	}

	/**
	 * Sets the listener for tag filter changes.
	 *
	 * @param listener The new tag filter listener
	 */
	public final void setTagFilterListener(TagFilterListener<E> listener) {
		tagFilterListener = listener;
	}

	/**
	 * Controls whether the label of the tag input should be displayed above
	 * the
	 * field instead of in front.
	 *
	 * @param useHeaderLabel TRUE to use a header label
	 */
	public final void setUseHeaderLabel(boolean useHeaderLabel) {
		this.useHeaderLabel = useHeaderLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> tagsEdited(Set<String> currentTags) {
		updateAllowedTags();

		return getAllowedTags();
	}

	/**
	 * Updates the set of tags that can be selected for the entity type and tag
	 * owner.
	 */
	public void updateAllowedTags() {
		try {
			tagInput.allowElements(getAllEntityTags(entityType, tagOwner));
		} catch (StorageException e) {
			throw new RuntimeProcessException(this, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void rollback() {
		tagInput = null;
	}

	/**
	 * Handles a tag filter action.
	 *
	 * @param action The action to handle
	 */
	private void handleFilterAction(TagFilterAction action) {
		Set<String> filterTags = tagInput.value();

		switch (action) {
			case HELP:
				helpAction.run();
				break;

			case CLEAR:
				filterTags.clear();
				break;

			case REMOVE:

				// remove the last element in the tag set
				Iterator<String> iterator = filterTags.iterator();

				if (iterator.hasNext()) {
					while (iterator.hasNext()) {
						iterator.next();
					}

					iterator.remove();
				}

				break;
		}

		tagInput.modified();

		tagFilterListener.filterTagsChanged(this);
	}

	/**
	 * Initializes the panel fragment containing the tag filter options.
	 *
	 * @param panel The panel fragment
	 */
	private void initOptionsPanel(InteractionFragment panel) {
		panel.layout(LayoutType.TABLE);
		filterJoin = panel
			.dropDown(TagFilterJoin.class)
			.resid("TagFilterJoin")
			.hideLabel()
			.onUpdate(a -> tagFilterListener.filterTagsChanged(this));

		filterNegate = panel
			.checkBox("TagFilterNegate")
			.sameRow()
			.onAction(a -> tagFilterListener.filterTagsChanged(this));

		filterAction = panel
			.imageButtons(TagFilterAction.class)
			.buttonStyle(buttonStyle)
			.layout(LayoutType.TABLE)
			.sameRow()
			.columns(3)
			.resid("TagFilterAction")
			.onAction(this::handleFilterAction);

		if (helpAction == null) {
			filterAction
				.columns(2)
				.allow(TagFilterAction.REMOVE, TagFilterAction.CLEAR);
		}
	}

	/**
	 * A listener interface for changes to the list of filter tags.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public interface TagFilterListener<E extends Entity> {

		/**
		 * Will be invoked when the list of filter tags has changed.
		 *
		 * @param filter The entity tag filter instance in which the change
		 *               occurred
		 */
		void filterTagsChanged(FilterEntityTags<E> filter);
	}
}
