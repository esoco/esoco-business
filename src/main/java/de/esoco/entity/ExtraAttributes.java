//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Action;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.esoco.storage.impl.jdbc.JdbcRelationTypes.SQL_OMIT_NAMESPACE;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.MetaTypes.RELATION_TYPE_INIT_ACTION;
import static org.obrel.type.MetaTypes.RELATION_TYPE_NAMESPACE;

/**
 * A relation type factory class to create relation types for
 * {@link ExtraAttribute ExtraAttributes} of {@link Entity Entities}. Extra
 * attribute relation types can be recognized
 *
 * @author eso
 */
public final class ExtraAttributes {

	/**
	 * The default namespace for extra attributes.
	 */
	public static final String EXTRA_ATTRIBUTES_NAMESPACE = "xattr";

	/**
	 * A final flag relation that indicates an extra attribute relation type.
	 */
	public static final RelationType<Boolean> EXTRA_ATTRIBUTE_FLAG =
		newType(RelationTypeModifier.FINAL);

	private static final Action<RelationType<?>> EXTRA_ATTR_INIT_ACTION =
		xa -> {
			RelationType<?> type = RelationType.valueOf(xa.getSimpleName());

			assert type == null || type == xa :
				"ExtraAttribute has same name as RelationType " +
					xa.getSimpleName();
		};

	static {
		RelationTypes.init(ExtraAttributes.class);
	}

	/**
	 * Private, only static use.
	 */
	private ExtraAttributes() {
	}

	/**
	 * Adds the necessary annotations to a new extra attribute relation type.
	 *
	 * @param type The relation type
	 * @return The annotated relation type
	 */
	private static <T> RelationType<T> annotateExtraAttribute(
		RelationType<T> type) {
		return type
			.annotate(RELATION_TYPE_NAMESPACE, EXTRA_ATTRIBUTES_NAMESPACE)
			.annotate(EXTRA_ATTRIBUTE_FLAG)
			.annotate(SQL_OMIT_NAMESPACE)
			.annotate(RELATION_TYPE_INIT_ACTION, EXTRA_ATTR_INIT_ACTION);
	}

	/**
	 * Factory method to create a new extra attribute with a boolean datatype.
	 *
	 * @param name The name of the key
	 * @return A new extra attribute instance with the given name
	 */
	public static RelationType<Boolean> newBooleanExtraAttribute(String name) {
		return newExtraAttribute(name, Boolean.class);
	}

	/**
	 * Factory method to create a new extra attribute with a certain enum
	 * datatype.
	 *
	 * @param name      The name of the key
	 * @param enumClass The enum class of the attribute
	 * @return A new extra attribute instance with the given name
	 */
	public static <E extends Enum<E>> RelationType<E> newEnumExtraAttribute(
		String name, Class<E> enumClass) {
		return newExtraAttribute(name, enumClass);
	}

	/**
	 * Factory method to create a new extra attribute with a certain name and
	 * datatype. The type parameter of the datatype argument has been
	 * relaxed to
	 * '? super T' to allow the usage of other generic datatypes like the
	 * collection classes. This is necessary because, for example, the literal
	 * {@code List.class} would otherwise not be a valid argument to define a
	 * key with the datatype {@code List<T>}.
	 *
	 * @return A new extra attribute instance with the given name and datatype
	 */
	public static <T> RelationType<T> newExtraAttribute() {
		return annotateExtraAttribute(RelationTypes.newType());
	}

	/**
	 * Factory method to create a new extra attribute with a certain name and
	 * datatype. The type parameter of the datatype argument has been
	 * relaxed to
	 * '? super T' to allow the usage of other generic datatypes like the
	 * collection classes. This is necessary because, for example, the literal
	 * {@code List.class} would otherwise not be a valid argument to define a
	 * key with the datatype {@code List<T>}.
	 *
	 * @param name     The name of the key
	 * @param datatype The class of the key datatype
	 * @return A new extra attribute instance with the given name and datatype
	 */
	public static <T> RelationType<T> newExtraAttribute(String name,
		Class<? super T> datatype) {
		return annotateExtraAttribute(
			RelationTypes.newRelationType(name, datatype));
	}

	/**
	 * Factory method to create a new extra attribute with an integer datatype.
	 *
	 * @param name The name of the key
	 * @return A new extra attribute instance with the given name
	 */
	public static RelationType<Integer> newIntegerExtraAttribute(String name) {
		return newExtraAttribute(name, Integer.class);
	}

	/**
	 * Factory method to create a new extra attribute with a {@link List}
	 * datatype. The element datatype must be of type ? super E to support
	 * collections of generic types.
	 *
	 * @param name        The name of the key
	 * @param elementType The class of the list element datatype
	 * @return A new extra attribute instance with the given name
	 */
	public static <E> RelationType<List<E>> newListExtraAttribute(String name,
		Class<? super E> elementType) {
		return annotateExtraAttribute(
			RelationTypes.newListType(name, elementType));
	}

	/**
	 * Factory method to create a new extra attribute with a {@link Map}
	 * datatype.
	 *
	 * @param name      The name of the key
	 * @param keyType   The datatype of the map keys
	 * @param valueType The datatype of the map values
	 * @return A new extra attribute instance with the given name
	 */
	public static <K, V> RelationType<Map<K, V>> newMapExtraAttribute(
		String name, Class<K> keyType, Class<V> valueType) {
		return annotateExtraAttribute(
			RelationTypes.newMapType(name, keyType, valueType, false, false));
	}

	/**
	 * Factory method to create an extra attribute with an ordered {@link Map}
	 * datatype. Extra attributes with such a key will be read from the
	 * database
	 * as an ordered map implementation like {@link LinkedHashMap}. Because
	 * extra attributes have no default values the caller is responsible for
	 * setting an ordered map when initializing such an extra attribute.
	 *
	 * @return A new extra attribute instance
	 */
	public static <K, V> RelationType<Map<K, V>> newOrderedMapExtraAttribute() {
		return ExtraAttributes
			.<Map<K, V>>newExtraAttribute()
			.annotate(MetaTypes.ORDERED);
	}

	/**
	 * Factory method to create an extra attribute with an ordered {@link Set}
	 * datatype. Extra attributes with such a key will be read from the
	 * database
	 * as an ordered set implementation like {@link LinkedHashSet}. Because
	 * extra attributes have no default values the caller is responsible for
	 * setting an ordered set when initializing such an extra attribute.
	 *
	 * @return A new extra attribute instance
	 */
	public static <E> RelationType<Set<E>> newOrderedSetExtraAttribute() {
		return ExtraAttributes
			.<Set<E>>newExtraAttribute()
			.annotate(MetaTypes.ORDERED);
	}

	/**
	 * Factory method to create a new extra attribute with a string datatype.
	 *
	 * @param name The name of the key
	 * @return A new extra attribute instance with the given name
	 */
	public static RelationType<String> newStringExtraAttribute(String name) {
		return newExtraAttribute(name, String.class);
	}
}
