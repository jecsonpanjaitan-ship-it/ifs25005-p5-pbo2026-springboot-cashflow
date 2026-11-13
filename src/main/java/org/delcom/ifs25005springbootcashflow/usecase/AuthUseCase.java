package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {

    public static final int MIN_USERNAME = 3;
    public static final int MIN_PASSWORD = 4;

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        String name = clean(username);
        String full = clean(fullName);
        if (name.length() < MIN_USERNAME) {
            throw new ValidationException("Username minimal " + MIN_USERNAME + " karakter");
        }
        if (full.isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD + " karakter");
        }
        if (userRepository.findByUsername(name).isPresent()) {
            throw new ValidationException("Username '" + name + "' sudah digunakan");
        }
        return userRepository.save(name, PasswordHasher.hash(password), full, Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        String name = clean(username);
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        User user = userRepository.findByUsername(name).orElse(null);
        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat admin dari ROOT_* bila belum ada. Mengembalikan admin yang aktif. */
    public User ensureRootAdmin(String username, String password) {
        return userRepository.findByUsername(username).orElseGet(() ->
                userRepository.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
