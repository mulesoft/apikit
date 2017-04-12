[![Build Status](https://travis-ci.org/advanced-rest-client/raml-json-enhance.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/raml-json-enhance)  

# raml-json-enhance

The `<raml-json-enhance>` enhaces the JSON output from the RAML parser so it can
be used in the ARC elements (which expect enhanced data structure).

The work is asynchronous. After the `json` property is set/changed it will call the
worker automatically and report the result via the `raml-json-enhance-ready` event.

Polymer application can bind to the `result` property which will notify the change.

### Example
```
<raml-json-enhance json="{...}"></raml-json-enhance>
window.addEventListener('raml-json-enhance-ready', function(e) {
  console.log(e.detail.json);
  // equals to
  console.log(e.target.json);
});
```

*Note** This element MUST be used to enhance parser JSON output in order to use
any RAML related ARC element. Enhancer creates common data structure and
expands RAML types. Element expects the JSON object to contain complete data
about method / endpoint / type / security scheme and so on. It will not look for
the data in the root of raml definition. Thanks to this, you can push just a part
of the JSON object to a specific element to make it work.

For example the `<raml-docs-method-viewer>` viewer ecpects the `raml` propety to
be a method definition only (without of the rest of the RAML structure). If
enhanced with the library, the JSON object describing the method will contain
all required information to render the view.

The element contains a set of Polyfills so it will work in the IE11+ browsers.

### Biuld process
This element uses web workers to expand JSON result (normalize it).
The element will attempty to load following scitps from the same location where
this script resides:
- polyfills.js
- browser/index.js
- raml2object.js
Build scripts should ensure that this resources are included in the final build.

