#%RAML ${ramlVersion}
title: ${title}
version: ${version}
traits: 
- orderby:
      queryParameters:
        parms:
          description: The number of pages to return
          type: string
- top:
      queryParameters:
        parms:
          description: The number of pages to return
          type: number
- skip:
      queryParameters:
        parms:
          description: The number of pages to return
          type: number
- filter:
      queryParameters:
        parms:
          description: Collection of filters
          type: string
- expand:
      queryParameters:
        parms:
          description: Collection of filters
          type: string
- format:
      queryParameters:
        parms:
          description: Collection of filters
          type: string
- select:
      queryParameters:
        parms:
          description: Collection of filters
          type: string
- inlinecount:
      queryParameters:
        parms:
          description: Collection of filters
          type: string
schemas: 
<#list schemas as schema>
- ${schema.name}: |
     ${schema.json}
</#list>
<#list resources as resource>
/${resource.name}: 
    displayName: ${resource.displayName}
    is: [orderby, top, skip, filter, expand, format, select, inlinecount]
    get: 
        description: Read
        responses:
         200:
          body:
            application/json: ~
            application/xml: ~
    post: 
        description: Create
        responses:
         200:
          body:
             application/json: ~
             application/xml: ~
    /${resource.key}: 
        displayName: ${resource.displayName} Id
        is: [filter, expand, format, select]
        get: 
            description: Read
            responses:
             200:
              body:
               application/json: ~
               application/xml: ~
        delete: 
            description: Delete
            responses:
             200:
              body:
               application/json: ~
               application/xml: ~
        put: 
            description: Update
            responses:
             200:
              body:
               application/json: ~
               application/xml: ~
</#list>