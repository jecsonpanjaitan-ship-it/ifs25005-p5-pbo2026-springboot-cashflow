package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.List;
import java.util.Locale;

/** Proses bisnis cashflow, selalu dibatasi per pengguna. Tanpa I/O UI. */
public class FinanceUseCase {

    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository, IUserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public Transaction addIncome(Integer userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(Integer userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.EXPENSE, proofPath);
    }

    public Transaction add(Integer userId, String description, String amountText,
                           TransactionType type, String proofPath) throws DomainException {
        requireUser(userId);
        String desc = requireDescription(description);
        long amount = parseAmount(amountText);
        return transactionRepository.save(userId, desc, amount, type, proofPath);
    }

    /** proofPath null berarti bukti lama dipertahankan. */
    public Transaction update(Integer requesterId, Integer transactionId, String description,
                              String amountText, TransactionType type, String proofPath) throws DomainException {
        Transaction transaction = findOwned(requesterId, transactionId);
        transaction.setDescription(requireDescription(description));
        transaction.setAmount(parseAmount(amountText));
        transaction.setType(type);
        if (proofPath != null) {
            transaction.setProofPath(proofPath);
        }
        return transactionRepository.save(transaction);
    }

    public void delete(Integer requesterId, Integer transactionId) throws DomainException {
        findOwned(requesterId, transactionId);
        transactionRepository.deleteById(transactionId);
    }

    public List<Transaction> list(Integer userId) {
        return search(userId, null, SortOption.NEWEST);
    }

    /** Cari keterangan (case-insensitive) lalu urutkan. keyword kosong berarti semua. */
    public List<Transaction> search(Integer userId, String keyword, SortOption sort) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        SortOption option = sort == null ? SortOption.NEWEST : sort;
        return transactionRepository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(option.getComparator())
                .toList();
    }

    /**
     * Daftar transaksi milik targetUserId yang dilihat oleh viewerId.
     * USER hanya boleh melihat miliknya sendiri; ADMIN boleh melihat milik siapa pun (read-only).
     */
    public List<Transaction> listFor(Integer viewerId, Integer targetUserId, String keyword, SortOption sort)
            throws DomainException {
        User viewer = requireUser(viewerId);
        User target = requireUser(targetUserId);
        if (!viewer.isAdmin() && !viewer.getId().equals(target.getId())) {
            throw new ValidationException("Anda tidak berhak melihat transaksi pengguna lain");
        }
        return search(target.getId(), keyword, sort);
    }

    public Balance getBalance(Integer userId) {
        List<Transaction> all = transactionRepository.findByUserId(userId);
        long income = all.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount).sum();
        long expense = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount).sum();
        return new Balance(all.size(), income, expense);
    }

    public static long parseAmount(String amountText) throws ValidationException {
        String text = amountText == null ? "" : amountText.trim();
        if (text.isEmpty()) {
            throw new ValidationException("Jumlah wajib diisi");
        }
        long amount;
        try {
            amount = Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat, contoh: 150000");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return amount;
    }

    private static String requireDescription(String description) throws ValidationException {
        String desc = description == null ? "" : description.trim();
        if (desc.isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return desc;
    }

    private User requireUser(Integer userId) throws DomainException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Pengguna", userId));
    }

    private Transaction findOwned(Integer requesterId, Integer transactionId) throws DomainException {
        requireUser(requesterId);
        if (transactionId == null || transactionId <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", transactionId));
        if (!transaction.getUserId().equals(requesterId)) {
            throw new ValidationException("Anda hanya dapat mengubah atau menghapus transaksi milik sendiri");
        }
        return transaction;
    }
}
