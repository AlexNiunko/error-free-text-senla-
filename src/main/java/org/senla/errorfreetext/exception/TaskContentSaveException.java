package org.senla.errorfreetext.exception;

import org.springframework.http.HttpStatus;

public class TaskContentSaveException extends ServiceException {

    public TaskContentSaveException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
