[![Build Status](https://travis-ci.org/advanced-rest-client/raml-request-headers-editor.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-request-headers-editor)  

# raml-request-headers-editor

`<raml-request-headers-editor>` A headers editor to be used with the RAML defined requests

### Example
```
<raml-request-headers-editor></raml-request-headers-editor>
```

## Events
This element listens for the `request-headers-changed` and `request-header-changed` events.
The first event when handled will replace current editor value. New value mast be set to a `value`
property of the event's detail object.
Second event, when handled, will update single header value if the header is on the list of append
header if it is a new header. Event handler expects `name` and `value` properties to be set on event's
detail object.

### `request-headers-changed` example
```
var init = {
  detail: {
    value: 'Authorization: Basic base64 string'
  },
  bubbles: true,
  cancelable: true
};
var event = new CustomEvent('request-headers-changed', init);
document.dispatchEvent(event);
```
This event will set editor value to:
```
Authorization: Basic base64 string
```

### `request-header-changed` example
```
var init = {
  detail: {
    name: 'Authorization',
    value: 'custom'
  },
  bubbles: true,
  cancelable: true
};
var event = new CustomEvent('request-header-changed', init);
document.dispatchEvent(event);
```
This event will update the Authorization header value to `custom`.

If the event was canceled by calling `event.preventDefault()` then the value won't be updated.

### Styling
`<raml-request-headers-editor>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--raml-request-headers-editor` | Mixin applied to the element | `{}`



### Events
| Name | Description | Params |
| --- | --- | --- |
| content-type-changed | Fired when the content type header has been set / updated. | value **String** - New Content type. |
| request-headers-changed | Fired when the editor value change | value **String** - Current editor value. |
