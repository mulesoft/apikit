# The API Console element

## Preview and development

1. Clone the element:
```
git clone https://github.com/advanced-rest-client/api-console.git
cd api-console
```

2. Checkout develop version
```
git checkout develop
```

3. Install [polymer-cli](https://www.polymer-project.org/1.0/docs/tools/polymer-cli) and Bower
```
sudo npm install -g bower polymer-cli
```

4. Install dependencies
```
bower install
```

5. Serve the element
```
polymer serve --open -p 8080
```

Default page is the element documentation. Switch to demo to see working example.

You can also append the `/demo/` to the URL to swith to demp permanently.

## Documentation
The API console element to be used as a Web Component.

To use the element import it to your project using Bower:
```
bower install --save advanced-rest-client/api-console
```

Before importing the element you should add polyfill for web components:
```javascript
(function() {
  'use strict';

  var onload = function() {
    // For native Imports, manually fire WebComponentsReady so user code
    // can use the same code path for native and polyfill'd imports.
    if (!window.HTMLImports) {
      document.dispatchEvent(
        new CustomEvent('WebComponentsReady', {bubbles: true})
      );
    }
  };

  var webComponentsSupported = (
    'registerElement' in document &&
    'import' in document.createElement('link') &&
    'content' in document.createElement('template')
  );

  if (!webComponentsSupported) {
    var script = document.createElement('script');
    script.async = true;
    script.src = '/bower_components/webcomponentsjs/webcomponents-lite.min.js';
    script.onload = onload;
    document.head.appendChild(script);
  } else {
    onload();
  }
})();
```

Next import the element into the document:
```html
<link rel="import" href="bower_components/api-console/api-console.html">
```

and finally use the element:
```html
<api-console raml="{...}"></api-console>
```

### Full example
```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes">
    <script>
      window.Polymer = {
        dom: 'shadow',
        lazyRegister: true
      };

      // Load webcomponentsjs polyfill if browser does not support native Web Components
      (function() {
        'use strict';

        var onload = function() {
          // For native Imports, manually fire WebComponentsReady so user code
          // can use the same code path for native and polyfill'd imports.
          if (!window.HTMLImports) {
            document.dispatchEvent(
              new CustomEvent('WebComponentsReady', {bubbles: true})
            );
          }
        };

        var webComponentsSupported = (
          'registerElement' in document &&
          'import' in document.createElement('link') &&
          'content' in document.createElement('template')
        );

        if (!webComponentsSupported) {
          var script = document.createElement('script');
          script.async = true;
          script.src = '/bower_components/webcomponentsjs/webcomponents-lite.min.js';
          script.onload = onload;
          document.head.appendChild(script);
        } else {
          onload();
        }
      })();

      // Load pre-caching Service Worker
      if ('serviceWorker' in navigator) {
        window.addEventListener('load', function() {
          navigator.serviceWorker.register('/service-worker.js');
        });
      }
    </script>
    <link rel="import" href="bower_components/api-console/api-console.html">
  </head>
<body>
  <api-console raml="{...}"></api-console>
</body>
</html>
```

Note: The polyfill script is not required if you targeting modern browsers.

## The Polymer
You don't have to care about the Polymer library. It is included internally by the components.
If the browser don't (yet) support shaddow DOM then Polymer will be visible in the
global scope. Otherwise it is not visible to the user.
In both cases you don't have to use or know Polymer to use this element. You must to know how to
use HTML, JavaScript and CSS, though :)

## Passing RAML data
### Direct
The element requires JavaScript object produced by the RAML to JSON parser and `raml-js-enhancer`
element. Parsing and enhancing RAML is not part of the `api-console` element and must be performed
separately.

You can use `raml-js-parser` element to parse YAML data or to download RAML from remote location.
Then you must use `raml-js-enhancer` to produce data output that is recognizable by the `api-console`.

### Example: parsing RAML
(example from [raml-js-parser docs](https://elements.advancedrestclient.com/elements/raml-js-parser))

```html
<raml-js-parser json></raml-js-parser>
<raml-json-enhance></raml-json-enhance>
<api-console></api-console>
```

```javascript
var parser = document.querySelector('raml-js-parser');
parser.addEventListener('api-parse-ready', function(e) {
  var enhacer = document.querySelector('raml-json-enhance');
  enhacer.json = e.detail.json.specification;
  // parsing errors: e.detail.json.errors;
});
window.addEventListener('raml-json-enhance-ready', function(e) {
  var apiConsole = document.querySelector('raml-json-enhance');
  apiConsole.raml = e.detail.json;
});
parser.loadApi(urlToApi);
```

The parsing and enhancing costs a lot depending on RAML structure and number of files included.
Therefore it is a good idea to do it once and cache the results. Then, when the user visit the
page again restore cached JSON object and pass it as the `api-console` parameter.

### RAML aware
The API console uses the [raml-aware](https://elements.advancedrestclient.com/elements/raml-aware) element internally.
It can be used to pass the RAML data to the console if direct access to the
element is not possible. This way the RAML data can be set for the elemnent even
if the elements don't have direct access to each others (e.g. in shadow DOM).

#### Example
```html
<raml-aware scope="main-raml"></raml-aware>
<api-console aware="main-raml"></api-console>
```
```javascript
window.addEventListener('raml-json-enhance-ready', function(e) {
  var aware = document.querySelector('raml-aware');
  aware.raml = e.detail.json;
});
parser.loadApi(urlToApi);
```

## Styling
The main stylesheet for the element is the `api-console-styles.html` file that resists in the repo.
The stylesheet contains CSS variables and mixins definitions that are used by the inner elements.
Styles documentation for an element can be find in it's documentation page in the
[elements catalog](https://elements.advancedrestclient.com/).

Theming is currently not supported.


## Usage

The `api-console` element includes whole UI for the user and can be controlled from within the
element. However it exposes few properties that can be used to control element's behavior
programmatically.

For example `path` property can be used to control to navigate through the RAML structure.
So, to display a request form for a particular endpoint of the API you can set a `path` property to:
```html
<api-console path="resources.0.method.1"></api-console>
```
Example above will display second method from first resource in the resources tree.
You can set attribute `display` to `request` to display a request panel for this method. By default
it is set to `docs`.

## CORS
There's no easy way to deal with CORS. In the API Console ecosystem there is an extension for Chrome
(and soon for Firefox) which will proxy the request without CORS limitations. The user, when using
selected browsers) will see the install extension banner in the request editor. After installing the
extension all traffic from the console will be redirected to the extension to get the endpoint
response.
The console listens for the `api-console-extension-installed` event that is fired from the
extension's content script. Once received the console will send an event to the extension
when the user make the HTTP request. The element responsible for the communication with the extension
is [api-console-ext-comm](https://elements.advancedrestclient.com/elements/api-console-ext-comm).

Other ways to deal with CORS are comming. File an issue report in the repo if you can help with
this issue.

## Sizing
The `api-console` must either be explicitly sized, or contained by the explicitly
sized parent. Parent container also has to be positioned relatively
(`position: relative` CSS property). "Explicitly sized", means it either has
an explicit CSS height property set via a class or inline style, or else is
sized by other layout means (e.g. the flex layout or absolute positioning).
