package br.edu.cesar.filafood;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.edu.cesar.filafood.aplicacao.estoque.ficha.FichaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.BaixaDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.DevolucaoDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaServico;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoServico;
import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;

/**
 * Composition root do FilaFood: o único lugar com Spring, JPA e Vaadin.
 *
 * <p>Como no modelo de referência do professor, os serviços de domínio e de aplicação não têm
 * anotação de componente: são instanciados aqui, recebendo as portas que a infraestrutura
 * implementa. Assim a fiação inteira — repositório → serviço → serviço de aplicação → tela — cabe
 * neste arquivo.
 */
@SpringBootApplication
public class VaadinAplicacao {

    @Bean
    public InsumoServico insumoServico(InsumoRepositorio repositorio) {
        return new InsumoServico(repositorio);
    }

    @Bean
    public FichaTecnicaServico fichaTecnicaServico(FichaTecnicaRepositorio repositorio) {
        return new FichaTecnicaServico(repositorio);
    }

    @Bean
    public PoliticaDeReposicao politicaDeReposicao() {
        return new ConsumoMedioSobrePrazoEEstoqueMinimo();
    }

    @Bean
    public InsumoServicoAplicacao insumoServicoAplicacao(InsumoServico insumos,
            PoliticaDeReposicao politica) {
        return new InsumoServicoAplicacao(insumos, politica);
    }

    @Bean
    public FichaServicoAplicacao fichaServicoAplicacao(ProdutoRepositorioAplicacao catalogo,
            FichaTecnicaRepositorio fichas, FichaTecnicaServico fichaServico, InsumoServico insumos) {
        return new FichaServicoAplicacao(catalogo, fichas, fichaServico, insumos);
    }

    @Bean
    public EntradaDeInsumoServicoAplicacao entradaDeInsumoServicoAplicacao(InsumoRepositorio insumos) {
        return new EntradaDeInsumoServicoAplicacao(insumos);
    }

    @Bean
    public BaixaDoPreparoServicoAplicacao baixaDoPreparoServicoAplicacao(InsumoRepositorio insumos,
            PedidoRepositorioAplicacao pedidos, FichaTecnicaRepositorio fichas) {
        return new BaixaDoPreparoServicoAplicacao(insumos, pedidos, fichas);
    }

    @Bean
    public DevolucaoDoPreparoServicoAplicacao devolucaoDoPreparoServicoAplicacao(InsumoRepositorio insumos,
            PedidoRepositorioAplicacao pedidos) {
        return new DevolucaoDoPreparoServicoAplicacao(insumos, pedidos);
    }

    @Bean
    public PreparoServicoAplicacao preparoServicoAplicacao(PedidoRepositorioAplicacao pedidos,
            FichaTecnicaRepositorio fichas, ProdutoRepositorioAplicacao catalogo,
            BaixaDoPreparoServicoAplicacao baixa, DevolucaoDoPreparoServicoAplicacao devolucao) {
        return new PreparoServicoAplicacao(pedidos, fichas, catalogo, baixa, devolucao);
    }

    public static void main(String[] args) {
        run(VaadinAplicacao.class, args);
    }
}
