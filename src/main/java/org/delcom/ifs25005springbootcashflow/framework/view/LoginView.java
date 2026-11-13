package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends Main implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final transient AuthUseCase authUseCase;

    public LoginView(IUserRepository userRepository, AuthUseCase authUseCase) {
        this.userRepository = userRepository;
        this.authUseCase = authUseCase;
        addClassName("auth-page");

        H1 title = new H1("Masuk ke Cashflow");
        title.addClassName("auth-title");
        Paragraph subtitle = new Paragraph("Catat pemasukan dan pengeluaran Anda dengan mudah.");
        subtitle.addClassName("auth-subtitle");

        TextField username = new TextField("Username");
        username.setId("login-username");
        username.setAutofocus(true);
        username.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.USERNAME);
        username.setWidthFull();

        PasswordField password = new PasswordField("Password");
        password.setId("login-password");
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.CURRENT_PASSWORD);
        password.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.getElement().setAttribute("aria-label", "Sedang masuk");

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> {
            loginButton.setEnabled(false);
            progress.setVisible(true);
            try {
                User user = this.authUseCase.login(username.getValue(), password.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (DomainException ex) {
                Notifier.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                loginButton.setEnabled(true);
            }
        });

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar", RegisterView.class);
        registerLink.addClassName("auth-link");

        VerticalLayout form = new VerticalLayout(title, subtitle, username, password, progress, loginButton, registerLink);
        form.setPadding(false);
        PageCard card = new PageCard(form);
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}
