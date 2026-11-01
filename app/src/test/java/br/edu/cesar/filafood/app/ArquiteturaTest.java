package br.edu.cesar.filafood.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Regras de camada do app (D-06).
 *
 * <p>As regras que envolvem os módulos {@code dominio-*} entram quando o app passar a depender
 * deles, na fase P1; por enquanto o ArchUnit só vê as classes do próprio app.
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
}
