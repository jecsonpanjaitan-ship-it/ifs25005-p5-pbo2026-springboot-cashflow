package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.Comparator;
import java.util.List;

public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    public User getUser(Integer id) throws DomainException {
        if (id == null || id <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** Ubah nama dan (opsional) password. newPassword kosong/null berarti password tidak diubah. */
    public User updateProfile(Integer id, String fullName, String newPassword) throws DomainException {
        User user = getUser(id);
        String full = fullName == null ? "" : fullName.trim();
        if (full.isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        user.setFullName(full);
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < AuthUseCase.MIN_PASSWORD) {
                throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD + " karakter");
            }
            user.setPasswordHash(PasswordHasher.hash(newPassword));
        }
        return userRepository.save(user);
    }

    public User updatePhoto(Integer id, String photoPath) throws DomainException {
        User user = getUser(id);
        user.setPhotoPath(photoPath);
        return userRepository.save(user);
    }
}
