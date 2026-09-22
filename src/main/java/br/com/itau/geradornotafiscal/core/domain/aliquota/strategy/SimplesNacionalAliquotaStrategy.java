package br.com.itau.geradornotafiscal.core.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.core.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SimplesNacionalAliquotaStrategy implements AliquotaStrategy {

    private static final BigDecimal FAIXA_1000 = new BigDecimal("1000");
    private static final BigDecimal FAIXA_2000 = new BigDecimal("2000");
    private static final BigDecimal FAIXA_5000 = new BigDecimal("5000");
    private static final BigDecimal ALIQUOTA_03 = new BigDecimal("0.03");
    private static final BigDecimal ALIQUOTA_07 = new BigDecimal("0.07");
    private static final BigDecimal ALIQUOTA_13 = new BigDecimal("0.13");
    private static final BigDecimal ALIQUOTA_19 = new BigDecimal("0.19");

    @Override
    public boolean aplica(Pedido pedido) {
        return pedido.getDestinatario().getTipoPessoa() == TipoPessoa.JURIDICA
                && pedido.getDestinatario().getRegimeTributacao() == RegimeTributacaoPJ.SIMPLES_NACIONAL;
    }

    @Override
    public BigDecimal aliquota(Pedido pedido) {
        BigDecimal valorTotalItens = pedido.getValorTotalItens();
        if (valorTotalItens.compareTo(FAIXA_1000) < 0) {
            return ALIQUOTA_03;
        }
        if (valorTotalItens.compareTo(FAIXA_2000) <= 0) {
            return ALIQUOTA_07;
        }
        if (valorTotalItens.compareTo(FAIXA_5000) <= 0) {
            return ALIQUOTA_13;
        }
        return ALIQUOTA_19;
    }
}
