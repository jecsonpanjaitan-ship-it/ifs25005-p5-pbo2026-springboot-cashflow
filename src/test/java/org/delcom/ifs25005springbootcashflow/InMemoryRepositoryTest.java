package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25005springbootcashflow.adapter.repository.InMemoryTransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.InMemoryUserRepository;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

class InMemoryRepositoryTest {

    @Test
    void userRepositoryCrud() {
        var repo = new InMemoryUserRepository();
        var a = repo.save("andi", "h", "Andi", Role.USER);
        var b = repo.save("budi", "h", "Budi", Role.ADMIN);
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals("andi", repo.findById(1).orElseThrow().getUsername());
        assertTrue(repo.findById(99).isEmpty());
        assertEquals(2, repo.findAll().size());
        assertEquals("budi", repo.findByUsername("budi").orElseThrow().getUsername());
        assertTrue(repo.findByUsername("x").isEmpty());
        a.setFullName("Andi 2");
        repo.save(a);
        assertEquals(2, repo.findAll().size());
        assertEquals("Andi 2", repo.findById(1).orElseThrow().getFullName());
        assertEquals(1, repo.findBy(u -> u.getRole() == Role.ADMIN).size());
        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }

    @Test
    void transactionRepositoryCrud() {
        var repo = new InMemoryTransactionRepository();
        Transaction t1 = repo.save(1, "gaji", 100, TransactionType.INCOME, null);
        repo.save(2, "makan", 50, TransactionType.EXPENSE, "a.png");
        assertEquals(1, t1.getId());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(0, repo.findByUserId(3).size());
        assertEquals(2, repo.findAll().size());
        assertTrue(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }
}
