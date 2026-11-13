package org.delcom.ifs25005springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public class Transaction {

    private Integer id;
    private final Integer userId;
    private String description;
    private long amount;
    private TransactionType type;
    private String proofPath;
    private final LocalDateTime createdAt;

    public Transaction(Integer id, Integer userId, String description, long amount,
                       TransactionType type, String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public void setProofPath(String proofPath) {
        this.proofPath = proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Pemasukan bernilai positif, pengeluaran bernilai negatif. */
    public long signedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }
}
