package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

/** (ADMIN) Transaksi milik pengguna lain, hanya baca. Pilih pengguna lewat ?userId= atau dropdown. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final transient UserUseCase userUseCase;
    private final transient FinanceUseCase financeUseCase;

    private final Select<User> userSelect = new Select<>();
    private final TextField search = new TextField("Cari keterangan");
    private final Select<SortOption> sort = new Select<>();
    private final SummaryCards summary;
    private final Grid<Transaction> grid;
    private final Paragraph emptyState = new Paragraph("Pilih pengguna untuk melihat transaksinya, atau pengguna ini belum punya transaksi.");
    private Integer adminId;
    private boolean updating;

    public UserTransactionsView(IUserRepository userRepository, UserUseCase userUseCase,
                                FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        setPadding(true);
        addClassName("page");

        H1 title = new H1("Transaksi Pengguna");
        title.addClassName("page-title");
        Paragraph note = new Paragraph("Tampilan hanya baca: Anda tidak dapat mengubah atau menghapus transaksi pengguna lain.");
        note.addClassName("muted");

        userSelect.setLabel("Pengguna");
        userSelect.setItemLabelGenerator(u -> u.getFullName() + " (" + u.getUsername() + ")");
        userSelect.addValueChangeListener(e -> refresh());

        search.setPlaceholder("Ketik untuk mencari...");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());
        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setValue(SortOption.NEWEST);
        sort.addValueChangeListener(e -> refresh());

        FlexLayout toolbar = new FlexLayout(userSelect, search, sort);
        toolbar.addClassName("toolbar");
        summary = new SummaryCards(presenter);
        grid = TransactionGrid.create(presenter, null, null);
        emptyState.addClassName("empty-state");

        add(title, note, summary, new PageCard(toolbar), new PageCard(grid, emptyState));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User admin = AccessGuard.requireAdmin(event, userRepository);
        if (admin == null) {
            return;
        }
        adminId = admin.getId();
        List<User> users = userUseCase.listUsers();
        updating = true;
        userSelect.setItems(users);
        event.getLocation().getQueryParameters().getSingleParameter("userId").ifPresent(raw -> {
            try {
                userSelect.setValue(userUseCase.getUser(Integer.valueOf(raw)));
            } catch (NumberFormatException | DomainException ex) {
                Notifier.error("Pengguna tidak ditemukan");
            }
        });
        updating = false;
        refresh();
    }

    private void refresh() {
        if (updating || adminId == null) {
            return;
        }
        User target = userSelect.getValue();
        if (target == null) {
            grid.setItems(List.of());
            grid.setVisible(false);
            emptyState.setVisible(true);
            summary.update(new org.delcom.ifs25005springbootcashflow.domain.entity.Balance(0, 0, 0));
            return;
        }
        try {
            List<Transaction> items = financeUseCase.listFor(adminId, target.getId(), search.getValue(), sort.getValue());
            grid.setItems(items);
            grid.setVisible(!items.isEmpty());
            emptyState.setVisible(items.isEmpty());
            summary.update(financeUseCase.getBalance(target.getId()));
        } catch (DomainException ex) {
            Notifier.error(ex.getMessage());
        }
    }
}
