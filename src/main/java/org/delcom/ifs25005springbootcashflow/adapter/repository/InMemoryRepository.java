package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Basis generik repository in-memory (untuk unit test). */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {

    protected final Map<ID, T> store = new LinkedHashMap<>();

    protected abstract ID idOf(T entity);

    protected abstract void assignId(T entity);

    @Override
    public T save(T entity) {
        if (idOf(entity) == null) {
            assignId(entity);
        }
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return store.values().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(ID id) {
        return store.remove(id) != null;
    }
}
