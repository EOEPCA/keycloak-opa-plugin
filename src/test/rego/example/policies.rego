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

package example

# import rego.v1 - TODO: Upgrade rules to V1 syntax

# Always-deny rule
default allow_none = false

# Always-permit rule
default allow_all = true

# Example rule: Permit if email address has been verified
default email_verified = false

email_verified {
    input.identity.attributes.email_verified[_] = "true"
}

# Example rule: Permit if user is in the list of privileged users
default privileged_user = false

privileged_user {
    data.src.test.rego.example.privileged_users[_] = input.identity.attributes.preferred_username[_]
}
