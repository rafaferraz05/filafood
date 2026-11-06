package br.edu.cesar.filafood.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root do FilaFood: é o único lugar da aplicação com Spring, JPA e Vaadin
 * (docs/padroes-do-projeto.md, seção 3). O AppShell do Vaadin vive em adapter.in.web.layout.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
