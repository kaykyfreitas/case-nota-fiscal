package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Item;
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
        Destinatario destinatario = destinatario(pedido);
        List<Item> itens = pedido.getItens();
        if (itens == null || itens.isEmpty()) {
            throw new PedidoInvalidoException("Pedido sem itens");
        }

        return strategies.stream()
                .filter(strategy -> strategy.aplica(pedido))
                .findFirst()
                .map(strategy -> calculadoraAliquotaProduto.calcularAliquota(
                        itens,
                        strategy.aliquota(pedido)))
                .orElseThrow(() -> new RegimeTributacaoNaoSuportadoException(
                        destinatario.getTipoPessoa(),
                        destinatario.getRegimeTributacao()));
    }

    private Destinatario destinatario(Pedido pedido) {
        if (pedido == null || pedido.getDestinatario() == null) {
            throw new PedidoInvalidoException("Pedido sem destinatario");
        }
        return pedido.getDestinatario();
    }
}
