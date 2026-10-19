package br.edu.cesar.filafood.dominio.atendimento;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("br.edu.cesar.filafood.dominio.atendimento")
public class RunCucumberTest { }
