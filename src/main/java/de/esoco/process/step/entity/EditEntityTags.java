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
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.StyleProperties;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ValueEventHandler;
import de.esoco.process.param.CollectionParameter.SetParameter;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.StorageException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static de.esoco.entity.EntityRelationTypes.ENTITY_TAGS;

/**
 * A generic interaction fragment for the viewing and editing of entity tags
 * that are stored in the {@link EntityRelationTypes#ENTITY_TAGS} extra
 * attribute.
 *
 * @author eso
 */
public class EditEntityTags<E extends Entity> extends InteractionFragment
	implements ValueEventHandler<Set<String>> {

	private static final long serialVersionUID = 1L;

	private Class<E> entityType;

	private final Entity tagOwner;

	private final FilterEntityTags<?> filterEntityTags;

	private final String label;

	private final boolean autoStore;

	private TagEditListener editListener;

	private boolean useHeaderLabel;

	private E entity;

	private Set<String> currentEntityTags;

	private final Set<String> inputTags = new LinkedHashSet<>();

	private SetParameter<String> tagInput;

	/**
	 * Creates a new instance with a certain collection of pre-set tags the
	 * user
	 * can choose from (or input new tags). If the argument collection is NULL
	 * the fragment will be in read-only mode so that tags cannot be edited.
	 *
	 * @param entityType       The entity class to edit the tags of
	 * @param tagOwner         The owner of the tags or NULL for global tags
	 * @param filterEntityTags An optional {@link FilterEntityTags} fragment to
	 *                         be notified of tag changes or NULL for none
	 * @param label            An optional label for this fragment (empty
	 *                            string
	 *                         for none, NULL for the default)
	 * @param autoStore        TRUE to automatically store each tag change
	 */
	public EditEntityTags(Class<E> entityType, Entity tagOwner,
		FilterEntityTags<?> filterEntityTags, String label,
		boolean autoStore) {
		this.entityType = entityType;
		this.tagOwner = tagOwner;
		this.label = label;
		this.filterEntityTags = filterEntityTags;
		this.autoStore = autoStore;

		editListener = filterEntityTags;
	}

	/**
	 * Displays the tags of a certain entity or clears the tag display if the
	 * entity is NULL.
	 *
	 * @param entity The entity to display the tags of or NULL for none
	 * @throws StorageException If accessing the entity's tags fails
	 */
	public void displayEntityTags(E entity) throws StorageException {
		this.entity = entity;

		inputTags.clear();

		if (entity != null) {
			if (tagOwner != null) {
				currentEntityTags =
					entity.getExtraAttributeFor(tagOwner, ENTITY_TAGS, null,
						false);
			} else {
				currentEntityTags = entity.getExtraAttribute(ENTITY_TAGS,
					null);
			}

			if (currentEntityTags != null) {
				inputTags.addAll(currentEntityTags);
			}
		}

		if (tagInput != null) {
			tagInput.value(inputTags);
		}

		enableEdit(entity != null);
	}

	/**
	 * Returns all existing tags that can be set on the entity.
	 *
	 * @return The allowed tags
	 * @throws StorageException If querying the tags fails
	 */
	public Set<String> getAllowedTags() throws StorageException {
		Set<String> allowedTags;

		if (filterEntityTags != null) {
			allowedTags = filterEntityTags.getAllowedTags();
		} else {
			allowedTags =
				FilterEntityTags.getAllEntityTags(entityType, tagOwner);
		}

		return allowedTags;
	}

	/**
	 * Returns the entity type of which this tag editor displays the tags.
	 *
	 * @return The entity type
	 */
	public Class<E> getEntityType() {
		return entityType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleValueUpdate(Set<String> values) {
		try {
			updateEntityTags();

			if (currentEntityTags != null) {
				tagInput.allowedElements().addAll(currentEntityTags);
			}
		} catch (Exception e) {
			throw new RuntimeProcessException(this, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		clearInteractionParameters();

		layout(LayoutType.TABLE).resid("EditEntityTagsFragment");

		Set<String> allowedTags = getAllowedTags();

		tagInput = inputTags(allowedTags)
			.resid("SelectedEntityTags")
			.tooltip("$ttSelectedEntityTags")
			.value(inputTags)
			.onUpdate(this);

		if (useHeaderLabel) {
			tagInput.set(StyleProperties.HEADER_LABEL);
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
	 * Sets the tags that can be selected during editing.
	 *
	 * @param tags The selectable tags
	 */
	public void setAllowedTags(Set<String> tags) {
		tagInput.allowElements(tags);
	}

	/**
	 * Sets a listener for tag edit events. By default the edit listener
	 * will be
	 * set to the current {@link FilterEntityTags} reference if such has been
	 * set. But applications can set their own implementation if they need to
	 * filter or propagate the even, e.g. when managing multiple tag editor
	 * instances.
	 *
	 * @param editListener The edit listener or NULL for none
	 */
	public void setEditListener(TagEditListener editListener) {
		this.editListener =
			editListener != null ? editListener : filterEntityTags;
	}

	/**
	 * Sets the entity type of which this tag editor displays the tags.
	 *
	 * @param entityType The new entity type
	 * @throws StorageException If querying the allowed tags fails
	 */
	public void setEntityType(Class<E> entityType) throws StorageException {
		this.entityType = entityType;

		if (tagInput != null) {
			tagInput.allowElements(getAllowedTags());
		}
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
	 * Updates the current entity with the displayed tags.
	 *
	 * @throws StorageException     If querying the entity tags fails
	 * @throws TransactionException If storing the updated entity fails
	 */
	public void updateEntityTags()
		throws StorageException, TransactionException {
		Set<String> selectedTags = tagInput != null ?
		                           tagInput.value() :
		                           Collections.emptySet();

		if (selectedTags.size() > 0 ||
			(currentEntityTags != null && currentEntityTags.size() > 0)) {
			if (currentEntityTags == null ||
				!currentEntityTags.equals(selectedTags)) {
				Set<String> newTags = new LinkedHashSet<>(selectedTags);

				if (tagOwner != null) {
					entity.setExtraAttributeFor(tagOwner, ENTITY_TAGS, newTags,
						getProcessUser());
				} else {
					entity.setExtraAttribute(ENTITY_TAGS,
						Collections.unmodifiableSet(newTags));
				}

				currentEntityTags = newTags;

				if (autoStore) {
					EntityManager.storeEntity(entity, getProcessUser());
				}

				if (editListener != null) {
					setAllowedTags(editListener.tagsEdited(newTags));
				}
			}
		}
	}

	/**
	 * An interface for listeners to changes of the tags in an
	 * {@link EditEntityTags} fragment.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public interface TagEditListener {

		/**
		 * Will be invoked after the tags have been edited.
		 *
		 * @param editedTags The current tags of the editor the listener is
		 *                   registered on
		 * @return The set of allowed tags after the editing
		 */
		Set<String> tagsEdited(Set<String> editedTags);
	}
}
