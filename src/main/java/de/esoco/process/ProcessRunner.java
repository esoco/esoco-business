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
import org.obrel.core.RelatedObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;

/**
 * An class that executes a certain {@link Process}. It implements the
 * {@link Runnable} interface so that it a process can be executed in a separate
 * thread.
 *
 * @author eso
 */
public class ProcessRunner extends RelatedObject
	implements Runnable, RunCheck, Stoppable {

	private static final long ENTITY_MODIFICATION_SLEEP_TIME = 1000L;

	private static final int MAX_ENTITY_MODIFICATION_SLEEP_TRIES = 30;

	private final Lock lock = new ReentrantLock();

	private final Condition pause = lock.newCondition();

	private ProcessScheduler processScheduler;

	private LogLevel logLevel = LogLevel.ERROR;

	private boolean logOnError = true;

	private boolean continueOnError = false;

	private boolean run = true;

	private boolean singleRun = false;

	private boolean running = false;

	private ProcessDefinition processDefinition;

	private Process process = null;

	private final Date nextScheduleTime = new Date();

	/**
	 * Creates a new instance for a certain process definition.
	 *
	 * @param processDefinition The process definition
	 */
	public ProcessRunner(ProcessDefinition processDefinition) {
		setProcessDefinition(processDefinition);
	}

	/**
	 * Creates a new instance.
	 */
	protected ProcessRunner() {
	}

	/**
	 * Run the process now even if it is currently paused or not scheduled to
	 * run in the future.
	 */
	public void executeOnce() {
		run = false;
		executeProcessNow();
	}

	/**
	 * Run the process now even if it is currently paused or not scheduled to
	 * run in the future.
	 */
	public void executeProcessNow() {
		singleRun = true;

		if (running) {
			resumeFromPause();
		} else {
			run();
		}
	}

	/**
	 * Returns the current process of this instance. Will be NULL if is has not
	 * yet been initialized.
	 *
	 * @return The current process (NULL for none)
	 */
	public final Process getProcess() {
		return process;
	}

	/**
	 * Checks whether this {@link ProcessRunner} runs an instance of the
	 * {@link ProcessDefinition} given as the parameter.
	 *
	 * @return TRUE it the given parameter is the class this
	 * {@link ProcessRunner} is running an instance of, FALSE otherwise.
	 */
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	/**
	 * TRUE if the ProcessRunner is running.
	 *
	 * @return TRUE if running.
	 */
	@Override
	public boolean isRunning() {
		return running;
	}

	/**
	 * Resumes the execution of this runner if it is currently waiting.
	 */
	public void resume() {
		run = true;
		resumeFromPause();
	}

	/**
	 * Resumes the execution of this runner if it is currently pausing.
	 */

	public void resumeFromPause() {
		if (lock.tryLock()) {
			try {
				pause.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Executes the process this runner is associated with.
	 */
	@Override
	public void run() {
		running = true;

		try {
			while (run || singleRun) {
				lock.lock();

				try {
					long scheduleTime = getNextScheduleTime().getTime();
					long sleepTime = 0;

					if (!singleRun) {
						sleepTime = scheduleTime - System.currentTimeMillis();
					}

					if (sleepTime > 0) {
						pause.await(sleepTime, TimeUnit.MILLISECONDS);
					}

					if (singleRun ||
						(run && System.currentTimeMillis() >= scheduleTime)) {
						singleRun = false;
						executeProcess();
					}
				} catch (InterruptedException e) {
					// if sleep was interrupted just continue with the next
					// loop
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			// just terminate if process thread is interrupted; this can happen
			// on shutdown
		} catch (Exception e) {
			handleProcessExecutionException(e);
		} finally {
			running = false;
		}
	}

	/**
	 * Sets an optional process scheduler instance that provides the scheduling
	 * context of this instance.
	 *
	 * @param processScheduler the process scheduler
	 */
	public void setProcessScheduler(ProcessScheduler processScheduler) {
		this.processScheduler = processScheduler;
	}

	/**
	 * Stops this runner and the associated process.
	 */

	@Override
	public void stop() {
		run = false;

		if (process != null) {
			process.setParameter(ProcessRelationTypes.STOP_PROCESS_EXECUTION,
				Boolean.TRUE);
		}

		resumeFromPause();
	}

	/**
	 * Internal method that is called right after the process execution has
	 * finished The default implementation does nothing.
	 *
	 * @param process the schedule process that was executed.
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void afterExecution(Process process) throws Exception {
	}

	/**
	 * Internal method that is called right before the process execution
	 * starts.
	 * The default implementation does nothing.
	 *
	 * @param process the schedule process that will be executed.
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void beforeExecution(Process process) throws Exception {
	}

	/**
	 * Returns the date and time of the next schedule at which the process
	 * needs
	 * to be executed. Subclasses may override this method to perform a
	 * schedule
	 * calculation or read the the schedule from a database record.
	 *
	 * <p>The default implementation sets the schedule time to the time at the
	 * creation of this instance so that the process would executed only once.
	 * </p>
	 *
	 * @return The schedule timestamp
	 * @throws Exception If determining the schedule date fails
	 */
	protected Date getNextScheduleTime() throws Exception {
		return nextScheduleTime;
	}

	/**
	 * Returns the entity that describes the user that is responsible for the
	 * process execution. This will be set into the process as the parameter #
	 *
	 * @return The user used for the executed process (can be NULL)
	 */
	protected Entity getProcessUser() {
		Entity user = null;

		if (processScheduler != null) {
			user = processScheduler.getScheduleProcessUser();
		}

		return user;
	}

	/**
	 * This is called when the execution of a process fails and throws an
	 * Exception. Implementing subclasses should take appropriate action.
	 *
	 * @param e rE The Exception that occurred.
	 */
	protected void handleProcessExecutionException(Exception e) {
		Log.error("Process execution failed", e);
	}

	/**
	 * Sets the continueOnError.
	 *
	 * @param continueOnError The continueOnError value
	 */
	protected void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	/**
	 * Sets the logLevel.
	 *
	 * @param logLevel The logLevel value
	 */
	protected void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * Sets the logOnError.
	 *
	 * @param logOnError The logOnError value
	 */
	protected void setLogOnError(boolean logOnError) {
		this.logOnError = logOnError;
	}

	/**
	 * Sets the process definition to be used for the instantiation of the
	 * executed processes.
	 *
	 * @param processDefinition The process definition
	 */
	protected void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	/**
	 * Invokes the execution support methods {@link #beforeExecution(Process)}
	 * and {@link #afterExecution(Process)} and handles framework exceptions
	 * like {@link ConcurrentEntityModificationException}.
	 */
	private void beforeAfterExecution(boolean before) throws Exception {
		int tries = 0;

		while (tries++ < MAX_ENTITY_MODIFICATION_SLEEP_TRIES) {
			try {
				if (before) {
					beforeExecution(process);
				} else {
					afterExecution(process);
				}

				break;
			} catch (ConcurrentEntityModificationException e) {
				Thread.sleep(ENTITY_MODIFICATION_SLEEP_TIME);

				if (tries >= MAX_ENTITY_MODIFICATION_SLEEP_TRIES) {
					throw e;
				}
			}
		}

		Log.info((before ? "Start " : "Finish ") + process.getFullName());
	}

	/**
	 * Creates a new process using the {@link ProcessDefinition} assigned to
	 * this {@link ProcessRunner}.
	 *
	 * @return The created {@link Process}
	 * @throws ProcessException if process creation fails
	 */
	private Process createProcess() throws ProcessException {
		Process process = ProcessManager.getProcess(processDefinition);

		if (processScheduler != null) {
			process.set(PROCESS_USER,
				processScheduler.getScheduleProcessUser());
		}

		return process;
	}

	/**
	 * Checks whether {@link #bLogOnError} is set to TRUE. If it is, logging is
	 * done using the {@link LogLevel} {@link #eLogLevel}
	 *
	 * @param logMessage The log message
	 * @param e          The ProcessException
	 */
	private void doLoggingIfEnabled(String logMessage, Exception e) {
		if (logOnError) {
			Log.log(logLevel, logMessage, e);
		}
	}

	/**
	 * Internal method that executes the given process
	 *
	 * @throws Exception If the process execution fails
	 */

	private void executeProcess() throws Exception {
		try {
			process = createProcess();

			if (processScheduler != null) {
				processScheduler.notifyScheduleProcessStarting(process);
			}

			beforeAfterExecution(true);
			process.execute();
		} catch (Exception e) {
			if (!continueOnError) {
				doLoggingIfEnabled(
					"Schedule process error [" + process.getName() +
						"], stopping", e);
				throw e;
			} else {
				doLoggingIfEnabled(
					"Schedule process error [" + process.getName() +
						"], continuing", e);
			}
		} finally {
			try {
				beforeAfterExecution(false);
			} catch (Exception e) {
				if (!continueOnError) {
					doLoggingIfEnabled(
						"Error during execution of 'execution support method" +
							" " + "-> after execution', stopping ", e);
					throw e;
				} else {
					doLoggingIfEnabled(
						"Error during execution of 'execution support method" +
							" " + "-> after execution', continuing ", e);
				}
			}

			if (processScheduler != null) {
				processScheduler.notifyScheduleProcessFinished(process);
			}
		}
	}
}
