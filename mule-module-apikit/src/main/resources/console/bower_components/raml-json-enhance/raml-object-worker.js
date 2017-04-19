/* global importScripts, self, raml2obj */
try {
  importScripts('polyfills.js', 'browser/index.js', 'raml2object.js');
} catch (e) {
  self.postMessage({
    error: true,
    message: 'Worker import error: ' + e.message
  });
}

self.onmessage = function(e) {
  try {
    if (performance && performance.mark) {
      performance.mark('raml-2-object-start');
    }
    raml2obj
    .parse(e.data.raml)
    .then(function(result) {
      if (performance && performance.mark) {
        performance.mark('raml-2-object-end');
      }
      self.postMessage({
        result: result
      });
    });
  } catch (e) {
    self.postMessage({
      error: true,
      message: 'Worker parser error: ' + e.message
    });
  }
};
