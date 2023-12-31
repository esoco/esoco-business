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
package de.esoco.process.step;

import de.esoco.process.ProcessStep;

/**
 * A base class for process steps that can perform a (simple) rollback of their
 * execution. The {@link #canRollback()} method is implemented to always return
 * TRUE and {@link #rollback()} to do nothing. Subclasses that perform parameter
 * modifications in their {@link #execute()} method should always override the
 * latter to revert any changes made during execution.
 *
 * @author eso
 */
public abstract class RollbackStep extends ProcessStep {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public RollbackStep() {
	}

	/**
	 * Always returns TRUE.
	 *
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Implemented to do nothing. Subclasses that perform parameter
	 * modifications must override this method to revert such changes.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception {
	}
}
