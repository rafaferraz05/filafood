package br.edu.cesar.filafood.apresentacao.entrega.zona;

import java.math.BigDecimal;
import java.util.List;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.aplicacao.entrega.zona.AlteracaoDeZona;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ExemploDeTaxa;
import br.edu.cesar.filafood.aplicacao.entrega.zona.NovaZona;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaDetalhe;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaResumo;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaServicoAplicacao;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Campo;
import br.edu.cesar.filafood.apresentacao.layout.Entradas;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/** Tela de Zonas e taxas (F2 RN 2 e RN 3). */
@Route(value = "zonas-taxas", layout = MainLayout.class)
@PageTitle("FilaFood · Zonas e taxas")
public class ZonasTaxasRota extends Div {

    private final ZonaServicoAplicacao servico;
    private final Div lista = new Div();
    private final Paragraph aviso = new Paragraph();

    public ZonasTaxasRota(ZonaServicoAplicacao servico) {
        this.servico = servico;
        aviso.addClassName("aviso-erro");
        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(Acao.primario("Nova zona", this::abrirCadastro));

        Paragraph rodape = new Paragraph("A taxa é gravada na entrega no despacho e não é recalculada depois. "
                + "Zona desativada não recebe novas entregas e pode ser reativada.");
        rodape.addClassName("rodape");

        add(new Cabecalho("Zonas e taxas", "Zonas de entrega da unidade e a taxa cobrada em cada uma."), aviso,
                barra, lista, rodape);
        render();
    }

