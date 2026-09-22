package br.com.itau.geradornotafiscal.core.domain.frete;

import br.com.itau.geradornotafiscal.core.domain.model.Pedido;

import java.math.BigDecimal;

public interface CalculoFrete {

    BigDecimal calcular(Pedido pedido);
}
