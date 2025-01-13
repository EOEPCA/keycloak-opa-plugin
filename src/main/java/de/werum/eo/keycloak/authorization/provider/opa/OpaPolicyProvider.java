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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.util.JsonSerialization;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A policy provider that delegates policy evaluation to an Open Policy Agent (OPA).
 *
 * @author w-scho
 * @version SVN $Revision$ $Date$
 * @since 0.1
 */
public class OpaPolicyProvider implements PolicyProvider {

   private static final Logger LOGGER = Logger.getLogger( OpaPolicyProvider.class );

   private final URI baseUri;
   private final File policyConfigDir;

   OpaPolicyProvider( URI baseUri, File policyConfigDir ) {
      this.baseUri = baseUri;
      this.policyConfigDir = policyConfigDir;
      LOGGER.trace( "new OpaPolicyProvider (" + baseUri + "," + policyConfigDir + ")" );
   }

   @Override
   public void evaluate( Evaluation evaluation ) {

      // ---- HttpClient variant
      try(final CloseableHttpClient httpClient = HttpClients.createDefault()) {
         // Take true as default for now
         final boolean includePermission = !isFalse( evaluation.getPolicy().getConfig().get( "input.includePermission" ) );
         final boolean includeResource = !isFalse( evaluation.getPolicy().getConfig().get( "input.includeResource" ) );
//         final boolean includePermission = isTrue( evaluation.getPolicy().getConfig().get( "input.includePermission" ) );
//         final boolean includeResource = isTrue( evaluation.getPolicy().getConfig().get( "input.includeResource" ) );

         final Identity identity = evaluation.getContext().getIdentity();
         final ResourcePermission permission = evaluation.getPermission();
         final Resource resource = permission.getResource();
         final JsPermission jsPermission = !includePermission ? null : new JsPermission( resource == null || !includeResource ? null : new JsResource(
               resource.getId(), resource.getName(), resource.getDisplayName(), resource.getOwner(), resource.getType(),
               resource.getUris(), mapResourceServer( resource.getResourceServer() ), mapScopes( resource.getScopes() ),
               resource.isOwnerManagedAccess(), resource.getAttributes() ),
               mapScopes( permission.getScopes() ),
               permission.getClaims(),
               mapResourceServer( permission.getResourceServer() ),
               permission.isGranted() );
         final JsInputDocument inputDoc = new JsInputDocument( evaluation.getContext().getAttributes().toMap(),
               new JsIdentity( identity.getId(), identity.getAttributes().toMap() ), jsPermission );
         final String inputJson = JsonSerialization.writeValueAsPrettyString( Map.of("input", inputDoc) );

         final URI uri = baseUri.resolve( getPolicyPath( evaluation ) );
         LOGGER.trace( "Effective URI: " + uri );
         System.out.println( "Input document: " + inputJson ); // TODO: -> Logger
         final HttpPost httpPost = new HttpPost( uri );
         httpPost.setEntity( new StringEntity( inputJson ) );
         httpPost.addHeader( HttpHeaders.CONTENT_TYPE, "application/json" );
         final CloseableHttpResponse response = httpClient.execute( httpPost );
         if( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ) {
            final JsonNode root = JsonSerialization.prettyMapper.readTree( response.getEntity().getContent() );

            System.out.println("Received: " + JsonSerialization.writeValueAsPrettyString( root ) ); // TODO: -> Logger

            if( root.has( "result" ) ){
               final JsonNode resultNode = root.get( "result" );

               if( resultNode.isBoolean() && resultNode.asBoolean() ) {
                  evaluation.grant();
               }
               // TODO: Maybe add support for further types of results...
            }
            evaluation.denyIfNoEffect();
         } else {
            throw new IOException( "HTTP request to OPA failed: " + response.getStatusLine() );
         }
      } catch( Exception ex ) {
         throw new RuntimeException( "Error evaluating OPA policy [" + evaluation.getPolicy().getName() + "].", ex );
      }
   }

   @Override
   public void close( ) {
      // nothing to do
   }

   private String getPolicyPath( Evaluation evaluation) {
      String result = evaluation.getPolicy().getConfig().get( "policyPath" );

      if (result == null) {
         final String policyName = evaluation.getPolicy().getName();
         if ( policyConfigDir != null ) {
            final Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream( new File(policyConfigDir, policyName + ".properties")  )) {
               p.load( fis );
               result = p.getProperty( "policyPath" );
            } catch( FileNotFoundException ex) {
               // Fallback: Use policy name as policy path
               result = policyName;
            } catch( IOException ex) {
               throw new IllegalStateException( "I/O error " + ex );
            }
         } else {
            result = policyName;
         }
      }

      if ( result == null ) {
         throw new IllegalStateException( "Unable to determine policy path." );
      }

