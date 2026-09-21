package br.com.itau.geradornotafiscal.model.validation;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegimeTributacaoParaPessoaJuridicaValidator
        implements ConstraintValidator<RegimeTributacaoParaPessoaJuridica, Destinatario> {

    @Override
    public boolean isValid(Destinatario destinatario, ConstraintValidatorContext context) {
        if (destinatario == null || destinatario.getTipoPessoa() != TipoPessoa.JURIDICA) {
            return true;
        }
        return destinatario.getRegimeTributacao() != null;
    }
}
