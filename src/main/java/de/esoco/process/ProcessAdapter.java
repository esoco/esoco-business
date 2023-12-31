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
package de.esoco.process;

/**
 * An abstract implementation of {@link ProcessListener} that contains empty
 * implementations of all listener methods. Subclasses need only to implement
 * the methods for the events they are interested in. This class also provides
 * the additional method {@link #processEnded(Process)} which will be invoked by
 * any of the methods that are invoked if the process ends, whether successful
 * or not.
 *
 * @author eso
 */
public abstract class ProcessAdapter implements ProcessListener {

	/**
	 * @see ProcessListener#processCanceled(Process)
	 */
	@Override
	public void processCanceled(Process rProcess) {
		processEnded(rProcess);
	}

	/**
	 * This method will be invoked from the default implementations of the
	 * {@link #processCanceled(Process)}, {@link #processFailed(Process)}, and
	 * {@link #processFinished(Process)} methods. It is recommended that
	 * subclasses override either these methods or only this method.
	 *
	 * @param rProcess The process
	 */
	public void processEnded(Process rProcess) {
	}

	/**
	 * @see ProcessListener#processFailed(Process)
	 */
	@Override
	public void processFailed(Process rProcess) {
		processEnded(rProcess);
	}

	/**
	 * @see ProcessListener#processFinished(Process)
	 */
	@Override
	public void processFinished(Process rProcess) {
		processEnded(rProcess);
	}

	/**
	 * @see ProcessListener#processResumed(Process)
	 */
	@Override
	public void processResumed(Process rProcess) {
	}

	/**
	 * @see ProcessListener#processStarted(Process)
	 */
	@Override
	public void processStarted(Process rProcess) {
	}

	/**
	 * @see ProcessListener#processSuspended(Process)
	 */
	@Override
	public void processSuspended(Process rProcess) {
	}
}
