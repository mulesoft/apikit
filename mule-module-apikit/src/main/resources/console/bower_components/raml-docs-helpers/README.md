# raml-docs-parser

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

