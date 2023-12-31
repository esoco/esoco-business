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

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.obrel.core.RelationType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class can be used to create an entity definition based on a given XML
 * data file.
 *
 * @author thomas
 */
public class XmlEntityDefinition extends EntityDefinition<Entity> {

	private static final long serialVersionUID = 1L;

	// A set of all future entities that are contained in the XML file
	static Set<String> aEntitySet = new HashSet<String>();

	// A list of all elements that are contained in the XML file
	static List<String> aElementsList = new LinkedList<String>();

	// A mapping of entity names to sets of attribute values
	static Map<String, Set<String>> aResultMap =
		new HashMap<String, Set<String>>();

	// A map that is used to count the number of attributes for each entity.
	static Map<String, Integer> aHelpMap = new HashMap<String, Integer>();

	static boolean bCheck = true;

	static String sRootElement = null;

	/**
	 * Creates a new instance.
	 *
	 * @param sXmlFile sEntityName rReference sName The name of the definition
	 */
	XmlEntityDefinition(String sXmlFile) {
		// TODO: read ID prefix and entity class from XML
		init("", "", Entity.class, readAttributes(sXmlFile));
	}

	/**
	 * Returns a List of relation types that will be used as attributes in the
	 * entity definition.
	 *
	 * @param sFile The XML file
	 * @return List of relation types
	 */
	private static List<RelationType<?>> readAttributes(String sFile) {
		List<RelationType<?>> aAttributeTypes =
			new ArrayList<RelationType<?>>();

		DefaultHandler handler = new XmlHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			factory.newSAXParser().parse(new File(sFile), handler);
		} catch (Exception e) {
			throw new IllegalArgumentException("XML parsing error: " + sFile,
				e);
		}

		System.out.println(aEntitySet);
		System.out.println(aResultMap);
		System.out.println("ROOT: " + sRootElement);

		return aAttributeTypes;
	}

	/**
	 * Implementation of a default handler
	 *
	 * @author thomas
	 */
	private static class XmlHandler extends DefaultHandler {

		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		@Override
		@SuppressWarnings("boxing")
		public void endElement(String namespaceURI, String localName,
			String qName) {
			System.out.println("end: " + qName);

			// If bCheck is false at this point, two endElements follow one
			// another, which means that an
			// entity element was found!
			if (!bCheck) {
				int nListSize = aElementsList.size();
				Set<String> aAttributeSet = new HashSet<String>();

				aEntitySet.add(qName);

				if (aHelpMap.get(qName) == null) {
					aHelpMap.put(qName, 0);
				}

				// find the attributes of the current element
				while (true) {
					// iterate backwards thru the whole list of elements until
					// the current element is equal
					// to an element in the list.
					if (!aElementsList.get(nListSize - 1).equals(qName)) {
						Set<String> aSet = new HashSet<String>();

						// go thru the set of already found entities and check
						// if an entity is contained in the List
						for (String sEntity : aEntitySet) {
							if (sEntity.equals(
								aElementsList.get(nListSize - 1))) {
								aSet = aResultMap.get(sEntity);
							}
						}

						aAttributeSet.add(aElementsList.get(nListSize - 1));

						//remove the attributes of contained entities
						if (aSet.size() != 0) {
							for (String string : aSet) {
								aAttributeSet.remove(string);
							}
						}

						nListSize--;
					} else {
						break;
					}

					if (aAttributeSet.size() > aHelpMap.get(qName)) {
						aHelpMap.put(qName, aAttributeSet.size());
						aResultMap.put(qName, aAttributeSet);
					}
				}
			}

			bCheck = false;
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, Attributes)
		 */
		@Override
		public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
			bCheck = true;

			if (sRootElement == null) {
				sRootElement = qName;
			}

			System.out.println("start: " + qName);
			aElementsList.add(qName);
		}
	}
}
