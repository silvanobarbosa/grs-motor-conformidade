package com.senior.grc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Motor GRC — core unico de risco, compliance e governanca (versao de demonstracao).
 *
 * Monolito modular: um pacote por componente da arquitetura (SPEC.md secao 13). A fundacao
 * (multi-tenant, trilha so-inclusao encadeada por hash, porta de entrada com contrato unico)
 * mora em {@code com.senior.grc.fundacao}. Os sistemas externos entram como adaptadores mock
 * atras de uma porta (Port), nunca gravando direto no core (CLAUDE.md, regra 2).
 */
@SpringBootApplication
public class MotorGrcApplication {
    public static void main(String[] args) {
        SpringApplication.run(MotorGrcApplication.class, args);
    }
}
