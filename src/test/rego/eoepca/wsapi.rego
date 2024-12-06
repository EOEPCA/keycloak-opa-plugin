# Copyright 2024 Werum Software & Systems AG (Germany)
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.

# Simple example OPA policy rules

package eoepca.wsapi

import rego.v1
import input.request

# TODO: Maybe move generic parts to a utility module (maybe called "iam") if possible
jwks_request(url) := http.send({
    "url": url,
    "method": "GET",
    "force_cache": true,
    "force_cache_duration_seconds": 3600 # Cache response for an hour
})
jwks := jwks_request("http://iam-keycloak/realms/eoepca/protocol/openid-connect/certs").raw_body

# Claims from JWT if JWT is present and can be verified; null otherwise
default verified_claims = null
verified_claims := claims if {
    [type, token] := split(request.headers.Authorization, " ")
    type == "Bearer"
    io.jwt.verify_rs256(token, jwks) == true
    claims := io.jwt.decode(token)[1]
}

default allow = false

allow if {
    claims := verified_claims
    claims != null
    path := split(request.path, "/")
    "" == path[0]
    "workspaces" == path[1]
    wsName := path[2]
    "ws_access" in claims.resource_access[wsName].roles
    print("[wsapi policy] Path: ", request.path, " -> ", path)
    print("[wsapi policy] Method: ", request.method)
    print("[wsapi policy] Claims: ", claims)
}
