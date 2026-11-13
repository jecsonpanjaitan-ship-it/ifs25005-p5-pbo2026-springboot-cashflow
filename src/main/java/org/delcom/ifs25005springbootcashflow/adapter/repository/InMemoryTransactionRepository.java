package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    protected Integer idOf(Transaction entity) {
        return entity.getId();
    }

    @Override
    protected void assignId(Transaction entity) {
        entity.setId(sequence.incrementAndGet());
    }

    @Override
    public Transaction save(Integer userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(null, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(Integer userId) {
        return findBy(t -> t.getUserId().equals(userId));
    }
}
