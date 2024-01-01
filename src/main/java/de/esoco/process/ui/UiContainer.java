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
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The base class for all UI containers.
 *
 * @author eso
 */
public abstract class UiContainer<C extends UiContainer<C>>
	extends UiComponent<List<RelationType<?>>, C> {

	private final UiLayout layout;

	private boolean built;

	private final List<UiComponent<?, ?>> components = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private final UiBuilder<C> containerBuilder = new UiBuilder<>((C) this);

	/**
	 * Creates a new instance with a certain layout.
	 *
	 * @param parent The parent container or NULL for a root container
	 * @param layout The layout of this container
	 */
	protected UiContainer(UiContainer<?> parent, UiLayout layout) {
		this(parent, new UiContainerFragment(), layout);
	}

	/**
	 * Creates a new instance that wraps a specific fragment.
	 *
	 * @param parent   The parent container or NULL for a root container
	 * @param fragment The fragment to wrap
	 * @param layout   The layout of this container
	 */
	protected UiContainer(UiContainer<?> parent, InteractionFragment fragment,
		UiLayout layout) {
		super(parent, fragment, getContainerParamType(parent));

		Objects.requireNonNull(layout, "Container layout must not be NULL");

		this.layout = layout;
	}

	/**
	 * Returns the parameter type for a container if the parent is not null.
	 *
	 * @param parent The parent container
	 * @return The container parameter type or NULL if the parent is NULL
	 */
	private static RelationType<List<RelationType<?>>> getContainerParamType(
		UiContainer<?> parent) {
		return parent != null ?
		       parent
			       .fragment()
			       .getTemporaryListType(null, RelationType.class) :
		       null;
	}

	/**
	 * Returns a {@link UiBuilder} instance for this container. The builder
	 * instance is cached internally so that it doesn't need to be kept by the
	 * invoking code.
	 *
	 * @return A UI builder instance for this container
	 */
	public final UiBuilder<C> builder() {
		return containerBuilder;
	}

	/**
	 * Clears this container by removing all child components.
	 */
	public void clear() {
		List<RelationType<?>> paramTypes =
			CollectionUtil.map(components, c -> c.type());

		fragment().removeInteractionParameters(paramTypes);
		components.clear();
	}

	/**
	 * Returns the components of this container in the order in which they have
	 * been added.
	 *
	 * @return The collection of components
	 */
	public List<UiComponent<?, ?>> getComponents() {
		return new ArrayList<>(components);
	}

	/**
	 * Sets the event handler for click events on this container's visible area
	 * that is not occupied by components. The handler will receive this
	 * container instance as it's argument.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final C onClickInContainerArea(Consumer<C> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> eventHandler.accept((C) this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + getComponents();
	}

	/**
	 * Overridden to apply the container layout and to invoke this method
	 * recursively on all child components.
	 *
	 * @see UiComponent#applyProperties()
	 */
	@Override
	protected void applyProperties() {
		if (!built) {
			buildContent(containerBuilder);
			built = true;
		}

		// apply layout first so it can add styles to the container before
		// applying them
		layout.applyTo(this);

		super.applyProperties();

		for (UiComponent<?, ?> child : components) {
			child.applyProperties();
		}
	}

	/**
	 * Overridden to setup the container fragment and to attach it to the
	 * parent
	 * fragment.
	 *
	 * @see UiComponent#attachTo(UiContainer)
	 */
	@Override
	protected void attachTo(UiContainer<?> parent) {
		InteractionFragment parentFragment = parent.fragment();

		parentFragment.addInputParameters(type());
		parentFragment.addSubFragment(type(), fragment());

		parent.addComponent(this);
	}

	/**
	 * This method can be overridden by subclasses to build the content of this
	 * container. Alternatively, the content can also be built by adding
	 * components to it after creation. If both mechanisms are used in
	 * combination the call to {@link #buildContent(UiBuilder)} will occur
	 * afterwards because it is invoked just before the container is made
	 * visible (from {@link #applyProperties()}.
	 *
	 * <p>The {@link UiBuilder} argument is the same instance that is returned
	 * by {@link #builder()}.</p>
	 *
	 * <p>The default implementation of this method does nothing.</p>
	 *
	 * @param builder The builder to create the container UI with
	 */
	protected void buildContent(UiBuilder<?> builder) {
	}

	/**
	 * Will be invoked if a new component has been added to this container. Can
	 * be overridden by subclasses to handle component additions. The complete
	 * list of child components (including the new one at the end) can be
	 * queried with {@link #getComponents()}.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @param component The component that has been added
	 */
	protected void componentAdded(UiComponent<?, ?> component) {
	}

	/**
	 * Will be invoked after the list of components has been modified. The base
	 * implementation resets the layout for recalculation.
	 */
	protected void componentListChanged() {
		if (built) {
			// if components are added after the initial building the layout
			// needs to be reprocessed
			layout.reset(layout.getRows().size(), layout.getColumns().size());
		}
	}

	/**
	 * Returns the layout of this container.
	 *
	 * @return The layout
	 */
	protected UiLayout getLayout() {
		return layout;
	}

	/**
	 * Checks if the content of this container has already been built. Can be
	 * used by subclasses to check the initialization status of a container.
	 *
	 * @return TRUE if the container content has been built
	 */
	protected final boolean isBuilt() {
		return built;
	}

	/**
	 * Removes a component from this container.
	 *
	 * @param component The component to remove
	 */
	protected void remove(UiComponent<?, ?> component) {
		fragment().removeInteractionParameters(component.type());
		components.remove(component);
	}

	/**
	 * Internal method to add a component to this container.
	 *
	 * @param component The component to add
	 */
	void addComponent(UiComponent<?, ?> component) {
		components.add(component);
		layout.addComponent(component);
		componentAdded(component);

		componentListChanged();
	}

	/**
	 * Internal method to provide access to the component list.
	 *
	 * @return The list of this container's components
	 */
	List<UiComponent<?, ?>> getComponentList() {
		return components;
	}

	/**
	 * Internal method to place a component before another component. Publicly
	 * available through {@link UiComponent#placeBefore(UiComponent)}.
	 *
	 * @param beforeComponent The component to insert before
	 * @param component       The component to place before the other
	 * @throws IllegalArgumentException If the given component is not found in
	 *                                  the parent container
	 */
	void placeComponentBefore(UiComponent<?, ?> beforeComponent,
		UiComponent<?, ?> component) {
		int index = components.indexOf(beforeComponent);

		if (index < 0) {
			throw new IllegalArgumentException(
				"Component to place before must be in the same container");
		}

		components.remove(component);
		components.add(index, component);

		List<RelationType<?>> params = fragment().getInteractionParameters();

		RelationType<?> componentParam = component.type();

		params.remove(componentParam);
		params.add(params.indexOf(beforeComponent.type()), componentParam);

		componentListChanged();
	}

	/**
	 * An interaction fragment subclass that wraps containers.
	 *
	 * @author eso
	 */
	protected static class UiContainerFragment extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
		}
	}
}
