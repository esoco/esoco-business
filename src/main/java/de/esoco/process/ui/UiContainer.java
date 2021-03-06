//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.step.InteractionFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
	private boolean  bBuilt;

	private List<UiComponent<?, ?>> aComponents = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private UiBuilder<C> aContainerBuilder = new UiBuilder<>((C) this);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain layout.
	 *
	 * @param rParent The parent container or NULL for a root container
	 * @param rLayout The layout of this container
	 */
	protected UiContainer(UiContainer<?> rParent, UiLayout rLayout)
	{
		this(rParent, new UiContainerFragment(), rLayout);
	}

	/***************************************
	 * Creates a new instance that wraps a specific fragment.
	 *
	 * @param rParent   The parent container or NULL for a root container
	 * @param rFragment The fragment to wrap
	 * @param rLayout   The layout of this container
	 */
	protected UiContainer(UiContainer<?>	  rParent,
						  InteractionFragment rFragment,
						  UiLayout			  rLayout)
	{
		super(rParent, rFragment, getContainerParamType(rParent));

		Objects.requireNonNull(rLayout, "Container layout must not be NULL");

		this.rLayout = rLayout;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the parameter type for a container if the parent is not null.
	 *
	 * @param  rParent The parent container
	 *
	 * @return The container parameter type or NULL if the parent is NULL
	 */
	private static RelationType<List<RelationType<?>>> getContainerParamType(
		UiContainer<?> rParent)
	{
		return rParent != null
			   ? rParent.fragment()
						.getTemporaryListType(null, RelationType.class) : null;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a {@link UiBuilder} instance for this container. The builder
	 * instance is cached internally so that it doesn't need to be kept by the
	 * invoking code.
	 *
	 * @return A UI builder instance for this container
	 */
	public final UiBuilder<C> builder()
	{
		return aContainerBuilder;
	}

	/***************************************
	 * Clears this container by removing all child components.
	 */
	public void clear()
	{
		List<RelationType<?>> rParamTypes =
			CollectionUtil.map(aComponents, c -> c.type());

		fragment().removeInteractionParameters(rParamTypes);
		aComponents.clear();
	}

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
	 * Sets the event handler for click events on this container's visible area
	 * that is not occupied by components. The handler will receive this
	 * container instance as it's argument.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final C onClickInContainerArea(Consumer<C> rEventHandler)
	{
		return setParameterEventHandler(
			InteractionEventType.ACTION,
			v -> rEventHandler.accept((C) this));
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
			buildContent(aContainerBuilder);
			bBuilt = true;
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
		InteractionFragment rParentFragment = rParent.fragment();

		rParentFragment.addInputParameters(type());
		rParentFragment.addSubFragment(type(), fragment());

		rParent.addComponent(this);
	}

	/***************************************
	 * This method can be overridden by subclasses to build the content of this
	 * container. Alternatively, the content can also be built by adding
	 * components to it after creation. If both mechanisms are used in
	 * combination the call to {@link #buildContent(UiBuilder)} will occur afterwards
	 * because it is invoked just before the container is made visible (from
	 * {@link #applyProperties()}.
	 *
	 * <p>The {@link UiBuilder} argument is the same instance that is returned
	 * by {@link #builder()}.</p>
	 *
	 * <p>The default implementation of this method does nothing.</p>
	 *
	 * @param rBuilder The builder to create the container UI with
	 */
	protected void buildContent(UiBuilder<?> rBuilder)
	{
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
	 * Will be invoked after the list of components has been modified. The base
	 * implementation resets the layout for recalculation.
	 */
	protected void componentListChanged()
	{
		if (bBuilt)
		{
			// if components are added after the initial building the layout
			// needs to be reprocessed
			rLayout.reset(
				rLayout.getRows().size(),
				rLayout.getColumns().size());
		}
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
	 * Checks if the content of this container has already been built. Can be
	 * used by subclasses to check the initialization status of a container.
	 *
	 * @return TRUE if the container content has been built
	 */
	protected final boolean isBuilt()
	{
		return bBuilt;
	}

	/***************************************
	 * Removes a component from this container.
	 *
	 * @param rComponent The component to remove
	 */
	protected void remove(UiComponent<?, ?> rComponent)
	{
		fragment().removeInteractionParameters(rComponent.type());
		aComponents.remove(rComponent);
	}

	/***************************************
	 * Internal method to add a component to this container.
	 *
	 * @param rComponent The component to add
	 */
	void addComponent(UiComponent<?, ?> rComponent)
	{
		aComponents.add(rComponent);
		rLayout.addComponent(rComponent);
		componentAdded(rComponent);

		componentListChanged();
	}

	/***************************************
	 * Internal method to provide access to the component list.
	 *
	 * @return The list of this container's components
	 */
	List<UiComponent<?, ?>> getComponentList()
	{
		return aComponents;
	}

	/***************************************
	 * Internal method to place a component before another component. Publicly
	 * available through {@link UiComponent#placeBefore(UiComponent)}.
	 *
	 * @param  rBeforeComponent The component to insert before
	 * @param  rComponent       The component to place before the other
	 *
	 * @throws IllegalArgumentException If the given component is not found in
	 *                                  the parent container
	 */
	void placeComponentBefore(
		UiComponent<?, ?> rBeforeComponent,
		UiComponent<?, ?> rComponent)
	{
		int nIndex = aComponents.indexOf(rBeforeComponent);

		if (nIndex < 0)
		{
			throw new IllegalArgumentException(
				"Component to place before must be in the same container");
		}

		aComponents.remove(rComponent);
		aComponents.add(nIndex, rComponent);

		List<RelationType<?>> rParams = fragment().getInteractionParameters();

		RelationType<?> rComponentParam = rComponent.type();

		rParams.remove(rComponentParam);
		rParams.add(rParams.indexOf(rBeforeComponent.type()), rComponentParam);

		componentListChanged();
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
