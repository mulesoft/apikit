[![Build Status](https://travis-ci.org/advanced-rest-client/api-console-ext-comm.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/api-console-ext-comm)  

# api-console-ext-comm

`<api-console-ext-comm>` is an element that support communication with the api-console-extension.

If the extension is installed then it will intercept the `api-console-request` and cancel it.
Data from the event will be passed to the extension and the request will be executed from within
the extension.



### Events
| Name | Description | Params |
| --- | --- | --- |
| api-console-extension-installed | Called when the API console extension has been detected, | __none__ |
