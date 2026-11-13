package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcUserRepository implements IUserRepository {

    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        try (Connection c = dataSource.getConnection()) {
            if (user.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getFullName());
                    ps.setString(4, user.getRole().name());
                    ps.setString(5, user.getPhotoPath());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        user.setId(keys.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?")) {
                    ps.setString(1, user.getPasswordHash());
                    ps.setString(2, user.getFullName());
                    ps.setString(3, user.getPhotoPath());
                    ps.setInt(4, user.getId());
                    ps.executeUpdate();
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menyimpan pengguna", e);
        }
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(null, username, passwordHash, fullName, role, null));
    }

    @Override
    public Optional<User> findById(Integer id) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE username = ?", ps -> ps.setString(1, username));
    }

    @Override
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca pengguna", e);
        }
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menghapus pengguna", e);
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<User> queryOne(String sql, Binder binder) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca pengguna", e);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), Role.valueOf(rs.getString("role")), rs.getString("photo_path"));
    }
}
