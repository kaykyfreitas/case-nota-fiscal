package br.com.itau.geradornotafiscal.core.domain.model.validation;

import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Endereco;
import br.com.itau.geradornotafiscal.core.domain.enums.Finalidade;
import br.com.itau.geradornotafiscal.core.domain.enums.Regiao;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadoresDeDestinatarioTest {

    private final EnderecoEntregaPresenteValidator enderecoValidator = new EnderecoEntregaPresenteValidator();
    private final RegimeTributacaoParaPessoaJuridicaValidator regimeValidator =
            new RegimeTributacaoParaPessoaJuridicaValidator();

    @Test
    void enderecoDeveAceitarDestinatarioNulo() {
        assertTrue(enderecoValidator.isValid(null, null));
    }

    @Test
    void enderecoDeveRecusarListaNula() {
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(null);
        assertFalse(enderecoValidator.isValid(destinatario, null));
    }

    @Test
    void enderecoDeveIgnorarEntradaNulaNaLista() {
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(new ArrayList<>(Arrays.asList(null, endereco(Finalidade.COBRANCA, Regiao.SUL))));
        assertFalse(enderecoValidator.isValid(destinatario, null));
    }

    @Test
    void enderecoDeveExigirRegiaoNaEntrega() {
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(java.util.List.of(endereco(Finalidade.ENTREGA, null)));
        assertFalse(enderecoValidator.isValid(destinatario, null));
    }

    @Test
    void enderecoDeveAceitarCobrancaEntregaComRegiao() {
        Destinatario destinatario = new Destinatario();
        destinatario.setEnderecos(java.util.List.of(endereco(Finalidade.COBRANCA_ENTREGA, Regiao.NORTE)));
        assertTrue(enderecoValidator.isValid(destinatario, null));
    }

    @Test
    void regimeDeveAceitarNuloOuPessoaFisica() {
        assertTrue(regimeValidator.isValid(null, null));

        Destinatario fisica = new Destinatario();
        fisica.setTipoPessoa(TipoPessoa.FISICA);
        assertTrue(regimeValidator.isValid(fisica, null));
    }

    @Test
    void regimeDeveRecusarPjSemRegime() {
        Destinatario pj = new Destinatario();
        pj.setTipoPessoa(TipoPessoa.JURIDICA);
        assertFalse(regimeValidator.isValid(pj, null));
    }

    @Test
    void regimeDeveAceitarPjComRegime() {
        Destinatario pj = new Destinatario();
        pj.setTipoPessoa(TipoPessoa.JURIDICA);
        pj.setRegimeTributacao(RegimeTributacaoPJ.SIMPLES_NACIONAL);
        assertTrue(regimeValidator.isValid(pj, null));
    }

    private Endereco endereco(Finalidade finalidade, Regiao regiao) {
        Endereco endereco = new Endereco();
        endereco.setFinalidade(finalidade);
        endereco.setRegiao(regiao);
        return endereco;
    }
}