    private void render() {
        List<ZonaResumo> zonas = servico.pesquisarResumos(UnidadeDeDemonstracao.ID);
        long ativas = zonas.stream().filter(ZonaResumo::ativa).count();

        lista.removeAll();
        Div badges = new Div();
        badges.addClassName("badges");
        badges.add(Badge.neutro(zonas.size() + " zonas"), Badge.ok(ativas + " ativas"));
        lista.add(badges);

        Tabela tabela = new Tabela("Zona", "Centro", "Raio", "Valor base", "Raio grátis", "Por km", "Teto",
                "Mínimo", "Estado", "Ações");
        for (ZonaResumo zona : zonas) {
            Div acoes = new Div();
            acoes.addClassName("acoes");
            acoes.add(Acao.link("Editar", () -> abrirEdicao(zona)));
            if (zona.ativa()) {
                acoes.add(Acao.apagada("Desativar", () -> desativar(zona)));
            } else {
                acoes.add(Acao.link("Reativar", () -> reativar(zona)));
            }
            tabela.linha(new Span(zona.nome()),
                    new Span(String.format("%.4f, %.4f", zona.centro().latitude(), zona.centro().longitude())),
                    new Span(km(zona.raioKm())), new Span(dinheiro(zona.valorBase())),
                    new Span(km(zona.raioGratuitoKm())), new Span(dinheiro(zona.valorPorKmExcedente())),
                    new Span(dinheiro(zona.teto())), new Span(dinheiro(zona.valorMinimoDePedido())),
                    zona.ativa() ? Badge.ok("ativa") : Badge.neutro("inativa"), acoes);
        }
        lista.add(tabela);

        Div card = new Div();
        card.addClassName("card");
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span("Como a taxa é calculada"));
        card.add(titulo);
        Element exemplos = new Element("ul");
        exemplos.getClassList().add("lista-exemplos");
        for (ExemploDeTaxa exemplo : servico.exemplosDeTaxa(UnidadeDeDemonstracao.ID)) {
            Element item = new Element("li");
            Element valor = new Element("b");
            valor.setText(dinheiro(exemplo.valor()));
            Element descricao = new Element("span");
            descricao.setText(exemplo.descricao());
            item.appendChild(valor);
            item.appendChild(descricao);
            exemplos.appendChild(item);
        }
        card.getElement().appendChild(exemplos);
        lista.add(card);
    }

    private void abrirCadastro() {
        abrirFormulario(null);
    }

    private void abrirEdicao(ZonaResumo resumo) {
        abrirFormulario(servico.obterDetalhe(resumo.id().toString()));
    }

    private void abrirFormulario(ZonaDetalhe detalhe) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(detalhe == null ? "Nova zona" : "Editar zona");

        Input nome = Entradas.texto();
        Input latitude = Entradas.texto();
        Input longitude = Entradas.texto();
        Input raio = Entradas.texto();
        Input base = Entradas.texto();
        Input gratis = Entradas.texto();
        Input porKm = Entradas.texto();
        Input teto = Entradas.texto();
        Input minimo = Entradas.texto();
        if (detalhe != null) {
            nome.setValue(detalhe.nome());
            latitude.setValue(String.valueOf(detalhe.centro().latitude()));
            longitude.setValue(String.valueOf(detalhe.centro().longitude()));
            raio.setValue(String.valueOf(detalhe.raioKm()));
            base.setValue(detalhe.valorBase().toPlainString());
            gratis.setValue(String.valueOf(detalhe.raioGratuitoKm()));
            porKm.setValue(detalhe.valorPorKmExcedente().toPlainString());
            teto.setValue(detalhe.teto().toPlainString());
            minimo.setValue(detalhe.valorMinimoDePedido().toPlainString());
        }

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Nome", nome), new Campo("Latitude", latitude), new Campo("Longitude", longitude),
                new Campo("Raio (km)", raio), new Campo("Valor base (R$)", base),
                new Campo("Raio grátis (km)", gratis), new Campo("Por km (R$)", porKm),
                new Campo("Teto (R$)", teto), new Campo("Mínimo (R$)", minimo));

        Div botoes = new Div();
        botoes.add(Acao.primario("Salvar", () -> salvar(detalhe, nome, latitude, longitude, raio, base, gratis,
                porKm, teto, minimo, dialog)), Acao.claro("Cancelar", dialog::close));
        dialog.add(corpo, botoes);
        dialog.open();
    }

    private void salvar(ZonaDetalhe detalhe, Input nome, Input latitude, Input longitude, Input raio, Input base,
            Input gratis, Input porKm, Input teto, Input minimo, Dialog dialog) {
        try {
            Coordenada centro = new Coordenada(numero(latitude), numero(longitude));
            double raioKm = numero(raio);
            BigDecimal valorBase = dinheiro(base);
            double raioGratis = numero(gratis);
            BigDecimal valorPorKm = dinheiro(porKm);
            BigDecimal valorTeto = dinheiro(teto);
            BigDecimal valorMinimo = dinheiro(minimo);
            if (detalhe == null) {
                servico.cadastrar(new NovaZona(UnidadeDeDemonstracao.ID, nome.getValue(), centro, raioKm,
                        valorBase, raioGratis, valorPorKm, valorTeto, valorMinimo));
            } else {
                servico.alterar(new AlteracaoDeZona(detalhe.id().toString(), nome.getValue(), centro, raioKm,
                        valorBase, raioGratis, valorPorKm, valorTeto, valorMinimo));
            }
            dialog.close();
            aviso.setVisible(false);
            render();
        } catch (RegraDeNegocioException | NumberFormatException erro) {
            aviso.setText("Não foi possível salvar: " + erro.getMessage());
            aviso.setVisible(true);
        }
    }

    private void desativar(ZonaResumo zona) {
        servico.desativar(zona.id().toString());
        render();
    }

    private void reativar(ZonaResumo zona) {
        servico.reativar(zona.id().toString());
        render();
    }

    private static double numero(Input input) {
        return Double.parseDouble(input.getValue().trim().replace(',', '.'));
    }

    private static BigDecimal dinheiro(Input input) {
        return new BigDecimal(input.getValue().trim().replace(',', '.'));
    }

    private static String km(double valor) {
        return String.format("%.0f km", valor);
    }

    private static String dinheiro(BigDecimal valor) {
        return "R$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
