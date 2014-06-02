/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import org.mule.api.transformer.DataType;

public class DataTypePair
{

    private final DataType sourceDataType;
    private final DataType resultDataType;

    public DataTypePair(DataType sourceDataType, DataType resultDataType)
    {
        this.sourceDataType = sourceDataType;
        this.resultDataType = resultDataType;
    }

    public DataType getSourceDataType()
    {
        return sourceDataType;
    }

    public DataType getResultDataType()
    {
        return resultDataType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        DataTypePair that = (DataTypePair) o;

        if (!resultDataType.equals(that.resultDataType))
        {
            return false;
        }
        if (!sourceDataType.equals(that.sourceDataType))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = sourceDataType.hashCode();
        result = 31 * result + resultDataType.hashCode();
        return result;
    }
}
