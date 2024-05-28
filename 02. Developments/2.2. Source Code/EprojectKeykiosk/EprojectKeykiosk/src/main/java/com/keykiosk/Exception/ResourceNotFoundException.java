package com.keykiosk.Exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException() {
        super("Resource Not Found on Server");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
