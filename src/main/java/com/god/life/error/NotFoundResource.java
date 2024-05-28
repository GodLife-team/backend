package com.god.life.error;

public class NotFoundResource extends RuntimeException {

    public NotFoundResource() {
    }

    public NotFoundResource(String message) {
        super(message);
    }

    public NotFoundResource(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundResource(Throwable cause) {
        super(cause);
    }

    public NotFoundResource(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
