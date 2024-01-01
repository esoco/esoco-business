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

import org.obrel.core.RelationType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class can be used to create an entity definition based on a given XML
 * data file.
 *
 * @author thomas
 */
public class XmlEntityDefinition extends EntityDefinition<Entity> {

	private static final long serialVersionUID = 1L;

	// A set of all future entities that are contained in the XML file
	static Set<String> entitySet = new HashSet<String>();

	// A list of all elements that are contained in the XML file
	static List<String> elementsList = new LinkedList<String>();

	// A mapping of entity names to sets of attribute values
	static Map<String, Set<String>> resultMap =
		new HashMap<String, Set<String>>();

	// A map that is used to count the number of attributes for each entity.
	static Map<String, Integer> helpMap = new HashMap<String, Integer>();

	static boolean check = true;

	static String rootElement = null;

	/**
	 * Creates a new instance.
	 *
	 * @param xmlFile entityName reference name The name of the definition
	 */
	XmlEntityDefinition(String xmlFile) {
		// TODO: read ID prefix and entity class from XML
		init("", "", Entity.class, readAttributes(xmlFile));
	}

	/**
	 * Returns a List of relation types that will be used as attributes in the
	 * entity definition.
	 *
	 * @param file The XML file
	 * @return List of relation types
	 */
	private static List<RelationType<?>> readAttributes(String file) {
		List<RelationType<?>> attributeTypes =
			new ArrayList<RelationType<?>>();

		DefaultHandler handler = new XmlHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			factory.newSAXParser().parse(new File(file), handler);
		} catch (Exception e) {
			throw new IllegalArgumentException("XML parsing error: " + file,
				e);
		}

		System.out.println(entitySet);
		System.out.println(resultMap);
		System.out.println("ROOT: " + rootElement);

		return attributeTypes;
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
			String name) {
			System.out.println("end: " + name);

			// If check is false at this point, two endElements follow one
			// another, which means that an
			// entity element was found!
			if (!check) {
				int listSize = elementsList.size();
				Set<String> attributeSet = new HashSet<String>();

				entitySet.add(name);

				if (helpMap.get(name) == null) {
					helpMap.put(name, 0);
				}

				// find the attributes of the current element
				while (true) {
					// iterate backwards thru the whole list of elements until
					// the current element is equal
					// to an element in the list.
					if (!elementsList.get(listSize - 1).equals(name)) {
						Set<String> set = new HashSet<String>();

						// go thru the set of already found entities and check
						// if an entity is contained in the List
						for (String entity : entitySet) {
							if (entity.equals(elementsList.get(listSize - 1))) {
								set = resultMap.get(entity);
							}
						}

						attributeSet.add(elementsList.get(listSize - 1));

						//remove the attributes of contained entities
						if (set.size() != 0) {
							for (String string : set) {
								attributeSet.remove(string);
							}
						}

						listSize--;
					} else {
						break;
					}

					if (attributeSet.size() > helpMap.get(name)) {
						helpMap.put(name, attributeSet.size());
						resultMap.put(name, attributeSet);
					}
				}
			}

			check = false;
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, Attributes)
		 */
		@Override
		public void startElement(String namespaceURI, String localName,
			String name, Attributes atts) throws SAXException {
			check = true;

			if (rootElement == null) {
				rootElement = name;
			}

			System.out.println("start: " + name);
			elementsList.add(name);
		}
	}
}
