package org.senla.errorfreetext.exception;

import org.springframework.http.HttpStatus;

public class ContentProcessException extends ServiceException{

    public ContentProcessException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
