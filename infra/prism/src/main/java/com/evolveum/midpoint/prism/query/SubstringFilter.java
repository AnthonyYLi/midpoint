/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.prism.query;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugUtil;

public class SubstringFilter<T> extends PropertyValueFilter<PrismPropertyValue<T>> {

	SubstringFilter(ItemPath parentPath, ItemDefinition definition, PrismPropertyValue<T> value) {
		super(parentPath, definition, value);
	}
	
	SubstringFilter(ItemPath parentPath, ItemDefinition definition, QName matchingRule, List<PrismPropertyValue<T>> value) {
		super(parentPath, definition, matchingRule, value);
	}
	
	SubstringFilter(ItemPath parentPath, ItemDefinition definition, QName matchingRule) {
		super(parentPath, definition, matchingRule);
	}
	

	public static <T> SubstringFilter createSubstring(QName path, PrismPropertyDefinition itemDefinition, PrismPropertyValue<T> values) {
		return createSubstring(new ItemPath(path), itemDefinition, null, values);
	}
	
	public static <T> SubstringFilter createSubstring(ItemPath path, PrismPropertyDefinition itemDefinition, PrismPropertyValue<T> values) {
		return createSubstring(path, itemDefinition, null, values);
	}
	
	public static <T> SubstringFilter createSubstring(QName path, PrismPropertyDefinition itemDefinition, QName matchingRule, PrismPropertyValue<T> values) {
		return createSubstring(new ItemPath(path), itemDefinition, matchingRule, values);
	}
	
	public static <T> SubstringFilter createSubstring(QName path, PrismPropertyDefinition itemDefinition, T realValues) {
		return createSubstring(new ItemPath(path), itemDefinition, null, realValues);
	}
	
	public static <T> SubstringFilter createSubstring(ItemPath path, PrismPropertyDefinition itemDefinition, T realValues) {
		return createSubstring(path, itemDefinition, null, realValues);
	}
	
	public static <T> SubstringFilter createSubstring(QName path, PrismPropertyDefinition itemDefinition, QName matchingRule, T realValues) {
		return createSubstring(new ItemPath(path), itemDefinition, matchingRule, realValues);
	}
	
	public static <T> SubstringFilter createSubstring(ItemPath path, PrismPropertyDefinition itemDefinition, QName matchingRule, T realValues) {
		if (realValues == null){
			return createNullSubstring(path, itemDefinition, matchingRule);
		}
		List<PrismPropertyValue<T>> pValues = createPropertyList(itemDefinition, realValues);
		return new SubstringFilter<T>(path, itemDefinition, matchingRule, pValues);
	}
	
	public static <T> SubstringFilter createSubstring(ItemPath path, PrismPropertyDefinition itemDefinition, QName matchingRule, PrismPropertyValue<T> values) {
		Validate.notNull(path, "Item path in substring filter must not be null.");
		Validate.notNull(itemDefinition, "Item definition in substring filter must not be null.");
		
		if (values == null){
			return createNullSubstring(path, itemDefinition, matchingRule);
		}
		
		List<PrismPropertyValue<T>> pValues = createPropertyList(itemDefinition, values);
				
		return new SubstringFilter(path, itemDefinition, matchingRule, pValues);
	}
	
	public static <O extends Objectable, T> SubstringFilter createSubstring(ItemPath path, Class<O> clazz, PrismContext prismContext, T value) {
		return createSubstring(path, clazz, prismContext, null, value);
	}
	
	public static <O extends Objectable, T> SubstringFilter createSubstring(ItemPath path, Class<O> clazz, PrismContext prismContext, QName matchingRule, T realValue) {
		
		ItemDefinition itemDefinition = findItemDefinition(path, clazz, prismContext);
		
		if (!(itemDefinition instanceof PrismPropertyDefinition)){
			throw new IllegalStateException("Bad definition. Expected property definition, but got " + itemDefinition);
		}
		
		if (realValue == null){
			return createNullSubstring(path, (PrismPropertyDefinition) itemDefinition, matchingRule);
		}
		
		List<PrismPropertyValue<T>> pVals = createPropertyList((PrismPropertyDefinition) itemDefinition, realValue);
		
		return new SubstringFilter(path, itemDefinition, matchingRule, pVals);
	}
	
	public static <O extends Objectable> SubstringFilter createSubstring(QName propertyName, Class<O> clazz, PrismContext prismContext, String value) {
		return createSubstring(propertyName, clazz, prismContext, null, value);
    }

    public static <O extends Objectable> SubstringFilter createSubstring(QName propertyName, Class<O> clazz, PrismContext prismContext, QName matchingRule, String value) {
        return createSubstring(new ItemPath(propertyName), clazz, prismContext, matchingRule, value);
    }
    
    private static SubstringFilter createNullSubstring(ItemPath itemPath, PrismPropertyDefinition propertyDef, QName matchingRule){
		return new SubstringFilter(itemPath, propertyDef, matchingRule);
		
	}
    

	@Override
	public SubstringFilter clone() {
		return new SubstringFilter(getFullPath(),getDefinition(), getMatchingRule(), getValues());
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("SUBSTRING: \n");
		return debugDump(indent, sb);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SUBSTRING: ");
		return toString(sb);
	}

	@Override
	public <T extends Objectable> boolean match(PrismObject<T> object, MatchingRuleRegistry matchingRuleRegistry) {
		Item item = getObjectItem(object);
		
		MatchingRule matching = getMatchingRuleFromRegistry(matchingRuleRegistry, item);
		
		for (Object val : item.getValues()){
			if (val instanceof PrismPropertyValue){
				Object value = ((PrismPropertyValue) val).getValue();
				Iterator iterator = toRealValues().iterator();
				while(iterator.hasNext()){
					if (matching.matches(value, ".*"+iterator.next()+".*")){
						return true;
					}
				}
			}
			if (val instanceof PrismReferenceValue) {
				throw new UnsupportedOperationException(
						"matching substring on the prism reference value not supported yet");
			}
		}
		
		return false;
	}

	private Set<T> toRealValues(){
		 return PrismPropertyValue.getRealValuesOfCollection(getValues());
	}
	
	@Override
	public QName getElementName() {
		return getDefinition().getName();
	}

	@Override
	public PrismContext getPrismContext() {
		return getDefinition().getPrismContext();
	}

	@Override
	public ItemPath getPath() {
		return getFullPath();
	}

}
