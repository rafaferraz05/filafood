package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Lista os insumos da unidade com saldo, ponto de pedido e sinal de reposição (D-11). */
@Service
public class ListarInsumos {

    private final Insumos insumos;

    public ListarInsumos(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional(readOnly = true)
    public List<InsumoResumo> executar(String unidadeId) {
        LocalDate hoje = LocalDate.now();
        return insumos.doEstoqueDaUnidade(unidadeId).stream()
                .map(insumo -> new InsumoResumo(
                        insumo.id(),
                        insumo.nome(),
                        insumo.unidadeDeMedida(),
                        quantidade(insumo.saldo()),
                        quantidade(insumo.estoqueMinimo()),
                        quantidade(insumo.pontoDePedido(hoje)),
                        insumo.isAtivo(),
                        insumo.estaARepor(hoje)))
                .toList();
    }

    static BigDecimal quantidade(double valor) {
        return BigDecimal.valueOf(valor).setScale(3, RoundingMode.HALF_UP);
    }
}
