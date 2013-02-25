
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.document.UpdateDocumentOperation;

public class UpdateCollectionMemberOperation extends UpdateDocumentOperation
{

    public UpdateCollectionMemberOperation()
    {
        this.type = RestOperationType.UPDATE;
    }


}
