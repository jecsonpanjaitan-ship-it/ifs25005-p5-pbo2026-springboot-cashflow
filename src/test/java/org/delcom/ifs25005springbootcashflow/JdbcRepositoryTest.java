package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.RepositoryException;
import org.delcom.ifs25005springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tes repository JDBC memakai H2 in-memory. */
class JdbcRepositoryTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource = ds;
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource); // idempotent: IF NOT EXISTS
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    private static DataSource brokenDataSource() throws SQLException {
        DataSource broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("down"));
        return broken;
    }

    @Test
    void userRepositoryRoundTrip() {
        var admin = users.save("admin", "hash", "Administrator", Role.ADMIN);
        var andi = users.save("andi", "hash", "Andi", Role.USER);
        assertTrue(admin.getId() > 0);
        assertEquals("Andi", users.findById(andi.getId()).orElseThrow().getFullName());
        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
        assertTrue(users.findById(9999).isEmpty());
        assertTrue(users.findByUsername("nobody").isEmpty());
        assertEquals(2, users.findAll().size());

        andi.setFullName("Andi Baru");
        andi.setPasswordHash("hash2");
        andi.setPhotoPath("a.png");
        users.save(andi);
        var reloaded = users.findById(andi.getId()).orElseThrow();
        assertEquals("Andi Baru", reloaded.getFullName());
        assertEquals("hash2", reloaded.getPasswordHash());
        assertEquals("a.png", reloaded.getPhotoPath());
        assertNull(users.findById(admin.getId()).orElseThrow().getPhotoPath());

        assertEquals(1, users.findBy(u -> u.getRole() == Role.ADMIN).size());
        assertTrue(users.deleteById(andi.getId()));
        assertFalse(users.deleteById(andi.getId()));
    }

    @Test
    void transactionRepositoryRoundTrip() {
        var andi = users.save("andi", "h", "Andi", Role.USER);
        var budi = users.save("budi", "h", "Budi", Role.USER);
        var gaji = transactions.save(andi.getId(), "Gaji", 5_000_000L, TransactionType.INCOME, null);
        transactions.save(andi.getId(), "Makan", 25_000L, TransactionType.EXPENSE, "bukti.png");
        transactions.save(budi.getId(), "Lain", 1L, TransactionType.INCOME, null);

        assertTrue(gaji.getId() > 0);
        var found = transactions.findById(gaji.getId()).orElseThrow();
        assertEquals(5_000_000L, found.getAmount());
        assertEquals(TransactionType.INCOME, found.getType());
        assertNull(found.getProofPath());
        assertEquals(gaji.getCreatedAt().withNano(0), found.getCreatedAt().withNano(0));
        assertTrue(transactions.findById(9999).isEmpty());

        assertEquals(2, transactions.findByUserId(andi.getId()).size());
        assertEquals(3, transactions.findAll().size());
        assertEquals(1, transactions.findBy(t -> t.getAmount() == 1L).size());

        found.setDescription("Gaji revisi");
        found.setAmount(6_000_000L);
        found.setType(TransactionType.EXPENSE);
        found.setProofPath("p.png");
        transactions.save(found);
        var updated = transactions.findById(gaji.getId()).orElseThrow();
        assertEquals("Gaji revisi", updated.getDescription());
        assertEquals(6_000_000L, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("p.png", updated.getProofPath());

        assertTrue(transactions.deleteById(gaji.getId()));
        assertFalse(transactions.deleteById(gaji.getId()));
    }

    @Test
    void failuresAreWrappedInRepositoryException() throws Exception {
        DataSource broken = brokenDataSource();
        assertThrows(RepositoryException.class, () -> SchemaInitializer.initialize(broken));

        var brokenUsers = new JdbcUserRepository(broken);
        assertThrows(RepositoryException.class, () -> brokenUsers.save("a", "h", "A", Role.USER));
        assertThrows(RepositoryException.class, () -> brokenUsers.findById(1));
        assertThrows(RepositoryException.class, () -> brokenUsers.findByUsername("a"));
        assertThrows(RepositoryException.class, brokenUsers::findAll);
        assertThrows(RepositoryException.class, () -> brokenUsers.deleteById(1));

        var brokenTx = new JdbcTransactionRepository(broken);
        assertThrows(RepositoryException.class, () -> brokenTx.save(1, "a", 1, TransactionType.INCOME, null));
        assertThrows(RepositoryException.class, () -> brokenTx.findById(1));
        assertThrows(RepositoryException.class, () -> brokenTx.findByUserId(1));
        assertThrows(RepositoryException.class, brokenTx::findAll);
        assertThrows(RepositoryException.class, () -> brokenTx.deleteById(1));
    }
}
