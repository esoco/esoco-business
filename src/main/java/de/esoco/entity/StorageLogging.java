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
package de.esoco.entity;

import de.esoco.lib.logging.BusinessLogAspect;
import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.logging.LogRecord;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;

import java.util.Collection;
import java.util.Date;

/**
 * A logging implementation that stores log records as {@link LogEntry} entities
 * in a database. It requires no additional configuration relations.
 *
 * @author eso
 */
public class StorageLogging extends BusinessLogAspect<LogEntry> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LogEntry createLogObject(LogRecord logRecord) {
		LogLevel logLevel = logRecord.getLevel();
		LogEntry logEntry = null;

		String message = logRecord.getMessage();

		if (logLevel.compareTo(get(MIN_STACK_LOG_LEVEL)) >= 0) {
			Throwable cause = logRecord.getCause();

			if (cause != null) {
				message += String.format(" [%s]\n--------------\n%s", cause,
					Log.CAUSE_TRACE.evaluate(logRecord));
			}
		}

		logEntry = new LogEntry();
		logEntry.set(LogEntry.LEVEL, logLevel);
		logEntry.set(LogEntry.TIME, new Date(logRecord.getTime()));
		logEntry.set(LogEntry.MESSAGE, message);
		logEntry.set(LogEntry.SOURCE, getLogSource());

		return logEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processLogObjects(Collection<LogEntry> logEntries)
		throws StorageException {
		Storage storage = StorageManager.newStorage(LogEntry.class);

		try {
			for (LogEntry logEntry : logEntries) {
				storage.store(logEntry);
			}

			storage.commit();
		} catch (Exception e) {
			storage.rollback();
			throw e;
		} finally {
			storage.release();
		}
	}
}
