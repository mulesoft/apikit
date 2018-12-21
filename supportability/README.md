# APIKit Supportability Document

## Table of Contents
- [Overview](#overview)
- [Documentation link](#documentation-link)
- [Code repositories](#code-repositories)
- [Team](#team)
- [Slack Channels](#slack-channels)
- [Jira Projects](#jira-projects)
- [Troubleshooting](#troubleshooting)
- [Compatibility Matrix](#compatibility-matrix)
- [Limitations](#limitations)

## Overview
APIKit is a toolkit that facilitates REST development. 
APIKit features include the ability to take a REST API designed in RAML, 
automatically generate backend implementation flows for it, and then run and test the API with a pre-packaged console.

See http://raml.org for more information about RAML.

## Documentation link
* **APIkit Overview:** [link](https://mule4-docs.mulesoft.com/apikit/)
* **What's new in APIkit:** [link](https://mule4-docs.mulesoft.com/apikit/apikit-whats-new)
* **Release Notes:** [link](https://mule4-docs.mulesoft.com/release-notes/apikit-4.0-release-notes)
* **APIkit XML Reference:** [link](https://mule4-docs.mulesoft.com/apikit/apikit-reference#apikit-dependency-information-beta)
* **APIkit Error Handling Reference:** [link](https://mule4-docs.mulesoft.com/apikit/apikit-basic-anatomy)


## Code repositories
* [APIkit repository](https://github.com/mulesoft/apikit)
* APIkit for SOAP Repositories: 
  * [Runtime Extension](https://github.com/mulesoft/soapkit)
  * [Studio Plugin](https://github.com/mulesoft/mule-tooling-soapkit)
* APIkit for OData Component Repositories: 
  * [Runtime Extension](https://github.com/mulesoft/apikit-odata-service)
  * [Runtime Extension Tests](https://github.com/mulesoft/apikit-odata-service-test)
  * [Studio Plugin](https://github.com/mulesoft/mule-tooling-odata)
* [Java RAML parser](https://github.com/raml-org/raml-java-parser.git). Master branch is for RAML 1.0, 
v1 branch is for RAML 0.8 but only for the community


## Team
* **PM:** Badi Azad: [badi.azad@mulesoft.com](mailto:badi.azad@mulesoft.com)
* **Engineering Manager:** Raúl Tardá: [raul.tarda@mulesoft.com](mailto:raul.tarda@mulesoft.com)
* **QA:** Nicolas Grossi: [nicolas.grossi@mulesoft.com](mailto:nicolas.grossi@mulesoft.com)
* **Developers:**  
  * Alejandro Amura: [alejandro.amura@mulesoft.com](mailto:alejandro.amura@mulesoft.com)
  * Santiago Fuentes: [santiago.fuentes@mulesoft.com](mailto:santiago.fuentes@mulesoft.com)
  * Diego Macias: [diego.macias@mulesoft.com](mailto:diego.macias@mulesoft.com)  
  * Diego Larralde: [diego.larralde@mulesoft.com](mailto:diego.larralde@mulesoft.com)

* **Team mailing account:** [apikit@mulesoft.com](mailto:apikit@mulesoft.com)

## Slack Channels
* APIkit : [#apikit](https://mulesoft.slack.com/archives/apikit)
* APIkit Support : [#apikit-support](https://mulesoft.slack.com/archives/apikit-support)
* RAML : [#raml](https://mulesoft.slack.com/archives/raml)

### Jira Projects
* APIKIT [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20APIKIT%20AND%20resolution%20%3D%20Unresolved)
* SOAPKIT [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20SOAPKIT%20AND%20resolution%20%3D%20Unresolved)
* ODATA [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20ODATA%20AND%20resolution%20%3D%20Unresolved)
* RAML Parser [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20RP%20AND%20resolution%20%3D%20Unresolved)
* Support Escalations [Link](https://www.mulesoft.org/jira/issues/?filter=19636)

## Troubleshooting
### Using Studio
When running a Mule project, in the Console tab you will see these lines related to APIkit: 
~~~
[WrapperListener_start_runner] org.mule.runtime.module.extension.internal.manager.DefaultExtensionManager: Registering extension APIKit (version: 1.0.0-SNAPSHOT vendor: Mulesoft )
...
...
org.mule.module.apikit.Console: 
********************************************************************************
* APIKit ConsoleURL : http://localhost:8081/console/                           *
********************************************************************************
...
...
org.mule.runtime.module.deployment.impl.internal.application.DefaultMuleApplication: 
**********************************************************************
* Started app 'xxxxx'                                                *
* Application plugins:                                               *
*  - HTTP                                                            *
*  - APIKit                                                          *
*  - Sockets                                                         *
* Application libraries:                                             *
*  - xxxxx                                                           *
**********************************************************************
~~~

### Using Mule Runtime
When using APIkit with a Mule runtime, in the logs you may find these lines: 
~~~
[WrapperListener_start_runner] org.mule.runtime.module.deployment.impl.internal.application.DefaultMuleApplication:
**********************************************************************
* Started app 'xxxx'                                                 *
* Application plugins:                                               *
*  - HTTP                                                            *
*  - APIKit                                                          *
*  - File                                                            *
*  - Sockets                                                         *
*  - File Common Plugin                                              *
* Application libraries:                                             *
*  - xxxx                                                            *
**********************************************************************
~~~


## Compatibility Matrix
APIkit v4.0.0 (EA) is compatible with:
* Mule Runtime 4.0.0
* Anypoint Studio 7.0.0
* Java Parser RAML 1.0 v1.0.16
* Java Parser RAML 0.8 v0.8.19
* API Console 4.0.0

## Limitations
This release does not support the following:
* Inbound endpoints
* APIkit for SOAP
* OData

Post requests using form parameters are supported, but not validated.
