package com.babel.venus.web.rest.errors;

import java.io.Serializable;
import java.util.Map;

/**
 * View Model for sending a parameterized error message.
 */
public class ParameterizedErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String err;
    private final Map<String, String> paramMap;

    public ParameterizedErrorVM(String err, Map<String, String> paramMap) {
        this.err = err;
        this.paramMap = paramMap;
    }

    
    public String getErr() {
		return err;
	}


	public Map<String, String> getParams() {
        return paramMap;
    }

}
