package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.Pedido;

import java.math.BigDecimal;

public interface AliquotaStrategy {

    boolean aplica(Pedido pedido);

    BigDecimal aliquota(Pedido pedido);
}
