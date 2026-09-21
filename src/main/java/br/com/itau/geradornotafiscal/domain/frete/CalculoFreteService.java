package br.com.itau.geradornotafiscal.domain.frete;

import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Finalidade;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.Regiao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalculoFreteService implements CalculoFrete {

    @Override
    public double calcular(Pedido pedido) {
        return resolverRegiaoEntrega(pedido)
                .map(regiao -> pedido.getValorFrete() * regiao.getFatorFrete())
                .orElse(0.0);
    }

    private Optional<Regiao> resolverRegiaoEntrega(Pedido pedido) {
        List<Endereco> enderecos = pedido.getDestinatario().getEnderecos();
        if (enderecos == null) {
            return Optional.empty();
        }

        return enderecos.stream()
                .filter(endereco -> endereco.getFinalidade() == Finalidade.ENTREGA
                        || endereco.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
                .map(Endereco::getRegiao)
                .findFirst();
    }
}
