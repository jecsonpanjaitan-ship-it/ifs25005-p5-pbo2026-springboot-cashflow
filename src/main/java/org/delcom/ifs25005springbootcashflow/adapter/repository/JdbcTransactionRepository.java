package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(Transaction t) {
        try (Connection c = dataSource.getConnection()) {
            if (t.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, t.getUserId());
                    ps.setString(2, t.getDescription());
                    ps.setLong(3, t.getAmount());
                    ps.setString(4, t.getType().name());
                    ps.setString(5, t.getProofPath());
                    ps.setTimestamp(6, Timestamp.valueOf(t.getCreatedAt()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        t.setId(keys.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?")) {
                    ps.setString(1, t.getDescription());
                    ps.setLong(2, t.getAmount());
                    ps.setString(3, t.getType().name());
                    ps.setString(4, t.getProofPath());
                    ps.setInt(5, t.getId());
                    ps.executeUpdate();
                }
            }
            return t;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menyimpan transaksi", e);
        }
    }

    @Override
    public Transaction save(Integer userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(null, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        List<Transaction> rows = query("SELECT " + COLUMNS + " FROM transactions WHERE id = ?", ps -> ps.setInt(1, id));
        return rows.stream().findFirst();
    }

    @Override
    public List<Transaction> findByUserId(Integer userId) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id",
                ps -> ps.setInt(1, userId));
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", ps -> { });
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menghapus transaksi", e);
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Transaction> query(String sql, Binder binder) {
        List<Transaction> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                            rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")),
                            rs.getString("proof_path"), rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca transaksi", e);
        }
    }
}
