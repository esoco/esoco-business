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
package de.esoco.process.step;

import de.esoco.lib.manage.Disposable;

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessStep;

import java.io.Closeable;
import java.io.IOException;

import java.net.Socket;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newRelationType;

/**
 * A process step that closes and/or disposes a resource that is contained in a
 * certain parameter. The ID of the parameter that contains the resource to be
 * disposed must be set in this step's parameter {@link #DISPOSE_RESOURCE}.
 *
 * <ul>
 *   <li>{@link Closeable}</li>
 *   <li>{@link Disposable}</li>
 *   <li>{@link Socket}</li>
 * </ul>
 *
 * @author thomas
 */
public class DisposeResource extends ProcessStep {

	/**
	 * The ID of the parameter that contains the resource to be disposed.
	 */
	public static final RelationType<RelationType<?>> DISPOSE_RESOURCE =
		newRelationType("DISPOSE_RESOURCE", RelationType.class);

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(DisposeResource.class);
	}

	/**
	 * Creates a new instance.
	 */
	public DisposeResource() {
		setMandatory(DISPOSE_RESOURCE);
	}

	/**
	 * Reads the resource object from the process parameters and disposes it.
	 * The ID of the parameter to read the object from must be contained in
	 * this
	 * step's parameter {@link #DISPOSE_RESOURCE}.
	 *
	 * @throws ProcessException if the parameter ID of the resource to be
	 *                          disposed is not set or the disposing of the
	 *                          resource fails
	 */
	@Override
	protected void execute() throws ProcessException, IOException {
		RelationType<?> rParam = checkParameter(DISPOSE_RESOURCE);
		Object rResource = checkParameter(rParam);

		if (rResource instanceof Closeable) {
			((Closeable) rResource).close();
		} else if (rResource instanceof Disposable) {
			((Disposable) rResource).dispose();
		} else if (rResource instanceof Socket) {
			((Socket) rResource).close();
		} else {
			throw new ProcessException(this,
				"Invalid resource in " + rParam + ": " + rResource);
		}
	}
}
