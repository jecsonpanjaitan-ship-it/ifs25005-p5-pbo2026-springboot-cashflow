package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Comparator;
import java.util.Map;

/** (ADMIN) Daftar seluruh pengguna beserta ringkasan saldo. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final transient UserUseCase userUseCase;
    private final transient FinanceUseCase financeUseCase;
    private final transient FinancePresenter presenter;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(IUserRepository userRepository, UserUseCase userUseCase,
                     FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        setPadding(true);
        addClassName("page");

        H1 title = new H1("Daftar Pengguna");
        title.addClassName("page-title");
        GridStyles.apply(grid, "Daftar pengguna");

        grid.addColumn(User::getId).setHeader("ID").setKey("id")
                .setComparator(Comparator.comparing(User::getId)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Avatar avatar = new Avatar(u.getFullName());
            if (u.getPhotoPath() != null) {
                avatar.setImage("/uploads/" + u.getPhotoPath());
            }
            Span name = new Span(u.getFullName());
            name.addClassName("user-cell-name");
            var cell = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(avatar, name);
            cell.setAlignItems(Alignment.CENTER);
            return cell;
        })).setHeader("Nama").setKey("fullName")
                .setComparator(Comparator.comparing(u -> u.getFullName().toLowerCase())).setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(User::getUsername).setHeader("Username").setKey("username")
                .setComparator(Comparator.comparing(User::getUsername)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Span badge = new Span(u.isAdmin() ? "Admin" : "User");
            badge.addClassNames("type-badge", u.isAdmin() ? "role-admin" : "role-user");
            return badge;
        })).setHeader("Role").setKey("role").setAutoWidth(true);
        grid.addColumn(u -> financeUseCase.getBalance(u.getId()).count()).setHeader("Transaksi")
                .setKey("count").setAutoWidth(true);
        grid.addColumn(u -> presenter.formatMoney(financeUseCase.getBalance(u.getId()).total()))
                .setHeader("Saldo").setKey("balance").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Button view = new Button("Lihat Transaksi", e -> UI.getCurrent().navigate(UserTransactionsView.class,
                    QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            view.setAriaLabel("Lihat transaksi " + u.getFullName());
            return view;
        })).setHeader("Tindakan").setKey("actions").setAutoWidth(true).setFlexGrow(0);

        add(title, new PageCard(grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AccessGuard.requireAdmin(event, userRepository) != null) {
            grid.setItems(userUseCase.listUsers());
        }
    }
}
