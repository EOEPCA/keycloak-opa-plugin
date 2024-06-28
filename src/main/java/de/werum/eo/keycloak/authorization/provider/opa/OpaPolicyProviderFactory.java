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
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Factory for {@link OpaPolicyProvider}s.
 *
 * @author w-scho
 * @version SVN $Revision$ $Date$
 * @since
 */
public class OpaPolicyProviderFactory implements PolicyProviderFactory<OpaPolicyRepresentation> {

   private static final Logger LOGGER = Logger.getLogger( OpaPolicyProviderFactory.class );

   private URI opaBaseUri;
   private File policyConfigDir;

   @Override
   public String getId( ) {
      return "opa";
   }

   @Override
   public String getName( ) {
      return "OPA";
   }

   @Override
   public String getGroup( ) {
      return "Rule Based";
   }

   @Override
   public PolicyProvider create( AuthorizationProvider authorization ) {
      return new OpaPolicyProvider(opaBaseUri, policyConfigDir);
   }

   @Override
   public PolicyProvider create( KeycloakSession session ) {
      return null;
   }

   @Override
   public OpaPolicyRepresentation toRepresentation( Policy policy, AuthorizationProvider authorization ) {
      final OpaPolicyRepresentation representation = new OpaPolicyRepresentation();
      representation.setPolicyPath(policy.getConfig().get( "policyPath" ));
      return representation;
   }

   @Override
   public Class<OpaPolicyRepresentation> getRepresentationType( ) {
      return OpaPolicyRepresentation.class;
   }

   @Override
   public void init( Config.Scope config ) {
      System.out.println( "OPA Policy Provider Config: " + config); // TODO: Remove

      final String baseUriStr = config.get( "opaBaseUri" );
      opaBaseUri = baseUriStr != null ? URI.create(baseUriStr) : null;

      final String policyDirStr = config.get( "opaPolicyDir" );
      policyConfigDir = policyDirStr != null ? new File(policyDirStr) : null;
      System.out.println( "opaBaseUri: " + baseUriStr); // TODO: -> Logger
      System.out.println( "opaPolicyDir: " + policyDirStr);
      System.out.println( "Property names: " + config.getPropertyNames() );
   }

   @Override
   public void postInit( KeycloakSessionFactory factory ) {

   }

   @Override
   public void close( ) {
      // nothing to do
   }

   @Override
   public List<ProviderConfigProperty> getConfigMetadata( ) {
      // see https://www.keycloak.org/docs/latest/server_development/ Chapter 11 Configuration techniques
      return List.of( new ProviderConfigProperty("opaBaseUri", "OPA Base URI",
            "Base URI for OPA rule evalation", ProviderConfigProperty.STRING_TYPE,
                  "http://localhost:8181/v1/data/"),
            new ProviderConfigProperty("opaPolicyDir", "OPA Policy Configuration Directory",
                  "Directory containing configuration files for OPA policies",
                  ProviderConfigProperty.STRING_TYPE, "${jboss.server.config.dir}/opa-policies"));
//      return PolicyProviderFactory.super.getConfigMetadata();
   }
}
