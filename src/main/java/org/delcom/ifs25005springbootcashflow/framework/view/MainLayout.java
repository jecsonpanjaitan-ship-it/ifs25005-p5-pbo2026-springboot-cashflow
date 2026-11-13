package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** Layout utama: sidebar, menu avatar (Profil / Keluar), dan pengecekan login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final Div sidebar = new Div();
    private final Div userMenuHolder = new Div();
    private final Main main = new Main();

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        setPrimarySection(Section.DRAWER);

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Buka atau tutup menu navigasi");
        Span brand = new Span("Cashflow");
        brand.addClassName("brand-title");
        userMenuHolder.addClassName("user-menu-holder");
        // <header> = landmark banner; <main> = landmark utama (WCAG / axe "region" + "landmark-one-main")
        Header header = new Header(toggle, brand, userMenuHolder);
        header.addClassName("app-header");
        addToNavbar(header);
        main.addClassName("app-main");
        setContent(main);

        sidebar.addClassName("sidebar");
        addToDrawer(sidebar);
    }

    /** Isi halaman dibungkus <main> agar menjadi landmark utama. */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        main.getElement().removeAllChildren();
        if (content != null) {
            main.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository).orElse(null);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        buildSidebar(user);
        buildUserMenu(user);
    }

    /** Dipanggil setelah profil berubah agar nama/foto di navbar ikut diperbarui. */
    public void refreshUser() {
        User user = AuthSession.currentUser(userRepository).orElse(null);;
        if (user != null) {
            buildUserMenu(user);
        }
    }

    private void buildSidebar(User user) {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("aria-label", "Navigasi utama");
        nav.addItem(new SideNavItem("Cashflow", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi Pengguna", UserTransactionsView.class,
                    VaadinIcon.LIST_UL.create()));
        }
        sidebar.removeAll();
        sidebar.add(nav);
    }

    private void buildUserMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menu.getElement().setAttribute("aria-label", "Menu akun " + user.getFullName());
        var root = menu.addItem(avatar);
        root.getElement().setAttribute("aria-label", "Menu akun");
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().getPage().setLocation("/login");
        });
        Span name = new Span(user.getFullName());
        name.addClassName("user-name");
        userMenuHolder.removeAll();
        userMenuHolder.add(name, menu);
    }
}