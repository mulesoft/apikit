package org.mule.module.apikit.rest.transform;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.DiscoverableTransformer;
import org.mule.transformer.AbstractMessageTransformer;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJacksonTransformer extends AbstractMessageTransformer implements DiscoverableTransformer
{

    protected int weighting = DiscoverableTransformer.MAX_PRIORITY_WEIGHTING;

    private ObjectMapper mapper;

    @Override
    public void initialise() throws InitialisationException
    {
        if (mapper == null)
        {
            mapper = new ObjectMapper();
        }
    }

    public ObjectMapper getMapper()
    {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    public int getPriorityWeighting()
    {
        return weighting;
    }

    public void setPriorityWeighting(int weighting)
    {
        this.weighting = weighting;
    }
}

