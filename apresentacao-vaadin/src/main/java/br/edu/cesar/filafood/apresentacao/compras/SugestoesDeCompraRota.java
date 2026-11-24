package br.edu.cesar.filafood.apresentacao.compras;

import static br.edu.cesar.filafood.apresentacao.compras.FornecedoresRota.dias;
import static br.edu.cesar.filafood.apresentacao.compras.FornecedoresRota.moeda;
import static br.edu.cesar.filafood.apresentacao.compras.FornecedoresRota.quantidade;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Campo;
import br.edu.cesar.filafood.apresentacao.layout.Entradas;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.aplicacao.compras.OfertaComparada;
import br.edu.cesar.filafood.aplicacao.compras.SugerirCompras;
import br.edu.cesar.filafood.aplicacao.compras.SugestaoDeCompraDetalhe;

/**
 * Tela de Compras (F7 RN 3 e RN 4): sugestões para os insumos a repor do estoque real da unidade e a
 * comparação dos fornecedores cadastrados no catálogo.
 */
@Route(value = "compras", layout = MainLayout.class)
@PageTitle("FilaFood · Sugestões de compra")
public class SugestoesDeCompraRota extends Div {

    private static final int COBERTURA_PADRAO = 5;
    private static final String RODAPE = "A sugestão é informativa: nenhuma ordem de compra é criada sem confirmação "
            + "do gestor. Comparam-se os fornecedores ativos que têm o insumo no catálogo; a quantidade é arredondada "
            + "ao mínimo de cada um antes do custo total, e só concorrem os que entregam dentro do prazo do insumo. "
            + "As ordens abertas contam zero enquanto a ordem de compra (RN 5) não entra no sistema.";

    private final SugerirCompras sugerir;

    private final Cabecalho cabecalho = new Cabecalho("Sugestões de compra",
            "Insumos a repor da unidade " + UnidadeDeDemonstracao.NOME
                    + ", com a quantidade sugerida e o fornecedor mais vantajoso do catálogo.");
    private final Input cobertura = Entradas.texto();
    private final Paragraph aviso = new Paragraph();
    private final Div conteudoDinamico = new Div();

    private List<SugestaoDeCompraDetalhe> sugestoes = List.of();
    private int coberturaAtual = COBERTURA_PADRAO;
    private String insumoSelecionadoId;

