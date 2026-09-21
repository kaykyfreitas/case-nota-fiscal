package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculoAliquotaService implements CalculoAliquota {

    private final List<AliquotaStrategy> strategies;
    private final CalculadoraAliquotaProduto calculadoraAliquotaProduto;

    @Override
    public List<ItemNotaFiscal> calcular(Pedido pedido) {
        return strategies.stream()
                .filter(strategy -> strategy.aplica(pedido))
                .findFirst()
                .map(strategy -> calculadoraAliquotaProduto.calcularAliquota(
                        pedido.getItens(),
                        strategy.aliquota(pedido)))
                .orElseGet(List::of);
    }
}
