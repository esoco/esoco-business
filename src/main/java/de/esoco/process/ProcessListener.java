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

/********************************************************************
 * An event listener interface to be notified of changes in the status of a
 * {@link Process} instance.
 *
 * @author eso
 */
public interface ProcessListener
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Will be notified when the process execution has been canceled.
	 *
	 * @param rProcess The process
	 */
	public void processCanceled(Process rProcess);

	/***************************************
	 * Will be notified when the process execution failed. If the failure was
	 * caused by an exception it can be queried from the process parameter
	 * {@link ProcessRelationTypes#PROCESS_EXCEPTION}. This method will also be
	 * invoked if another listener method caused the process failure by throwing
	 * a runtime exception.
	 *
	 * @param rProcess The process
	 */
	public void processFailed(Process rProcess);

	/***************************************
	 * Will be notified when the process execution has finished. If the method
	 * wants to prevent the process from committing it's transaction it should
	 * throw a runtime exception.
	 *
	 * @param  rProcess The process
	 *
	 * @throws Exception An exception to prevent the process from finishing
	 */
	public void processFinished(Process rProcess);

	/***************************************
	 * Will be notified when the process execution is resumed after a previous
	 * suspension.
	 *
	 * @param rProcess The process
	 */
	public void processResumed(Process rProcess);

	/***************************************
	 * Will be notified when the process execution has started.
	 *
	 * @param rProcess The process
	 */
	public void processStarted(Process rProcess);

	/***************************************
	 * Will be notified when the process execution has been suspended
	 * temporarily.
	 *
	 * @param rProcess The process
	 */
	public void processSuspended(Process rProcess);
}
