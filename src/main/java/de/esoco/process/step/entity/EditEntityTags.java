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
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.StyleProperties;

import de.esoco.process.CollectionParameter.SetParameter;
import de.esoco.process.ParameterEventHandler;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.StorageException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static de.esoco.entity.EntityRelationTypes.ENTITY_TAGS;


/********************************************************************
 * A generic interaction fragment for the viewing and editing of entity tags
 * that are stored in the {@link EntityRelationTypes#ENTITY_TAGS} extra
 * attribute.
 *
 * @author eso
 */
public class EditEntityTags<E extends Entity> extends InteractionFragment
	implements ParameterEventHandler<Set<String>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Class<E>		    rEntityType;
	private Entity			    rTagOwner;
	private FilterEntityTags<?> rFilterEntityTags;
	private String			    sLabel;
	private boolean			    bAutoStore;

	private TagEditListener rEditListener;
	private boolean		    bUseHeaderLabel;

	private E		    rEntity;
	private Set<String> rCurrentEntityTags;
	private Set<String> aInputTags = new LinkedHashSet<>();

	private SetParameter<String> aTagInput;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain collection of pre-set tags the user
	 * can choose from (or input new tags). If the argument collection is NULL
	 * the fragment will be in read-only mode so that tags cannot be edited.
	 *
	 * @param rEntityType       The entity class to edit the tags of
	 * @param rTagOwner         The owner of the tags or NULL for global tags
	 * @param rFilterEntityTags An optional {@link FilterEntityTags} fragment to
	 *                          be notified of tag changes or NULL for none
	 * @param sLabel            An optional label for this fragment (empty
	 *                          string for none, NULL for the default)
	 * @param bAutoStore        TRUE to automatically store each tag change
	 */
	public EditEntityTags(Class<E>			  rEntityType,
						  Entity			  rTagOwner,
						  FilterEntityTags<?> rFilterEntityTags,
						  String			  sLabel,
						  boolean			  bAutoStore)
	{
		this.rEntityType	   = rEntityType;
		this.rTagOwner		   = rTagOwner;
		this.sLabel			   = sLabel;
		this.rFilterEntityTags = rFilterEntityTags;
		this.bAutoStore		   = bAutoStore;

		rEditListener = rFilterEntityTags;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Displays the tags of a certain entity or clears the tag display if the
	 * entity is NULL.
	 *
	 * @param  rEntity The entity to display the tags of or NULL for none
	 *
	 * @throws StorageException If accessing the entity's tags fails
	 */
	public void displayEntityTags(E rEntity) throws StorageException
	{
		this.rEntity = rEntity;

		aInputTags.clear();

		if (rEntity != null)
		{
			if (rTagOwner != null)
			{
				rCurrentEntityTags =
					rEntity.getExtraAttributeFor(rTagOwner,
												 ENTITY_TAGS,
												 null,
												 false);
			}
			else
			{
				rCurrentEntityTags =
					rEntity.getExtraAttribute(ENTITY_TAGS, null);
			}

			if (rCurrentEntityTags != null)
			{
				aInputTags.addAll(rCurrentEntityTags);
			}
		}

		if (aTagInput != null)
		{
			aTagInput.value(aInputTags);
		}

		enableEdit(rEntity != null);
	}

	/***************************************
	 * Returns all existing tags that can be set on the entity.
	 *
	 * @return The allowed tags
	 *
	 * @throws StorageException If querying the tags fails
	 */
	public Set<String> getAllowedTags() throws StorageException
	{
		Set<String> aAllowedTags;

		if (rFilterEntityTags != null)
		{
			aAllowedTags = rFilterEntityTags.getAllowedTags();
		}
		else
		{
			aAllowedTags =
				FilterEntityTags.getAllEntityTags(rEntityType, rTagOwner);
		}

		return aAllowedTags;
	}

	/***************************************
	 * Returns the entity type of which this tag editor displays the tags.
	 *
	 * @return The entity type
	 */
	public Class<E> getEntityType()
	{
		return rEntityType;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleParameterUpdate(Set<String> aValues)
	{
		try
		{
			updateEntityTags();
			aTagInput.allowedElements().addAll(rCurrentEntityTags);
		}
		catch (Exception e)
		{
			throw new RuntimeProcessException(this, e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		clearInteractionParameters();

		layout(Layout.TABLE).resid("EditEntityTagsFragment");

		Set<String> aAllowedTags = getAllowedTags();

		aTagInput =
			inputTags(aAllowedTags).resid("SelectedEntityTags")
								   .tooltip("$ttSelectedEntityTags")
								   .value(aInputTags)
								   .onUpdate(this);

		if (bUseHeaderLabel)
		{
			aTagInput.set(StyleProperties.HEADER_LABEL);
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
	 * Sets the tags that can be selected during editing.
	 *
	 * @param rTags The selectable tags
	 */
	public void setAllowedTags(Set<String> rTags)
	{
		aTagInput.allowElements(rTags);
	}

	/***************************************
	 * Sets a listener for tag edit events. By default the edit listener will be
	 * set to the current {@link FilterEntityTags} reference if such has been
	 * set. But applications can set their own implementation if they need to
	 * filter or propagate the even, e.g. when managing multiple tag editor
	 * instances.
	 *
	 * @param rEditListener The edit listener or NULL for none
	 */
	public void setEditListener(TagEditListener rEditListener)
	{
		this.rEditListener =
			rEditListener != null ? rEditListener : rFilterEntityTags;
	}

	/***************************************
	 * Sets the entity type of which this tag editor displays the tags.
	 *
	 * @param  rEntityType The new entity type
	 *
	 * @throws StorageException If querying the allowed tags fails
	 */
	public void setEntityType(Class<E> rEntityType) throws StorageException
	{
		this.rEntityType = rEntityType;

		if (aTagInput != null)
		{
			aTagInput.allowElements(getAllowedTags());
		}
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
	 * Updates the current entity with the displayed tags.
	 *
	 * @throws StorageException     If querying the entity tags fails
	 * @throws TransactionException If storing the updated entity fails
	 */
	public void updateEntityTags() throws StorageException, TransactionException
	{
		Set<String> rSelectedTags =
			aTagInput != null ? aTagInput.value()
							  : Collections.<String>emptySet();

		if (rSelectedTags.size() > 0 ||
			(rCurrentEntityTags != null && rCurrentEntityTags.size() > 0))
		{
			if (rCurrentEntityTags == null ||
				!rCurrentEntityTags.equals(rSelectedTags))
			{
				Set<String> rNewTags = new LinkedHashSet<>(rSelectedTags);

				if (rTagOwner != null)
				{
					rEntity.setExtraAttributeFor(rTagOwner,
												 ENTITY_TAGS,
												 rNewTags,
												 getProcessUser());
				}
				else
				{
					rEntity.setExtraAttribute(ENTITY_TAGS,
											  Collections.unmodifiableSet(rNewTags));
				}

				rCurrentEntityTags = rNewTags;

				if (bAutoStore)
				{
					EntityManager.storeEntity(rEntity, getProcessUser());
				}

				if (rEditListener != null)
				{
					setAllowedTags(rEditListener.tagsEdited(rNewTags));
				}
			}
		}
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface for listeners to changes of the tags in an {@link
	 * EditEntityTags} fragment.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public static interface TagEditListener
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked after the tags have been edited.
		 *
		 * @param  aEditedTags The current tags of the editor the listener is
		 *                     registered on
		 *
		 * @return The set of allowed tags after the editing
		 */
		public Set<String> tagsEdited(Set<String> aEditedTags);
	}
}
