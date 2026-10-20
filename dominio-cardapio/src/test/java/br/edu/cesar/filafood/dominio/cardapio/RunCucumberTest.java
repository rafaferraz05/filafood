package br.edu.cesar.filafood.dominio.cardapio;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("br.edu.cesar.filafood.dominio.cardapio")
public class RunCucumberTest {
}
