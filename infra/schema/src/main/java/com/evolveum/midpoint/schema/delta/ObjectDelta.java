/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */
package com.evolveum.midpoint.schema.delta;

import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.schema.processor.*;
import com.evolveum.midpoint.schema.util.DebugUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.PropertyModificationType;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Relative difference (delta) of the object.
 * <p/>
 * This class describes how the object changes. It can describe either object addition, modification of deletion.
 * <p/>
 * Addition described complete new (absolute) state of the object.
 * <p/>
 * Modification contains a set property deltas that describe relative changes to individual properties
 * <p/>
 * Deletion does not contain anything. It only marks object for deletion.
 * <p/>
 * The OID is mandatory for modification and deletion.
 *
 * @author Radovan Semancik
 * @see PropertyDelta
 */
public class ObjectDelta<T extends ObjectType> implements Dumpable, DebugDumpable {

    private ChangeType changeType;

    /**
     * OID of the object that this delta applies to.
     */
    private String oid;

    /**
     * New object to add. Valid only if changeType==ADD
     */
    private MidPointObject<T> objectToAdd;

    /**
     * Set of relative property deltas. Valid only if changeType==MODIFY
     */
    private Collection<PropertyDelta> modifications;

    /**
     * Class of the object that we describe.
     */
    private Class<T> objectTypeClass;

