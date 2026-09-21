package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.Pedido;

public interface AliquotaStrategy {

    boolean aplica(Pedido pedido);

    double aliquota(Pedido pedido);
}
