# Simple example OPA policy rules

package example

# Always-deny rule
default allow_none = false

# Always-permit rule
default allow_all = true

# Example rule: Permit if email address has been verified
#default email_verified = false
#
#email_verified if {
##    some i
#    input.identity.email_verified[_] = "true"
#}

# Example rule: Permit if user is in the list of privileged users
#default privileged_user = false
#
#privileged_user if {
#    example_data.privileged_users[_] = input.identity.attributes.preferred_username
#}
