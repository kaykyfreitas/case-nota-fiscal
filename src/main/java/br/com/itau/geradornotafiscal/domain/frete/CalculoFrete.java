package br.com.itau.geradornotafiscal.domain.frete;

import br.com.itau.geradornotafiscal.model.Pedido;

public interface CalculoFrete {

    double calcular(Pedido pedido);
}
