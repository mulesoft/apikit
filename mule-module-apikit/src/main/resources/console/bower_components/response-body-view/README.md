[![Build Status](https://travis-ci.org/advanced-rest-client/response-body-view.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/response-body-view)  

# response-body-view

`<response-body-view>` An element to display a HTTP response body.

The element will try to select best view for given `contentType`. It will
choose the JSON viewer for JSON response and XML viewer for XML responses.
Otherise it will display a syntax hagligter.

Note that the `contentType` property **must** be set in order to display any
view.

### Save content to file
The element will use web way of file saving. However, it will send the
`save-content-to-file` first to check if hosting application implements native
save functionality. See event description for more info.

### Example
```
<response-body-view
  response-text="I am the resposne"
  content-type="text/plain"></response-body-view>
```

### Styling
`<response-body-view>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--response-body-view` | Mixin applied to the element | `{}`



### Events
| Name | Description | Params |
| --- | --- | --- |
| save-content-to-file | Fired when the user click on save to file button.  Application can handle this event if it has a way to natively implement save to file functionality. In this case this event must be canceled by calling `preventDefault()` on it. If the event is not canceled then save to file dialog will appear with regular download link. | content **String** - A text to save in file. |
