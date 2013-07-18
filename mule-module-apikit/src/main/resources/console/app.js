angular.module("helpers",[]).factory("ramlPaser",function(){return RAML.Parser}).factory("ramlHelper",function(){return{processQueryParts:function(query){var param,queryParams=[];for(var prop in query)param=query[prop],param.name=prop,queryParams.push(param);return queryParams},processUrlParts:function(url){var urlParts=[],paths=url.split("/");return angular.forEach(paths,function(path){var template;path&&(template=path.match(/{(.*?)}/gi),template?urlParts.push({name:template[0],editable:!0,memberName:template[0].replace("{","").replace("}","")}):urlParts.push({name:path,editable:!1}))}),urlParts},massage:function(resource,parent){if(resource.use=this.readTraits(resource.use),resource.resources){var temp=JSON.parse(JSON.stringify(resource));delete temp.resources,temp.relativeUri="",temp.methods&&resource.resources.unshift(temp),angular.forEach(resource.resources,function(r){r.relativeUri=resource.relativeUri+r.relativeUri;var exists=null;parent&&parent.resources&&(exists=parent.resources.filter(function(p){return p.name===r.name}.bind(this)).pop()),parent&&!exists&&parent.resources.push(r),this.massage(r,resource)}.bind(this))}else{var exists=parent.resources.filter(function(p){return p.name===p.name}.bind(this)).pop();parent&&!exists&&parent.resources.push(resource)}},readTraits:function(usages){var temp=[];return usages&&angular.forEach(usages,function(use){if("string"==typeof use&&-1===temp.indexOf(use))temp.push(use);else if("object"==typeof use){var keys=Object.keys(use);if(keys.length){var key=Object.keys(use)[0];-1===temp.indexOf(key)&&temp.push(key)}}}),temp}}}).factory("commons",function(){return{extend:function(destination,source){for(var elem in source)source.hasOwnProperty(elem)&&(destination[elem]=source[elem]);return destination},joinUrl:function(url1,url2){return url1.lastIndexOf("/")===url1.length-1&&(url1=url1.substring(0,url1.lastIndexOf("/"))),0!==url2.indexOf("/")&&(url2="/"+url2),url1+url2},resolveParams:function(urlTemplate,params){return params&&params.forEach(function(p){p.value&&(urlTemplate=urlTemplate.replace(p.name,p.value))}),urlTemplate},makeReadyStateHandler:function(xhr,callback){xhr.onreadystatechange=function(){4===xhr.readyState&&callback&&callback.call(null,xhr.responseText,xhr)}},setRequestHeaders:function(xhr,headers){if(headers)for(var name in headers)xhr.setRequestHeader(name,headers[name])},toQueryString:function(params){var r=[];for(var n in params){var v=params[n];n=encodeURIComponent(n),r.push(null==v?n:n+"="+encodeURIComponent(v))}return r.join("&")},request:function(options){var xhr=new XMLHttpRequest,url=options.url,method=options.method||"GET",async=!options.sync,params=this.toQueryString(options.params);return params&&"GET"===method&&(url+=(url.indexOf("?")>0?"&":"?")+params),xhr.open(method,url,async),this.makeReadyStateHandler(xhr,options.callback),this.setRequestHeaders(xhr,options.headers),xhr.send("POST"===method||"PUT"===method?options.body||params:null),async||xhr.onreadystatechange(xhr),xhr}}}),angular.module("helpers").factory("showdown",function(){var showdown=new Showdown.converter;return showdown}),angular.module("helpers").factory("eventService",function($rootScope){var sharedService={};return sharedService.broadcast=function(eventName,data){$rootScope.$broadcast(eventName,data)},sharedService}),angular.module("ramlConsoleApp",["helpers","ngResource"]),angular.module("ramlConsoleApp").directive("preventDefault",function(){return function(scope,element){var preventDefaultHandler=function(event){event.preventDefault(),event.stopPropagation(),event.stopImmediatePropagation()};element[0].addEventListener("click",preventDefaultHandler,!1)}}),angular.module("ramlConsoleApp").directive("ramlConsole",function($rootScope){return{restrict:"E",templateUrl:"views/raml-console.tmpl.html",replace:!0,transclude:!1,scope:{id:"@",definition:"@"},link:function($scope){$scope.resources=[],$rootScope.$on("event:raml-parsed",function(e,args){var baseUri=(args.baseUri||"").replace(/\/\/*$/g,""),version=args.version||"";baseUri=baseUri.replace(":0","\\:0"),baseUri=baseUri.replace(":1","\\:1"),baseUri=baseUri.replace(":2","\\:2"),baseUri=baseUri.replace(":3","\\:3"),baseUri=baseUri.replace(":4","\\:4"),baseUri=baseUri.replace(":5","\\:5"),baseUri=baseUri.replace(":6","\\:6"),baseUri=baseUri.replace(":7","\\:7"),baseUri=baseUri.replace(":8","\\:8"),baseUri=baseUri.replace(":9","\\:9"),$scope.baseUri=baseUri.replace("{version}",version),$scope.resources=args.resources,$scope.documentation=args.documentation,$scope.$apply()})}}}),angular.module("ramlConsoleApp").directive("ramlDefinition",function($rootScope){return{restrict:"E",templateUrl:"views/raml-definition.tmpl.html",replace:!0,transclude:!1,scope:{id:"@",src:"@"},controller:function($scope,$element,$attrs,ramlHelper,ramlPaser){ramlPaser.loadFile($attrs.src).done(function(result){angular.forEach(result.resources,function(resource){ramlHelper.massage(resource)}),console.log(result),$rootScope.$emit("event:raml-parsed",result)})}}}),angular.module("ramlConsoleApp").directive("markdown",function(showdown){return{restrict:"C",link:function($scope,element,attrs){$scope.$watch(attrs.ngModel,function(value){"undefined"!=typeof value&&element.html(showdown.makeHtml(value))})}}}),angular.module("ramlConsoleApp").controller("ramlOperation",function($scope,$filter,ramlHelper,eventService){$scope.headerClick=function(){this.toggle("active")},$scope.changeMethod=function(methodName){var method=$filter("filter")(this.resource.methods,{method:methodName});method&&method.length&&($scope.operation=method[0],$scope.urlParams=ramlHelper.processUrlParts($scope.resource.relativeUri),$scope.queryParams=ramlHelper.processQueryParts($scope.operation.query),eventService.broadcast("event:raml-method-changed",methodName))},$scope.isMethodActive=function(methodName){return this.operation&&this.operation.method===methodName},$scope.toggle=function(member){this[member]=!this[member]},$scope.init=function(){this.resource.methods.length&&this.changeMethod(this.resource.methods[0].method)},$scope.init()}),angular.module("ramlConsoleApp").controller("ramlOperationList",function($scope){$scope.model={},$scope.$on("event:raml-sidebar-clicked",function(e,eventData){$scope.model=eventData.isResource?eventData.data:{}})}),angular.module("ramlConsoleApp").controller("ramlDocumentation",function($scope){$scope.model={},$scope.$on("event:raml-sidebar-clicked",function(e,eventData){$scope.model=eventData.isDocumentation?eventData.data[0]:{}})}),angular.module("ramlConsoleApp").controller("ramlConsoleSidebar",function($scope,$filter,eventService,$rootScope){var broadcast=function(data,isDoc,isRes){var result={data:data,isDocumentation:isDoc,isResource:isRes};$rootScope.elementName=data.name||data[0].title,$rootScope.type=isDoc&&!isRes?"document":"resource",eventService.broadcast("event:raml-sidebar-clicked",result)};$rootScope.elementName="",$rootScope.type="",$scope.loaded=function(doc,res){"undefined"!=typeof doc?broadcast([doc],!0,!1):"undefined"!=typeof res&&broadcast(res,!1,!0)},$scope.elementClick=function(id){var data=this.resource||this.documentation;broadcast($filter("filter")(data,function(el){return el.name===id||el.title===id}),this.documentation?!0:!1,this.resource?!0:!1)},$scope.isElementActive=function(elementName,type){return elementName===$rootScope.elementName&&type===$rootScope.type}}),angular.module("ramlConsoleApp").controller("ramlOperationDetails",function($scope){$scope.tabName="try-it",$scope.isTabActive=function(tabName){return tabName===this.tabName},$scope.changeTab=function(tabName){this.tabName=tabName}}),angular.module("ramlConsoleApp").controller("ramlOperationDetailsTryIt",function($scope,$resource,commons){$scope.hasAdditionalParams=function(operation){return operation.query||"post"===operation.method||"put"===operation.method},$scope.hasRequestBody=function(operation){return"post"===operation.method||"put"===operation.method},$scope.showResponse=function(){return this.response},$scope.tryIt=function(){var params={},tester=new this.testerResource;commons.extend(params,this.url),commons.extend(params,this.query[this.operation.method]),this.response=null,this["$"+this.operation.method](tester,params)},$scope.$get=function(tester,params){var that=this;tester.$get(params,function(data,headers,status,url){that.response={data:data.data,headers:data.headers,statusCode:status,url:url}},function(error){that.response={data:error.data.data,headers:error.data.headers,statusCode:error.status,url:error.config.url}})},$scope.$post=function(tester,params){var that=this;tester.$post(params,function(data,headers,status,url){that.response={data:data.data,headers:data.headers,statusCode:status,url:url}},function(error){that.response={data:error.data.data,headers:error.data.headers,statusCode:error.status,url:error.config.url}})},$scope.transformResponse=function(data,headers){try{data=JSON.parse(data),data=angular.toJson(data,!0)}catch(e){}return{data:data,headers:angular.toJson(headers(),!0)}},$scope.transformRequest=function(data){return data},$scope.buildTester=function(){var resourceUri=this.baseUri+this.resource.relativeUri.replace(/{/g,":").replace(/}/g,"");this.testerResource=$resource(resourceUri,null,{get:{method:"GET",isArray:!1,transformResponse:this.transformResponse,transformRequest:this.transformRequest},post:{method:"POST"},put:{method:"PUT"},"delete":{method:"DELETE"}})},$scope.init=function(){this.request||(this.request={}),this.requestBody||(this.requestBody={put:"",post:""}),this.url||(this.url={}),this.query||(this.query={get:{},put:{},post:{},"delete":{}}),this.response=null,this.buildTester()},$scope.$on("event:raml-method-changed",function(){$scope.init()}),$scope.init()}),angular.module("ramlConsoleApp").controller("ramlOperationDetailsResponse",function($scope,$filter){$scope.$on("event:raml-method-changed",function(){$scope.init()}),$scope.init=function(){var statusCodes=[],methodDescriptor=$filter("filter")($scope.resource.methods,{method:$scope.operation.method})[0];if(methodDescriptor.responses)for(var prop in methodDescriptor.responses){var response=methodDescriptor.responses[prop]||{},example=response.body?response.body["application/json"].example:"",schema=response.body?response.body["application/json"].schema:"";statusCodes.push({name:prop,description:response.description,example:example,schema:schema})}$scope.statusCodes=statusCodes},$scope.init()}),angular.module("ramlConsoleApp").controller("ramlOperationDetailsRequest",function($scope,$filter){$scope.$on("event:raml-method-changed",function(){$scope.init()}),$scope.init=function(){var description=null,methodDescriptor=$filter("filter")($scope.resource.methods,{method:$scope.operation.method})[0];methodDescriptor.body&&methodDescriptor.body["application/json"]&&(description={example:methodDescriptor.body["application/json"].example,schema:methodDescriptor.body["application/json"].schema}),$scope.description=description},$scope.init()}),angular.module("ramlConsoleApp").run(["$templateCache",function($templateCache){$templateCache.put("views/raml-console-navbar.tmpl.html","<header>\n    <h1>api:<em>Console</em></h1>\n    <span>{{api.title}} {{api.version}}</span>\n</header>"),$templateCache.put("views/raml-console-sidebar.tmpl.html",'<div ng-controller="ramlConsoleSidebar">\n  <section role="documentation" ng-if="documentation.length">\n    <header>\n      <h1>Overview</h1>\n    </header>\n    <ul role="documentation">\n      <li ng-class="{active:isElementActive(document.title, \'document\')}" ng-repeat="document in documentation">\n        <a href="#" ng-click="elementClick(document.title)">{{document.title}}</a>\n      </li>\n    </ul>\n  </section>\n  <section role="resources" ng-if="resources.length">\n    <header>\n      <h1>Api Reference</h1>\n    </header>\n    <ul>\n      <li ng-class="{active:isElementActive(resource.name, \'resource\')}" ng-repeat="resource in resources">\n        <a href="#" ng-click="elementClick(resource.name)">{{resource.name}}</a>\n      </li>\n    </ul>\n  </section>\n</div>'),$templateCache.put("views/raml-console.tmpl.html",'<section role="api-console">\n    <header>\n        <ng-include src="\'views/raml-console-navbar.tmpl.html\'"></ng-include>\n    </header>\n    <aside role="sidebar">\n        <ng-include src="\'views/raml-console-sidebar.tmpl.html\'" onload="loaded(documentation[0], resources[0])" ng-controller="ramlConsoleSidebar"></ng-include>\n    </aside>\n    <section role="main">\n        <ng-include src="\'views/raml-documentation.tmpl.html\'" ng-controller="ramlDocumentation"></ng-include>\n        <ng-include src="\'views/raml-operation-list.tmpl.html\'"></ng-include>\n    </section>\n</section>\n'),$templateCache.put("views/raml-definition.tmpl.html","<div></div>"),$templateCache.put("views/raml-documentation.tmpl.html",'<section role="api-documentation" ng-show="model">\n  <header>\n    <h1>{{model.title}}</h1>\n  </header>\n  <div id="content" class="markdown" ng-model="model.content">\n  </div>\n</section>'),$templateCache.put("views/raml-operation-details-parameters.tmpl.html",'<section role="api-operation-details-section-parameters">\n  <section role="parameter-list" ng-show="(urlParams | filter: {editable: true}).length">\n    <header>\n      <h1>Url Parameters</h1>\n    </header>\n    <table>\n      <thead>\n        <tr>\n          <th>Param</th>\n          <th>Type</th>\n          <th>Description</th>\n          <th>Example</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr ng-repeat="param in urlParams | filter: {editable: true}">\n          <td>{{param.name}}</td>\n          <td>{{param.type}}</td>\n          <td>{{param.description}}</td>\n          <td>{{param.example}}</td>\n        </tr>\n      </tbody>\n    </table>\n  </section>\n  <section role="parameter-list" ng-show="queryParams.length">\n    <header>\n      <h1>Query Parameters</h1>\n    </header>\n    <table>\n      <thead>\n        <tr>\n          <th>Param</th>\n          <th>Type</th>\n          <th>Description</th>\n          <th>Example</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr ng-repeat="param in queryParams">\n          <td>{{param.name}}</td>\n          <td>{{param.type}}</td>\n          <td>{{param.description}}</td>\n          <td>{{param.example}}</td>\n        </tr>\n      </tbody>\n    </table>\n  </section>\n</section>\n'),$templateCache.put("views/raml-operation-details-request.tmpl.html",'<section role="api-operation-details-section-request" ng-controller="ramlOperationDetailsRequest">\n  <section role="codes-list" ng-show="!description">\n    <header>\n      <h1>No request information</h1>\n    </header>\n  </section>\n\n  <section role="codes-list">\n    <table ng-show="description.schema">\n      <thead>\n        <tr>\n          <th>Schema</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr>\n        	<td>\n				<pre>{{description.schema}}</pre>\n			</td>\n        </tr>\n      </tbody>\n    </table>\n\n    <table ng-show="description.example">\n      <thead>\n        <tr>\n          <th>Example</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr>\n        	<td>\n				<pre>{{description.example}}</pre>\n			</td>\n        </tr>\n      </tbody>\n    </table>\n   \n  </section>\n</section>\n'),$templateCache.put("views/raml-operation-details-response.tmpl.html",'<section role="api-operation-details-section-response" ng-controller="ramlOperationDetailsResponse">\n  <section role="codes-list" ng-show="!statusCodes.length">\n    <header>\n      <h1>No response information</h1>\n    </header>\n  </section>\n\n  <section role="codes-list" ng-repeat="code in statusCodes">\n    <header>\n      <h1>{{code.name}}</h1>\n      <p>{{code.description}}</p>\n    </header>\n    <table ng-show="code.schema">\n      <thead>\n        <tr>\n          <th>Schema</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr>\n        	<td>\n				<pre>{{code.schema}}</pre>\n			</td>\n        </tr>\n      </tbody>\n    </table>\n\n    <table ng-show="code.example">\n      <thead>\n        <tr>\n          <th>Example</th>\n        </tr>\n      </thead>\n      <tbody>\n        <tr>\n        	<td>\n				<pre>{{code.example}}</pre>\n			</td>\n        </tr>\n      </tbody>\n    </table>\n   \n  </section>\n</section>\n'),$templateCache.put("views/raml-operation-details-try-it.tmpl.html",'<section role="api-operation-details-section-try-it" ng-controller="ramlOperationDetailsTryIt">\n    <section role="request" id="request">\n        <header>\n            <h1>Request</h1>\n            <ul role="type">\n                <li>JSON</li>\n                <li>XML</li>\n                <li>JSONP</li>\n            </ul>\n        </header>\n        <div role="uri-params">\n            <h2>Resource Uri</h2>\n            <div role="uri">\n                <ng-include src="\'views/raml-uri-part.tmpl.html\'" ng-repeat="uriPart in urlParams"></ng-include>\n            </div>\n        </div>\n        <div role="additional-params" ng-show="hasAdditionalParams(operation)">\n            <div role="query-params">\n                <h2>Additional parameters</h2>\n                <div role="params">\n                    <ng-include src="\'views/raml-query-param.tmpl.html\'" ng-repeat="queryParam in queryParams"></ng-include>\n                </div>\n            </div>\n            <div role="request-body" ng-show="hasRequestBody(operation)">\n                <label>Body\n                    <textarea ng-model="requestBody[operation.method]"></textarea>\n                </label>\n            </div>\n        </div>\n        <div role="try-it">\n            <span ng-click="tryIt()">Try It!</span>\n        </div>\n    </section>\n    <section role="response" ng-show="showResponse()">\n        <header>\n            <h1>Response</h1>\n        </header>\n        <section role="request-uri">\n            <h1>Request URL</h1>\n            <p>{{response.url}}</p>\n        </section>\n        <section role="response-code">\n            <h1>Response code</h1>\n            <p>{{response.statusCode}}</p>\n        </section>\n        <section role="response-headers">\n            <h1>Response headers</h1>\n            <p>\n                <pre>{{response.headers}}</pre>\n            </p>\n        </section>\n        <section role="response-body">\n            <h1>Response body</h1>\n            <p>\n                <pre>{{response.data}}</pre>\n            </p>\n        </section>\n    </section>\n</section>'),$templateCache.put("views/raml-operation-details.tmpl.html",'<section role="api-operation-details" ng-show="operation" ng-controller="ramlOperationDetails">\n  <header>\n      <h1>Summary</h1>\n      <p>{{operation.summary}}</p>\n  </header>\n  <ul role="details-sections">\n      <li role="try-it" ng-class="{active:isTabActive(\'try-it\')}" ng-click="changeTab(\'try-it\')">\n        <span>Try it</span>\n      </li>\n      <li role="parameters" ng-class="{active:isTabActive(\'parameters\')}" ng-click="changeTab(\'parameters\')" ng-show="(urlParams | filter: {editable: true}).length || queryParams.length">\n        <span>Parameters</span>\n      </li>\n      <li role="requests" ng-class="{active:isTabActive(\'requests\')}" ng-click="changeTab(\'requests\')">\n        <span>Request</span>\n      </li>\n      <li role="response" ng-class="{active:isTabActive(\'response\')}" ng-click="changeTab(\'response\')">\n        <span>Response</span>\n      </li>\n  </ul>\n  <ng-include ng-show="isTabActive(\'try-it\')" src="\'views/raml-operation-details-try-it.tmpl.html\'"></ng-include>\n  <ng-include ng-show="isTabActive(\'parameters\')" src="\'views/raml-operation-details-parameters.tmpl.html\'"></ng-include>\n  <ng-include ng-show="isTabActive(\'requests\')" src="\'views/raml-operation-details-request.tmpl.html\'"></ng-include>\n  <ng-include ng-show="isTabActive(\'response\')" src="\'views/raml-operation-details-response.tmpl.html\'"></ng-include>\n</section>\n'),$templateCache.put("views/raml-operation-list.tmpl.html",'<div ng-show="model" ng-controller="ramlOperationList">\n  <header ng-show="topResource">\n    {{model}}\n    <h1>{{model.name}}</h1>\n  </header>\n  <section role="api-operation-list">\n    <ng-include src="\'views/raml-operation.tmpl.html\'" ng-repeat="resource in model.resources"></ng-include>\n  </section>\n</div>'),$templateCache.put("views/raml-operation.tmpl.html",'<section role="api-operation" ng-class="{active:active}" ng-controller="ramlOperation">\n  <header id="operationHeader" ng-click="headerClick()">\n    <hgroup>\n      <h1>{{resource.name}}</h1>\n      <h2>{{resource.relativeUri}}</h2>\n    </hgroup>\n    <div role="summary">\n      <ul role="traits">\n        <li ng-repeat="trait in resource.use">{{trait}}</li>\n      </ul>\n      <ul role="operations">\n        <li role="{{op.method}}" ng-repeat="op in resource.methods" ng-class="{active:isMethodActive(op.method)}" ng-click="changeMethod(op.method)" prevent-default>\n          <span>{{op.method}}</span>\n        </li>\n      </ul>\n    </div>\n  </header>\n  <ng-include src="\'views/raml-operation-details.tmpl.html\'"></ng-include>\n</scection>'),$templateCache.put("views/raml-query-param.tmpl.html",'<label>{{queryParam.name}}\n    <input type="text" placeholder="{{queryParam.example}}" ng-model="query[operation.method][queryParam.name]">\n</label>'),$templateCache.put("views/raml-uri-part.tmpl.html",'/&nbsp<span ng-hide="uriPart.editable">{{uriPart.name}}</span><input type="text" ng-model="url[uriPart.memberName]" ng-show="uriPart.editable" placeholder="{{uriPart.name}}"></input>\n')}]);