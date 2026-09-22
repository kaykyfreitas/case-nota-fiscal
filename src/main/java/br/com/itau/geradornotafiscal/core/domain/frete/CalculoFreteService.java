package br.com.itau.geradornotafiscal.core.domain.frete;

import br.com.itau.geradornotafiscal.core.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.core.domain.model.Endereco;
import br.com.itau.geradornotafiscal.core.domain.enums.Finalidade;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.Regiao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class CalculoFreteService implements CalculoFrete {

    @Override
    public BigDecimal calcular(Pedido pedido) {
        Regiao regiao = resolverRegiaoEntrega(pedido);
        return pedido.getValorFrete()
                .multiply(regiao.getFatorFrete())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Regiao resolverRegiaoEntrega(Pedido pedido) {
        if (pedido == null || pedido.getDestinatario() == null) {
            throw new PedidoInvalidoException("Pedido sem destinatario");
        }

        List<Endereco> enderecos = pedido.getDestinatario().getEnderecos();
        if (enderecos == null || enderecos.isEmpty()) {
            throw new PedidoInvalidoException("Pedido sem endereco de entrega");
        }

        return enderecos.stream()
                .filter(endereco -> endereco.getFinalidade() == Finalidade.ENTREGA
                        || endereco.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
                .map(Endereco::getRegiao)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new PedidoInvalidoException("Pedido sem endereco de entrega"));
    }
}
