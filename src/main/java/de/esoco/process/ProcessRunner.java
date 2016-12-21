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
package de.esoco.process;

import de.esoco.entity.ConcurrentEntityModificationException;
import de.esoco.entity.Entity;

import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.manage.RunCheck;
import de.esoco.lib.manage.Stoppable;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obrel.core.RelatedObject;

import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;


/********************************************************************
 * An class that executes a certain {@link Process}. It implements the {@link
 * Runnable} interface so that it a process can be executed in a separate
 * thread.
 *
 * @author eso
 */
public class ProcessRunner extends RelatedObject implements Runnable, RunCheck,
															Stoppable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long ENTITY_MODIFICATION_SLEEP_TIME	  = 1000L;
	private static final int  MAX_ENTITY_MODIFICATION_SLEEP_TRIES = 30;

	//~ Instance fields --------------------------------------------------------

	private final Lock	    aLock  = new ReentrantLock();
	private final Condition aPause = aLock.newCondition();

	private ProcessScheduler rProcessScheduler;

	private LogLevel eLogLevel		  = LogLevel.ERROR;
	private boolean  bLogOnError	  = true;
	private boolean  bContinueOnError = false;
	private boolean  bRun			  = true;
	private boolean  bSingleRun		  = false;
	private boolean  bRunning		  = false;

	private ProcessDefinition rProcessDefinition;
	private Process			  aProcess = null;

	private Date aNextScheduleTime = new Date();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain process definition.
	 *
	 * @param rProcessDefinition The process definition
	 */
	public ProcessRunner(ProcessDefinition rProcessDefinition)
	{
		setProcessDefinition(rProcessDefinition);
	}

	/***************************************
	 * Creates a new instance.
	 */
	protected ProcessRunner()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Run the process now even if it is currently paused or not scheduled to
	 * run in the future.
	 */
	public void executeOnce()
	{
		bRun = false;
		executeProcessNow();
	}

	/***************************************
	 * Run the process now even if it is currently paused or not scheduled to
	 * run in the future.
	 */
	public void executeProcessNow()
	{
		bSingleRun = true;

		if (bRunning)
		{
			resumeFromPause();
		}
		else
		{
			run();
		}
	}

	/***************************************
	 * Returns the current process of this instance. Will be NULL if is has not
	 * yet been initialized.
	 *
	 * @return The current process (NULL for none)
	 */
	public final Process getProcess()
	{
		return aProcess;
	}

	/***************************************
	 * Checks whether this {@link ProcessRunner} runs an instance of the {@link
	 * ProcessDefinition} given as the parameter.
	 *
	 * @return TRUE it the given parameter is the class this {@link
	 *         ProcessRunner} is running an instance of, FALSE otherwise.
	 */
	public ProcessDefinition getProcessDefinition()
	{
		return rProcessDefinition;
	}

	/***************************************
	 * TRUE if the ProcessRunner is running.
	 *
	 * @return TRUE if running.
	 */
	@Override
	public boolean isRunning()
	{
		return bRunning;
	}

	/***************************************
	 * Resumes the execution of this runner if it is currently waiting.
	 */
	public void resume()
	{
		bRun = true;
		resumeFromPause();
	}

	/***************************************
	 * Resumes the execution of this runner if it is currently pausing.
	 */

	public void resumeFromPause()
	{
		if (aLock.tryLock())
		{
			try
			{
				aPause.signalAll();
			}
			finally
			{
				aLock.unlock();
			}
		}
	}

	/***************************************
	 * Executes the process this runner is associated with.
	 */
	@Override
	public void run()
	{
		bRunning = true;

		try
		{
			while (bRun || bSingleRun)
			{
				aLock.lock();

				try
				{
					long nScheduleTime = getNextScheduleTime().getTime();
					long nSleepTime    = 0;

					if (!bSingleRun)
					{
						nSleepTime = nScheduleTime - System.currentTimeMillis();
					}

					if (nSleepTime > 0)
					{
						aPause.await(nSleepTime, TimeUnit.MILLISECONDS);
					}

					if (bSingleRun ||
						(bRun && System.currentTimeMillis() >= nScheduleTime))
					{
						bSingleRun = false;
						executeProcess();
					}
				}
				catch (InterruptedException e)
				{
					// if sleep was interrupted just continue with the next loop
				}
				finally
				{
					aLock.unlock();
				}
			}
		}
		catch (InterruptedException e)
		{
			// just terminate if process thread is interrupted; this can happen
			// on shutdown
		}
		catch (Exception e)
		{
			handleProcessExecutionException(e);
		}
		finally
		{
			bRunning = false;
		}
	}

	/***************************************
	 * Sets an optional process scheduler instance that provides the scheduling
	 * context of this instance.
	 *
	 * @param rProcessScheduler the process scheduler
	 */
	public void setProcessScheduler(ProcessScheduler rProcessScheduler)
	{
		this.rProcessScheduler = rProcessScheduler;
	}

	/***************************************
	 * Stops this runner and the associated process.
	 */

	@Override
	public void stop()
	{
		bRun = false;

		if (aProcess != null)
		{
			aProcess.setParameter(ProcessRelationTypes.STOP_PROCESS_EXECUTION,
								  Boolean.TRUE);
		}

		resumeFromPause();
	}

	/***************************************
	 * Internal method that is called right after the process execution has
	 * finished The default implementation does nothing.
	 *
	 * @param  rProcess the schedule process that was executed.
	 *
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void afterExecution(Process rProcess) throws Exception
	{
	}

	/***************************************
	 * Internal method that is called right before the process execution starts.
	 * The default implementation does nothing.
	 *
	 * @param  rProcess the schedule process that will be executed.
	 *
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void beforeExecution(Process rProcess) throws Exception
	{
	}

	/***************************************
	 * Returns the date and time of the next schedule at which the process needs
	 * to be executed. Subclasses may override this method to perform a schedule
	 * calculation or read the the schedule from a database record.
	 *
	 * <p>The default implementation sets the schedule time to the time at the
	 * creation of this instance so that the process would executed only once.
	 * </p>
	 *
	 * @return The schedule timestamp
	 *
	 * @throws Exception If determining the schedule date fails
	 */
	protected Date getNextScheduleTime() throws Exception
	{
		return aNextScheduleTime;
	}

	/***************************************
	 * Returns the entity that describes the user that is responsible for the
	 * process execution. This will be set into the process as the parameter #
	 *
	 * @return The user used for the executed process (can be NULL)
	 */
	protected Entity getProcessUser()
	{
		Entity rUser = null;

		if (rProcessScheduler != null)
		{
			rUser = rProcessScheduler.getScheduleProcessUser();
		}

		return rUser;
	}

	/***************************************
	 * This is called when the execution of a process fails and throws an
	 * Exception. Implementing subclasses should take appropriate action.
	 *
	 * @param e rE The Exception that occurred.
	 */
	protected void handleProcessExecutionException(Exception e)
	{
		Log.error("Process execution failed", e);
	}

	/***************************************
	 * Sets the continueOnError.
	 *
	 * @param bContinueOnError The continueOnError value
	 */
	protected void setContinueOnError(boolean bContinueOnError)
	{
		this.bContinueOnError = bContinueOnError;
	}

	/***************************************
	 * Sets the logLevel.
	 *
	 * @param eLogLevel The logLevel value
	 */
	protected void setLogLevel(LogLevel eLogLevel)
	{
		this.eLogLevel = eLogLevel;
	}

	/***************************************
	 * Sets the logOnError.
	 *
	 * @param bLogOnError The logOnError value
	 */
	protected void setLogOnError(boolean bLogOnError)
	{
		this.bLogOnError = bLogOnError;
	}

	/***************************************
	 * Sets the process definition to be used for the instantiation of the
	 * executed processes.
	 *
	 * @param rProcessDefinition The process definition
	 */
	protected void setProcessDefinition(ProcessDefinition rProcessDefinition)
	{
		this.rProcessDefinition = rProcessDefinition;
	}

	/***************************************
	 * Invokes the execution support methods {@link #beforeExecution(Process)}
	 * and {@link #afterExecution(Process)} and handles framework exceptions
	 * like {@link ConcurrentEntityModificationException}.
	 *
	 * @param  bBefore
	 *
	 * @throws Exception
	 */
	private void beforeAfterExecution(boolean bBefore) throws Exception
	{
		int nTries = 0;

		while (nTries++ < MAX_ENTITY_MODIFICATION_SLEEP_TRIES)
		{
			try
			{
				if (bBefore)
				{
					beforeExecution(aProcess);
				}
				else
				{
					afterExecution(aProcess);
				}

				break;
			}
			catch (ConcurrentEntityModificationException e)
			{
				Thread.sleep(ENTITY_MODIFICATION_SLEEP_TIME);

				if (nTries >= MAX_ENTITY_MODIFICATION_SLEEP_TRIES)
				{
					throw e;
				}
			}
		}

		Log.info((bBefore ? "Start " : "Finish ") + aProcess.getFullName());
	}

	/***************************************
	 * Creates a new process using the {@link ProcessDefinition} assigned to
	 * this {@link ProcessRunner}.
	 *
	 * @return The created {@link Process}
	 *
	 * @throws ProcessException if process creation fails
	 */
	private Process createProcess() throws ProcessException
	{
		Process rProcess = ProcessManager.getProcess(rProcessDefinition);

		if (rProcessScheduler != null)
		{
			rProcess.set(PROCESS_USER,
						 rProcessScheduler.getScheduleProcessUser());
		}

		return rProcess;
	}

	/***************************************
	 * Checks whether {@link #bLogOnError} is set to TRUE. If it is, logging is
	 * done using the {@link LogLevel} {@link #eLogLevel}
	 *
	 * @param rLogMessage The log message
	 * @param e           The ProcessException
	 */
	private void doLoggingIfEnabled(String rLogMessage, Exception e)
	{
		if (bLogOnError)
		{
			Log.log(eLogLevel, rLogMessage, e);
		}
	}

	/***************************************
	 * Internal method that executes the given process
	 *
	 * @throws Exception If the process execution fails
	 */

	private void executeProcess() throws Exception
	{
		try
		{
			aProcess = createProcess();

			if (rProcessScheduler != null)
			{
				rProcessScheduler.notifyScheduleProcessStarting(aProcess);
			}

			beforeAfterExecution(true);
			aProcess.execute();
		}
		catch (Exception e)
		{
			if (!bContinueOnError)
			{
				doLoggingIfEnabled("Schedule process error [" +
								   aProcess.getName() + "], stopping",
								   e);
				throw e;
			}
			else
			{
				doLoggingIfEnabled("Schedule process error [" +
								   aProcess.getName() + "], continuing",
								   e);
			}
		}
		finally
		{
			try
			{
				beforeAfterExecution(false);
			}
			catch (Exception e)
			{
				if (!bContinueOnError)
				{
					doLoggingIfEnabled("Error during execution of 'execution support method -> after execution', stopping ",
									   e);
					throw e;
				}
				else
				{
					doLoggingIfEnabled("Error during execution of 'execution support method -> after execution', continuing ",
									   e);
				}
			}

			if (rProcessScheduler != null)
			{
				rProcessScheduler.notifyScheduleProcessFinished(aProcess);
			}
		}
	}
}
