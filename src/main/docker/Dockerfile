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

# Dockerfile to create a Keycloak container with the plugin already installed

FROM docker.io/bitnami/keycloak:latest

# Keycloak OPA adapter
COPY build/libs/keycloak-opa-plugin-*.jar /opt/bitnami/keycloak/providers

RUN mkdir /opt/bitnami/keycloak/conf/opa-policies

RUN /opt/bitnami/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/bitnami/scripts/keycloak/entrypoint.sh"]
CMD [ "/opt/bitnami/scripts/keycloak/run.sh" ]
