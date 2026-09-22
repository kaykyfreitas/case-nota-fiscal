package br.com.itau.geradornotafiscal.core.domain.aliquota;

import br.com.itau.geradornotafiscal.core.domain.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;

import java.util.List;

public interface CalculoAliquota {

    List<ItemNotaFiscal> calcular(Pedido pedido);
}
