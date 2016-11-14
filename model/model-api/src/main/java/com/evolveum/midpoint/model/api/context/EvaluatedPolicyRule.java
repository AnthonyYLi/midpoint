/**
 * Copyright (c) 2016 Evolveum
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
package com.evolveum.midpoint.model.api.context;

import java.io.Serializable;
import java.util.Collection;

import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractPolicyConstraintType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EvaluatedPolicyRuleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PolicyActionsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PolicyConstraintsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PolicyRuleType;

/**
 * @author semancik
 *
 */
public interface EvaluatedPolicyRule extends DebugDumpable, Serializable {
	
	Collection<EvaluatedPolicyRuleTrigger> getTriggers();

	String getName();
	
	PolicyRuleType getPolicyRule();
	
	PolicyConstraintsType getPolicyConstraints();
	
	String getPolicySituation();
	
	PolicyActionsType getActions();
	
}
