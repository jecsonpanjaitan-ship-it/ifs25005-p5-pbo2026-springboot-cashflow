package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25005springbootcashflow.adapter.repository.InMemoryTransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.InMemoryUserRepository;
import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseCaseTest {

    private InMemoryUserRepository users;
    private InMemoryTransactionRepository transactions;
    private AuthUseCase auth;
    private UserUseCase userUseCase;
    private FinanceUseCase finance;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        transactions = new InMemoryTransactionRepository();
        auth = new AuthUseCase(users);
        userUseCase = new UserUseCase(users);
        finance = new FinanceUseCase(transactions, users);
    }

    // ---------- AuthUseCase ----------

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        User u = auth.register("  budi ", "rahasia", " Budi Santoso ");
        assertEquals("budi", u.getUsername());
        assertEquals("Budi Santoso", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals(PasswordHasher.hash("rahasia"), u.getPasswordHash());
    }

    @Test
    void registerValidatesInput() throws Exception {
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia", "Budi"));
        assertThrows(ValidationException.class, () -> auth.register("ab", "rahasia", "Budi"));
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", "  "));
        assertThrows(ValidationException.class, () -> auth.register("budi", null, "Budi"));
        assertThrows(ValidationException.class, () -> auth.register("budi", "abc", "Budi"));
        auth.register("budi", "rahasia", "Budi");
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", "Budi 2"));
    }

    @Test
    void loginChecksCredentials() throws Exception {
        auth.register("budi", "rahasia", "Budi");
        assertEquals("budi", auth.login(" budi ", "rahasia").getUsername());
        assertThrows(ValidationException.class, () -> auth.login(null, "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
    }

    @Test
    void ensureRootAdminIsIdempotent() {
        User first = auth.ensureRootAdmin("admin", "admin");
        User second = auth.ensureRootAdmin("admin", "lain");
        assertEquals(Role.ADMIN, first.getRole());
        assertSame(first, second);
        assertEquals(1, users.findAll().size());
    }

    // ---------- UserUseCase ----------

    @Test
    void userUseCaseListsGetsAndUpdates() throws Exception {
        User a = auth.register("andi", "rahasia", "Andi");
        User b = auth.register("budi", "rahasia", "Budi");
        assertEquals(List.of(a.getId(), b.getId()), userUseCase.listUsers().stream().map(User::getId).toList());

        assertEquals("Andi", userUseCase.getUser(a.getId()).getFullName());
        assertThrows(ValidationException.class, () -> userUseCase.getUser(null));
        assertThrows(ValidationException.class, () -> userUseCase.getUser(0));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.getUser(999));

        String oldHash = a.getPasswordHash();
        assertEquals("Andi Baru", userUseCase.updateProfile(a.getId(), " Andi Baru ", null).getFullName());
        assertEquals(oldHash, users.findById(a.getId()).orElseThrow().getPasswordHash());
        userUseCase.updateProfile(a.getId(), "Andi Baru", "");
        assertEquals(oldHash, users.findById(a.getId()).orElseThrow().getPasswordHash());
        userUseCase.updateProfile(a.getId(), "Andi Baru", "passbaru");
        assertEquals(PasswordHasher.hash("passbaru"), users.findById(a.getId()).orElseThrow().getPasswordHash());
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(a.getId(), null, null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(a.getId(), "  ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(a.getId(), "Andi", "abc"));

        assertEquals("foto.png", userUseCase.updatePhoto(a.getId(), "foto.png").getPhotoPath());
        assertNull(userUseCase.updatePhoto(a.getId(), null).getPhotoPath());
    }

    // ---------- FinanceUseCase ----------

    private User newUser(String name) throws Exception {
        return auth.register(name, "rahasia", name.toUpperCase());
    }

    @Test
    void addIncomeAndExpenseAffectBalance() throws Exception {
        User u = newUser("andi");
        Transaction gaji = finance.addIncome(u.getId(), " Gaji ", "5000000", null);
        Transaction makan = finance.addExpense(u.getId(), "Makan", "25000", "bukti.png");
        assertEquals("Gaji", gaji.getDescription());
        assertEquals(TransactionType.INCOME, gaji.getType());
        assertEquals("bukti.png", makan.getProofPath());
        Balance balance = finance.getBalance(u.getId());
        assertEquals(new Balance(2, 5_000_000, 25_000), balance);
        assertEquals(4_975_000, balance.total());
    }

    @Test
    void addValidatesInput() throws Exception {
        User u = newUser("andi");
        assertThrows(ValidationException.class, () -> finance.addIncome(null, "x", "1", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(0, "x", "1", null));
        assertThrows(EntityNotFoundException.class, () -> finance.addIncome(999, "x", "1", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), null, "1", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "  ", "1", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "x", null, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "x", "  ", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "x", "abc", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "x", "12.5", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(u.getId(), "x", "0", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(u.getId(), "x", "-5", null));
        assertEquals(0, finance.getBalance(u.getId()).count());
    }

    @Test
    void searchIsCaseInsensitiveScopedAndSorted() throws Exception {
        User a = newUser("andi");
        User b = newUser("budi");
        finance.addIncome(a.getId(), "Gaji Bulanan", "1000", null);
        finance.addExpense(a.getId(), "makan siang", "50", null);
        finance.addExpense(a.getId(), "Makan malam", "75", null);
        finance.addIncome(b.getId(), "Gaji budi", "9999", null);

        assertEquals(3, finance.list(a.getId()).size());
        assertEquals(2, finance.search(a.getId(), "MAKAN", SortOption.AMOUNT_ASC).size());
        assertEquals(50, finance.search(a.getId(), " makan ", SortOption.AMOUNT_ASC).get(0).getAmount());
        assertEquals(75, finance.search(a.getId(), "makan", SortOption.AMOUNT_DESC).get(0).getAmount());
        assertEquals(3, finance.search(a.getId(), "", null).size());
        assertEquals(0, finance.search(a.getId(), "tidak ada", SortOption.NEWEST).size());
        assertEquals(1, finance.search(b.getId(), null, SortOption.NEWEST).size());
    }

    @Test
    void updateAndDeleteOnlyOwnTransactions() throws Exception {
        User a = newUser("andi");
        User b = newUser("budi");
        Transaction t = finance.addExpense(a.getId(), "Bensin", "20000", "lama.png");

        Transaction same = finance.update(a.getId(), t.getId(), "Bensin motor", "30000", TransactionType.EXPENSE, null);
        assertEquals("Bensin motor", same.getDescription());
        assertEquals(30000, same.getAmount());
        assertEquals("lama.png", same.getProofPath());
        assertEquals("baru.png",
                finance.update(a.getId(), t.getId(), "Bensin motor", "30000", TransactionType.INCOME, "baru.png")
                        .getProofPath());

        assertThrows(ValidationException.class,
                () -> finance.update(b.getId(), t.getId(), "x", "1", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.delete(b.getId(), t.getId()));
        assertThrows(ValidationException.class, () -> finance.delete(a.getId(), null));
        assertThrows(ValidationException.class, () -> finance.delete(a.getId(), 0));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(a.getId(), 999));
        assertThrows(ValidationException.class, () -> finance.delete(null, t.getId()));

        finance.delete(a.getId(), t.getId());
        assertEquals(0, finance.getBalance(a.getId()).count());
    }

    @Test
    void listForEnforcesViewerRules() throws Exception {
        User admin = auth.ensureRootAdmin("admin", "admin");
        User a = newUser("andi");
        User b = newUser("budi");
        finance.addIncome(a.getId(), "Gaji", "1000", null);

        assertEquals(1, finance.listFor(admin.getId(), a.getId(), null, SortOption.NEWEST).size());
        assertEquals(1, finance.listFor(a.getId(), a.getId(), "gaji", SortOption.NEWEST).size());
        assertThrows(ValidationException.class, () -> finance.listFor(b.getId(), a.getId(), null, null));
        assertThrows(EntityNotFoundException.class, () -> finance.listFor(admin.getId(), 999, null, null));
    }

    @Test
    void parseAmountAcceptsOnlyPositiveIntegers() throws Exception {
        assertEquals(15, FinanceUseCase.parseAmount(" 15 "));
        assertTrue(assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("99999999999999999999"))
                .getMessage().contains("angka"));
        assertFalse(FinanceUseCase.parseAmount("1") < 1);
    }
}
