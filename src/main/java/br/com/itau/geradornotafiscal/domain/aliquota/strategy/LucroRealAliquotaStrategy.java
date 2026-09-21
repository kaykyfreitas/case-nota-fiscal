package br.com.itau.geradornotafiscal.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import org.springframework.stereotype.Component;

@Component
public class LucroRealAliquotaStrategy implements AliquotaStrategy {

    @Override
    public boolean aplica(Pedido pedido) {
        return pedido.getDestinatario().getTipoPessoa() == TipoPessoa.JURIDICA
                && pedido.getDestinatario().getRegimeTributacao() == RegimeTributacaoPJ.LUCRO_REAL;
    }

    @Override
    public double aliquota(Pedido pedido) {
        double valorTotalItens = pedido.getValorTotalItens();
        if (valorTotalItens < 1000) {
            return 0.03;
        }
        if (valorTotalItens <= 2000) {
            return 0.09;
        }
        if (valorTotalItens <= 5000) {
            return 0.15;
        }
        return 0.20;
    }
}
