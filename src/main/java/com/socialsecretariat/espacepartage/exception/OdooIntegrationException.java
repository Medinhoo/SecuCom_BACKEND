package com.socialsecretariat.espacepartage.exception;

public class OdooIntegrationException extends RuntimeException {
    
    public OdooIntegrationException(String message) {
        super(message);
    }
    
    public OdooIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
