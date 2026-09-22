package br.com.itau.geradornotafiscal.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AliquotaStrategyTest {

    @ParameterizedTest
    @CsvSource({
            "499, 0",
            "500, 0.12",
            "2000, 0.12",
            "2001, 0.15",
            "3500, 0.15",
            "3501, 0.17"
    })
    void pessoaFisica(double valorTotalItens, double aliquotaEsperada) {
        AliquotaStrategy strategy = new PessoaFisicaAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.FISICA, null, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertEquals(aliquotaEsperada, strategy.aliquota(pedido));
    }

    @ParameterizedTest
    @CsvSource({
            "999, 0.03",
            "1000, 0.07",
            "2000, 0.07",
            "2001, 0.13",
            "5000, 0.13",
            "5001, 0.19"
    })
    void simplesNacional(double valorTotalItens, double aliquotaEsperada) {
        AliquotaStrategy strategy = new SimplesNacionalAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertEquals(aliquotaEsperada, strategy.aliquota(pedido));
    }

    @ParameterizedTest
    @CsvSource({
            "999, 0.03",
            "1000, 0.09",
            "2000, 0.09",
            "2001, 0.15",
            "5000, 0.15",
            "5001, 0.20"
    })
    void lucroReal(double valorTotalItens, double aliquotaEsperada) {
        AliquotaStrategy strategy = new LucroRealAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertEquals(aliquotaEsperada, strategy.aliquota(pedido));
    }

    @ParameterizedTest
    @CsvSource({
            "999, 0.03",
            "1000, 0.09",
            "2000, 0.09",
            "2001, 0.16",
            "5000, 0.16",
            "5001, 0.20"
    })
    void lucroPresumido(double valorTotalItens, double aliquotaEsperada) {
        AliquotaStrategy strategy = new LucroPresumidoAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertEquals(aliquotaEsperada, strategy.aliquota(pedido));
    }

    @Test
    void naoDeveAplicarStrategyEmRegimeOuTipoDiferente() {
        assertFalse(new PessoaFisicaAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, 1000)));
        assertFalse(new SimplesNacionalAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, 1000)));
        assertFalse(new LucroRealAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL, 1000)));
        assertFalse(new LucroRealAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, 1000)));
        assertFalse(new LucroPresumidoAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, 1000)));
        assertFalse(new LucroPresumidoAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, 1000)));
    }

    private Pedido pedido(TipoPessoa tipoPessoa, RegimeTributacaoPJ regime, double valorTotalItens) {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(valorTotalItens);

        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(tipoPessoa);
        destinatario.setRegimeTributacao(regime);
        pedido.setDestinatario(destinatario);
        return pedido;
    }
}
