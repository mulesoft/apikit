package org.mule.module.apikit.api;

import java.math.BigDecimal;

public interface Representation
{

    String getMediaType();
    String getSchemaType();
    String getSchemaLocation();
    BigDecimal getQuality();

}
