package br.edu.cesar.filafood;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Regras de camada do FilaFood (D-06), agora sobre os módulos do modelo de referência.
 *
 * <p>As regras valem para o classpath da apresentação, que enxerga domínio, aplicação e
 * infraestrutura. O domínio continua sem conhecer framework; a aplicação não conhece infraestrutura
 * nem Vaadin; a apresentação não conhece JPA.
 */
class ArquiteturaTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("br.edu.cesar.filafood");

    @Test
    void vaadinSoNaApresentacao() {
        noClasses()
                .that().resideOutsideOfPackage("br.edu.cesar.filafood.apresentacao..")
                .should().dependOnClassesThat().resideInAPackage("com.vaadin..")
                .check(classes);
    }

    @Test
    void aplicacaoNaoDependeDeInfraestruturaNemDeApresentacao() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.aplicacao..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "br.edu.cesar.filafood.infraestrutura..",
                        "br.edu.cesar.filafood.apresentacao..")
                .check(classes);
    }

    @Test
    void apresentacaoNaoAcessaPersistencia() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.apresentacao..")
                .should().dependOnClassesThat().resideInAPackage("br.edu.cesar.filafood.infraestrutura..")
                .check(classes);
    }

    @Test
    void infraestruturaNaoDependeDaApresentacao() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.infraestrutura..")
                .should().dependOnClassesThat().resideInAPackage("br.edu.cesar.filafood.apresentacao..")
                .check(classes);
    }

    @Test
    void dominioNaoDependeDasOutrasCamadas() {
        noClasses()
                .that().resideInAPackage("br.edu.cesar.filafood.dominio..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "br.edu.cesar.filafood.aplicacao..",
                        "br.edu.cesar.filafood.infraestrutura..",
                        "br.edu.cesar.filafood.apresentacao..")
                .check(classes);
    }

    @Test
    void dominioNaoConheceFramework() {
        // Estoque, cardápio e compras: as portas e regras de domínio seguem sem Spring, JPA ou Vaadin.
        JavaClasses dominio = new ClassFileImporter().importPackages("br.edu.cesar.filafood.dominio");
        noClasses()
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.vaadin..")
                .check(dominio);
    }
}
