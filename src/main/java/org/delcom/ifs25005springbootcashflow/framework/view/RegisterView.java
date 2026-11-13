package org.delcom.ifs25005springbootcashflow.framework.view;

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

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends Main implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final transient AuthUseCase authUseCase;

    public RegisterView(IUserRepository userRepository, AuthUseCase authUseCase) {
        this.userRepository = userRepository;
        this.authUseCase = authUseCase;
        addClassName("auth-page");

        H1 title = new H1("Buat Akun Cashflow");
        title.addClassName("auth-title");
        Paragraph subtitle = new Paragraph("Daftar gratis untuk mulai mencatat keuangan pribadi.");
        subtitle.addClassName("auth-subtitle");

        TextField fullName = new TextField("Nama lengkap");
        fullName.setWidthFull();
        fullName.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NAME);
        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setHelperText("Minimal " + AuthUseCase.MIN_USERNAME + " karakter");
        username.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.USERNAME);
        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.setHelperText("Minimal " + AuthUseCase.MIN_PASSWORD + " karakter");
        password.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        PasswordField confirm = new PasswordField("Konfirmasi password");
        confirm.setWidthFull();
        confirm.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.getElement().setAttribute("aria-label", "Sedang mendaftar");

        Button submit = new Button("Daftar");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.setWidthFull();
        submit.addClickListener(e -> {
            if (!password.getValue().equals(confirm.getValue())) {
                Notifier.error("Konfirmasi password tidak sama");
                return;
            }
            submit.setEnabled(false);
            progress.setVisible(true);
            try {
                var user = this.authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
                AuthSession.login(user);
                Notifier.success("Registrasi berhasil. Selamat datang, " + user.getFullName() + "!");
                UI.getCurrent().navigate(FinanceView.class);
            } catch (DomainException ex) {
                Notifier.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                submit.setEnabled(true);
            }
        });

        RouterLink loginLink = new RouterLink("Sudah punya akun? Masuk", LoginView.class);
        loginLink.addClassName("auth-link");

        VerticalLayout form = new VerticalLayout(title, subtitle, fullName, username, password, confirm,
                progress, submit, loginLink);
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
