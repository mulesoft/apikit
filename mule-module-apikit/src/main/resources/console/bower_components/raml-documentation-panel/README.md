[![Build Status](https://travis-ci.org/advanced-rest-client/raml-documentation-panel.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-documentation-panel)  

# raml-documentation-panel

The documentation details panel.
Is shows a documentation panel depending on if the selected object is a
method, resource or the documentation node.

The computation of the selected object should be performed outside the element.
Use the `raml-path-selector` with `raml-path-to-object` to get the data
structure that this element can work with.

### Example
```
<raml-path-to-object selected-object="{{obj}}" ...></raml-path-to-object>
<raml-documentation-panel
  selected-object="[[obj]]"
  selected-parent="[[selectedParent]]"
  path="[[path]]"></raml-documentation-panel>
```
or
```
document.querySelector('raml-documentation-panel').selectedObject = obj;
```

The `path` property is required because the `raml-docs-resource-viewer`
required current path to compute relative paths to sub-resources.


### Styling
`<raml-documentation-panel>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--raml-documentation-panel` | Mixin applied to the element | `{}`
`--raml-docs-main-content` | Mixin applied to the main docs content (where the docs content is displayed). | `{}`
`--raml-docs-main-content-width` | Max width of the documentation panel. Additional space is required for innner panels navigation | `900px`
`--raml-docs-documentation-width` | Max width of the documentation panel. It should be used to avoid usability issues for reading long texts. | `700px`

# raml-documentation-empty-state

The `raml-documentation-empty-state` is an element that shows an empty state
screen when the documentation panel has no selection.

Note: The element is set to use 100% of height. To render it on full page
height all parent elements has to be 100% height.

### Styling
`<raml-documentation-empty-state>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--raml-documentation-empty-state` | Mixin applied to the element | `{}`
`--raml-documentation-empty-state-container` | Mixin applied to the main container where the flex layout is applied | `{}`
`--raml-documentation-empty-state-message` | Mixin applied to the message element. | `{}`
`--raml-documentation-empty-state-background-color` | Background color of the element | `#FAFAFA`
`--raml-documentation-empty-state-icon-color` | Color of the icon (paths that are possible to style) | `#75c9e1`
`--raml-documentation-empty-state-icon-size` | Size of the icon | `256px`
`--raml-documentation-empty-state-message-color` | Color of the message | `#757575`
`--arc-font-body1` | Mixin applied to the message container | `{}`

