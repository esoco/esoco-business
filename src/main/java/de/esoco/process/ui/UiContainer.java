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
package de.esoco.process.ui;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.container.UiBuilder;

import java.util.ArrayList;
import java.util.List;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for all UI containers.
 *
 * @author eso
 */
public abstract class UiContainer<C extends UiContainer<C>>
	extends UiComponent<List<RelationType<?>>, C>
{
	//~ Instance fields --------------------------------------------------------

	private UiLayout rLayout;

	private boolean bBuilt;

	private List<UiComponent<?, ?>> aComponents = new ArrayList<>();

	private UiBuilder<C> aContainerBuilder;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container or NULL for a root container
	 * @param rLayout The layout of the container
	 */
	public UiContainer(UiContainer<?> rParent, UiLayout rLayout)
	{
		// NULL for datatype to prevent component attachTo() as a special list
		// parameter type needs to be created in the container implementation
		super(rParent, null);

		this.rLayout = rLayout;

		if (rParent != null)
		{
			attachTo(rParent);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the components of this container in the order in which they have
	 * been added.
	 *
	 * @return The collection of components
	 */
	public List<UiComponent<?, ?>> getComponents()
	{
		return new ArrayList<>(aComponents);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return super.toString() + getComponents();
	}

	/***************************************
	 * Overridden to apply the container layout and to invoke this method
	 * recursively on all child components.
	 *
	 * @see UiComponent#applyProperties()
	 */
	@Override
	protected void applyProperties()
	{
		if (!bBuilt)
		{
			build();
			bBuilt			  = true;
			aContainerBuilder = null;
		}

		// apply layout first so it can add styles to the container before
		// applying them
		rLayout.applyTo(this);

		super.applyProperties();

		for (UiComponent<?, ?> rChild : aComponents)
		{
			rChild.applyProperties();
		}
	}

	/***************************************
	 * Overridden to setup the container fragment and to attach it to the parent
	 * fragment.
	 *
	 * @see UiComponent#attachTo(UiContainer)
	 */
	@Override
	protected void attachTo(UiContainer<?> rParent)
	{
		setupContainerFragment(rParent);
		attachToParentFragment(rParent.fragment());

		rParent.addComponent(this);
	}

	/***************************************
	 * Attaches this container to it's parent fragment.
	 *
	 * @param rParentFragment The parent fragment
	 */
	protected void attachToParentFragment(InteractionFragment rParentFragment)
	{
		rParentFragment.addInputParameters(type());
		rParentFragment.addSubFragment(type(), fragment());
	}

	/***************************************
	 * Can be overridden by subclasses to build the contents of this container.
	 * Alternatively the contents can also be built by adding components to it
	 * after creation. This may also be used in combination. In that case this
	 * {@link #build()} method has already been invoked because that happens
	 * upon initialization.
	 *
	 * <p>The default implementation of this method does nothing.</p>
	 */
	protected void build()
	{
	}

	/***************************************
	 * Returns a {@link UiBuilder} instance for this container. The builder
	 * instance is cached internally so that it doesn't need to be kept by the
	 * invoking code.
	 *
	 * @return A UI builder instance for this container
	 */
	@SuppressWarnings("unchecked")
	protected UiBuilder<C> builder()
	{
		if (aContainerBuilder == null)
		{
			aContainerBuilder = new UiBuilder<>((C) this);
		}

		return aContainerBuilder;
	}

	/***************************************
	 * Will be invoked if a new component has been added to this container. Can
	 * be overridden by subclasses to handle component additions. The complete
	 * list of child components (including the new one at the end) can be
	 * queried with {@link #getComponents()}.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @param rComponent The component that has been added
	 */
	protected void componentAdded(UiComponent<?, ?> rComponent)
	{
	}

	/***************************************
	 * Returns the layout of this container.
	 *
	 * @return The layout
	 */
	protected UiLayout getLayout()
	{
		return rLayout;
	}

	/***************************************
	 * Removes a component from this container.
	 *
	 * @param rComponent The component to remove
	 */
	protected void removeComponent(UiComponent<?, ?> rComponent)
	{
		if (aComponents.remove(rComponent))
		{
			fragment().removeInteractionParameters(rComponent.type());
		}
	}

	/***************************************
	 * Creates the interaction fragment this container is rendered in and the
	 * corresponding list parameter type.
	 *
	 * @param rParent The parent container
	 */
	protected void setupContainerFragment(UiContainer<?> rParent)
	{
		setFragment(new UiContainerFragment());
		setParameterType(rParent.fragment()
						 .getTemporaryListType(null, RelationType.class));
	}

	/***************************************
	 * Internal method to add a component to this container.
	 *
	 * @param rComponent The component to add
	 */
	void addComponent(UiComponent<?, ?> rComponent)
	{
		aComponents.add(rComponent);
		rLayout.layoutComponent(rComponent);

		componentAdded(rComponent);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An interaction fragment subclass that wraps containers.
	 *
	 * @author eso
	 */
	protected static class UiContainerFragment extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
		}
	}
}
