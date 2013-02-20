package org.mule.module.apikit.rest.action;

import org.mule.api.processor.MessageProcessor;

public class RestExistsByRetrieveAction extends RestExistsAction
{

    private RestAction retrieve;

    public RestExistsByRetrieveAction(RestAction retrieve)
    {
        this.retrieve = retrieve;
    }

    @Override
    public MessageProcessor getHandler()
    {
        return retrieve.getHandler();
    }

}
