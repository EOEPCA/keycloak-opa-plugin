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

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DecisionResultCollector;
import org.keycloak.authorization.policy.evaluation.DefaultEvaluation;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;

import org.junit.jupiter.api.Test;

/**
 * @author w-scho
 * @version SVN $Revision$ $Date$
 * @since
 */
public class OpaPolicyProviderTest {

   private static final String OPA_BASE_URL = "http://localhost:8181/v1/data";

   private static final Attributes dummyAttributes = new Attributes() {
      @Override
      public Map<String,Collection<String>> toMap( ) {
         return Collections.singletonMap( "key", Collections.singleton( "value" ) );
      }
   };

   @Test
   public void testSimplePolicyCall() {
      final OpaPolicyProvider provider = new OpaPolicyProvider(
            URI.create( OPA_BASE_URL ),
            new File("."));
      final Evaluation evaluation = new DefaultEvaluation(
            new ResourcePermission(null, null, (ResourceServer) null),
            new EvaluationContext() {
               @Override
               public Identity getIdentity( ) {
                  return new Identity() {
                     @Override
                     public String getId( ) {
                        return "identityId";
                     }

                     @Override
                     public Attributes getAttributes( ) {
                        return dummyAttributes;
                     }
                  };
               }

               @Override
               public Attributes getAttributes( ) {
                  return () -> Map.of( "attr1" +
                        "user", List.of("bob"), "action", List.of("update"), "type", List.of("finance"));
//                  return dummyAttributes;
               }
            },
            null,
            new Policy() {
               @Override
               public String getId( ) {
                  return "policyId";
               }

               @Override
               public String getType( ) {
                  return "policyType";
               }

               @Override
               public DecisionStrategy getDecisionStrategy( ) {
                  return DecisionStrategy.AFFIRMATIVE;
               }

               @Override
               public void setDecisionStrategy( DecisionStrategy decisionStrategy ) {

               }

               @Override
               public Logic getLogic( ) {
                  return Logic.POSITIVE;
               }

               @Override
               public void setLogic( Logic logic ) {

               }

               @Override
               public Map<String,String> getConfig( ) {
                  return Collections.singletonMap( "policyPath", "app/rbac/allow" );
               }

               @Override
               public void setConfig( Map<String,String> config ) {

               }

               @Override
               public void removeConfig( String name ) {

               }

               @Override
               public void putConfig( String name, String value ) {

               }

               @Override
               public String getName( ) {
                  return "name";
               }

               @Override
               public void setName( String name ) {

               }

               @Override
               public String getDescription( ) {
                  return "description";
               }

               @Override
               public void setDescription( String description ) {

               }

               @Override
               public ResourceServer getResourceServer( ) {
                  return null;
               }

               @Override
               public Set<Policy> getAssociatedPolicies( ) {
                  return Collections.emptySet();
               }

               @Override
               public Set<Resource> getResources( ) {
                  return Collections.emptySet();
               }

               @Override
               public Set<Scope> getScopes( ) {
                  return Collections.emptySet();
               }

               @Override
               public String getOwner( ) {
                  return "owner";
               }

               @Override
               public void setOwner( String owner ) {

               }

               @Override
               public void addScope( Scope scope ) {

               }

               @Override
               public void removeScope( Scope scope ) {

               }

               @Override
               public void addAssociatedPolicy( Policy associatedPolicy ) {

               }

               @Override
               public void removeAssociatedPolicy( Policy associatedPolicy ) {

               }

               @Override
               public void addResource( Resource resource ) {

               }

               @Override
               public void removeResource( Resource resource ) {

               }
            },
            new DecisionResultCollector() {
               @Override
               public void onDecision( DefaultEvaluation evaluation ) {
                  super.onDecision( evaluation );
                  System.out.println("Decision effect: " + evaluation.getEffect());
               }
            },
            null,
            null );

      provider.evaluate( evaluation );
   }
}
