package br.edu.cesar.filafood.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;

/**
 * Declara a política de reposição usada pela unidade. Hoje é a padrão da RN 6; uma segunda
 * implementação (por unidade, por segmento) entra aqui sem tocar nos casos de uso.
 */
@Configuration
public class ReposicaoConfig {

    @Bean
    PoliticaDeReposicao politicaDeReposicao() {
        return new ConsumoMedioSobrePrazoEEstoqueMinimo();
    }
}