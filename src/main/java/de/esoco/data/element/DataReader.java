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
 * An interface for objects that allow to read data from some source.
 *
 * @author eso
 */
public interface DataReader {

	/**
	 * Reads a boolean value.
	 *
	 * @return The boolean
	 */
	boolean readBoolean();

	/**
	 * Reads a byte value.
	 *
	 * @return The byte
	 */
	byte readByte();

	/**
	 * Reads a char value.
	 *
	 * @return The char
	 */
	char readChar();

	/**
	 * Reads a double value.
	 *
	 * @return The double
	 */
	double readDouble();

	/**
	 * Reads a float value.
	 *
	 * @return The float
	 */
	float readFloat();

	/**
	 * Reads a int value.
	 *
	 * @return The int
	 */
	int readInt();

	/**
	 * Reads a long value.
	 *
	 * @return The long
	 */
	long readLong();

	/**
	 * Reads a object value.
	 *
	 * @return The object
	 */
	Object readObject();

	/**
	 * Reads a short value.
	 *
	 * @return The short
	 */
	short readShort();

	/**
	 * Reads a string value.
	 *
	 * @return The string
	 */
	String readString();
}
