package br.com.itau.geradornotafiscal.core.domain.aliquota;

import br.com.itau.geradornotafiscal.core.domain.aliquota.strategy.LucroPresumidoAliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.aliquota.strategy.LucroRealAliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.aliquota.strategy.PessoaFisicaAliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.aliquota.strategy.SimplesNacionalAliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.core.domain.exception.RegimeTributacaoNaoSuportadoException;
import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Item;
import br.com.itau.geradornotafiscal.core.domain.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        Pedido pedido = pedido(TipoPessoa.FISICA, null, "400", item("100", 4));

        List<ItemNotaFiscal> itens = calculoAliquotaService.calcular(pedido);

        assertEquals(1, itens.size());
        assertThat(itens.get(0).getValorTributoItem()).isEqualByComparingTo("0");
    }

    @Test
    void deveCalcularAliquotaDeLucroPresumidoAcimaDe5000() {
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO, "6000", item("1000", 6));

        List<ItemNotaFiscal> itens = calculoAliquotaService.calcular(pedido);

        assertEquals(1, itens.size());
        assertThat(itens.get(0).getValorTributoItem()).isEqualByComparingTo("1200.00");
    }

    @Test
    void deveFalharQuandoRegimeForOutros() {
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.OUTROS, "6000", item("1000", 6));

        assertThrows(RegimeTributacaoNaoSuportadoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverDestinatario() {
        Pedido pedido = new Pedido();
        pedido.setItens(List.of(item("100", 1)));

        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoForNulo() {
        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(null));
    }

    @Test
    void deveFalharQuandoListaDeItensForNula() {
        Pedido pedido = new Pedido();
        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(TipoPessoa.FISICA);
        pedido.setDestinatario(destinatario);
        pedido.setItens(null);

        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoListaDeItensEstiverVazia() {
        Pedido pedido = pedido(TipoPessoa.FISICA, null, "400");

        assertThrows(PedidoInvalidoException.class, () -> calculoAliquotaService.calcular(pedido));
    }

    private Pedido pedido(TipoPessoa tipoPessoa, RegimeTributacaoPJ regime, String valorTotal, Item... itens) {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(new BigDecimal(valorTotal));
        pedido.setItens(List.of(itens));

        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(tipoPessoa);
        destinatario.setRegimeTributacao(regime);
        pedido.setDestinatario(destinatario);
        return pedido;
    }

    private Item item(String valorUnitario, int quantidade) {
        Item item = new Item();
        item.setValorUnitario(new BigDecimal(valorUnitario));
        item.setQuantidade(quantidade);
        return item;
    }
}
