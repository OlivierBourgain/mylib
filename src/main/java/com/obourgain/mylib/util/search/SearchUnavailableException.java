package com.obourgain.mylib.util.search;

public class SearchUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public SearchUnavailableException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public SearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