    public SugestoesDeCompraRota(SugerirCompras sugerir) {
        this.sugerir = sugerir;

        aviso.setVisible(false);
        cobertura.setValue(String.valueOf(COBERTURA_PADRAO));

        RouterLink fornecedores = new RouterLink("Gerenciar fornecedores e catálogo", FornecedoresRota.class);
        fornecedores.addClassNames("btn", "btn-claro");

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(new Campo("Cobertura (dias)", cobertura), Acao.primario("Recalcular", this::recalcular),
                fornecedores);

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho, aviso, barra, conteudoDinamico, rodape);
        carregar();
    }

    private void recalcular() {
        try {
            coberturaAtual = Integer.parseInt(cobertura.getValue() == null ? "" : cobertura.getValue().strip());
        } catch (NumberFormatException e) {
            mostrarAviso("informe a cobertura em dias inteiros", false);
            return;
        }
        if (carregar()) {
            aviso.setVisible(false);
        }
    }

    private boolean carregar() {
        try {
            sugestoes = sugerir.executar(UnidadeDeDemonstracao.ID, coberturaAtual);
        } catch (IllegalArgumentException excecao) {
            mostrarAviso(excecao.getMessage(), false);
            return false;
        }
        if (insumoSelecionadoId == null
                || sugestoes.stream().noneMatch(s -> s.insumoId().equals(insumoSelecionadoId))) {
            insumoSelecionadoId = sugestoes.stream().filter(SugestaoDeCompraDetalhe::haSugestao)
                    .map(SugestaoDeCompraDetalhe::insumoId).findFirst().orElse(null);
        }
        render();
        return true;
    }

    private void render() {
        long comSugestao = sugestoes.stream().filter(SugestaoDeCompraDetalhe::haSugestao).count();
        long comAtraso = sugestoes.stream().filter(SugestaoDeCompraDetalhe::atraso).count();
        long semFornecedor = sugestoes.stream()
                .filter(s -> s.haSugestao() && s.fornecedorEscolhido() == null).count();
        cabecalho.badges(
                sugestoes.isEmpty() ? Badge.ok("nenhum insumo a repor") : Badge.repor(sugestoes.size() + " a repor"),
                Badge.neutro("nenhuma ordem criada"));

        conteudoDinamico.removeAll();
        conteudoDinamico.add(cardKpis(comSugestao, comAtraso, semFornecedor), cardSugestoes());
        sugestoes.stream()
                .filter(s -> s.insumoId().equals(insumoSelecionadoId) && s.haSugestao())
                .findFirst()
                .ifPresent(s -> conteudoDinamico.add(cardComparacao(s)));
    }

    private Div cardKpis(long comSugestao, long comAtraso, long semFornecedor) {
        Div kpis = new Div();
        kpis.addClassName("kpis");
        kpis.add(
                kpi("Insumos a repor", String.valueOf(sugestoes.size()), "ativos, saldo no ponto de pedido"),
                kpi("Cobertura", dias(coberturaAtual), "dias de consumo além do ponto"),
                kpi("Sugestões", String.valueOf(comSugestao), "nenhuma ordem criada"),
                kpi("Com atraso previsto", String.valueOf(comAtraso), "nenhum fornecedor no prazo"),
                kpi("Sem fornecedor", String.valueOf(semFornecedor), "insumo fora dos catálogos"));
        Div card = new Div();
        card.addClassName("card");
        card.add(kpis);
        return card;
    }

    private Div kpi(String rotulo, String valor, String nota) {
        Div kpi = new Div();
        kpi.addClassName("kpi");
        Div r = new Div();
        r.addClassName("kpi-rotulo");
        r.setText(rotulo);
        Div v = new Div();
        v.addClassName("kpi-valor");
        v.setText(valor);
        Div n = new Div();
        n.addClassName("kpi-nota");
        n.setText(nota);
        kpi.add(r, v, n);
        return kpi;
    }

    private Div cardSugestoes() {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Insumos a repor (F7 RN 3)", "simulação da cobertura de " + dias(coberturaAtual)));

        if (sugestoes.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum insumo ativo está a repor: o saldo de todos cobre o ponto de pedido.");
            vazio.addClassName("vazio");
            card.add(vazio);
            return card;
        }

        Tabela tabela = new Tabela("Insumo", "Saldo", "Ponto de pedido", "Consumo médio/dia", "Ordens abertas",
                "Quantidade bruta", "Sugestão", "Fornecedor");
        for (SugestaoDeCompraDetalhe s : sugestoes) {
            tabela.linha(
                    celulaInsumo(s),
                    new Span(quantidade(s.saldo())),
                    new Span(quantidade(s.pontoDePedido())),
                    new Span(quantidade(s.consumoMedioDiario())),
                    new Span(quantidade(s.emOrdensAbertas())),
                    new Span(quantidade(s.pontoDePedido()) + " + (" + quantidade(s.consumoMedioDiario()) + " × "
                            + s.cobertura() + ") − " + quantidade(s.saldo()) + " − " + quantidade(s.emOrdensAbertas())
                            + " = " + quantidade(s.quantidadeBruta())),
                    celulaSugestao(s),
                    celulaFornecedor(s));
        }
        card.add(tabela);
        return card;
    }

    private Component celulaInsumo(SugestaoDeCompraDetalhe s) {
        Div celula = new Div();
        if (s.haSugestao() && !s.insumoId().equals(insumoSelecionadoId)) {
            celula.add(Acao.link(s.insumoNome(), () -> {
                insumoSelecionadoId = s.insumoId();
                render();
            }));
        } else {
            Span nome = new Span(s.insumoNome());
            nome.addClassName("destaque");
            celula.add(nome);
        }
        Span medida = new Span(s.unidadeDeMedida());
        medida.addClassName("unidade");
        celula.add(medida);
        return celula;
    }

    private Component celulaSugestao(SugestaoDeCompraDetalhe s) {
        if (!s.haSugestao()) {
            return Badge.neutro("sem sugestão");
        }
        Div celula = new Div();
        if (s.fornecedorEscolhido() == null) {
            celula.add(Badge.alerta(quantidade(s.quantidadeSugerida()) + " " + s.unidadeDeMedida()));
            Span nota = new Span("sem arredondamento: falta fornecedor");
            nota.addClassName("unidade");
            celula.add(nota);
            return celula;
        }
        celula.add(s.atraso()
                ? Badge.repor(quantidade(s.quantidadeSugerida()) + " " + s.unidadeDeMedida())
                : Badge.ok(quantidade(s.quantidadeSugerida()) + " " + s.unidadeDeMedida()));
        Span custo = new Span(moeda(s.custoTotal()));
        custo.addClassName("unidade");
        celula.add(custo);
        return celula;
    }

    private Component celulaFornecedor(SugestaoDeCompraDetalhe s) {
        if (!s.haSugestao()) {
            return new Span("—");
        }
        if (s.fornecedorEscolhido() == null) {
            return Badge.alerta(s.mensagem());
        }
        Div celula = new Div();
        celula.add(new Span(s.fornecedorEscolhido()));
        if (s.atraso()) {
            Span atraso = new Span("atraso previsto de " + dias(s.diasDeAtraso()));
            atraso.addClassName("unidade");
            celula.add(atraso);
        }
        return celula;
    }

    private Div cardComparacao(SugestaoDeCompraDetalhe s) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Escolha para " + s.insumoNome() + " (F7 RN 4)",
                "prazo máximo aceito: " + dias(s.prazoLimiteEmDias())));

        if (s.ofertas().isEmpty()) {
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            Paragraph texto = new Paragraph("Não há fornecedor para o insumo: nenhum fornecedor ativo tem "
                    + s.insumoNome() + " no catálogo.");
            RouterLink catalogo = new RouterLink("Incluir " + s.insumoNome() + " em um catálogo", FornecedoresRota.class);
            catalogo.addClassNames("btn", "btn-primario");
            corpo.add(texto, catalogo);
            card.add(corpo);
            return card;
        }

        Tabela tabela = new Tabela("Fornecedor", "Preço unitário", "Mínimo", "Quantidade ajustada", "Prazo",
                "Custo total", "Resultado");
        for (OfertaComparada oferta : s.ofertas()) {
            Span nome = new Span(oferta.fornecedor());
            nome.addClassName("destaque");
            tabela.linha(
                    nome,
                    new Span(moeda(oferta.precoUnitario())),
                    new Span(quantidade(oferta.quantidadeMinima()) + " " + s.unidadeDeMedida()),
                    new Span(quantidade(oferta.quantidadeAjustada()) + " " + s.unidadeDeMedida()),
                    new Span(dias(oferta.prazoDeEntregaEmDias())),
                    new Span(moeda(oferta.custoTotal())),
                    resultado(oferta, s));
        }
        card.add(tabela);

        if (s.atraso()) {
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            corpo.add(new Paragraph("Nenhum fornecedor entrega em até " + dias(s.prazoLimiteEmDias())
                    + ". Foi escolhido o de menor prazo, " + s.fornecedorEscolhido() + ", com atraso previsto de "
                    + dias(s.diasDeAtraso()) + "."));
            card.add(corpo);
        }
        return card;
    }

    private Component resultado(OfertaComparada oferta, SugestaoDeCompraDetalhe s) {
        if (oferta.escolhida()) {
            return s.atraso() ? Badge.repor("Escolhido · atraso") : Badge.ok("Escolhido");
        }
        return oferta.dentroDoPrazo() ? Badge.neutro("No prazo") : Badge.neutro("Prazo excedido");
    }

    private Div tituloDoCard(String texto, String direita) {
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span(texto));
        Span dir = new Span(direita);
        dir.addClassName("dir");
        titulo.add(dir);
        return titulo;
    }

    private void mostrarAviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }
}
