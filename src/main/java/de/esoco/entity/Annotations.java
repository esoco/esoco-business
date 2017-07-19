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
package de.esoco.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/********************************************************************
 * Contains Entity-specific annotations.
 *
 * @author eso
 */
public class Annotations
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Annotations()
	{
	}

	//~ Annotations ------------------------------------------------------------

	/********************************************************************
	 * Marks an entity class that only represents a certain type value in the
	 * type attribute of a parent entity class and has no entity definition of
	 * it's own.
	 *
	 * @author eso
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TypeSpecificEntity
	{
	}
}
