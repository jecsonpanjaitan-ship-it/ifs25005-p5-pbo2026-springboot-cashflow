package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 8, 0);

    private static Transaction tx(int id, long amount, TransactionType type, int minutes) {
        return new Transaction(id, 1, "t" + id, amount, type, null, T0.plusMinutes(minutes));
    }

    private static List<Integer> ids(SortOption option, List<Transaction> input) {
        return input.stream().sorted(option.getComparator()).map(Transaction::getId).toList();
    }

    @Test
    void passwordHasherIsSha256Hex() {
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", PasswordHasher.hash("hello"));
        assertTrue(PasswordHasher.matches("hello", PasswordHasher.hash("hello")));
        assertFalse(PasswordHasher.matches("other", PasswordHasher.hash("hello")));
    }

    @Test
    void exceptionsCarryMessages() {
        assertEquals("x", new ValidationException("x").getMessage());
        assertEquals("Pengguna dengan ID 7 tidak ditemukan", new EntityNotFoundException("Pengguna", 7).getMessage());
    }

    @Test
    void userAccessors() {
        User u = new User(null, "bob", "h", "Bob", Role.USER, null);
        u.setId(5);
        u.setPasswordHash("h2");
        u.setFullName("Bobby");
        u.setPhotoPath("p.png");
        assertEquals(5, u.getId());
        assertEquals("bob", u.getUsername());
        assertEquals("h2", u.getPasswordHash());
        assertEquals("Bobby", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals("p.png", u.getPhotoPath());
        assertFalse(u.isAdmin());
        assertTrue(new User(1, "a", "h", "A", Role.ADMIN, null).isAdmin());
    }

    @Test
    void transactionAccessorsAndSignedAmount() {
        Transaction t = new Transaction(null, 2, "gaji", 100, TransactionType.INCOME, null, T0);
        t.setId(9);
        t.setDescription("bonus");
        t.setAmount(250);
        t.setProofPath("a.png");
        assertEquals(9, t.getId());
        assertEquals(2, t.getUserId());
        assertEquals("bonus", t.getDescription());
        assertEquals(250, t.getAmount());
        assertEquals("a.png", t.getProofPath());
        assertEquals(T0, t.getCreatedAt());
        assertEquals(250, t.signedAmount());
        t.setType(TransactionType.EXPENSE);
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals(-250, t.signedAmount());
        assertNull(new Transaction(1, 1, "d", 1, TransactionType.INCOME, null, T0).getProofPath());
    }

    @Test
    void balanceTotalIsIncomeMinusExpense() {
        Balance b = new Balance(3, 5_000_000, 25_000);
        assertEquals(4_975_000, b.total());
        assertEquals(3, b.count());
    }

    @Test
    void sortOptionsOrderAsLabelled() {
        List<Transaction> data = List.of(
                tx(1, 300, TransactionType.EXPENSE, 0),
                tx(2, 100, TransactionType.INCOME, 10),
                tx(3, 200, TransactionType.INCOME, 20),
                tx(4, 50, TransactionType.EXPENSE, 30));
        assertEquals(List.of(4, 3, 2, 1), ids(SortOption.NEWEST, data));
        assertEquals(List.of(1, 2, 3, 4), ids(SortOption.OLDEST, data));
        assertEquals(List.of(4, 2, 3, 1), ids(SortOption.AMOUNT_ASC, data));
        assertEquals(List.of(1, 3, 2, 4), ids(SortOption.AMOUNT_DESC, data));
        assertEquals(List.of(3, 2, 1, 4), ids(SortOption.INCOME_FIRST, data));
        assertEquals(List.of(1, 4, 3, 2), ids(SortOption.EXPENSE_FIRST, data));
        for (SortOption option : SortOption.values()) {
            assertFalse(option.getLabel().isBlank());
        }
    }
}
