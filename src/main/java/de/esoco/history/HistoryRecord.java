//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.history;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityRelationTypes;

import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.HasProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.RelationType;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.arbitraryEntityAttribute;
import static de.esoco.entity.EntityRelationTypes.childAttribute;
import static de.esoco.entity.EntityRelationTypes.parentAttribute;
import static de.esoco.entity.EntityRelationTypes.rootAttribute;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.MAX_CHARS;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newType;

/**
 * An entity subclass that implements special history record handling.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.entity.history")
public class HistoryRecord extends Entity {

	/**
	 * An enumeration of the possible types of history records. The type value
	 * {@link #GROUP} is only for internal use and must not be used by
	 * application code, or else an exception will be thrown. The suggested
	 * usage of the other types is as follows:
	 *
	 * <ul>
	 *   <li>{@link #NOTE}: a manually entered note on the target object</li>
	 *   <li>{@link #INFO}: an automatically created information message</li>
	 *   <li>{@link #ERROR}: an automatically created error message</li>
	 *   <li>{@link #CHANGE}: an automatically created record about
	 *     modifications</li>
	 * </ul>
	 *
	 * @author eso
	 */
	public enum HistoryType {NOTE, INFO, ERROR, CHANGE, GROUP}

	/**
	 * An enumeration of the possible keys for the
	 * {@link HistoryRecord#REFERENCE} field.
	 */
	public enum ReferenceType {EMAIL, DOCUMENT}

	/**
	 * The parent record of the history record (NULL for the root)
	 */
	public static final RelationType<HistoryRecord> PARENT = parentAttribute();

	/**
	 * The root history record in a hierarchy or NULL for the root itself
	 */
	public static final RelationType<HistoryRecord> ROOT = rootAttribute();

	/**
	 * The child history records
	 */
	public static final RelationType<List<HistoryRecord>> DETAILS =
		childAttribute();

	/**
	 * Type of the history record
	 */
	public static final RelationType<HistoryType> TYPE = newType(FINAL);

	/**
	 * Creation time of the history record
	 */
	public static final RelationType<Date> TIME = newType(FINAL);

	/**
	 * The entity that is the origin of the history record
	 */
	public static final RelationType<Entity> ORIGIN =
		arbitraryEntityAttribute(FINAL);

	/**
	 * The root target in a hierarchy of history records
	 */
	public static final RelationType<Entity> ROOT_TARGET =
		arbitraryEntityAttribute();

	/**
	 * The entity that is the target of the history record
	 */
	public static final RelationType<Entity> TARGET =
		EntityRelationTypes.TARGET;

	/**
	 * A reference to additional informations.
	 */
	public static final RelationType<String> REFERENCE = newType();

	/**
	 * The value of the history record
	 */
	public static final RelationType<String> VALUE = newType();

	/**
	 * The prefix for global entity IDs
	 */
	public static final String ID_PREFIX = "HST";

	//- entity definition constants

	/**
	 * The storage name
	 */
	public static final String STORAGE_NAME = "history";

	/**
	 * Minimal display attributes
	 */
	public static final RelationType<?>[] DISPLAY_ATTRIBUTES_MINIMAL =
		new RelationType<?>[] { ENTITY_ID, TYPE, TIME };

	/**
	 * Compact display attributes
	 */
	public static final RelationType<?>[] DISPLAY_ATTRIBUTES_COMPACT =
		new RelationType<?>[] { ENTITY_ID, TYPE, TIME, VALUE };

	/**
	 * The attribute display properties map.
	 */
	public static final Map<RelationType<?>, HasProperties>
		ATTRIBUTE_DISPLAY_PROPERTIES =
		new HashMap<RelationType<?>, HasProperties>();

	private static final long serialVersionUID = 1L;

	static {
		Class<? extends Entity> rClass = HistoryRecord.class;

		setAttributeDisplayFlag(rClass, HAS_IMAGES, TYPE);
		setAttributeDisplayProperty(rClass, 20, MAX_CHARS, ORIGIN);
		setAttributeDisplayProperty(rClass, CONTENT_TYPE,
			ContentType.DATE_TIME,
			TIME);
	}

	/**
	 * Overridden to only return TRUE if the type of this record is
	 * {@link HistoryType#NOTE NOTE}. All other types of history will be
	 * ignored
	 * for the change logging.
	 *
	 * @see Entity#hasChangeLogging()
	 */
	@Override
	public boolean hasChangeLogging() {
		return get(TYPE) == HistoryType.NOTE;
	}

	/**
	 * Starts a new detail history record.
	 *
	 * @param rType           The type of this history record
	 * @param rTarget         The target entity referenced by this record (may
	 *                        be NULL)
	 * @param sValue          The value of this record
	 * @param rReferenceType  The type of the reference value
	 * @param sReferenceValue The reference value
	 * @return The new history record
	 */
	HistoryRecord addDetail(HistoryType rType, Entity rTarget, String sValue,
		ReferenceType rReferenceType, String sReferenceValue) {
		HistoryRecord rRoot = get(ROOT);

		HistoryRecord aDetailRecord =
			HistoryManager.createRecord(rType, get(ORIGIN),
				rTarget != null ? rTarget : get(TARGET), get(ROOT_TARGET),
				sValue, rReferenceType, sReferenceValue);

		aDetailRecord.set(ROOT, rRoot != null ? rRoot : this);
		aDetailRecord.set(PARENT, this);

		get(DETAILS).add(aDetailRecord);

		return aDetailRecord;
	}
}
