package br.com.itau.geradornotafiscal.domain.frete;

import br.com.itau.geradornotafiscal.model.Pedido;

import java.math.BigDecimal;

public interface CalculoFrete {

    BigDecimal calcular(Pedido pedido);
}
