package org.delcom.ifs25005springbootcashflow.adapter.presenter;

import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang, baris, saldo, tanggal, dan pesan. Bukan tanggung jawab entity. */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.of("id", "ID"));

    /** 1000000 -> "Rp 1,000,000.00" */
    public String formatMoney(long amount) {
        return "Rp " + String.format(Locale.US, "%,.2f", (double) amount);
    }

    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** "[1] Gaji | Rp 1,000,000.00 | [Pemasukan]" */
    public String formatRow(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + formatMoney(t.getAmount())
                + " | [" + typeLabel(t.getType()) + "]";
    }

    /** "Saldo: Rp 4,975,000.00" */
    public String formatBalance(Balance balance) {
        return "Saldo: " + formatMoney(balance.total());
    }

    public String formatDate(LocalDateTime dateTime) {
        return DATE_FORMAT.format(dateTime);
    }

    public String addedMessage(Transaction t) {
        return typeLabel(t.getType()) + " berhasil dicatat: " + formatMoney(t.getAmount());
    }

    public String updatedMessage(Transaction t) {
        return "Transaksi [" + t.getId() + "] berhasil diperbarui";
    }

    public String deletedMessage(Integer id) {
        return "Transaksi [" + id + "] berhasil dihapus";
    }

    public String errorMessage(DomainException e) {
        return e.getMessage();
    }
}
