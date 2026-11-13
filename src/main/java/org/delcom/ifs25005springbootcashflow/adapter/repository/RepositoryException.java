package org.delcom.ifs25005springbootcashflow.adapter.repository;

/** Kegagalan teknis akses data (membungkus SQLException). */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
