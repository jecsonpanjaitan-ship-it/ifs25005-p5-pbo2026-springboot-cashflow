package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income =
            new Transaction(1, 1, "Gaji", 1_000_000, TransactionType.INCOME, null, LocalDateTime.of(2026, 3, 5, 9, 7));
    private final Transaction expense =
            new Transaction(2, 1, "Makan", 25_000, TransactionType.EXPENSE, null, LocalDateTime.of(2026, 3, 5, 9, 7));

    @Test
    void formatsMoneyWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Rp -25,000.00", presenter.formatMoney(-25_000));
    }

    @Test
    void formatsRowAndBalanceAsSpecified() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.formatRow(income));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.formatRow(expense));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new Balance(2, 5_000_000, 25_000)));
    }

    @Test
    void formatsDatesAndMessages() {
        assertEquals("05", presenter.formatDate(income.getCreatedAt()).substring(0, 2));
        assertEquals("Pemasukan berhasil dicatat: Rp 1,000,000.00", presenter.addedMessage(income));
        assertEquals("Pengeluaran berhasil dicatat: Rp 25,000.00", presenter.addedMessage(expense));
        assertEquals("Transaksi [1] berhasil diperbarui", presenter.updatedMessage(income));
        assertEquals("Transaksi [7] berhasil dihapus", presenter.deletedMessage(7));
        assertEquals("gagal", presenter.errorMessage(new ValidationException("gagal")));
    }
}