      return result;
   }

   public static class JsInputDocument {
      private final Map<String,Collection<String>> attributes; // orig: context.attributes
      private final JsIdentity identity; // orig: context.identity
      private final JsPermission permission;

      public JsInputDocument( Map<String,Collection<String>> attributes, JsIdentity identity, JsPermission permission ) {
         this.attributes = attributes;
         this.identity = identity;
         this.permission = permission;
      }

      public Map<String,Collection<String>> getAttributes( ) {
         return attributes;
      }

      public JsIdentity getIdentity( ) {
         return identity;
      }

      public JsPermission getPermission( ) {
         return permission;
      }
   }

   public static class JsIdentity {
      private final String id;
      private final Map<String,Collection<String>> attributes;
      // Note: Roles can be accessed via attributes.

      JsIdentity( String id, Map<String,Collection<String>> attributes ) {
         this.id = id;
         this.attributes = attributes;
      }

      public String getId( ) {
         return id;
      }

      public Map<String,Collection<String>> getAttributes( ) {
         return attributes;
      }
   }

   public static class JsPermission {

      private final JsResource resource;
      private List<JsScope> scopes;
      private final Map<String,Set<String>> claims;
      private final JsResourceServer resourceServer;
      private final boolean granted;

      JsPermission( JsResource resource, List<JsScope> scopes, Map<String,Set<String>> claims,
            JsResourceServer resourceServer, boolean granted ) {
         this.resource = resource;
         this.scopes = scopes;
         this.claims = claims;
         this.resourceServer = resourceServer;
         this.granted = granted;
      }

      public JsResource getResource( ) {
         return resource;
      }

      public List<JsScope> getScopes( ) {
         return scopes;
      }

      public Map<String,Set<String>> getClaims( ) {
         return claims;
      }

      public JsResourceServer getResourceServer( ) {
         return resourceServer;
      }

      public boolean isGranted( ) {
         return granted;
      }
   }

   public static class JsResource {

      private final String id;
      private final String name;
      private final String displayName;
      private final String owner;
      private final String type;
      private final Set<String> uris;
      private final JsResourceServer resourceServer;
      private final List<JsScope> scopes;
      private final boolean ownerManaged;
      private final Map<String,List<String>> attributes;

      JsResource( String id, String name, String displayName, String owner, String type, Set<String> uris,
            JsResourceServer resourceServer, List<JsScope> scopes, boolean ownerManaged, Map<String,List<String>> attributes ) {
         this.id = id;
         this.name = name;
         this.displayName = displayName;
         this.owner = owner;
         this.type = type;
         this.uris = uris;
         this.resourceServer = resourceServer;
         this.scopes = scopes;
         this.ownerManaged = ownerManaged;
         this.attributes = attributes;
      }

      public String getId( ) {
         return id;
      }

      public String getName( ) {
         return name;
      }

      public String getDisplayName( ) {
         return displayName;
      }

      public String getOwner( ) {
         return owner;
      }

      public String getType( ) {
         return type;
      }

      public Set<String> getUris( ) {
         return uris;
      }

      public JsResourceServer getResourceServer( ) {
         return resourceServer;
      }

      public List<JsScope> getScopes( ) {
         return scopes;
      }

      public boolean isOwnerManaged( ) {
         return ownerManaged;
      }

      public Map<String,List<String>> getAttributes( ) {
         return attributes;
      }
   }

   public static class JsResourceServer {
      private final String id;
      private final String clientId;
      private final boolean allowRemoteResourceManagement;
      // decisionStrategy and policyEnforcementMode omitted

      JsResourceServer( String id, String clientId, boolean allowRemoteResourceManagement ) {
         this.id = id;
         this.clientId = clientId;
         this.allowRemoteResourceManagement = allowRemoteResourceManagement;
      }

      public String getId( ) {
         return id;
      }

      public String getClientId( ) {
         return clientId;
      }

      public boolean isAllowRemoteResourceManagement( ) {
         return allowRemoteResourceManagement;
      }
   }

   public static class JsScope {
      private final String id;
      private final String name;
      private final String displayName;
      private final String iconUri;
      private final JsResourceServer resourceServer;

      JsScope( String id, String name, String displayName, String iconUri, JsResourceServer resourceServer ) {
         this.id = id;
         this.name = name;
         this.displayName = displayName;
         this.iconUri = iconUri;
         this.resourceServer = resourceServer;
      }

      public String getId( ) {
         return id;
      }

      public String getName( ) {
         return name;
      }

      public String getDisplayName( ) {
         return displayName;
      }

      public String getIconUri( ) {
         return iconUri;
      }

      public JsResourceServer getResourceServer( ) {
         return resourceServer;
      }
   }

   private static JsResourceServer mapResourceServer( ResourceServer resourceServer ) {
      if( resourceServer == null ) {
         return null;
      } else {
         return new JsResourceServer( resourceServer.getId(), resourceServer.getClientId(), resourceServer.isAllowRemoteResourceManagement() );
      }
   }

   private static List<JsScope> mapScopes( Collection<Scope> scopes ) {
      if( scopes == null ) {
         return null;
      } else {
         return scopes.stream()
               .map( s -> new JsScope( s.getId(), s.getName(), s.getDisplayName(), s.getIconUri(), mapResourceServer( s.getResourceServer() ) ) )
               .collect( Collectors.toList() );
      }
   }

   private static boolean isTrue( Object value ) {
      if( value == null )
         return false;
      if( value instanceof Boolean )
         return (( Boolean ) value);
      else
         return value.toString().equalsIgnoreCase( "true" );
   }

   private static boolean isFalse( Object value ) {
      if( value == null )
         return false;
      if( value instanceof Boolean )
         return !(( Boolean ) value);
      else
         return value.toString().equalsIgnoreCase( "false" );
   }
}
