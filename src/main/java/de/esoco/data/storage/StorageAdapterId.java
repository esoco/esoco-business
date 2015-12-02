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
package de.esoco.data.storage;

/********************************************************************
 * Instances of this class that identify storage adapters that are registered
 * with a {@link StorageAdapterRegistry}.
 *
 * @author eso
 */
public class StorageAdapterId
{
	//~ Instance fields --------------------------------------------------------

	private int nId;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nId The numerical ID of the storage adapter
	 */
	public StorageAdapterId(int nId)
	{
		this.nId = nId;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj)
	{
		return rObj == this ||
			   rObj != null && rObj.getClass() == StorageAdapterId.class &&
			   nId == ((StorageAdapterId) rObj).nId;
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return nId;
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return Integer.toString(nId);
	}
}
