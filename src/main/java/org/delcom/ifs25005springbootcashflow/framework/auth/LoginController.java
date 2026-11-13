package org.delcom.ifs25005springbootcashflow.framework.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Menerima form login native (POST /auth/login) lalu redirect 302.
 * Cara ini tidak bergantung pada websocket Vaadin sehingga juga bisa dipakai
 * oleh alat uji otomatis (Puppeteer) yang mengisi form dan menekan tombol submit.
 */
@Controller
public class LoginController {

    private final AuthUseCase authUseCase;

    public LoginController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Void> login(@RequestParam(defaultValue = "") String username,
                                      @RequestParam(defaultValue = "") String password,
                                      HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = authUseCase.login(username, password);
            AuthSession.loginHttp(request, response, user);
            return redirect("/");
        } catch (DomainException e) {
            String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return redirect("/login?error=" + message);
        }
    }

    private static ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(location).toString())
                .build();
    }
}
