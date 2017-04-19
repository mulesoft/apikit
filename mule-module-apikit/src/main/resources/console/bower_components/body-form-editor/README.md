[![Build Status](https://travis-ci.org/advanced-rest-client/body-form-editor.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/body-form-editor)  

# body-form-editor

`<body-form-editor>` A Form data editor for the HTTP body

The element provides varlidation against RAML type it the `type` proeprty is set. Although it will
display form error (invalida data, wrong pattern, type, etc) it will set the value as the form
controls has them right now. So the output bight be not valid in terms of the spec. But the user
will be warned in the UI.

### Example
```
<body-form-editor value="{{value}}" type="[[ramlType]]"></body-form-editor>
```

If the `type` is provided (as a RAML definition type) and it contains the `formParameters` object
then the for will render predefinied fields.

### Styling
The `<body-form-editor>` is consistent with the `<raml-request-parameters-form>` styling.

Custom property | Description | Default
----------------|-------------|----------
`--body-form-editor` | Mixin applied to the element | `{}`
`--raml-request-parameters-form` | The same as above. | `{}`
`--raml-request-parameters-editor-input-label-color` | Color of the paper input's labels | `rgba(0, 0, 0, 0.48)`
`--raml-request-parameters-editor-predefined-label-color` | Color of the predefinied parameter name label | `rgba(0, 0, 0, 0.87)`
`--raml-request-parameters-editor-docs-color` | Color of the documentation string below the input field. Note that it will appy also `marked-element` styles to this element | `rgba(0, 0, 0, 0.87)`
`--raml-request-parameters-editor-predefined-row` | Mixin applied to each predefined by schema row of the form | `{}`
`--raml-request-parameters-editor-user-defined-row` | Mixin applied to each user defined row of the form | `{}`
`--form-label` | Mixin applied to the predefinied parameter name label | `{}`

Additionally when the `narrow` attribute is set then the form will be displayed in the mobile friendly view.

