package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.config.AppConfig;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/** Smoke test: seluruh konteks Spring naik dengan H2 (profil test) dan admin ter-seed. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private FileStorage fileStorage;

    @Test
    void contextLoadsAndRootAdminIsSeeded() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    void uploadsResourceHandlerIsRegistered() {
        var registry = new ResourceHandlerRegistry(new GenericWebApplicationContext(), new MockServletContext());
        new AppConfig().uploadsResourceConfigurer(fileStorage).addResourceHandlers(registry);
        assertTrue(registry.hasMappingForPattern("/uploads/**"));
        assertNotNull(fileStorage.getRoot());
    }
}
