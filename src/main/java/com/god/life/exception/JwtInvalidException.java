package com.god.life.exception;

public class JwtInvalidException extends RuntimeException {

    public JwtInvalidException() {
    }

    public JwtInvalidException(String message) {
        super(message);
    }

    public JwtInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtInvalidException(Throwable cause) {
        super(cause);
    }

    public JwtInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
