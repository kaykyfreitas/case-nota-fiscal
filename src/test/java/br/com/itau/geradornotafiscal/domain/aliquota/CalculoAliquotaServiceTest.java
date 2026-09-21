package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.domain.aliquota.strategy.LucroPresumidoAliquotaStrategy;
import br.com.itau.geradornotafiscal.domain.aliquota.strategy.LucroRealAliquotaStrategy;
import br.com.itau.geradornotafiscal.domain.aliquota.strategy.PessoaFisicaAliquotaStrategy;
import br.com.itau.geradornotafiscal.domain.aliquota.strategy.SimplesNacionalAliquotaStrategy;
import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculoAliquotaServiceTest {

    private CalculoAliquotaService calculoAliquotaService;

    @BeforeEach
    void setup() {
        calculoAliquotaService = new CalculoAliquotaService(
                List.of(
                        new PessoaFisicaAliquotaStrategy(),
                        new SimplesNacionalAliquotaStrategy(),
                        new LucroRealAliquotaStrategy(),
                        new LucroPresumidoAliquotaStrategy()
                ),
                new CalculadoraAliquotaProduto()
        );
    }

    @Test
    void deveCalcularAliquotaZeroParaPessoaFisicaAbaixoDe500() {
        Pedido pedido = pedido(TipoPessoa.FISICA, null, 400, item(100, 4));

        List<ItemNotaFiscal> itens = calculoAliquotaService.calcular(pedido);

        assertEquals(1, itens.size());
        assertEquals(0, itens.get(0).getValorTributoItem());
    }

    @Test
    void deveCalcularAliquotaDeLucroPresumidoAcimaDe5000() {
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO, 6000, item(1000, 6));

        List<ItemNotaFiscal> itens = calculoAliquotaService.calcular(pedido);

        assertEquals(1, itens.size());
        assertEquals(0.20 * 1000, itens.get(0).getValorTributoItem());
    }

    @Test
    void deveFalharQuandoRegimeForOutros() {
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.OUTROS, 6000, item(1000, 6));

        assertThrows(RegimeTributacaoNaoSuportadoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverDestinatario() {
        Pedido pedido = new Pedido();
        pedido.setItens(List.of(item(100, 1)));

        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverItens() {
        Pedido pedido = pedido(TipoPessoa.FISICA, null, 400);

        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    private Pedido pedido(TipoPessoa tipoPessoa, RegimeTributacaoPJ regime, double valorTotal, Item... itens) {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(valorTotal);
        pedido.setItens(List.of(itens));

        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(tipoPessoa);
        destinatario.setRegimeTributacao(regime);
        pedido.setDestinatario(destinatario);
        return pedido;
    }

    private Item item(double valorUnitario, int quantidade) {
        Item item = new Item();
        item.setValorUnitario(valorUnitario);
        item.setQuantidade(quantidade);
        return item;
    }
}
