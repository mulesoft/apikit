angular.module('helpers', [])
    .factory('ramlPaser', function () {
        return RAML.Parser;
    })
    .factory('ramlHelper', function () {
        return {
            processQueryParts: function (query) {
                var queryParams = [];
                var param;

                for (var prop in query) {
                    param = query[prop];
                    param.name = prop;
                    queryParams.push(param);
                }

                return queryParams;
            },
            processUrlParts: function (url) {
                var urlParts = [];
                var paths = url.split('/');

                angular.forEach(paths, function (path) {
                    var template;
                    if (!path) {
                        return;
                    }
                    template = path.match(/{(.*?)}/ig);
                    if (template) {
                        urlParts.push({ name: template[0], editable: true, memberName: template[0].replace('{', '').replace('}', '') });
                    } else {
                        urlParts.push({ name: path, editable: false });
                    }
                });

                return urlParts;
            },
            massage: function (resource, parent) {
                resource.use = this.readTraits(resource.use);

                if (resource.resources) {
                    var temp = JSON.parse(JSON.stringify(resource));

                    delete temp.resources;

                    temp.relativeUri = '';

                    if (temp.methods) {
                        resource.resources.unshift(temp);
                    }

                    angular.forEach(resource.resources, function (r) {
                        r.relativeUri = resource.relativeUri + r.relativeUri;

                        var exists = null;

                        if (parent && parent.resources) {
                            exists = parent.resources.filter(function (p) {
                                return p.name === r.name;
                            }.bind(this)).pop();
                        }

                        if (parent && !exists) {
                            parent.resources.push(r);
                        }

                        this.massage(r, resource);
                    }.bind(this));
                } else {
                    var exists = parent.resources.filter(function (p) {
                        return p.name === p.name;
                    }.bind(this)).pop();

                    if (parent && !exists) {
                        parent.resources.push(resource);
                    }
                }
            },
            readTraits: function (usages) {
                var temp = [];

                if (usages) {
                    angular.forEach(usages, function (use) {
                        if (typeof use === 'string' && temp.indexOf(use) === -1) {
                            temp.push(use);
                        } else if (typeof use === 'object') {
                            var keys = Object.keys(use);

                            if (keys.length) {
                                var key = Object.keys(use)[0];

                                if (temp.indexOf(key) === -1) {
                                    temp.push(key);
                                }
                            }
                        }
                    });
                }

                return temp;
            }
        };
    })
    .factory('commons', function () {
        return {
            extend: function (destination, source) {
                for (var elem in source) {
                    if (source.hasOwnProperty(elem)) {
                        destination[elem] = source[elem];
                    }
                }

                return destination;
            },
            joinUrl: function (url1, url2) {
                if (url1.lastIndexOf('/') === url1.length - 1) {
                    url1 = url1.substring(0, url1.lastIndexOf('/'));
                }

                if (url2.indexOf('/') !== 0) {
                    url2 = '/' + url2;
                }

                return url1 + url2;
            },
            resolveParams: function (urlTemplate, params) {
                if (params) {
                    params.forEach(function (p) {
                        if (p.value) {
                            urlTemplate = urlTemplate.replace(p.name, p.value);
                        }
                    });
                }

                return urlTemplate;
            },
            makeReadyStateHandler: function (xhr, callback) {
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4) {
                        callback && callback.call(null, xhr.responseText, xhr);
                    }
                };
            },
            setRequestHeaders: function (xhr, headers) {
                if (headers) {
                    for (var name in headers) {
                        xhr.setRequestHeader(name, headers[name]);
                    }
                }
            },
            toQueryString: function (params) {
                var r = [];
                for (var n in params) {
                    var v = params[n];
                    n = encodeURIComponent(n);
                    r.push(v == null ? n : (n + '=' + encodeURIComponent(v)));
                }
                return r.join('&');
            },
            /**
             * Sends a HTTP request to the server and returns the XHR object.
             *
             * @method request
             * @param {Object} inOptions
             *    @param {String} inOptions.url The url to which the request is sent.
             *    @param {String} inOptions.method The HTTP method to use, default is GET.
             *    @param {boolean} inOptions.sync By default, all requests are sent asynchronously.
             *        To send synchronous requests, set to true.
             *    @param {Object} inOptions.params Data to be sent to the server.
             *    @param {Object} inOptions.body The content for the request body for POST method.
             *    @param {Object} inOptions.headers HTTP request headers.
             *    @param {Object} inOptions.callback Called when request is completed.
             * @returns {Object} XHR object.
             */
            request: function (options) {
                var xhr = new XMLHttpRequest();
                var url = options.url;
                var method = options.method || 'GET';
                var async = !options.sync;
                var params = this.toQueryString(options.params);
                if (params && method === 'GET') {
                    url += (url.indexOf('?') > 0 ? '&' : '?') + params;
                }
                xhr.open(method, url, async);
                this.makeReadyStateHandler(xhr, options.callback);

                this.setRequestHeaders(xhr, options.headers);
                xhr.send(method === 'POST' || method === 'PUT' ? (options.body || params) : null);
                if (!async) {
                    xhr.onreadystatechange(xhr);
                }
                return xhr;
            }
        };
    });