    public ObjectDelta(Class<T> objectTypeClass, ChangeType changeType) {
        this.changeType = changeType;
        this.objectTypeClass = objectTypeClass;
        objectToAdd = null;
        modifications = createEmptyModifications();
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public MidPointObject<T> getObjectToAdd() {
        return objectToAdd;
    }

    public void setObjectToAdd(MidPointObject<T> objectToAdd) {
        this.objectToAdd = objectToAdd;
        if (objectToAdd != null) {
            this.objectTypeClass = objectToAdd.getJaxbClass();
        }
    }

    public Collection<PropertyDelta> getModifications() {
        return modifications;
    }

    public void addModification(PropertyDelta propertyDelta) {
        modifications.add(propertyDelta);
    }

    public PropertyDelta getPropertyDelta(PropertyPath propertyPath) {
        if (changeType == ChangeType.ADD) {
            Property property = objectToAdd.findProperty(propertyPath);
            if (property == null) {
                return null;
            }
            PropertyDelta propDelta = new PropertyDelta(propertyPath);
            propDelta.addValuesToAdd(property.getValues());
            return propDelta;
        } else if (changeType == ChangeType.MODIFY) {
            return findModification(propertyPath);
        } else {
            return null;
        }
    }

    public Class<T> getObjectTypeClass() {
        return objectTypeClass;
    }

    public void setObjectTypeClass(Class<T> objectTypeClass) {
        this.objectTypeClass = objectTypeClass;
    }

    /**
     * Top-level path is assumed.
     */
    public PropertyDelta getPropertyDelta(QName propertyName) {
        return getPropertyDelta(new PropertyPath(propertyName));
    }

    public PropertyDelta getPropertyDelta(PropertyPath parentPath, QName propertyName) {
        return getPropertyDelta(new PropertyPath(parentPath, propertyName));
    }

    private PropertyDelta findModification(PropertyPath propertyPath) {
        if (modifications == null) {
            return null;
        }
        for (PropertyDelta delta : modifications) {
            if (delta.getPath().equals(propertyPath)) {
                return delta;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return (objectToAdd == null && (modifications == null || modifications.isEmpty()));
    }

    /**
     * Semi-deep clone.
     */
    public ObjectDelta<T> clone() {
        ObjectDelta<T> clone = new ObjectDelta<T>(this.objectTypeClass, this.changeType);
        clone.oid = this.oid;
        clone.modifications = createEmptyModifications();
        clone.modifications.addAll(this.modifications);
        if (this.objectToAdd == null) {
            clone.objectToAdd = null;
        } else {
            clone.objectToAdd = this.objectToAdd.clone();
        }
        return clone;
    }

    /**
     * Merge provided delta into this delta.
     * This delta is assumed to be chronologically earlier.
     */
    public void merge(ObjectDelta<T> deltaToMerge) {
        if (changeType == ChangeType.ADD) {
            if (deltaToMerge.changeType == ChangeType.ADD) {
                // Maybe we can, be we do not want. This is usually an error anyway.
                throw new IllegalArgumentException("Cannot merge two ADD deltas: " + this + ", " + deltaToMerge);
            } else if (deltaToMerge.changeType == ChangeType.MODIFY) {
                if (objectToAdd == null) {
                    throw new IllegalStateException("objectToAdd is null");
                }
                deltaToMerge.applyTo(objectToAdd);
            } else if (deltaToMerge.changeType == ChangeType.DELETE) {
                this.changeType = ChangeType.DELETE;
            }
        } else if (changeType == ChangeType.MODIFY) {
            if (deltaToMerge.changeType == ChangeType.ADD) {
                throw new IllegalArgumentException("Cannot merge 'add' delta to a 'modify' object delta");
            } else if (deltaToMerge.changeType == ChangeType.MODIFY) {
                // TODO: merge changes to the object to create
                throw new UnsupportedOperationException();
            } else if (deltaToMerge.changeType == ChangeType.DELETE) {
                this.changeType = ChangeType.DELETE;
            }
        } else { // DELETE
            if (deltaToMerge.changeType == ChangeType.ADD) {
                this.changeType = ChangeType.ADD;
                // TODO: clone?
                this.objectToAdd = deltaToMerge.objectToAdd;
            } else if (deltaToMerge.changeType == ChangeType.MODIFY) {
                // Just ignore the modification of a deleted object
            } else if (deltaToMerge.changeType == ChangeType.DELETE) {
                // Nothing to do
            }
        }
    }

    /**
     * Union of several object deltas. The deltas are merged to create a single delta
     * that contains changes from all the deltas.
     */
    public static <T extends ObjectType> ObjectDelta<T> union(ObjectDelta<T>... deltas) {
        List<ObjectDelta<T>> modifyDeltas = new ArrayList<ObjectDelta<T>>(deltas.length);
        ObjectDelta<T> addDelta = null;
        ObjectDelta<T> deleteDelta = null;
        for (ObjectDelta<T> delta : deltas) {
            if (delta == null) {
                continue;
            }
            if (delta.changeType == ChangeType.MODIFY) {
                modifyDeltas.add(delta);
            } else if (delta.changeType == ChangeType.ADD) {
                if (addDelta != null) {
                    // Maybe we can, be we do not want. This is usually an error anyway.
                    throw new IllegalArgumentException("Cannot merge two add deltas: " + addDelta + ", " + delta);
                }
                addDelta = delta;
            } else if (delta.changeType == ChangeType.DELETE) {
                deleteDelta = delta;
            }

        }

        if (deleteDelta != null && addDelta == null) {
            // Merging DELETE with anything except ADD is still a DELETE
            return deleteDelta.clone();
        }

        if (deleteDelta != null && addDelta != null) {
            throw new IllegalArgumentException("Cannot merge add and delete deltas: " + addDelta + ", " + deleteDelta);
        }

        if (addDelta != null) {
            return mergeToDelta(addDelta, modifyDeltas);
        } else {
            if (modifyDeltas.size() == 0) {
                return null;
            }
            if (modifyDeltas.size() == 1) {
                return modifyDeltas.get(0);
            }
            return mergeToDelta(modifyDeltas.get(0), modifyDeltas.subList(1, modifyDeltas.size()));
        }
    }

    private static <T extends ObjectType> ObjectDelta<T> mergeToDelta(ObjectDelta<T> firstDelta,
            List<ObjectDelta<T>> modifyDeltas) {
        if (modifyDeltas.size() == 0) {
            return firstDelta;
        }
        ObjectDelta<T> delta = firstDelta.clone();
        for (ObjectDelta<T> modifyDelta : modifyDeltas) {
            if (modifyDelta == null) {
                continue;
            }
            if (modifyDelta.changeType != ChangeType.MODIFY) {
                throw new IllegalArgumentException("Can only merge MODIFY changes, got " + modifyDelta.changeType);
            }
            delta.mergeModifications(modifyDelta.modifications);
        }
        return delta;
    }

    private void mergeModifications(Collection<PropertyDelta> modificationsToMerge) {
        for (PropertyDelta propDelta : modificationsToMerge) {
            if (changeType == ChangeType.ADD) {
                propDelta.applyTo(objectToAdd);
            } else if (changeType == ChangeType.MODIFY) {
                PropertyDelta myDelta = findModification(propDelta.getPath());
                if (myDelta == null) {
                    modifications.add(propDelta);
                } else {
                    myDelta.merge(propDelta);
                }
            } // else it is DELETE. There's nothing to do. Merging anything to delete is still delete
        }
    }


    /**
     * Applies this object delta to specified object, returns updated object.
     * It modifies the provided object.
     */
    public void applyTo(MidPointObject<T> mpObject) {
        if (changeType != ChangeType.MODIFY) {
            throw new IllegalStateException("Can apply only MODIFY delta to object, got " + changeType + " delta");
        }
        for (PropertyDelta propDelta : modifications) {
            propDelta.applyTo(mpObject);
        }
    }

    /**
     * Applies this object delta to specified object, returns updated object.
     * It leaves the original object unchanged.
     *
     * @param objectOld object before change
     * @return object with applied changes or null if the object should not exit (was deleted)
     */
    public MidPointObject<T> computeChangedObject(MidPointObject<T> objectOld) {
        if (objectOld == null) {
            if (getChangeType() == ChangeType.ADD) {
                objectOld = getObjectToAdd();
            } else {
                //throw new IllegalStateException("Cannot apply "+getChangeType()+" delta to a null old object");
                // This seems to be quite OK
                return null;
            }
        }
        if (getChangeType() == ChangeType.DELETE) {
            return null;
        }
        // MODIFY change
        MidPointObject<T> objectNew = objectOld.clone();
        for (PropertyDelta modification : modifications) {
            modification.applyTo(objectNew);
        }
        return objectNew;
    }

    /**
     * Creates new delta from the ObjectModificationType (XML). Object type and schema are used to locate definitions
     * needed to convert properties from XML.
     */
    public static <T extends ObjectType> ObjectDelta<T> createDelta(ObjectModificationType objectModification,
            Schema schema, Class<T> type) throws SchemaException {
        return createDelta(objectModification, schema.findObjectDefinition(type));
    }

    public static <T extends ObjectType> ObjectDelta<T> createDelta(ObjectModificationType objectModification,
            ObjectDefinition<T> objDef) throws SchemaException {
        ObjectDelta<T> objectDelta = new ObjectDelta<T>(objDef.getJaxbClass(), ChangeType.MODIFY);
        objectDelta.setOid(objectModification.getOid());

        for (PropertyModificationType propMod : objectModification.getPropertyModification()) {
            PropertyDelta propDelta = PropertyDelta.createDelta(propMod, objDef);
            objectDelta.addModification(propDelta);
        }

        return objectDelta;
    }

    /**
     * Converts this delta to ObjectModificationType (XML).
     */
    public ObjectModificationType toObjectModificationType() throws SchemaException {
        if (changeType != ChangeType.MODIFY) {
            throw new IllegalStateException("Cannot produce ObjectModificationType from delta of type " + changeType);
        }
        ObjectModificationType modType = new ObjectModificationType();
        modType.setOid(getOid());
        List<PropertyModificationType> propModTypes = modType.getPropertyModification();
        for (PropertyDelta propDelta : modifications) {
            Collection<PropertyModificationType> propPropModTypes;
            try {
                propPropModTypes = propDelta.toPropertyModificationTypes();
            } catch (SchemaException e) {
                throw new SchemaException(e.getMessage() + " in " + this.toString(), e);
            }
            propModTypes.addAll(propPropModTypes);
        }
        return modType;
    }

    /**
     * Incorporates the property delta into the existing property deltas
     * (regardless of the change type).
     */
    public void swallow(PropertyDelta newPropertyDelta) {
        if (changeType == ChangeType.MODIFY) {
            // TODO: check for conflict
            addModification(newPropertyDelta);
        } else if (changeType == ChangeType.ADD) {
        	Class<?> valueClass = newPropertyDelta.getValueClass();
            Property property = objectToAdd.findOrCreateProperty(newPropertyDelta.getParentPath(), newPropertyDelta.getName(), valueClass);
            newPropertyDelta.applyTo(property);
        }
        // nothing to do for DELETE
    }

    private Collection<PropertyDelta> createEmptyModifications() {
        return new HashSet<PropertyDelta>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObjectDelta(oid=");
        sb.append(oid).append(",").append(changeType).append(": ");
        if (changeType == ChangeType.ADD) {
            if (objectToAdd == null) {
                sb.append("null");
            } else {
                sb.append(objectToAdd.toString());
            }
        } else if (changeType == ChangeType.MODIFY) {
            Iterator<PropertyDelta> i = modifications.iterator();
            while (i.hasNext()) {
                sb.append(i.next().toString());
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        // Nothing to print for delete
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String debugDump() {
        return debugDump(0);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("ObjectDelta(oid=");
        sb.append(oid).append(",").append(changeType).append("):\n");
        if (changeType == ChangeType.ADD) {
            if (objectToAdd == null) {
            	DebugUtil.indentDebugDump(sb, indent + 1);
                sb.append("null");
            } else {
                sb.append(objectToAdd.debugDump(indent + 1));
            }
        } else if (changeType == ChangeType.MODIFY) {
            Iterator<PropertyDelta> i = modifications.iterator();
            while (i.hasNext()) {
                sb.append(i.next().debugDump(indent + 1));
                if (i.hasNext()) {
                    sb.append("\n");
                }
            }
        }
        // Nothing to print for delete
        return sb.toString();
    }

    @Override
    public String dump() {
        return debugDump(0);
    }

}
