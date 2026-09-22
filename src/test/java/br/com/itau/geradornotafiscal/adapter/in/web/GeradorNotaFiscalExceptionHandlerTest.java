package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorNotaFiscalExceptionHandlerTest {

    @Test
    void deveMapearErroDeClasseSemField() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Pedido(), "pedido");
        binding.addError(new ObjectError("pedido", "erro de classe"));
        MethodParameter parameter = new MethodParameter(
                GeradorNFController.class.getDeclaredMethod("gerarNotaFiscal", Pedido.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ProblemDetail problem = new GeradorNotaFiscalExceptionHandler().handleValidacao(ex);

        @SuppressWarnings("unchecked")
        List<Object> errors = (List<Object>) problem.getProperties().get("errors");
        assertEquals(1, errors.size());
        assertEquals("REQUISICAO_INVALIDA", problem.getProperties().get("codigo"));
        assertTrue(errors.get(0).toString().contains("pedido"));
        assertTrue(errors.get(0).toString().contains("erro de classe"));
    }
}
