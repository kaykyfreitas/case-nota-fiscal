package br.com.itau.geradornotafiscal.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LucroPresumidoAliquotaStrategy implements AliquotaStrategy {

    private static final BigDecimal FAIXA_1000 = new BigDecimal("1000");
    private static final BigDecimal FAIXA_2000 = new BigDecimal("2000");
    private static final BigDecimal FAIXA_5000 = new BigDecimal("5000");
    private static final BigDecimal ALIQUOTA_03 = new BigDecimal("0.03");
    private static final BigDecimal ALIQUOTA_09 = new BigDecimal("0.09");
    private static final BigDecimal ALIQUOTA_16 = new BigDecimal("0.16");
    private static final BigDecimal ALIQUOTA_20 = new BigDecimal("0.20");

    @Override
    public boolean aplica(Pedido pedido) {
        return pedido.getDestinatario().getTipoPessoa() == TipoPessoa.JURIDICA
                && pedido.getDestinatario().getRegimeTributacao() == RegimeTributacaoPJ.LUCRO_PRESUMIDO;
    }

    @Override
    public BigDecimal aliquota(Pedido pedido) {
        BigDecimal valorTotalItens = pedido.getValorTotalItens();
        if (valorTotalItens.compareTo(FAIXA_1000) < 0) {
            return ALIQUOTA_03;
        }
        if (valorTotalItens.compareTo(FAIXA_2000) <= 0) {
            return ALIQUOTA_09;
        }
        if (valorTotalItens.compareTo(FAIXA_5000) <= 0) {
            return ALIQUOTA_16;
        }
        return ALIQUOTA_20;
    }
}
