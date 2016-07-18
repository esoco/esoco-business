//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.process.ParameterEventHandler;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.CollectionParameter.SetParameter;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.StorageException;

import java.util.Collections;
import java.util.HashSet;
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
		layout(Layout.TABLE);
		fragmentParam().resid("EditEntityTagsFragment");

		Set<String> aTags =
			FilterEntityTags.getAllEntityTags(rEntityType, rTagOwner);

		if (aTagInput == null)
		{
			aTagInput =
				inputTags(aTags).onUpdate(this).resid("SelectedEntityTags")
								.value(aInputTags);
		}

		aTagInput.allowElements(new LinkedHashSet<String>());

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
				HashSet<String> rNewTags = new LinkedHashSet<>(rSelectedTags);

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

				if (rFilterEntityTags != null)
				{
					rFilterEntityTags.updateTagList();
				}
			}
		}
	}
}
