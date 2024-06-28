package de.samuelgesang.backend.exceptions;

public class SitemapException extends Exception {

    public SitemapException(String message) {
        super(message);
    }

    public SitemapException(String message, Throwable cause) {
        super(message, cause);
    }
}
