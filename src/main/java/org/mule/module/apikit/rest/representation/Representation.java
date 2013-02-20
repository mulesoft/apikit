package org.mule.module.apikit.rest.representation;

import java.math.BigDecimal;

public interface Representation
{

    String getMediaType();
    String getSchemaType();
    String getSchemaLocation();
    BigDecimal getQuality();

}
