package br.edu.cesar.filafood;

import static org.springframework.boot.SpringApplication.run;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.edu.cesar.filafood.aplicacao.entrega.despacho.DespachoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.evento.OuvinteDeEventosDeEntrega;
import br.edu.cesar.filafood.aplicacao.entrega.evento.PedidoDaEntregaListener;
import br.edu.cesar.filafood.aplicacao.entrega.evento.PublicadorDeEventos;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.ficha.FichaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.BaixaDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.DevolucaoDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.despacho.Despacho;
import br.edu.cesar.filafood.dominio.entrega.despacho.MenorCargaMaisAntiga;
import br.edu.cesar.filafood.dominio.entrega.despacho.PoliticaDeEscolhaDeEntregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorRepositorio;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorServico;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaRepositorio;
import br.edu.cesar.filafood.dominio.entrega.zona.EscolhaDaZona;
import br.edu.cesar.filafood.dominio.entrega.zona.PoliticaDeTaxa;
import br.edu.cesar.filafood.dominio.entrega.zona.PoliticaDeTaxaPorZona;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntregaRepositorio;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntregaServico;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaServico;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoServico;
import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;
import br.edu.cesar.filafood.aplicacao.entrega.evento.BarramentoDeEventosSimples;

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

    // ----- F2: entrega --------------------------------------------------------------------

    @Bean
    public EntregadorServico entregadorServico(EntregadorRepositorio repositorio) {
        return new EntregadorServico(repositorio);
    }

    @Bean
    public ZonaDeEntregaServico zonaDeEntregaServico(ZonaDeEntregaRepositorio repositorio) {
        return new ZonaDeEntregaServico(repositorio);
    }

    @Bean
    public EscolhaDaZona escolhaDaZona() {
        return new EscolhaDaZona();
    }

    @Bean
    public PoliticaDeTaxa politicaDeTaxa() {
        return new PoliticaDeTaxaPorZona();
    }

    @Bean
    public PoliticaDeEscolhaDeEntregador politicaDeEscolhaDeEntregador() {
        return new MenorCargaMaisAntiga();
    }

    @Bean
    public Despacho despacho(PoliticaDeEscolhaDeEntregador escolha, PoliticaDeTaxa taxa) {
        return new Despacho(escolha, taxa);
    }

    @Bean
    public PublicadorDeEventos publicadorDeEventos(List<OuvinteDeEventosDeEntrega> ouvintes) {
        return new BarramentoDeEventosSimples(ouvintes);
    }

    @Bean
    public PedidoDaEntregaListener pedidoDaEntregaListener(PedidoDaEntregaRepositorioAplicacao pedidos) {
        return new PedidoDaEntregaListener(pedidos);
    }

    @Bean
    public EntregadorServicoAplicacao entregadorServicoAplicacao(EntregadorServico entregadores) {
        return new EntregadorServicoAplicacao(entregadores);
    }

    @Bean
    public ZonaServicoAplicacao zonaServicoAplicacao(ZonaDeEntregaServico zonas, PoliticaDeTaxa taxa) {
        return new ZonaServicoAplicacao(zonas, taxa);
    }

    @Bean
    public DespachoServicoAplicacao despachoServicoAplicacao(EntregaRepositorio entregas,
            EntregadorRepositorio entregadores, ZonaDeEntregaRepositorio zonas,
            PedidoDaEntregaRepositorioAplicacao pedidos, Despacho despacho, EscolhaDaZona escolhaDaZona,
            PublicadorDeEventos eventos) {
        return new DespachoServicoAplicacao(entregas, entregadores, zonas, pedidos, despacho, escolhaDaZona,
                eventos);
    }

    @Bean
    public EntregaServicoAplicacao entregaServicoAplicacao(EntregaRepositorio entregas,
            EntregadorRepositorio entregadores, PedidoDaEntregaRepositorioAplicacao pedidos,
            PublicadorDeEventos eventos) {
        return new EntregaServicoAplicacao(entregas, entregadores, pedidos, eventos);
    }

    public static void main(String[] args) {
        run(VaadinAplicacao.class, args);
    }
}
