[![Build Status](https://travis-ci.org/advanced-rest-client/raml-body-editor-panel.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-body-editor-panel)  

# raml-body-editor-panel

`<raml-body-editor-panel>` A body editor panel containin JSON, XML and form editors.

It is meant to work with the RAML spec. However without it, it will work as a CodeMirror editor.

The default view for the editor is source editor (powered by CodeMirror).
User can switch to the form view at any time using editor controls.

### Example
```
<raml-body-editor-panel></raml-body-editor-panel>
```

### Styling
`<raml-body-editor-panel>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--raml-body-editor-panel` | Mixin applied to the element | `{}`

Use `paper-tabs` and `code-mirror` variables to style this elements.



### Events
| Name | Description | Params |
| --- | --- | --- |
| body-value-changed | Fires when the value change. | value **String** - Current editor value |
