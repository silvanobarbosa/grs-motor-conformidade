package com.senior.grc.admin;

import com.senior.grc.demo.SeedDemo;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * Garante o admin master do tenant de demonstracao no boot, para o login administrativo funcionar
 * mesmo antes de qualquer "carregar demo".
 */
@Component
public class AdminSeedRunner implements ApplicationRunner {

    private final AdminService admin;

    public AdminSeedRunner(AdminService admin) {
        this.admin = admin;
    }

    @Override
    public void run(ApplicationArguments args) {
        admin.semearAdminMaster(SeedDemo.TENANT);
    }
}
