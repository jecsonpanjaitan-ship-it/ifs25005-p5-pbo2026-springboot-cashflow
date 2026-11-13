package org.delcom.ifs25005springbootcashflow.domain.exception;

/** Entitas tidak ditemukan berdasarkan ID. */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan");
    }
}
