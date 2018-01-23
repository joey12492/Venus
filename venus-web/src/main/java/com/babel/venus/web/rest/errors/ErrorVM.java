package com.babel.venus.web.rest.errors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * View Model for transferring error message with a list of field errors.
 */
public class ErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String err;
    private final String msg;

    private List<FieldErrorVM> fieldErrors;

    public ErrorVM(String message) {
        this(message, null);
    }

    public ErrorVM(String err, String msgs) {
        this.err = err;
        this.msg = msgs;
    }

    public ErrorVM(String err, String msg, List<FieldErrorVM> fieldErrors) {
        this.err = err;
        this.msg = msg;
        this.fieldErrors = fieldErrors;
    }

    public void add(String objectName, String field, String message) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }
        fieldErrors.add(new FieldErrorVM(objectName, field, message));
    }

   

    public String getErr() {
		return err;
	}

	public String getMsg() {
		return msg;
	}

	public List<FieldErrorVM> getFieldErrors() {
        return fieldErrors;
    }
}
