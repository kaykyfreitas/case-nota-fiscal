package br.com.itau.geradornotafiscal.core.domain.frete;

import br.com.itau.geradornotafiscal.core.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Endereco;
import br.com.itau.geradornotafiscal.core.domain.enums.Finalidade;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.Regiao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculoFreteServiceTest {

    private final CalculoFreteService calculoFreteService = new CalculoFreteService();

    @ParameterizedTest
    @CsvSource({
            "NORTE, 108.00",
            "NORDESTE, 108.50",
            "CENTRO_OESTE, 107.00",
            "SUDESTE, 104.80",
            "SUL, 106.00"
    })
    void deveAplicarFatorDaRegiaoDeEntrega(Regiao regiao, BigDecimal freteEsperado) {
        Pedido pedido = pedido("100", endereco(Finalidade.ENTREGA, regiao));

        assertThat(calculoFreteService.calcular(pedido)).isEqualByComparingTo(freteEsperado);
    }

    @Test
    void deveAceitarEnderecoDeCobrancaEntrega() {
        Pedido pedido = pedido("100", endereco(Finalidade.COBRANCA_ENTREGA, Regiao.SUL));

        assertThat(calculoFreteService.calcular(pedido)).isEqualByComparingTo("106.00");
    }

    @Test
    void deveFalharQuandoSoHouverEnderecoDeCobranca() {
        Pedido pedido = pedido("100", endereco(Finalidade.COBRANCA, Regiao.SUDESTE));

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoForNulo() {
        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(null));
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverDestinatario() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(new BigDecimal("100"));

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoNaoHouverEnderecos() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(new BigDecimal("100"));
        pedido.setDestinatario(new Destinatario());

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoListaDeEnderecosEstiverVazia() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(new BigDecimal("100"));
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(List.of());
        pedido.setDestinatario(destinatario);

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoEntregaNaoTiverRegiao() {
        Pedido pedido = pedido("100", endereco(Finalidade.ENTREGA, null));

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveUsarOPrimeiroEnderecoDeEntrega() {
        Pedido pedido = pedido(
                "100",
                endereco(Finalidade.COBRANCA, Regiao.NORTE),
                endereco(Finalidade.ENTREGA, Regiao.SUL),
                endereco(Finalidade.ENTREGA, Regiao.NORDESTE)
        );

        assertThat(calculoFreteService.calcular(pedido)).isEqualByComparingTo("106.00");
    }

    private Pedido pedido(String valorFrete, Endereco... enderecos) {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(new BigDecimal(valorFrete));

        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(List.of(enderecos));
        pedido.setDestinatario(destinatario);
        return pedido;
    }

    private Endereco endereco(Finalidade finalidade, Regiao regiao) {
        Endereco endereco = new Endereco();
        endereco.setFinalidade(finalidade);
        endereco.setRegiao(regiao);
        return endereco;
    }
}
