package br.com.itau.geradornotafiscal.core.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.core.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PessoaFisicaAliquotaStrategy implements AliquotaStrategy {

    private static final BigDecimal FAIXA_500 = new BigDecimal("500");
    private static final BigDecimal FAIXA_2000 = new BigDecimal("2000");
    private static final BigDecimal FAIXA_3500 = new BigDecimal("3500");
    private static final BigDecimal ALIQUOTA_12 = new BigDecimal("0.12");
    private static final BigDecimal ALIQUOTA_15 = new BigDecimal("0.15");
    private static final BigDecimal ALIQUOTA_17 = new BigDecimal("0.17");

    @Override
    public boolean aplica(Pedido pedido) {
        return pedido.getDestinatario().getTipoPessoa() == TipoPessoa.FISICA;
    }

    @Override
    public BigDecimal aliquota(Pedido pedido) {
        BigDecimal valorTotalItens = pedido.getValorTotalItens();
        if (valorTotalItens.compareTo(FAIXA_500) < 0) {
            return BigDecimal.ZERO;
        }
        if (valorTotalItens.compareTo(FAIXA_2000) <= 0) {
            return ALIQUOTA_12;
        }
        if (valorTotalItens.compareTo(FAIXA_3500) <= 0) {
            return ALIQUOTA_15;
        }
        return ALIQUOTA_17;
    }
}
