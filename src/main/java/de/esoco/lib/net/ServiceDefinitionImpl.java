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
package de.esoco.lib.net;

import java.util.Collection;

import org.obrel.core.RelatedObject;

/**
 * An implementation of the {@link ExternalServiceDefinition} interface.
 *
 * @author eso
 */
public class ServiceDefinitionImpl extends RelatedObject
	implements ExternalServiceDefinition {

	private final Class<? extends ExternalService> rServiceClass;

	/**
	 * Creates a new instance.
	 *
	 * @param rServiceClass      The implementation class of the service type
	 * @param sAccessScopePrefix A prefix for the full access scope names
	 * @param rAccessScopes      The access scopes the service supports (can be
	 *                           empty)
	 */
	public ServiceDefinitionImpl(Class<? extends ExternalService> rServiceClass,
		String sAccessScopePrefix, Collection<String> rAccessScopes) {
		this.rServiceClass = rServiceClass;

		set(ExternalServices.ACCESS_SCOPE_PREFIX, sAccessScopePrefix);
		get(ExternalServices.ACCESS_SCOPES).addAll(rAccessScopes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends ExternalService> getServiceClass() {
		return rServiceClass;
	}
}
