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

import de.esoco.entity.Entity;


/********************************************************************
 * An interface for classes that perform the periodic scheduling of processes.
 *
 * @author eso
 */
public interface ProcessScheduler
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds the schedule process defined by the Entity to the management of the
	 * scheduler.
	 *
	 * @param rProcessDescription The Entity that defines the schedule process.
	 */
	public void addScheduleProcess(Entity rProcessDescription);

	/***************************************
	 * Must be implemented by subclasses to provide access to the entity to be
	 * used as the schedule process user.
	 *
	 * @return An entity instance that will be used as the user of schedule
	 *         processes
	 */
	public Entity getScheduleProcessUser();

	/***************************************
	 * Returns TRUE if the process scheduler is enabled and FALSE otherwise.
	 *
	 * @return TRUE if the process scheduler is enabled and FALSE otherwise.
	 */
	public boolean isEnabled();

	/***************************************
	 * Notify about a change or edit of a schedule process.
	 */
	public void notifyScheduleProcessChanged();

	/***************************************
	 * Notify about a schedule process that has finished execution.
	 *
	 * @param rProcess The schedule process that has finished
	 */
	public void notifyScheduleProcessFinished(Process rProcess);

	/***************************************
	 * Notify about a schedule process that is about to start execution.
	 *
	 * @param rProcess The schedule process that is starting
	 */
	public void notifyScheduleProcessStarting(Process rProcess);

	/***************************************
	 * Removes the process from the management of the scheduler.
	 *
	 * @param sEntityId ProcessDescription the Entity that defines the schedule
	 *                  process
	 */
	public void removeScheduleProcess(String sEntityId);

	/***************************************
	 * Resumes the execution of a process if it is currently suspended. This is
	 * useful if a schedule process should be run immediately although it is in
	 * an idle state waiting the period time for the next execution.
	 *
	 * @param rProcessDefinitionClass The process description of the schedule
	 *                                process
	 */
	public void resumeProcess(
		Class<? extends ProcessDefinition> rProcessDefinitionClass);

	/***************************************
	 * Resumes the process defined by the Entity-Id.
	 *
	 * @param sEntityId The Id of the Entity that defines the process.
	 */
	public void resumeScheduleProcess(String sEntityId);

	/***************************************
	 * Runs the process defined by the Entity immediately.
	 *
	 * @param sEntityId rProcessDescription The Entity that defines the schedule
	 *                  process.
	 */
	public void runScheduleProcessNow(String sEntityId);

	/***************************************
	 * Stops the schedule process and suspends the execution of this process
	 * until resumeScheduleProcess is called.
	 *
	 * @param sEntityId The Id of the Entity that defines the process.
	 */
	public void suspendScheduleProcess(String sEntityId);
}
