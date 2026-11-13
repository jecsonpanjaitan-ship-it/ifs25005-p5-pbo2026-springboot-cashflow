package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryUserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {

    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    protected Integer idOf(User entity) {
        return entity.getId();
    }

    @Override
    protected void assignId(User entity) {
        entity.setId(sequence.incrementAndGet());
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(null, username, passwordHash, fullName, role, null));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
