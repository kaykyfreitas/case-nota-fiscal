package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraAliquotaProdutoTest {

    @Test
    void deveAplicarAliquotaNoValorUnitarioDeCadaItem() {
        Item item = new Item();
        item.setIdItem("1");
        item.setDescricao("Produto");
        item.setValorUnitario(100);
        item.setQuantidade(2);

        List<ItemNotaFiscal> itens = new CalculadoraAliquotaProduto()
                .calcularAliquota(List.of(item), 0.12);

        assertEquals(1, itens.size());
        assertEquals("1", itens.get(0).getIdItem());
        assertEquals(12, itens.get(0).getValorTributoItem());
        assertEquals(2, itens.get(0).getQuantidade());
    }
}
