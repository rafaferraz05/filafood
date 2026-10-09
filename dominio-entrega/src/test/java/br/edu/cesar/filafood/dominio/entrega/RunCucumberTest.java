package br.edu.cesar.filafood.dominio.entrega;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Executa os cenários BDD do contexto de entrega.
 *
 * A estrutura é a mesma do contexto de estoque: os cenários moram em {@code src/test/resources}
 * espelhando o pacote do conceito, e as opções do Cucumber ficam em
 * {@code junit-platform.properties}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("br.edu.cesar.filafood.dominio.entrega")
public class RunCucumberTest {
}
