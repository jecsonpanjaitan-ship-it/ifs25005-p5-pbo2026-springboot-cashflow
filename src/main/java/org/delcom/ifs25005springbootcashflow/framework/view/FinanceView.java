package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Dashboard cashflow pribadi: ringkasan saldo, cari, urut, tambah, ubah, hapus. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow | Catatan Keuangan")
public class FinanceView extends VerticalLayout {

    private final transient IUserRepository userRepository;
    private final transient FinanceUseCase financeUseCase;
    private final transient FinancePresenter presenter;
    private final transient FileStorage fileStorage;

    private final SummaryCards summary;
    private final TextField search = new TextField("Cari keterangan");
    private final Select<SortOption> sort = new Select<>();
    private final Grid<Transaction> grid;
    private final Paragraph emptyState = new Paragraph("Belum ada transaksi. Tambahkan pemasukan atau pengeluaran pertama Anda.");
    private Integer userId;

    public FinanceView(IUserRepository userRepository, FinanceUseCase financeUseCase,
                       FinancePresenter presenter, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        setPadding(true);
        addClassName("page");

        H1 title = new H1("Catatan Keuangan");
        title.addClassName("page-title");
        summary = new SummaryCards(presenter);

        search.setPlaceholder("Ketik untuk mencari...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());
        search.addClassName("toolbar-search");

        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setValue(SortOption.NEWEST);
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> {
            refresh();
            Notifier.success("Data dimuat ulang");
        });
        Button addIncome = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openForm(TransactionType.INCOME, null));
        addIncome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIncome.addClassName("btn-income");
        Button addExpense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openForm(TransactionType.EXPENSE, null));
        addExpense.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addExpense.addClassName("btn-expense");

        FlexLayout toolbar = new FlexLayout(search, sort, reload, addIncome, addExpense);
        toolbar.addClassName("toolbar");

        grid = TransactionGrid.create(presenter, t -> openForm(t.getType(), t), this::confirmDelete);
        emptyState.addClassName("empty-state");

        add(title, summary, new PageCard(toolbar), new PageCard(grid, emptyState));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            userId = user.getId();
            refresh();
        }
    }

    private void refresh() {
        if (userId == null) {
            return;
        }
        var items = financeUseCase.search(userId, search.getValue(), sort.getValue());
        grid.setItems(items);
        emptyState.setVisible(items.isEmpty());
        grid.setVisible(!items.isEmpty());
        summary.update(financeUseCase.getBalance(userId));
    }

    private void openForm(TransactionType type, Transaction existing) {
        new TransactionFormDialog(userId, type, existing, financeUseCase, presenter, fileStorage,
                this::refresh).open();
    }

    private void confirmDelete(Transaction transaction) {
        ModalDialog dialog = new ModalDialog("Hapus Transaksi", "Konfirmasi");
        dialog.setWidth("min(92vw, 480px)");
        Paragraph question = new Paragraph("Yakin ingin menghapus transaksi berikut? Tindakan ini tidak dapat dibatalkan.");
        Span row = new Span(presenter.formatRow(transaction));
        row.addClassName("confirm-row");
        dialog.addBody(new VerticalLayout(question, row));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addClassName("btn-expense");
        delete.addClickListener(e -> {
            dialog.setBusy(true);
            try {
                financeUseCase.delete(userId, transaction.getId());
                fileStorage.delete(transaction.getProofPath());
                dialog.close();
                refresh();
                Notifier.success(presenter.deletedMessage(transaction.getId()) + " | "
                        + presenter.formatBalance(financeUseCase.getBalance(userId)));
            } catch (DomainException ex) {
                Notifier.error(presenter.errorMessage(ex));
            } finally {
                dialog.setBusy(false);
            }
        });
        dialog.addActions(cancel, delete);
        dialog.open();
    }
}
