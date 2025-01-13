# Keycloak OPA Plugin

**Note**: Development of this plugin is still in progress.
The current version already works, but still has some rough edges.

## Purpose

The Keycloak OPA plugin is an adapter that implements Keycloak's
internal Policy Provider SPI. It allows creating authorization policies
that delegate policy evaluation to an Open Policy Agent (OPA).

## Current Status

### Existing capabilities

The plugin can be integrated into Keycloak as a Policy SPI.
It is possible to create and edit OPA-related policies via the
Keycloak Admin UI.

The input document currently contains a mostly fixed set of
information, which includes all attributes of the affected user and some
general information. However, it is also possible to optionally
add permission and resource metadata for individual policies. See section
"Policy configuration" below for details.

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

### Possible future enhancements

The following features are just ideas for now. They may be added if required:

* Support for non-boolean OPA responses 
* Streamlining of input document tailoring
* Further policy-specific tailoring of input document

### Caveats

The Policy SPI of Keycloak is an internal SPI. This implies that the plugin
may not work with future versions of Keycloak out of the box.

The Theme SPI used to add a custom UI for OPA policies also depends on
the Keycloak version. The current Admin UI extension only works with
Keycloak 24. An update for Keycloak 26 is planned.

## Configuration

### Global properties (`keycloak.conf`)

The following global properties are currently supported. Like other Keycloak
properties, they can be configured in `keycloak.conf` or provided via the
command line or as environment variables.

* `spi-policy-opa-opa-base-uri`: Base URI for OPA (e.g. http://opa-host:8181/v1/data/)
* `spi-policy-opa-opa-policy-dir`: Directory where the plugin looks for policy 
  configuration files (typically `${kc.home.dir}/conf/opa-policies`)

### Policy configuration

Policies can meanwhile be created and configured via the Keycloak
Admin UI. It is possible to configure all the properties listed below.

Additionally, it is still possible (but not recommended any more) to place
a configuration file in the configured OPA policy directory. The base name
of this file must match the policy name, and its extension must be
`.properties`. The file may contain the following properties:

* `policyPath`: Subpath (e.g. `my_package/my_policy_rule`) to append to the OPA
  base URI
* `input.includePermission`: If this is not set to `false`, the plugin adds
  a section with permission-related information to the input document.
* `input.includeResource`: If this is not set to `false`, the plugin adds
  resource-related information to the permission section of the input
  document. This implies that `input.includePermission` must also be set to
  `true`.

Note that this way of configuring policies was a preliminary solution and
is meanwhile considered deprecated. It will be removed in a future release.
