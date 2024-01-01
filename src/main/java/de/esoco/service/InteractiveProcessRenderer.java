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
package de.esoco.service;

import de.esoco.lib.app.RestService;
import de.esoco.lib.security.AuthenticationService;
import org.obrel.core.RelationType;
import org.obrel.space.ObjectSpace;

import java.util.Map;

import static org.obrel.core.RelationTypes.newType;

/**
 * A renderer for interactive processes that are executed by a remote service
 * (typically a {@link InteractiveProcessExecutor}).
 *
 * @author eso
 */
public abstract class InteractiveProcessRenderer extends RestService
	implements AuthenticationService {

	private static final RelationType<Map<String, String>> REGISTER_PROCESSES =
		newType();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ObjectSpace<Object> buildRestServerSpace() {
		ObjectSpace<Object> rootSpace = super.buildRestServerSpace();
		ObjectSpace<String> apiSpace = rootSpace.get(API);

		apiSpace.init(REGISTER_PROCESSES).onUpdate(this::registerProcesses);

		return rootSpace;
	}

	/**
	 * Registers interactive processes that can be executed.
	 *
	 * @param request The registration request
	 */
	private void registerProcesses(Map<String, String> request) {
	}
}
