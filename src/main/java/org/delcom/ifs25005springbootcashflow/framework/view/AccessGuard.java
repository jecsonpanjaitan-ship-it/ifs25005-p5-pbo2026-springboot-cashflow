package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.router.BeforeEnterEvent;

/** Guard rute: login wajib, dan khusus ADMIN. */
public final class AccessGuard {

    private AccessGuard() {
    }

    /** Mengembalikan admin yang sah, atau melakukan forward lalu mengembalikan null. */
    public static User requireAdmin(BeforeEnterEvent event, IUserRepository userRepository) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return null;
        }
        if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return null;
        }
        return user;
    }
}
