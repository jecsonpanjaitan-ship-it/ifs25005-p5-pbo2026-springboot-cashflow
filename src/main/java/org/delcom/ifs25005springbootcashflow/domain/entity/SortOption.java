package org.delcom.ifs25005springbootcashflow.domain.entity;

import java.util.Comparator;

/** Opsi pengurutan transaksi; tiap opsi membawa Comparator. */
public enum SortOption {
    NEWEST("Terbaru", Comparator.comparing(Transaction::getCreatedAt).thenComparing(Transaction::getId).reversed()),
    OLDEST("Terlama", Comparator.comparing(Transaction::getCreatedAt).thenComparing(Transaction::getId)),
    AMOUNT_ASC("Jumlah: kecil ke besar", Comparator.comparingLong(Transaction::getAmount)),
    AMOUNT_DESC("Jumlah: besar ke kecil", Comparator.comparingLong(Transaction::getAmount).reversed()),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.getType() == TransactionType.INCOME ? 0 : 1)
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed())),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.getType() == TransactionType.EXPENSE ? 0 : 1)
                    .thenComparing(Comparator.comparingLong(Transaction::getAmount).reversed()));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}
