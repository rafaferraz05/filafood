package br.edu.cesar.filafood.apresentacao.estoque.preparo;

import java.math.BigDecimal;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoParaPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.InsumoDoPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.ItemDoPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoDetalhe;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.PreparoServicoAplicacao;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Identificadores;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;

/** Preparo dos pedidos no layout e no CSS do protótipo (F1 RN 4 e RN 5). */
@Route(value = "preparo", layout = MainLayout.class)
@PageTitle("FilaFood · Preparo do pedido")
public class PreparoRota extends Div {

    private static final String RODAPE = "Ao iniciar o preparo, a ficha técnica dos produtos é explodida e a "
            + "baixa é tudo ou nada: se faltar saldo de qualquer insumo, nenhuma movimentação é gravada. "
            + "Cancelar o preparo devolve exatamente o que foi consumido.";

    private final PreparoServicoAplicacao preparo;
    private final Paragraph aviso = new Paragraph();
    private String pedidoSelecionado;

    public PreparoRota(PreparoServicoAplicacao preparo) {
        this.preparo = preparo;
        aviso.setVisible(false);
        montar();
    }

    private void montar() {
        List<PedidoParaPreparo> pedidos = preparo.listar(UnidadeDeDemonstracao.ID);
        if (pedidoSelecionado == null || pedidos.stream().noneMatch(pedido -> pedido.id().equals(pedidoSelecionado))) {
            pedidoSelecionado = pedidos.stream()
                    .filter(pedido -> pedido.estado() == EstadoDoPedido.ENVIADO)
                    .map(PedidoParaPreparo::id)
                    .findFirst()
                    .orElse(pedidos.isEmpty() ? null : pedidos.get(0).id());
        }

        removeAll();
        aviso.setVisible(false);

        long enviados = pedidos.stream().filter(pedido -> pedido.estado() == EstadoDoPedido.ENVIADO).count();
        long emPreparo = pedidos.stream().filter(pedido -> pedido.estado() == EstadoDoPedido.EM_PREPARO).count();
        Cabecalho cabecalho = new Cabecalho("Preparo do pedido",
                "Fila de pedidos da unidade; abra um pedido para ver itens e insumos.");
        cabecalho.badges(Badge.neutro(pedidos.size() + " pedidos"),
                Badge.repor(enviados + " enviados"), Badge.alerta(emPreparo + " em preparo"));

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho, aviso, cardPedidos(pedidos), rodape);
        if (pedidoSelecionado == null) {
            return;
        }

        PreparoDetalhe detalhe = preparo.detalhar(pedidoSelecionado);
        Div esquerda = new Div();
        esquerda.add(cardItens(detalhe), cardDeAcao(detalhe));

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(esquerda, cardInsumos(detalhe));
        add(grid);
    }

    private Div cardPedidos(List<PedidoParaPreparo> pedidos) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Pedidos da unidade", "clique em Abrir"));

        if (pedidos.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum pedido com itens para preparar nesta unidade.");
            vazio.addClassName("vazio");
            card.add(vazio);
            return card;
        }

        Tabela tabela = new Tabela("Pedido", "Estado", "Itens", "Porções", "Ações");
        for (PedidoParaPreparo pedido : pedidos) {
            Div acoes = new Div();
            acoes.addClassName("acoes");
            if (pedido.id().equals(pedidoSelecionado)) {
                acoes.add(new Span("selecionado"));
            } else {
                acoes.add(Acao.link("Abrir", () -> {
                    pedidoSelecionado = pedido.id();
                    montar();
                }));
            }
            tabela.linha(new Span(Identificadores.pedido(pedido.id())), badgeDoEstado(pedido.estado()),
                    new Span(String.valueOf(pedido.itens().size())), new Span(String.valueOf(porcoes(pedido))),
                    acoes);
        }
        card.add(tabela);
        return card;
    }

    private static int porcoes(PedidoParaPreparo pedido) {
        return pedido.itens().stream().mapToInt(ItemDoPedido::quantidade).sum();
    }

    private Component badgeDoEstado(EstadoDoPedido estado) {
        return switch (estado) {
            case EM_PREPARO -> Badge.alerta("em preparo");
            case CONCLUIDO -> Badge.ok("concluído");
            case CANCELADO -> Badge.neutro("cancelado");
            case RECUSADO -> Badge.alerta("recusado");
            case ENVIADO -> Badge.neutro("aguardando preparo");
            case RASCUNHO -> Badge.neutro("rascunho");
        };
    }

    private Div cardItens(PreparoDetalhe detalhe) {
        Tabela tabela = new Tabela("Produto", "Quantidade");
        for (ItemDoPreparo item : detalhe.itens()) {
            tabela.linha(destaque(item.produto()), new Span(String.valueOf(item.quantidade())));
        }
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Itens do pedido " + Identificadores.pedido(detalhe.pedidoId()),
                detalhe.itens().size() + " produtos"), tabela);
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
                return cardConcluir();
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
        executar(() -> preparo.iniciar(pedidoSelecionado), "Preparo iniciado e insumos baixados.");
    }

    private void concluirPreparo() {
        executar(() -> preparo.concluir(pedidoSelecionado), "Preparo concluído.");
    }

    private void cancelarPreparo() {
        executar(() -> preparo.cancelar(pedidoSelecionado),
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