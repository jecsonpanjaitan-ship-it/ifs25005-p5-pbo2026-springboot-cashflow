package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.Comparator;
import java.util.function.Consumer;

/** Grid transaksi yang dipakai FinanceView (editable) dan UserTransactionsView (read-only). */
public final class TransactionGrid {

    private TransactionGrid() {
    }

    public static Grid<Transaction> create(FinancePresenter presenter,
                                           Consumer<Transaction> onEdit,
                                           Consumer<Transaction> onDelete) {
        Grid<Transaction> grid = new Grid<>(Transaction.class, false);
        GridStyles.apply(grid, "Daftar transaksi");

        grid.addColumn(Transaction::getId).setHeader("ID").setKey("id")
                .setComparator(Comparator.comparing(Transaction::getId)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setKey("description")
                .setComparator(Comparator.comparing(t -> t.getDescription().toLowerCase()))
                .setFlexGrow(2).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Span amount = new Span(presenter.formatMoney(t.getAmount()));
            amount.addClassName(t.getType() == TransactionType.INCOME ? "amount-income" : "amount-expense");
            return amount;
        })).setHeader("Jumlah").setKey("amount")
                .setComparator(Comparator.comparingLong(Transaction::getAmount)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Span badge = new Span(presenter.typeLabel(t.getType()));
            badge.addClassNames("type-badge", t.getType() == TransactionType.INCOME ? "income" : "expense");
            return badge;
        })).setHeader("Tipe").setKey("type")
                .setComparator(Comparator.comparing(Transaction::getType)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> proofCell(t))).setHeader("Bukti").setKey("proof")
                .setAutoWidth(true);
        grid.addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu").setKey("createdAt")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);

        if (onEdit != null && onDelete != null) {
            grid.addColumn(new ComponentRenderer<>(t -> {
                Button edit = new Button(VaadinIcon.EDIT.create(), e -> onEdit.accept(t));
                edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                edit.setAriaLabel("Ubah transaksi " + t.getId());
                edit.setTooltipText("Ubah");
                Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(t));
                delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                delete.setAriaLabel("Hapus transaksi " + t.getId());
                delete.setTooltipText("Hapus");
                return new HorizontalLayout(edit, delete);
            })).setHeader("Tindakan").setKey("actions").setAutoWidth(true).setFlexGrow(0);
        }
        return grid;
    }

    private static com.vaadin.flow.component.Component proofCell(Transaction t) {
        if (t.getProofPath() == null) {
            Span none = new Span("-");
            none.addClassName("muted");
            none.getElement().setAttribute("aria-label", "Tidak ada bukti");
            return none;
        }
        String url = "/uploads/" + t.getProofPath();
        Image thumb = new Image(url, "Bukti transaksi " + t.getId());
        thumb.addClassName("proof-thumb");
        Button button = new Button(thumb,
                e -> new ImagePreviewDialog("Bukti Transaksi #" + t.getId(), url).open());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.setAriaLabel("Lihat bukti transaksi " + t.getId());
        return button;
    }
}
