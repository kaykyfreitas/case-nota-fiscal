package br.com.itau.geradornotafiscal.domain.aliquota;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraAliquotaProdutoTest {

    @Test
    void deveAplicarAliquotaNoValorDaLinha() {
        Item item = new Item();
        item.setIdItem("1");
        item.setDescricao("Produto");
        item.setValorUnitario(new BigDecimal("100"));
        item.setQuantidade(2);

        List<ItemNotaFiscal> itens = new CalculadoraAliquotaProduto()
                .calcularAliquota(List.of(item), new BigDecimal("0.12"));

        assertEquals(1, itens.size());
        assertEquals("1", itens.get(0).getIdItem());
        assertThat(itens.get(0).getValorTributoItem()).isEqualByComparingTo("24.00");
        assertEquals(2, itens.get(0).getQuantidade());
    }

    @Test
    void deveArredondarTributoDaLinhaComDuasCasasHalfUp() {
        Item item = new Item();
        item.setValorUnitario(new BigDecimal("10.15"));
        item.setQuantidade(2);

        List<ItemNotaFiscal> itens = new CalculadoraAliquotaProduto()
                .calcularAliquota(List.of(item), new BigDecimal("0.12"));

        assertThat(itens.get(0).getValorTributoItem()).isEqualByComparingTo("2.44");
    }
}
