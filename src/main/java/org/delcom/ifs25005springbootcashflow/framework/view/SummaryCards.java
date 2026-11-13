package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.Balance;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** Kartu ringkasan: total transaksi, pemasukan, pengeluaran, dan saldo. */
public class SummaryCards extends Div {

    private final FinancePresenter presenter;
    private final Span count = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        addClassName("summary-cards");
        add(card("Total Transaksi", count, "neutral"),
                card("Total Pemasukan", income, "income"),
                card("Total Pengeluaran", expense, "expense"),
                card("Saldo", balance, "balance"));
        update(new Balance(0, 0, 0));
    }

    public void update(Balance data) {
        count.setText(String.valueOf(data.count()));
        income.setText(presenter.formatMoney(data.income()));
        expense.setText(presenter.formatMoney(data.expense()));
        balance.setText(presenter.formatMoney(data.total()));
    }

    private static Div card(String label, Span value, String kind) {
        Span title = new Span(label);
        title.addClassName("summary-label");
        value.addClassName("summary-value");
        Div card = new Div(title, value);
        card.addClassNames("summary-card", kind);
        return card;
    }
}
