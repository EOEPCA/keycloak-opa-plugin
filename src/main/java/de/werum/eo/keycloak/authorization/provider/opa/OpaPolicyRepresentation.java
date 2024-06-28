/*
 * Copyright 2024 Werum Software & Systems AG (Germany)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package de.werum.eo.keycloak.authorization.provider.opa;

import org.keycloak.representations.idm.authorization.JSPolicyRepresentation;

/**
 * Policy representation for OPA-based policies.
 *
 * @author w-scho
 * @version SVN $Revision$ $Date$
 * @since
 */
public class OpaPolicyRepresentation extends JSPolicyRepresentation/*AbstractPolicyRepresentation*/ { // TODO: Revert to AbstractPolicyRepresentation when UI is ready!

   private String policyPath;

   @Override
   public String getType() {
      return "opa";
   }

   public String getPolicyPath( ) {
      return policyPath;
   }

   public void setPolicyPath( String policyPath ) {
      this.policyPath = policyPath;
   }
}
