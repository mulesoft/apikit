package org.mule.examples.leagues.exceptions;

import org.mule.module.apikit.exception.MuleRestException;

public class ConflictException extends MuleRestException {

    public ConflictException(String message) {
        super(message);
    }
}
