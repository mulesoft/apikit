[![Build Status](https://travis-ci.org/advanced-rest-client/arc-demo-helpers.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/arc-demo-helpers)  

# arc-demo-snippet

The `<arc-demo-snippet>` displays a parsed code of the code sample and renders the element
initializing Polymer's data binding.
It similar to use Google's `<demo-snippet>` but you don't need to add `is="dom-bind"` to the
template and you can set initial values on the `arc-demo-snippet` element itself.

## Example
```
<arc-demo-snippet initialValue="5" selected="thisElement">
  <template>
    <some-element init="{{initialValue}}"></some-element>
    <other-element selected="[[selected]]"></other-element>
  </template>
</arc-demo-snippet>
```
This is equivalent to:
```
<demo-snippet>
  <template is="dom-bind">
    <some-element init="{{initialValue}}"></some-element>
    <other-element selected="[[selected]]"></other-element>
    <script>
      (function(app) {
        app.initialValue = 0;
        app.selected = 'thisElement';
      })(document.querySelector('template[is="dom-bind"]'));
    </script>
  </template>
</demo-snippet>
```
But it's just less work to do it.

Inside the template you can use usual data binding.

# raml-demo-parser

# `<raml-docs-parser>`
The `<raml-docs-parser>` is an element that is intended to use in demo pages as a
set of RAML parser, RAML entry lookup and produces JSON output that is used by
other elements.

## Usage
```
<raml-docs-parser raml="{{raml}}"></raml-docs-parser>
```
The `raml` property will contain a JSON output from the parser. Also `raml-ready` event will
be fired with the `raml` property on the detail object.

# raml-demo-page

A full screen RAML elements demo.
It has RAML parser, data processor and basic UI for displaying the demo page.

## Usage
The element accepts a `h1` as a page title placed in the header element and
any element which has the `main` attribute.

To extend the list of APIs (the dropdown) add a `paper-item` child node with
the `data-url` attribute set to the URL of the RAML file. You can add as much
children as you want.

### Example
```
<raml-demo-page selected-object="{{selected}}" is-resource="{{isResource}}">
  <h1>My demo page</h1>
  <div main>
    <my-element hidden$="[[!isResource]]" raml="[[selected]]"></my-element>
  </div>
  <paper-item data-url="https://raw.githubusercontent.com/api.raml">My API</paper-item>
</raml-demo-page>
```

## Raml aware
It uses the `raml-aware` element with scope set to `raml`.

