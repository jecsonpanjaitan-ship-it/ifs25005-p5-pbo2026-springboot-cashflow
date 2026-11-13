package org.delcom.ifs25005springbootcashflow.domain.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {

    User save(String username, String passwordHash, String fullName, Role role);

    Optional<User> findByUsername(String username);
}