angular.module('helpers').factory('showdown', function ($rootScope) {
    var showdown = new Showdown.converter();

    return showdown;
});
angular.module('helpers').factory('eventService', function ($rootScope) {
    var sharedService = {};

    sharedService.broadcast = function (eventName, data) {
        $rootScope.$broadcast(eventName, data);
    };

    return sharedService;
});
angular.module('ramlConsoleApp', ['helpers', 'ngResource']);
angular.module('ramlConsoleApp')
    .directive('preventDefault', function () {
        return function (scope, element, attrs) {
            var preventDefaultHandler = function (event) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
            };
            element[0].addEventListener('click', preventDefaultHandler, false);
        };
    });
angular.module('ramlConsoleApp')
    .directive('ramlConsole', function ($rootScope) {
        return {
            restrict: 'E',
            templateUrl: 'views/raml-console.tmpl.html',
            replace: true,
            transclude: false,
            scope: {
                'id': '@',
                'definition': '@'
            },
            link: function ($scope, $element, $attributes) {
                $scope.resources = [];

                $rootScope.$on('event:raml-parsed', function (e, args) {
                    var baseUri = (args.baseUri || '').replace(/\/\/*$/g, '');
                    var version = args.version || '';

                    baseUri = baseUri.replace(':0', '\\:0');
                    baseUri = baseUri.replace(':1', '\\:1');
                    baseUri = baseUri.replace(':2', '\\:2');
                    baseUri = baseUri.replace(':3', '\\:3');
                    baseUri = baseUri.replace(':4', '\\:4');
                    baseUri = baseUri.replace(':5', '\\:5');
                    baseUri = baseUri.replace(':6', '\\:6');
                    baseUri = baseUri.replace(':7', '\\:7');
                    baseUri = baseUri.replace(':8', '\\:8');
                    baseUri = baseUri.replace(':9', '\\:9');

                    $scope.baseUri = baseUri.replace('{version}', version);
                    $scope.resources = args.resources;
                    $scope.documentation = args.documentation;
                    $scope.$apply();
                });
            }
        };
    });
angular.module('ramlConsoleApp')
    .directive('ramlDefinition', function ($rootScope) {
        return {
            restrict: 'E',
            templateUrl: 'views/raml-definition.tmpl.html',
            replace: true,
            transclude: false,
            scope: {
                'id': '@',
                'src': '@'
            },
            controller: function ($scope, $element, $attrs, ramlHelper, ramlPaser) {
                ramlPaser.loadFile($attrs.src)
                    .done(function (result) {
                        angular.forEach(result.resources, function (resource) {
                            ramlHelper.massage(resource);
                        });
                        console.log(result);
                        $rootScope.$emit('event:raml-parsed', result);
                    });
            }
        };
    });
angular.module('ramlConsoleApp').directive('markdown', function (showdown) {
    return {
        restrict: 'C',
        link: function ($scope, element, attrs, ngModel) {
            $scope.$watch(attrs.ngModel, function (value) {
                if (typeof value !== 'undefined') {
                    element.html(showdown.makeHtml(value));
                }
            });
        }
    };
});
angular.module('ramlConsoleApp')
    .controller('ramlOperation', function ($scope, $filter, ramlHelper, eventService) {
        $scope.headerClick = function () {
            this.toggle('active');
        };

        $scope.changeMethod = function (methodName) {
            var method = $filter('filter')(this.resource.methods, { method: methodName });
            if (method && method.length) {
                $scope.operation = method[0];
                $scope.urlParams = ramlHelper.processUrlParts($scope.resource.relativeUri);
                $scope.queryParams = ramlHelper.processQueryParts($scope.operation.query);

                eventService.broadcast('event:raml-method-changed', methodName);
            }
        };

        $scope.isMethodActive = function (methodName) {
            return this.operation && (this.operation.method === methodName)
        };

        $scope.toggle = function (member) {
            this[member] = !this[member];
        };

        $scope.init = function () {
            if (this.resource.methods.length) {
                this.changeMethod(this.resource.methods[0].method);
            }
        };

        $scope.init();
    });
angular.module('ramlConsoleApp')
    .controller('ramlOperationList', function ($scope, $filter) {
        $scope.model = {};

        $scope.$on('event:raml-sidebar-clicked', function (e, eventData) {
            if (eventData.isResource) {
                $scope.model = eventData.data;
            } else {
                $scope.model = {};
            }
        });
    });
angular.module('ramlConsoleApp')
    .controller('ramlDocumentation', function ($scope, $filter) {
        $scope.model = {};

        $scope.$on('event:raml-sidebar-clicked', function (e, eventData) {
            if (eventData.isDocumentation) {
                $scope.model = eventData.data[0];
            } else {
                $scope.model = {};
            }
        });
    });
angular.module('ramlConsoleApp')
    .controller('ramlConsoleSidebar', function ($scope, $filter, eventService, $rootScope) {
        var broadcast = function (data, isDoc, isRes) {
            var result = {
                data: data,
                isDocumentation: isDoc,
                isResource: isRes
            };

            $rootScope.elementName = data.name || data[0].title;
            $rootScope.type = isDoc && !isRes ? 'document' : 'resource';

            eventService.broadcast('event:raml-sidebar-clicked', result);
        };

        $rootScope.elementName = '';
        $rootScope.type = '';

        $scope.loaded = function (doc, res) {
            if (typeof doc !== 'undefined') {
                broadcast([doc], true, false);
            } else if (typeof res !== 'undefined') {
                broadcast(res, false, true);
            }

        };

        $scope.elementClick = function (id) {
            var data = this.resource || this.documentation;

            broadcast($filter('filter')(data, function (el) {
                return el.name === id || el.title === id;
            }), this.documentation ? true : false, this.resource ? true : false);
        };

        $scope.isElementActive = function (elementName, type) {
            return elementName === $rootScope.elementName && type === $rootScope.type;
        };
    });
angular.module('ramlConsoleApp')
    .controller('ramlOperationDetails', function ($scope) {
        $scope.tabName = 'try-it';

        $scope.isTabActive = function (tabName) {
            return tabName === this.tabName;
        };
        $scope.changeTab = function (tabName) {
            this.tabName = tabName;
        };
    });
angular.module('ramlConsoleApp')
    .controller('ramlOperationDetailsTryIt', function ($scope, $resource, commons, eventService) {
        $scope.hasAdditionalParams = function (operation) {
            return operation.query || operation.method === 'post' || operation.method === 'put';
        };

        $scope.hasRequestBody = function (operation) {
            return operation.method === 'post' || operation.method === 'put';
        };

        $scope.showResponse = function () {
            return this.responseAvailable;
        };

        $scope.tryIt = function () {
            var params = {};
            var tester = new this.testerResource();

            commons.extend(params, this.url);
            commons.extend(params, this.query[this.operation.method]);

            this['$' + this.operation.method](tester, params);
        };

        $scope.$get = function (tester, params) {
            var that = this;
            this.responseAvailable = true;

            tester.$get(params, function (data, headers, status, url) {
                that.response = {
                    data: data.data,
                    headers: data.headers,
                    statusCode: status,
                    url: url
                };
            }, function (error) {
                that.response = {
                    data: error.data.data,
                    headers: error.data.headers,
                    statusCode: error.status,
                    url: error.config.url
                };

                console.log(error);
            });
        }

        $scope.transformResponse = function (data) {
            console.log(data);
            return { data: data };
        };

        $scope.buildTester = function () {
            var resourceUri = this.baseUri + this.resource.relativeUri.replace(/{/g, ':').replace(/}/g, '');
            this.testerResource = $resource(resourceUri, null, {
                    'get': {
                        method:'GET',
                        isArray: false,
                        transformResponse: function (data, headers) {
                            try {
                                data = JSON.parse(data);
                                data = angular.toJson(data, true);
                            }
                            catch (e) {}

                            return { data: data, headers: angular.toJson(headers(), true) };
                        },
                        transformRequest: function (data, headers) {
                            return data;
                        }
                    },
                    'post': { method:'POST' },
                    'put': { method:'PUT' },
                    'delete': { method:'DELETE' }
                });
        };

        $scope.init = function () {
            this.request = {};
            this.url = {};
            this.query = { get: {}, put: {}, post: {}, delete: {} };
            this.requestBody = '';
            this.responseAvailable = false;

            this.buildTester();
        };

        $scope.init();
    });

angular.module('ramlConsoleApp')
    .controller('ramlOperationDetailsResponse', function ($scope, $filter) {
        $scope.$on('event:raml-method-changed', function () {
            $scope.init();
        });

        $scope.init = function () {
            var statusCodes = [],
                methodDescriptor = $filter('filter')($scope.resource.methods, {
                    method: $scope.operation.method
                })[0];

            if (methodDescriptor.responses) {
                for (var prop in methodDescriptor.responses) {
                    var response = methodDescriptor.responses[prop] || {},
                        example = response.body ? response.body['application/json'].example : '',
                        schema = response.body ? response.body['application/json'].schema : '';

                    statusCodes.push({
                        name: prop,
                        description: response.description,
                        example: example,
                        schema: schema
                    });
                }
            }

            $scope.statusCodes = statusCodes;
        };

        $scope.init();
    });
angular.module('ramlConsoleApp')
    .controller('ramlOperationDetailsRequest', function ($scope, $filter) {
        $scope.$on('event:raml-method-changed', function () {
            $scope.init();
        });

        $scope.init = function () {
            var description = null,
                methodDescriptor = $filter('filter')($scope.resource.methods, {
                    method: $scope.operation.method
                })[0];

            if (methodDescriptor.body && methodDescriptor.body['application/json']) {
                description = {
                    example: methodDescriptor.body['application/json'].example,
                    schema: methodDescriptor.body['application/json'].schema
                };
            }

            $scope.description = description;
        };

        $scope.init();
    });
angular.module("ramlConsoleApp").run(["$templateCache", function($templateCache) {

  $templateCache.put("views/raml-console-navbar.tmpl.html",
    "<header>\n" +
    "    <h1>api:<em>Console</em></h1>\n" +
    "    <span>{{api.title}} {{api.version}}</span>\n" +
    "</header>"
  );

  $templateCache.put("views/raml-console-sidebar.tmpl.html",
    "<div ng-controller=\"ramlConsoleSidebar\">\n" +
    "  <section role=\"documentation\" ng-if=\"documentation.length\">\n" +
    "    <header>\n" +
    "      <h1>Overview</h1>\n" +
    "    </header>\n" +
    "    <ul role=\"documentation\">\n" +
    "      <li ng-class=\"{active:isElementActive(document.title, 'document')}\" ng-repeat=\"document in documentation\">\n" +
    "        <a href=\"#\" ng-click=\"elementClick(document.title)\">{{document.title}}</a>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "  </section>\n" +
    "  <section role=\"resources\" ng-if=\"resources.length\">\n" +
    "    <header>\n" +
    "      <h1>Api Reference</h1>\n" +
    "    </header>\n" +
    "    <ul>\n" +
    "      <li ng-class=\"{active:isElementActive(resource.name, 'resource')}\" ng-repeat=\"resource in resources\">\n" +
    "        <a href=\"#\" ng-click=\"elementClick(resource.name)\">{{resource.name}}</a>\n" +
    "      </li>\n" +
    "    </ul>\n" +
    "  </section>\n" +
    "</div>"
  );

  $templateCache.put("views/raml-console.tmpl.html",
    "<section role=\"api-console\">\n" +
    "    <header>\n" +
    "        <ng-include src=\"'views/raml-console-navbar.tmpl.html'\"></ng-include>\n" +
    "    </header>\n" +
    "    <aside role=\"sidebar\">\n" +
    "        <ng-include src=\"'views/raml-console-sidebar.tmpl.html'\" onload=\"loaded(documentation[0], resources[0])\" ng-controller=\"ramlConsoleSidebar\"></ng-include>\n" +
    "    </aside>\n" +
    "    <section role=\"main\">\n" +
    "        <ng-include src=\"'views/raml-documentation.tmpl.html'\" ng-controller=\"ramlDocumentation\"></ng-include>\n" +
    "        <ng-include src=\"'views/raml-operation-list.tmpl.html'\"></ng-include>\n" +
    "    </section>\n" +
    "</section>\n"
  );

  $templateCache.put("views/raml-definition.tmpl.html",
    "<div></div>"
  );

  $templateCache.put("views/raml-documentation.tmpl.html",
    "<section role=\"api-documentation\" ng-show=\"model\">\n" +
    "  <header>\n" +
    "    <h1>{{model.title}}</h1>\n" +
    "  </header>\n" +
    "  <div id=\"content\" class=\"markdown\" ng-model=\"model.content\">\n" +
    "  </div>\n" +
    "</section>"
  );

  $templateCache.put("views/raml-operation-details-parameters.tmpl.html",
    "<section role=\"api-operation-details-section-parameters\">\n" +
    "  <section role=\"parameter-list\" ng-show=\"(urlParams | filter: {editable: true}).length\">\n" +
    "    <header>\n" +
    "      <h1>Url Parameters</h1>\n" +
    "    </header>\n" +
    "    <table>\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Param</th>\n" +
    "          <th>Type</th>\n" +
    "          <th>Description</th>\n" +
    "          <th>Example</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr ng-include=\"'views/raml-parameter-row-part.tmpl.html'\" ng-repeat=\"param in urlParams | filter: {editable: true}\"></tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "  </section>\n" +
    "  <section role=\"parameter-list\" ng-show=\"queryParams.length\">\n" +
    "    <header>\n" +
    "      <h1>Query Parameters</h1>\n" +
    "    </header>\n" +
    "    <table>\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Param</th>\n" +
    "          <th>Type</th>\n" +
    "          <th>Description</th>\n" +
    "          <th>Example</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr ng-include=\"'views/raml-parameter-row-part.tmpl.html'\" ng-repeat=\"param in queryParams\"></tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "  </section>\n" +
    "</section>\n"
  );

  $templateCache.put("views/raml-operation-details-request.tmpl.html",
    "<section role=\"api-operation-details-section-request\" ng-controller=\"ramlOperationDetailsRequest\">\n" +
    "  <section role=\"codes-list\" ng-show=\"!description\">\n" +
    "    <header>\n" +
    "      <h1>No request information</h1>\n" +
    "    </header>\n" +
    "  </section>\n" +
    "\n" +
    "  <section role=\"codes-list\">\n" +
    "    <table ng-show=\"description.schema\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Schema</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr>\n" +
    "        \t<td>\n" +
    "\t\t\t\t<pre>{{description.schema}}</pre>\n" +
    "\t\t\t</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <table ng-show=\"description.example\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Example</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr>\n" +
    "        \t<td>\n" +
    "\t\t\t\t<pre>{{description.example}}</pre>\n" +
    "\t\t\t</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "   \n" +
    "  </section>\n" +
    "</section>\n"
  );

  $templateCache.put("views/raml-operation-details-response.tmpl.html",
    "<section role=\"api-operation-details-section-response\" ng-controller=\"ramlOperationDetailsResponse\">\n" +
    "  <section role=\"codes-list\" ng-show=\"!statusCodes.length\">\n" +
    "    <header>\n" +
    "      <h1>No response information</h1>\n" +
    "    </header>\n" +
    "  </section>\n" +
    "\n" +
    "  <section role=\"codes-list\" ng-repeat=\"code in statusCodes\">\n" +
    "    <header>\n" +
    "      <h1>{{code.name}}</h1>\n" +
    "      <p>{{code.description}}</p>\n" +
    "    </header>\n" +
    "    <table ng-show=\"code.schema\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Schema</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr>\n" +
    "        \t<td>\n" +
    "\t\t\t\t<pre>{{code.schema}}</pre>\n" +
    "\t\t\t</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "\n" +
    "    <table ng-show=\"code.example\">\n" +
    "      <thead>\n" +
    "        <tr>\n" +
    "          <th>Example</th>\n" +
    "        </tr>\n" +
    "      </thead>\n" +
    "      <tbody>\n" +
    "        <tr>\n" +
    "        \t<td>\n" +
    "\t\t\t\t<pre>{{code.example}}</pre>\n" +
    "\t\t\t</td>\n" +
    "        </tr>\n" +
    "      </tbody>\n" +
    "    </table>\n" +
    "   \n" +
    "  </section>\n" +
    "</section>\n"
  );

  $templateCache.put("views/raml-operation-details-try-it.tmpl.html",
    "<section role=\"api-operation-details-section-try-it\" ng-controller=\"ramlOperationDetailsTryIt\">\n" +
    "    <section role=\"request\" id=\"request\">\n" +
    "        <header>\n" +
    "            <h1>Request</h1>\n" +
    "            <ul role=\"type\">\n" +
    "                <li>JSON</li>\n" +
    "                <li>XML</li>\n" +
    "                <li>JSONP</li>\n" +
    "            </ul>\n" +
    "        </header>\n" +
    "        <div role=\"uri-params\">\n" +
    "            <h2>Resource Uri</h2>\n" +
    "            <div role=\"uri\">\n" +
    "                <ng-include src=\"'views/raml-uri-part.tmpl.html'\" ng-repeat=\"uriPart in urlParams\"></ng-include>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div role=\"additional-params\" ng-show=\"hasAdditionalParams(operation)\">\n" +
    "            <div role=\"query-params\">\n" +
    "                <h2>Additional parameters</h2>\n" +
    "                <div role=\"params\">\n" +
    "                    <ng-include src=\"'views/raml-query-param.tmpl.html'\" ng-repeat=\"queryParam in queryParams\"></ng-include>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "            <div role=\"request-body\" ng-show=\"hasRequestBody(operation)\">\n" +
    "                <label>Body\n" +
    "                    <textarea ng-model=\"requestBody\"></textarea>\n" +
    "                </label>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div role=\"try-it\">\n" +
    "            <span ng-click=\"tryIt()\">Try It!</span>\n" +
    "        </div>\n" +
    "    </section>\n" +
    "    <section role=\"response\" ng-show=\"showResponse()\">\n" +
    "        <header>\n" +
    "            <h1>Response</h1>\n" +
    "        </header>\n" +
    "        <section role=\"request-uri\">\n" +
    "            <h1>Request URL</h1>\n" +
    "            <p>{{response.url}}</p>\n" +
    "        </section>\n" +
    "        <section role=\"response-code\">\n" +
    "            <h1>Response code</h1>\n" +
    "            <p>{{response.statusCode}}</p>\n" +
    "        </section>\n" +
    "        <section role=\"response-headers\">\n" +
    "            <h1>Response headers</h1>\n" +
    "            <p>\n" +
    "                <pre>{{response.headers}}</pre>\n" +
    "            </p>\n" +
    "        </section>\n" +
    "        <section role=\"response-body\">\n" +
    "            <h1>Response body</h1>\n" +
    "            <p>\n" +
    "                <pre>{{response.data}}</pre>\n" +
    "            </p>\n" +
    "        </section>\n" +
    "    </section>\n" +
    "</section>"
  );

  $templateCache.put("views/raml-operation-details.tmpl.html",
    "<section role=\"api-operation-details\" ng-show=\"operation\" ng-controller=\"ramlOperationDetails\">\n" +
    "  <header>\n" +
    "      <h1>Summary</h1>\n" +
    "      <p>{{operation.summary}}</p>\n" +
    "  </header>\n" +
    "  <ul role=\"details-sections\">\n" +
    "      <li role=\"try-it\" ng-class=\"{active:isTabActive('try-it')}\" ng-click=\"changeTab('try-it')\">\n" +
    "        <span>Try it</span>\n" +
    "      </li>\n" +
    "      <li role=\"parameters\" ng-class=\"{active:isTabActive('parameters')}\" ng-click=\"changeTab('parameters')\">\n" +
    "        <span>Parameters</span>\n" +
    "      </li>\n" +
    "      <li role=\"requests\" ng-class=\"{active:isTabActive('requests')}\" ng-click=\"changeTab('requests')\">\n" +
    "        <span>Request</span>\n" +
    "      </li>\n" +
    "      <li role=\"response\" ng-class=\"{active:isTabActive('response')}\" ng-click=\"changeTab('response')\">\n" +
    "        <span>Response</span>\n" +
    "      </li>\n" +
    "  </ul>\n" +
    "  <ng-include ng-show=\"isTabActive('try-it')\" src=\"'views/raml-operation-details-try-it.tmpl.html'\"></ng-include>\n" +
    "  <ng-include ng-show=\"isTabActive('parameters')\" src=\"'views/raml-operation-details-parameters.tmpl.html'\"></ng-include>\n" +
    "  <ng-include ng-show=\"isTabActive('requests')\" src=\"'views/raml-operation-details-request.tmpl.html'\"></ng-include>\n" +
    "  <ng-include ng-show=\"isTabActive('response')\" src=\"'views/raml-operation-details-response.tmpl.html'\"></ng-include>\n" +
    "</section>\n"
  );

  $templateCache.put("views/raml-operation-list.tmpl.html",
    "<div ng-show=\"model\" ng-controller=\"ramlOperationList\">\n" +
    "  <header ng-show=\"topResource\">\n" +
    "    {{model}}\n" +
    "    <h1>{{model.name}}</h1>\n" +
    "  </header>\n" +
    "  <section role=\"api-operation-list\">\n" +
    "    <ng-include src=\"'views/raml-operation.tmpl.html'\" ng-repeat=\"resource in model.resources\"></ng-include>\n" +
    "  </section>\n" +
    "</div>"
  );

  $templateCache.put("views/raml-operation.tmpl.html",
    "<section role=\"api-operation\" ng-class=\"{active:active}\" ng-controller=\"ramlOperation\">\n" +
    "  <header id=\"operationHeader\" ng-click=\"headerClick()\">\n" +
    "    <hgroup>\n" +
    "      <h1>{{resource.name}}</h1>\n" +
    "      <h2>{{resource.relativeUri}}</h2>\n" +
    "    </hgroup>\n" +
    "    <div role=\"summary\">\n" +
    "      <ul role=\"traits\">\n" +
    "        <li ng-repeat=\"trait in resource.use\">{{trait}}</li>\n" +
    "      </ul>\n" +
    "      <ul role=\"operations\">\n" +
    "        <li role=\"{{op.method}}\" ng-repeat=\"op in resource.methods\" ng-class=\"{active:isMethodActive(op.method)}\" ng-click=\"changeMethod(op.method)\" prevent-default>\n" +
    "          <span>{{op.method}}</span>\n" +
    "        </li>\n" +
    "      </ul>\n" +
    "    </div>\n" +
    "  </header>\n" +
    "  <ng-include src=\"'views/raml-operation-details.tmpl.html'\"></ng-include>\n" +
    "</scection>"
  );

  $templateCache.put("views/raml-parameter-row-part.tmpl.html",
    "<td>{{param.name}}</td>\n" +
    "<td>{{param.type}}</td>\n" +
    "<td>{{param.description}}</td>\n" +
    "<td>{{param.example}}</td>\n" +
    "\n"
  );

  $templateCache.put("views/raml-query-param.tmpl.html",
    "<label>{{queryParam.name}}\n" +
    "    <input type=\"text\" placeholder=\"{{queryParam.example}}\" ng-model=\"query[operation.method][queryParam.name]\">\n" +
    "</label>"
  );

  $templateCache.put("views/raml-uri-part.tmpl.html",
    "/&nbsp<span ng-hide=\"uriPart.editable\">{{uriPart.name}}</span><input type=\"text\" ng-model=\"url[uriPart.memberName]\" ng-show=\"uriPart.editable\" placeholder=\"{{uriPart.name}}\"></input>\n"
  );

}]);
