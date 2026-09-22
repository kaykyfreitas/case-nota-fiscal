package br.com.itau.geradornotafiscal.core.domain.aliquota;

import br.com.itau.geradornotafiscal.core.domain.model.Pedido;

import java.math.BigDecimal;

public interface AliquotaStrategy {

    boolean aplica(Pedido pedido);

    BigDecimal aliquota(Pedido pedido);
}
