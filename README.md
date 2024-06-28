# Keycloak OPA Plugin

**Note**: This plugin is work in progress in an early state.
It is still far from being fit for operational use.

## Purpose

The Keycloak OPA plugin is an adapter that implements Keycloak's
internal Policy Provider SPI. It allows creating authorization policies
that delegate policy evaluation to an Open Policy Agent (OPA).

## Current Status

### Existing capabilities

The plugin can be integrated into Keycloak as a Policy SPI.
It is possible to create OPA-related policies via the Keycloak Admin UI.
However, this is currently simply reusing the JS policy form, which only
allows entering a name and a description, but no OPA-specific attributes.

As a preliminary solution, by default, the name of the policy is used as
the path to the policy document in OPA (relative to the OPA base URI).
Alternatively, the path can be configured in a property file. See section
"Policy configuration" below for details.

Furthermore, the input document currently contains a mostly fixed set of
information, which includes all attributes of the affected user and some
general information. However, meanwhile it is also possible to optionally
add permission and resource metadata for individual policies.

As the result of a policy evaluation, the adapter currently only accepts a
simple boolean value that denotes whether the policy evaluation resulted in
a grant (true) or a deny (false).

The plugin does not send any authentication to OPA, so that only public
policies can be accessed. HTTPS may work, but has not been tested yet.

### Missing features

The following features need to be added in order to make the plugin usable
operationally:

* Authentication of Keycloak against OPA
* Encryption (HTTPS/ TLS)
* Full configurability of policies via Keycloak Admin UI 

### Possible future enhancements

The following features are just ideas for now. They may be added if required:

* Support for non-boolean OPA responses 
* Further policy-specific tailoring of input document

### Caveats

The Policy SPI of Keycloak is an internal SPI. This implies that the plugin
may not work with future versions of Keycloak out of the box.

## Configuration

### Global properties (`keycloak.conf`)

The following global properties are currently supported. Like other Keycloak
properties, they can be configured in `keycloak.conf` or provided via the
command line or as environment variables.

* `spi-policy-opa-opa-base-uri`: Base URI for OPA (e.g. http://opa-host:8181/v1/data/)
* `spi-policy-opa-opa-policy-dir`: Directory where the plugin looks for policy 
  configuration files (typically `${kc.home.dir}/conf/opa-policies`)

### Policy configuration

Policies can be created via the Keycloak Admin UI. However, as already stated above,
there is currently no specific UI for creating OPA-related policies. By default,
the plugin interprets the policy name as a subpath to append to the OPA base URI.
This allows creating policy rules that refer to certain OPA rules in an easy way,
but forces the policy names to follow a certain convention.

As an alternative, it is possible to place a configuration file in the configured
OPA policy directory. The base name of this file must match the policy name, and
its extension must be `.properties`. The file may contain the following properties:

* `policyPath`: Subpath (e.g. `my_package/my_policy_rule`) to append to the OPA
  base URI
* `input.includePermission`: If this is set to `true`, the plugin adds
  a section with permission-related information to the input document.
* `input.includeResource`: If this is set to `true`, the plugin adds
  resource-related information to the permission section of the input
  document. This implies that `input.includePermission` must also be set to
  `true`.
