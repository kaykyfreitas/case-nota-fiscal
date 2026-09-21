package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.domain.exception.GeradorNotaFiscalException;
import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GeradorNotaFiscalExceptionHandler {

    private static final URI TIPO_PEDIDO_INVALIDO =
            URI.create("urn:gerador-nota-fiscal:problema:pedido-invalido");
    private static final URI TIPO_REGIME_NAO_SUPORTADO =
            URI.create("urn:gerador-nota-fiscal:problema:regime-tributacao-nao-suportado");
    private static final URI TIPO_REQUISICAO_INVALIDA =
            URI.create("urn:gerador-nota-fiscal:problema:requisicao-invalida");
    private static final URI TIPO_ERRO_INTERNO =
            URI.create("urn:gerador-nota-fiscal:problema:erro-interno");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidacao(MethodArgumentNotValidException ex) {
        ProblemDetail problem = problema(
                HttpStatus.BAD_REQUEST,
                TIPO_REQUISICAO_INVALIDA,
                "Requisição inválida",
                "Um ou mais campos do pedido sao invalidos.",
                "REQUISICAO_INVALIDA");
        problem.setProperty("errors", erros(ex));
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleCorpoInvalido(HttpMessageNotReadableException ex) {
        return problema(
                HttpStatus.BAD_REQUEST,
                TIPO_REQUISICAO_INVALIDA,
                "Requisição inválida",
                "O corpo da requisicao e invalido.",
                "REQUISICAO_INVALIDA");
    }

    @ExceptionHandler(PedidoInvalidoException.class)
    ProblemDetail handlePedidoInvalido(PedidoInvalidoException ex) {
        return problema(
                HttpStatus.UNPROCESSABLE_ENTITY,
                TIPO_PEDIDO_INVALIDO,
                "Pedido inválido",
                ex.getMessage(),
                "PEDIDO_INVALIDO");
    }

    @ExceptionHandler(RegimeTributacaoNaoSuportadoException.class)
    ProblemDetail handleRegimeNaoSuportado(RegimeTributacaoNaoSuportadoException ex) {
        return problema(
                HttpStatus.UNPROCESSABLE_ENTITY,
                TIPO_REGIME_NAO_SUPORTADO,
                "Regime tributário não suportado",
                ex.getMessage(),
                "REGIME_TRIBUTACAO_NAO_SUPORTADO");
    }

    @ExceptionHandler(GeradorNotaFiscalException.class)
    ProblemDetail handleDominio(GeradorNotaFiscalException ex) {
        return problema(
                HttpStatus.UNPROCESSABLE_ENTITY,
                TIPO_PEDIDO_INVALIDO,
                "Não foi possível gerar a nota fiscal",
                ex.getMessage(),
                "GERADOR_NOTA_FISCAL");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleErroInterno(Exception ex) {
        log.error("Erro inesperado ao processar a requisicao", ex);
        return problema(
                HttpStatus.INTERNAL_SERVER_ERROR,
                TIPO_ERRO_INTERNO,
                "Erro interno",
                "Ocorreu um erro interno ao gerar a nota fiscal.",
                "ERRO_INTERNO");
    }

    private List<CampoInvalido> erros(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .map(this::campoInvalido)
                .toList();
    }

    private CampoInvalido campoInvalido(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return new CampoInvalido(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new CampoInvalido(error.getObjectName(), error.getDefaultMessage());
    }

    private ProblemDetail problema(HttpStatus status, URI type, String title, String detail, String codigo) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        problem.setProperty("codigo", codigo);
        return problem;
    }

    private record CampoInvalido(String field, String message) {
    }
}
