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

/********************************************************************
 * An interface for objects that allow to write data rValues to some
 * destination.
 *
 * @author eso
 */
public interface DataWriter
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Writes a boolean rValue.
	 *
	 * @param bValue The boolean
	 */
	void writeBoolean(boolean bValue);

	/***************************************
	 * Writes a byte rValue.
	 *
	 * @param nValue The byte
	 */
	void writeByte(byte nValue);

	/***************************************
	 * Writes a char rValue.
	 *
	 * @param cValue The char
	 */
	void writeChar(char cValue);

	/***************************************
	 * Writes a double rValue.
	 *
	 * @param fValue The double
	 */
	void writeDouble(double fValue);

	/***************************************
	 * Writes a float rValue.
	 *
	 * @param fValue The float
	 */
	void writeFloat(float fValue);

	/***************************************
	 * Writes a int rValue.
	 *
	 * @param nValue The int
	 */
	void writeInt(int nValue);

	/***************************************
	 * Writes a long rValue.
	 *
	 * @param nValue The long
	 */
	void writeLong(long nValue);

	/***************************************
	 * Writes a object rValue.
	 *
	 * @param rValue The object
	 */
	void writeObject(Object rValue);

	/***************************************
	 * Writes a short rValue.
	 *
	 * @param nValue The short
	 */
	void writeShort(short nValue);

	/***************************************
	 * Writes a string rValue.
	 *
	 * @param sValue The string
	 */
	void writeString(String sValue);
}
