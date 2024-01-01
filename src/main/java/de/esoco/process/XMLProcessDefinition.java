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

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Defines a process from an XML process description.
 *
 * @author eso
 */
public class XMLProcessDefinition extends StepListProcessDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new XML process definition from a specific XML file.
	 *
	 * @param mLFile The XML file name
	 */
	public XMLProcessDefinition(String mLFile) {
		this(new File(mLFile));
	}

	/**
	 * Creates a new XML process definition from a specific XML file.
	 *
	 * @param mLFile The XML file name
	 * @throws IllegalArgumentException If the file doesn't exist or if it has
	 *                                  an invalid format
	 */
	public XMLProcessDefinition(File mLFile) {
		if (!mLFile.exists()) {
			throw new IllegalArgumentException(
				"Process definition file not found: " + mLFile);
		}

		try {
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(mLFile);

			parseXML(document);
		} catch (Exception e) {
			IllegalArgumentException iA = new IllegalArgumentException(
				"Error parsing process definition file: " + mLFile, e);

			throw iA;
		}
	}

	/**
	 * Parses the XML representation of the process definition and creates the
	 * corresponding internal process step list.
	 */
	private void parseXML(Document document) {
		// TODO: Auto-generated method stub
	}
}
