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

# Tie to revision 24.0.5 for now in order to avoid having different Keycloak versions on different platforms.
# Note: Image -r8 does not seem to work with Helm chart 21.4.4, so use -r0 for now.
#FROM docker.io/bitnami/keycloak:24.0.5-debian-12-r0
FROM docker.io/bitnamilegacy/keycloak:26.1.3-debian-12-r0
#FROM docker.io/bitnami/keycloak:latest

ARG PLUGIN_VERSION="*"
ARG ADMIN_UI_VERSION="26.1.3"

# Keycloak OPA adapter
COPY build/libs/keycloak-opa-plugin-${PLUGIN_VERSION}.jar /opt/bitnami/keycloak/providers
#COPY build/libs/keycloak-opa-plugin-*.jar /opt/bitnami/keycloak/providers

# Copy Keycloak Admin UI Theme into Container
COPY src/main/docker/keycloak-admin-ui-${ADMIN_UI_VERSION}-OPA.jar /opt/bitnami/keycloak/providers

RUN mkdir /opt/bitnami/keycloak/conf/opa-policies

RUN /opt/bitnami/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/bitnami/scripts/keycloak/entrypoint.sh"]
CMD [ "/opt/bitnami/scripts/keycloak/run.sh" ]
