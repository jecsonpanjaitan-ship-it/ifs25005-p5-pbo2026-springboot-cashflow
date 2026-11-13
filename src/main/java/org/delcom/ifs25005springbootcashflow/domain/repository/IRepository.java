package org.delcom.ifs25005springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port generik CRUD. save() menyisipkan entitas baru (id null) atau memperbarui yang sudah ada. */
public interface IRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findBy(Predicate<T> predicate);

    boolean deleteById(ID id);
}
