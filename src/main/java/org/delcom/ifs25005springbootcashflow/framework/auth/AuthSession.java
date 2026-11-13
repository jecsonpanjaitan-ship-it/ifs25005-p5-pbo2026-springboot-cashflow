package org.delcom.ifs25005springbootcashflow.framework.auth;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Menyimpan sesi login di VaadinSession + HTTP session + cookie (cashflow_uid).
 * Cookie ditandatangani agar tidak bisa dipalsukan; LOGGED_OUT_KEY mencegah restore setelah logout.
 */
public final class AuthSession {

    public static final String USER_KEY = "cashflow.userId";
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    public static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, null);
        session.setAttribute(USER_KEY, user.getId());
        WrappedSession http = session.getSession();
        if (http != null) {
            http.setAttribute(USER_KEY, user.getId());
            http.removeAttribute(LOGGED_OUT_KEY);
        }
        writeCookie(sign(user.getId()), COOKIE_MAX_AGE);
    }

    /**
     * Login lewat request HTTP biasa (form POST native). Sesi lama dibuang agar status
     * "logout" sebelumnya tidak terbawa, lalu id pengguna disimpan di sesi HTTP baru + cookie.
     */
    public static void loginHttp(HttpServletRequest request, HttpServletResponse response, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(USER_KEY, user.getId());
        Cookie cookie = new Cookie(COOKIE_NAME, sign(user.getId()));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession http = session.getSession();
        if (http != null) {
            http.removeAttribute(USER_KEY);
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /** Pengguna yang sedang login (dipulihkan setelah refresh), atau null. */
    public static User currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || isLoggedOut(session)) {
            return null;
        }
        Integer id = (Integer) session.getAttribute(USER_KEY);
        WrappedSession http = session.getSession();
        if (id == null && http != null) {
            id = (Integer) http.getAttribute(USER_KEY);
        }
        if (id == null) {
            id = idFromCookie();
        }
        if (id == null) {
            return null;
        }
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            session.setAttribute(USER_KEY, user.getId());
        }
        return user;
    }

    private static boolean isLoggedOut(VaadinSession session) {
        if (Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return true;
        }
        WrappedSession http = session.getSession();
        return http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY));
    }

    private static Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String secret() {
        return System.getProperty("SESSION_SECRET",
                System.getProperty("ROOT_PASSWORD", "cashflow-dev-secret"));
    }

    private static String sign(Integer id) {
        return id + "." + PasswordHasher.hash(id + ":" + secret());
    }

    private static Integer verify(String value) {
        if (value == null) {
            return null;
        }
        int dot = value.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value.substring(0, dot));
            return sign(id).equals(value) ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}