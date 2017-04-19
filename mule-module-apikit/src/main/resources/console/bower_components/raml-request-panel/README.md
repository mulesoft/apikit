[![Build Status](https://travis-ci.org/advanced-rest-client/raml-request-panel.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-request-panel)  

# raml-request-panel

The request panel view for the request defined as a RAML method.
It is a main view element for the API console to display the request panel related to the RAML
specification.

The element has it's own XHR / Fetch transport method and it will be used if the hostng application
do not handle the `api-console-request` event.

When the user request to make the HTTP request then cancellable `api-console-request` event will
be fired with the request details (see below).
The hosting application, if it about to use different transport method, should cancel the event by
calling `preventDefault()` function on the event (and possibly `stopPropagation()`) and handle the
request. If the event was not prevented (canceled) then internall Fetch/XHR will be used.

When the request is ready then the hosting app must fire the `api-console-response` event with
created Request and Response objects. This element listens on the `window` property for the
`api-console-response` event.

## Events
### api-console-request
This event is fired when the user request to make a HTTP request.
This event will have the following properties set on the `detail` object:

Property | Type | Description
----------------|-------------|----------
`url` | String | The request URL
`method` | String | The HTTP method
`headers` | String | Headers to send
`payload` | String | Payload to send
`auth` | Object | Optional. For some authorization methodss (like NTLM) the authorization header or query param can't be set and the authorization must be made on the connection. In this cases the auth object will be set with `type` and `settings` properties. While `type` is the name of the authorization method, the `settings` object depends on the authorization method and may vary. Detailed documentation for the auth methods is in the `auth-methods` element.

### api-console-response
This event must be fired when the hosring app finish the request. It must contains generated Request
and Response object according to the [Fetch specification](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).

Becaue the Fetch API is a new API not all browsers support it. In this case the polyfill must be used
in the hosted app. Add the `fetch-polyfill` element (advanced-rest-client/fetch-polyfill) to the
hosted app to have the support. It is recommended to use this element so the polyfill will be loaded
only once. See the `raml-request-panel-simple-xhr` element for example implementation.

Property | Type | Description
----------------|-------------|----------
`request` | Object | The request object as defined in the Fetch API spec.
`response` | Object | The response object as defined in the Fetch API spec.
`isXhr` | Boolean | If not set the element assumes it's true. Indicated if the transport method doesn't support advanced timings and redirects information. See below.
`error` | Error | When the request / response is errored (`request.ok` equals `false`) then the error object should be set with the human readable message that will be displayed to the user.
`loadingTime` | Number | The response full loading time

See `Advanced transport options` for more event options.

### Example
```
<raml-request-panel
  method="[[ramlMethod]]"
  redirect-url="http://oauth.redirect.url"
  request="{{request}}"
  response="{{response}}"
  response-error="{{responseError}}"
  loading-time="{{loadingTime}}"></raml-request-panel>
```

## Advanced transport options
The response panel in the ARC elements is able to display the response in simple and advanced view.

Simple is meant to be used when the HTTP request has been made by the simple transports like XHR or
Fetch. It just displays the response status, headers and paylaod.

Advanced view is reserved for transport methods that are able to generate additional informations
about the request and resposne. This information is timings for the request/response, timings for
the redirects and information about redirects.

When the advanced options are set then the `isXhr` flag on the `api-console-response` event's detail
object must be set to true.

#### timings
The `timings` propery added to the `api-console-response` is the request / response timings as
defined in HAR 1.2 spec. For example:
```
"timings": {
  "blocked": 0,
  "dns": -1,
  "connect": 15,
  "send": 20,
  "wait": 38,
  "receive": 12,
  "ssl": -1,
  "comment": ""
}
```
If the `timings` property is set the `loadingTime` property is optional since it will be calculated
from the detailed timing.

#### redirectTimings
The `redirectTimings` propery added to the `api-console-response` is the list of the `timings`
objects as defined in HAR 1.2 specification.
The list should be ordered list of redirections. For example:

```
"redirect-timings": [{
  "blocked": 0,
  "dns": -1,
  "connect": 15,
  "send": 20,
  "wait": 38,
  "receive": 12,
  "ssl": -1,
  "comment": ""
}]
```

#### redirects
The `redirects` property added to the `api-console-response` event is the list of objects. Each
object should have the `headers` property as a HTTP headers string, `status` as a HTTP status
and optionally `statusText`. It is consisted with the `Response` object except the headers are
String instead of the Headers object.

```
"redirects": [Response {
  "status": 301,
  "statusText": "Moved Permanently",
  "headers": "Content-lenght: 0"
}]
```

#### sourceMessage
The HTTP source message sent to the server. It should be full message from the message header to
the request body.

### Advanced event example
```
var event = new CustomEvent('api-console-response', {
  cancelable: true,
  bubbles: true,
  composed: true,
  detail: {
    isXhr: true,
    request: request,
    response: response,
    error: new Error('Dummy error'), // Has the response details so it shouldn't be set.
    loadingTime: 125, // This is optional because timings is set
    timings: { dns: 123, ... }
    redirectTimings: [{ dns: 123, ... }],
    redirects: [redirectResponse1, ...],
    sourceMessage: 'HTTP/1.1 200 OK\n ....'
  }
});
document.body.dispatchEvent(event);
```

### Styling
`<raml-request-panel>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--raml-request-panel` | Mixin applied to the element | `{}`
`--raml-request-panel-panel-border-color` | Border color of each block in the tabs | `rgba(0, 0, 0, 0.24)`
`--raml-request-panel-container` | Mixin applied to the main content container | `{}`
`--raml-request-panel-container-narrow` | Mixin applied to the main content container when layout is narrow | `{}`
`--action-button` | Mixin applied ot the action button | `{}`
`--primary-color` | background-color of the main action button | `--primary-color`
`--primary-action-color` | Color of the main action button | `--primary-action-color`

You can set the `narrow` property so the element will be rendered in the mobile view.
This property will be propagated to all sub-elements that uses this property to change layout.



### Events
| Name | Description | Params |
| --- | --- | --- |
| api-console-response-ready | Fired when the response has been recorded and request, response, redirects and timings data are set. | isXhr **Boolean** - True if the transport method is a basic transport. |
response **Response** - The response object |
responseError **Error** - Error object if the response is errored |
request **Request** - The request object. |
loadingTime **Number** - Request loading time |
timings **Object** - As defined in HAR 1.2 timnings object |
redirectTimings **Array.<Object>** - List of redirect timings |
redirects **Array** - List of redirect Responses |
sourceMessage **String** - Source HTTP message sent to the server. |
# raml-request-panel-simple-xhr


The `raml-request-panel-simple-xhr` is an element that executes the request if the hosted app do
not handle the `api-console-request` event.

It can be used as a boilerplate for communication with the `raml-request-panel` element to handle
request event, execute the request and return the result.

The `raml-request-panel` is based on the fetch's API Request and Response object. The returning
event transport them to the source element.
Because the Response object is not intended to carry a custom errors, in case of error, the
Response object only carrying nformation `response.ok` equals `false` and the `error` Error
object is added to the event's detail object.

This element don't listen for the `api-console-request` event giving a chance for the hosted app to
handle it. If the hosted app intend to handle the requests it should listen for an event:

```
window.addEventListener('api-console-request', function(e) {
  var data = e.detail;
  console.log(data.url);
  console.log(data.method);
  console.log(data.payload);
  console.log(data.headers);
  console.log(data.auth); // optional, see `raml-request-panel.serializeRequest()`
});

```

After the request is made (successfully or not) the `api-console-response` event must be fired.
It should have `request` and `response` objects added to the event's `detail` object. If the request
is errored the it must contain additional `error` proeprty which is JS' Error object with the human
readable message (it will be displayed as an error message).
Additionaly the detail object can contain the `isXhr` property which is set to true be default.
It should be set to false if the request has been made by more than XHR advanced transport which
dives information about timings and redirects.

See the `response-panel` for more information.

```
var event = new CustomEvent('api-console-request', {
  cancelable: false,
  bubbles: true,
  composed: true,
  detail: {
    request: new Request(...),
    response: new Response(body, {status: 200})
  }
});
this.dispatchEvent(event);
```

Becaue the Fetch API is a new API not all browsers support it. Therefore the polyfill must be used
in the hosted app. Add the `fetch-polyfill` element (advanced-rest-client/fetch-polyfill) to the
hosted app to have it supported.

Because this element fires back an event it has to be attached to the DOM.

