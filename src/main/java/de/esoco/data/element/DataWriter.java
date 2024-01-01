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
package de.esoco.data.element;

/**
 * An interface for objects that allow to write data values to some
 * destination.
 *
 * @author eso
 */
public interface DataWriter {

	/**
	 * Writes a boolean value.
	 *
	 * @param value The boolean
	 */
	void writeBoolean(boolean value);

	/**
	 * Writes a byte value.
	 *
	 * @param value The byte
	 */
	void writeByte(byte value);

	/**
	 * Writes a char value.
	 *
	 * @param value The char
	 */
	void writeChar(char value);

	/**
	 * Writes a double value.
	 *
	 * @param value The double
	 */
	void writeDouble(double value);

	/**
	 * Writes a float value.
	 *
	 * @param value The float
	 */
	void writeFloat(float value);

	/**
	 * Writes a int value.
	 *
	 * @param value The int
	 */
	void writeInt(int value);

	/**
	 * Writes a long value.
	 *
	 * @param value The long
	 */
	void writeLong(long value);

	/**
	 * Writes a object value.
	 *
	 * @param value The object
	 */
	void writeObject(Object value);

	/**
	 * Writes a short value.
	 *
	 * @param value The short
	 */
	void writeShort(short value);

	/**
	 * Writes a string value.
	 *
	 * @param value The string
	 */
	void writeString(String value);
}
