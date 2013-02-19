
package org.mule.module.apikit.rest.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class DocumentResourceTestCase extends AbstractMuleTestCase
{
    @Mock
    MuleEvent event;
    @Mock
    MuleMessage message;
    @Mock
    RestRequest request;
    @Mock
    HttpRestProtocolAdapter httpAdapter;
    DocumentResource doc;

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter).handleException(any(RestException.class), any(MuleEvent.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        doc = new DocumentResource("doc");
    }

    @Test
    public void supportedActions()
    {
        Assert.assertTrue(doc.getSupportedActionTypes().contains(ActionType.RETRIEVE));
        Assert.assertTrue(doc.getSupportedActionTypes().contains(ActionType.EXISTS));
        Assert.assertFalse(doc.getSupportedActionTypes().contains(ActionType.CREATE));
        Assert.assertTrue(doc.getSupportedActionTypes().contains(ActionType.UPDATE));
        Assert.assertFalse(doc.getSupportedActionTypes().contains(ActionType.DELETE));
    }

    @Test(expected = InitialisationException.class)
    public void errorOnDuplicageChildResourceNames() throws Exception
    {
        DocumentResource childDoc1 = new DocumentResource("child");
        DocumentResource childDoc2 = new DocumentResource("child");
        doc.setResources(Arrays.asList(new RestResource[] {childDoc1, childDoc2}));
        doc.buildRoutingTable();
    }

}