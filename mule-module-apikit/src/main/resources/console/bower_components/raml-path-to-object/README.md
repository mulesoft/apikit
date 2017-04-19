[![Build Status](https://travis-ci.org/advanced-rest-client/raml-path-to-object.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-path-to-object)  

# raml-path-to-object

The `<raml-path-to-object>` is a helper element that can work with the
`raml-js-parser` and `raml-path-selector` to compute additional properties
alongside the raml object.

It will compute wherther selected `path` (from the `raml-path-selector`) is
method, resource or documentation.

If the `auto` property is set then it will set a listeners for the
`raml-selected-path-changed` event and fire back one of the events supported by
this element as descrived below in the event section.

The most basic task for this element is to transform the `path` into a RAML
object reporesented by the path. When the path change then the `selectedObject`
will contain a JS object that corresponds to the path.

### Usage
```
<raml-path-selector raml="[[raml]]" selected-path="{{selectedPath}}"></raml-path-selector>
<raml-path-to-object
  raml="[[raml]]"
  selected-path="{{selectedPath}}"
  selected-object="{{selectedObject}}"
  is-method="{{isMethod}}"
  is-resource="{{isResource}}"
  is-documentation="{{isDocumentation}}"></raml-path-to-object>
```

### The path
Pa§
```
resources.1.methods.0
```
means that the path to the value is through resources array, 2nd element, then methods array
first element.

