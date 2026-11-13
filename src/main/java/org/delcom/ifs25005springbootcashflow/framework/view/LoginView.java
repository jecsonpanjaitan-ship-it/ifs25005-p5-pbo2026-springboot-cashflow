package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends Main implements BeforeEnterObserver {

    private final transient IUserRepository userRepository;
    private final Paragraph error = new Paragraph();

    public LoginView(IUserRepository userRepository) {
        this.userRepository = userRepository;
        addClassName("auth-page");

        H1 title = new H1("Masuk ke Cashflow");
        title.addClassName("auth-title");
        Paragraph subtitle = new Paragraph("Catat pemasukan dan pengeluaran Anda dengan mudah.");
        subtitle.addClassName("auth-subtitle");

        error.addClassName("auth-error");
        error.getElement().setAttribute("role", "alert");
        error.setVisible(false);

        // Form HTML native: field ber-name, tombol type=submit, POST ke /auth/login (redirect 302).
        Html form = new Html("""
                <form class="auth-form" method="post" action="/auth/login">
                  <div class="auth-field">
                    <label for="login-username">Username</label>
                    <input id="login-username" name="username" type="text" autocomplete="username"
                           autocapitalize="none" spellcheck="false" required autofocus>
                  </div>
                  <div class="auth-field">
                    <label for="login-password">Password</label>
                    <input id="login-password" name="password" type="password"
                           autocomplete="current-password" required>
                  </div>
                  <button id="login-submit" class="auth-submit" type="submit">Masuk</button>
                </form>""");

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar", RegisterView.class);
        registerLink.addClassName("auth-link");

        VerticalLayout layout = new VerticalLayout(title, subtitle, error, form, registerLink);
        layout.setPadding(false);
        PageCard card = new PageCard(layout);
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
            return;
        }
        var message = event.getLocation().getQueryParameters().getSingleParameter("error");
        error.setText(message.orElse(""));
        error.setVisible(message.isPresent());
    }
}