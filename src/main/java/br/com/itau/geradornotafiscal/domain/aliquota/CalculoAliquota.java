package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;

import java.util.List;

public interface CalculoAliquota {

    List<ItemNotaFiscal> calcular(Pedido pedido);
}
