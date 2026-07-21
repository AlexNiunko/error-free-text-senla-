package org.senla.errorfreetext.exception;

import org.springframework.http.HttpStatus;

public class TaskSaveException extends ServiceException {

    public TaskSaveException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
