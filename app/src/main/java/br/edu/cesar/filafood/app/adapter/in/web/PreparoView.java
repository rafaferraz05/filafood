package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Tabela;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.InsumoDoPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.ItemDoPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoDetalhe;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoServicoAplicacao;
import br.edu.cesar.filafood.app.config.PedidoDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Preparo do pedido no layout e no CSS do protótipo (F1 RN 4 e RN 5). */
@Route(value = "preparo", layout = MainLayout.class)
@PageTitle("FilaFood · Preparo do pedido")
public class PreparoView extends Div {

    private static final String RODAPE = "Ao iniciar o preparo, a ficha técnica dos produtos é explodida e a "
            + "baixa é tudo ou nada: se faltar saldo de qualquer insumo, nenhuma movimentação é gravada. "
            + "Cancelar o preparo devolve exatamente o que foi consumido.";

    private final PreparoServicoAplicacao preparo;
    private final Paragraph aviso = new Paragraph();

    public PreparoView(PreparoServicoAplicacao preparo) {
        this.preparo = preparo;
        aviso.setVisible(false);
        montar();
    }

    private void montar() {
        PreparoDetalhe detalhe = preparo.detalhar(PedidoDeDemonstracao.ID);
        removeAll();
        aviso.setVisible(false);

        Div esquerda = new Div();
        esquerda.add(cardItens(detalhe), cardDeAcao(detalhe));

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(esquerda, cardInsumos(detalhe));

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho(detalhe), aviso, grid, rodape);
    }

    private Cabecalho cabecalho(PreparoDetalhe detalhe) {
        Cabecalho cabecalho = new Cabecalho("Pedido #" + PedidoDeDemonstracao.NUMERO,
                "Ao iniciar o preparo, o sistema dá baixa nos insumos pela ficha técnica.");
        cabecalho.badges(badgeDoEstado(detalhe.estado()), Badge.neutro(detalhe.porcoes() + " porções"));
        return cabecalho;
    }

    private Component badgeDoEstado(EstadoDoPedido estado) {
        return switch (estado) {
            case EM_PREPARO -> Badge.alerta("em preparo");
            case CONCLUIDO -> Badge.ok("concluído");
            case CANCELADO -> Badge.neutro("cancelado");
            case RECUSADO -> Badge.alerta("recusado");
            case ENVIADO, RASCUNHO -> Badge.neutro(estado.name().toLowerCase().replace('_', ' '));
        };
    }

    private Div cardItens(PreparoDetalhe detalhe) {
        Tabela tabela = new Tabela("Produto", "Quantidade");
        for (ItemDoPreparo item : detalhe.itens()) {
            tabela.linha(destaque(item.produto()), new Span(String.valueOf(item.quantidade())));
        }
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Itens do pedido", detalhe.itens().size() + " produtos"), tabela);
        return card;
    }

    private Div cardInsumos(PreparoDetalhe detalhe) {
        Div card = new Div();
        card.addClassName("card");

        if (detalhe.impedimento().isPresent()) {
            card.add(tituloDoCard("Insumos a baixar", "sem ficha"));
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            corpo.add(new Paragraph(detalhe.impedimento().orElseThrow()), Badge.alerta("ficha incompleta"));
            card.add(corpo);
            return card;
        }

        card.add(tituloDoCard("Insumos a baixar", "quantidade pedida × quantidade da ficha"));
        Tabela tabela = new Tabela("Insumo", "Necessidade", "Saldo antes", "Saldo depois");
        for (InsumoDoPreparo insumo : detalhe.insumos()) {
            tabela.linha(
                    destaque(insumo.insumo()),
                    new Span(texto(insumo.necessidade()) + " " + insumo.unidadeDeMedida()),
                    new Span(texto(insumo.saldoAntes()) + " " + insumo.unidadeDeMedida()),
                    saldoDepois(insumo));
        }
        card.add(tabela);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        boolean tudoCoberto = detalhe.insumos().stream().allMatch(InsumoDoPreparo::suficiente);
        corpo.add(tudoCoberto ? Badge.ok("saldo cobre o pedido") : Badge.repor("saldo insuficiente"));
        card.add(corpo);
        return card;
    }

    private Component saldoDepois(InsumoDoPreparo insumo) {
        Span span = new Span(texto(insumo.saldoDepois()) + " " + insumo.unidadeDeMedida());
        if (!insumo.suficiente()) {
            span.addClassName("destaque");
        }
        return span;
    }

    private Div cardDeAcao(PreparoDetalhe detalhe) {
        Div card = new Div();
        card.addClassName("card");

        switch (detalhe.estado()) {
            case ENVIADO -> {
                card.add(tituloDoCard("Iniciar o preparo", "F1 RN 4"));
                Div corpo = new Div();
                corpo.addClassName("card-corpo");
                corpo.add(new Paragraph("A baixa é tudo ou nada: se faltar saldo de qualquer insumo, "
                        + "nenhuma movimentação é gravada e o pedido é recusado com o motivo."));
                Div acoes = new Div();
                acoes.addClassName("acoes-form");
                acoes.add(Acao.primario("Iniciar preparo e dar baixa", this::darBaixa));
                corpo.add(acoes);
                card.add(corpo);
            }
            case EM_PREPARO -> {
                // dois cards: concluir e cancelar
                card = cardConcluir();
                return card;
            }
            case CONCLUIDO -> {
                card.add(tituloDoCard("Preparo concluído", "sem ações"));
                card.add(corpoComTexto("O pedido foi concluído e os insumos consumidos não voltam ao estoque."));
            }
            case CANCELADO -> {
                card.add(tituloDoCard("Preparo cancelado", "F1 RN 5"));
                card.add(corpoComTexto("Os insumos consumidos voltaram ao estoque, exatamente na quantidade baixada."));
            }
            case RECUSADO -> {
                card.add(tituloDoCard("Pedido recusado", "F1 RN 4"));
                card.add(corpoComTexto("O preparo não foi iniciado porque faltou saldo ou ficha técnica."));
            }
            case RASCUNHO -> {
                card.add(tituloDoCard("Pedido em rascunho", "sem ações"));
                card.add(corpoComTexto("O pedido ainda não foi enviado para a unidade."));
            }
        }
        return card;
    }

    private Div cardConcluir() {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Concluir o preparo", "F1 RN 4"));
        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Paragraph("Concluir fecha o preparo; o que foi consumido não volta ao estoque."));
        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(Acao.primario("Concluir preparo", this::concluirPreparo));
        corpo.add(acoes);
        card.add(corpo);

        Div cancelar = new Div();
        cancelar.addClassName("card");
        cancelar.add(tituloDoCard("Cancelar o preparo", "F1 RN 5"));
        Div corpoCancelar = new Div();
        corpoCancelar.addClassName("card-corpo");
        corpoCancelar.add(new Paragraph("O cancelamento devolve ao estoque as mesmas quantidades lançadas na baixa, "
                + "gravando uma entrada do tipo devolução."));
        Div acoesCancelar = new Div();
        acoesCancelar.addClassName("acoes-form");
        acoesCancelar.add(Acao.perigo("Cancelar preparo e devolver os insumos", this::cancelarPreparo));
        corpoCancelar.add(acoesCancelar);
        cancelar.add(corpoCancelar);

        Div ambos = new Div();
        ambos.add(card, cancelar);
        return ambos;
    }

    private Div corpoComTexto(String texto) {
        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Paragraph(texto));
        return corpo;
    }

    private void darBaixa() {
        executar(() -> preparo.iniciar(PedidoDeDemonstracao.ID),
                "Preparo iniciado e insumos baixados.");
    }

    private void concluirPreparo() {
        executar(() -> preparo.concluir(PedidoDeDemonstracao.ID), "Preparo concluído.");
    }

    private void cancelarPreparo() {
        executar(() -> preparo.cancelar(PedidoDeDemonstracao.ID),
                "Preparo cancelado e insumos devolvidos ao estoque.");
    }

    private void executar(Runnable acao, String sucesso) {
        try {
            acao.run();
            montar();
            aviso(sucesso, true);
        } catch (RegraDeNegocioException excecao) {
            aviso(excecao.getMessage(), false);
        }
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

    private Span destaque(String texto) {
        Span span = new Span(texto);
        span.addClassName("destaque");
        return span;
    }

    private void aviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}