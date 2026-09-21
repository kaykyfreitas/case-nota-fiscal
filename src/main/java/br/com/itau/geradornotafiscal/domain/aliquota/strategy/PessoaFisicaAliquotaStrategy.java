package br.com.itau.geradornotafiscal.domain.aliquota.strategy;

import br.com.itau.geradornotafiscal.domain.aliquota.AliquotaStrategy;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaFisicaAliquotaStrategy implements AliquotaStrategy {

    @Override
    public boolean aplica(Pedido pedido) {
        return pedido.getDestinatario().getTipoPessoa() == TipoPessoa.FISICA;
    }

    @Override
    public double aliquota(Pedido pedido) {
        double valorTotalItens = pedido.getValorTotalItens();
        if (valorTotalItens < 500) {
            return 0;
        }
        if (valorTotalItens <= 2000) {
            return 0.12;
        }
        if (valorTotalItens <= 3500) {
            return 0.15;
        }
        return 0.17;
    }
}
