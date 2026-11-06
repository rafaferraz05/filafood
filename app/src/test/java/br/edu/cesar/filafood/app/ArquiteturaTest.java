package br.edu.cesar.filafood.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Regras de camada do app (D-06).
 *
 * <p>O app passa a depender de {@code dominio-estoque} (D-09), então a regra de pureza do domínio
 * também é cobrada aqui; as demais regras de domínio entram quando os outros módulos forem ligados.
 */
class ArquiteturaTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("br.edu.cesar.filafood.app");

    @Test
    void vaadinSoNoAdaptadorDeEntrada() {
        noClasses()
                .that().resideOutsideOfPackage("br.edu.cesar.filafood.app.adapter.in.web..")
                .should().dependOnClassesThat().resideInAPackage("com.vaadin..")
                .check(classes);
    }

    @Test
    void aplicacaoNaoDependeDeAdaptador() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.app.application..")
                .should().dependOnClassesThat().resideInAPackage("br.edu.cesar.filafood.app.adapter..")
                // O pacote application nasce na P1; até lá a regra não tem classe para checar.
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void viewNaoAcessaPersistencia() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.app.adapter.in.web..")
                .should().dependOnClassesThat().resideInAPackage("br.edu.cesar.filafood.app.adapter.out.persistence..")
                .check(classes);
    }

    @Test
    void dominioNaoConheceFramework() {
        JavaClasses dominio = new ClassFileImporter().importPackages("br.edu.cesar.filafood.dominio.estoque");
        noClasses()
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.vaadin..")
                .check(dominio);
    }
}
