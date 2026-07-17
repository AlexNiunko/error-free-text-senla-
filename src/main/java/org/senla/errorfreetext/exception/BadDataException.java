package org.senla.errorfreetext.exception;

import org.springframework.http.HttpStatus;

public class BadDataException extends ServiceException {
    public BadDataException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
