package br.com.itau.geradornotafiscal.domain.exception;

import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;

public class RegimeTributacaoNaoSuportadoException extends GeradorNotaFiscalException {

    public RegimeTributacaoNaoSuportadoException(TipoPessoa tipoPessoa, RegimeTributacaoPJ regimeTributacao) {
        super("Regime tributario nao suportado para " + tipoPessoa + ": " + regimeTributacao);
    }
}
