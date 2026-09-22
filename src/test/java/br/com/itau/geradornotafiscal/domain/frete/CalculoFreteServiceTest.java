package br.com.itau.geradornotafiscal.domain.frete;

import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Finalidade;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.Regiao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculoFreteServiceTest {

    private final CalculoFreteService calculoFreteService = new CalculoFreteService();

    @ParameterizedTest
    @CsvSource({
            "NORTE, 108",
            "NORDESTE, 108.5",
            "CENTRO_OESTE, 107",
            "SUDESTE, 104.8",
            "SUL, 106"
    })
    void deveAplicarFatorDaRegiaoDeEntrega(Regiao regiao, double freteEsperado) {
        Pedido pedido = pedido(100, endereco(Finalidade.ENTREGA, regiao));

        assertEquals(freteEsperado, calculoFreteService.calcular(pedido), 0.0001);
    }

    @Test
    void deveAceitarEnderecoDeCobrancaEntrega() {
        Pedido pedido = pedido(100, endereco(Finalidade.COBRANCA_ENTREGA, Regiao.SUL));

        assertEquals(106, calculoFreteService.calcular(pedido), 0.0001);
    }

    @Test
    void deveFalharQuandoSoHouverEnderecoDeCobranca() {
        Pedido pedido = pedido(100, endereco(Finalidade.COBRANCA, Regiao.SUDESTE));

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoPedidoForNulo() {
        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(null));
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverDestinatario() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(100);

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoNaoHouverEnderecos() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(100);
        pedido.setDestinatario(new Destinatario());

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoListaDeEnderecosEstiverVazia() {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(100);
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(List.of());
        pedido.setDestinatario(destinatario);

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveFalharQuandoEntregaNaoTiverRegiao() {
        Pedido pedido = pedido(100, endereco(Finalidade.ENTREGA, null));

        assertThrows(PedidoInvalidoException.class, () -> calculoFreteService.calcular(pedido));
    }

    @Test
    void deveUsarOPrimeiroEnderecoDeEntrega() {
        Pedido pedido = pedido(
                100,
                endereco(Finalidade.COBRANCA, Regiao.NORTE),
                endereco(Finalidade.ENTREGA, Regiao.SUL),
                endereco(Finalidade.ENTREGA, Regiao.NORDESTE)
        );

        assertEquals(106, calculoFreteService.calcular(pedido), 0.0001);
    }

    private Pedido pedido(double valorFrete, Endereco... enderecos) {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(valorFrete);

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
