package br.com.itau.geradornotafiscal.core.domain.exception;

import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;

public class RegimeTributacaoNaoSuportadoException extends GeradorNotaFiscalException {

    public RegimeTributacaoNaoSuportadoException(TipoPessoa tipoPessoa, RegimeTributacaoPJ regimeTributacao) {
        super("Regime tributario nao suportado para " + tipoPessoa + ": " + regimeTributacao);
    }
}
