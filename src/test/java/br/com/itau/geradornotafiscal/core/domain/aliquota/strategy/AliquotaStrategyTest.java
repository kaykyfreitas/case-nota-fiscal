package br.com.itau.geradornotafiscal.core.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.core.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
    void pessoaFisica(BigDecimal valorTotalItens, BigDecimal aliquotaEsperada) {
        AliquotaStrategy strategy = new PessoaFisicaAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.FISICA, null, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertThat(strategy.aliquota(pedido)).isEqualByComparingTo(aliquotaEsperada);
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
    void simplesNacional(BigDecimal valorTotalItens, BigDecimal aliquotaEsperada) {
        AliquotaStrategy strategy = new SimplesNacionalAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertThat(strategy.aliquota(pedido)).isEqualByComparingTo(aliquotaEsperada);
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
    void lucroReal(BigDecimal valorTotalItens, BigDecimal aliquotaEsperada) {
        AliquotaStrategy strategy = new LucroRealAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertThat(strategy.aliquota(pedido)).isEqualByComparingTo(aliquotaEsperada);
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
    void lucroPresumido(BigDecimal valorTotalItens, BigDecimal aliquotaEsperada) {
        AliquotaStrategy strategy = new LucroPresumidoAliquotaStrategy();
        Pedido pedido = pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO, valorTotalItens);

        assertTrue(strategy.aplica(pedido));
        assertThat(strategy.aliquota(pedido)).isEqualByComparingTo(aliquotaEsperada);
    }

    @Test
    void naoDeveAplicarStrategyEmRegimeOuTipoDiferente() {
        assertFalse(new PessoaFisicaAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, new BigDecimal("1000"))));
        assertFalse(new SimplesNacionalAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, new BigDecimal("1000"))));
        assertFalse(new LucroRealAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL, new BigDecimal("1000"))));
        assertFalse(new LucroRealAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, new BigDecimal("1000"))));
        assertFalse(new LucroPresumidoAliquotaStrategy()
                .aplica(pedido(TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL, new BigDecimal("1000"))));
        assertFalse(new LucroPresumidoAliquotaStrategy()
                .aplica(pedido(TipoPessoa.FISICA, null, new BigDecimal("1000"))));
    }

    private Pedido pedido(TipoPessoa tipoPessoa, RegimeTributacaoPJ regime, BigDecimal valorTotalItens) {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(valorTotalItens);

        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(tipoPessoa);
        destinatario.setRegimeTributacao(regime);
        pedido.setDestinatario(destinatario);
        return pedido;
    }
}
