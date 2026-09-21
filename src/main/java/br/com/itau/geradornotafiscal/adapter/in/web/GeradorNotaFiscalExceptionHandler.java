package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.domain.exception.GeradorNotaFiscalException;
import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GeradorNotaFiscalExceptionHandler {

    private static final URI TIPO_PEDIDO_INVALIDO =
            URI.create("urn:gerador-nota-fiscal:problema:pedido-invalido");
    private static final URI TIPO_REGIME_NAO_SUPORTADO =
            URI.create("urn:gerador-nota-fiscal:problema:regime-tributacao-nao-suportado");
    private static final URI TIPO_ERRO_INTERNO =
            URI.create("urn:gerador-nota-fiscal:problema:erro-interno");

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

    private ProblemDetail problema(HttpStatus status, URI type, String title, String detail, String codigo) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        problem.setProperty("codigo", codigo);
        return problem;
    }
}
